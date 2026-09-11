package app.saylune.capture

/**
 * FLAC, written here: 16-bit mono at 16 kHz, for what goes up to the analysis server.
 *
 * **Lossless, so it is transport and not treatment**: what the server decodes is the very
 * samples the device holds, and the reading is the same to the bit. That is what lets it be
 * always on, with no setting and no stamp of its own -- unlike a lossy law, which moved the
 * worst control and narrowed the band (`docs/design/remote-analysis.md`). Measured on the test
 * set, it halves a render and takes a quarter off a take.
 *
 * Written rather than borrowed from the platform's encoder, whose presence and output vary
 * from one device to the next and which cannot run on the desktop: this one gives the same
 * bytes everywhere, and `bench/flac.py` holds it against the reference decoder.
 *
 * Only what the format needs for this: fixed predictors of order 0 to 4, Rice-coded residuals
 * split into partitions, a constant subframe for a run of one value, verbatim where nothing
 * beats it. No LPC -- a few percent it would buy on speech, at many times the code.
 */
object Flac {

    const val SAMPLE_RATE = 16_000
    private const val BLOCK = 4096

    /** [pcm] is 16-bit little-endian samples, as a wav's data holds them. */
    fun encodePcm(pcm: ByteArray): ByteArray {
        require(pcm.size % 2 == 0) { "half a sample: ${pcm.size} bytes" }
        return encode(ShortArray(pcm.size / 2) {
            ((pcm[2 * it].toInt() and 0xFF) or (pcm[2 * it + 1].toInt() shl 8)).toShort()
        })
    }

    fun encode(samples: ShortArray): ByteArray {
        val out = Bits()
        for (char in "fLaC") out.bits(char.code.toLong(), 8)
        // STREAMINFO, the only metadata block and so the last one.
        out.bits(1, 1)
        out.bits(0, 7)
        out.bits(34, 24)
        val block = if (samples.size >= BLOCK) BLOCK else maxOf(samples.size, 16)
        out.bits(block.toLong(), 16)
        out.bits(block.toLong(), 16)
        out.bits(0, 24)                  // smallest frame: not stated
        out.bits(0, 24)                  // largest frame: not stated
        out.bits(SAMPLE_RATE.toLong(), 20)
        out.bits(0, 3)                   // one channel
        out.bits(15, 5)                  // sixteen bits
        out.bits(samples.size.toLong() ushr 32, 4)
        out.bits(samples.size.toLong(), 32)
        repeat(4) { out.bits(0, 32) }    // no MD5: the close carries its own digest
        var from = 0
        var number = 0
        while (from < samples.size) {
            val size = minOf(BLOCK, samples.size - from)
            frame(out, samples, from, size, number++)
            from += size
        }
        return out.toByteArray()
    }

    private fun frame(out: Bits, samples: ShortArray, from: Int, size: Int, number: Int) {
        val start = out.length()
        out.bits(0b11111111111110, 14)
        out.bits(0, 1)
        out.bits(0, 1)                   // fixed block size: frames are numbered
        val sizeCode = if (size == BLOCK) 0b1100 else 0b0111
        out.bits(sizeCode.toLong(), 4)
        out.bits(0b0101, 4)              // 16 kHz
        out.bits(0, 4)                   // mono
        out.bits(0b100, 3)               // 16 bits
        out.bits(0, 1)
        utf8(out, number)
        if (sizeCode == 0b0111) out.bits((size - 1).toLong(), 16)
        out.bits(crc8(out.bytes(), start, out.length()).toLong(), 8)
        subframe(out, samples, from, size)
        out.align()
        out.bits(crc16(out.bytes(), start, out.length()).toLong(), 16)
    }

    private fun subframe(out: Bits, samples: ShortArray, from: Int, size: Int) {
        val first = samples[from]
        if ((from until from + size).all { samples[it] == first }) {
            out.bits(0, 8)               // padding, CONSTANT, no wasted bits
            out.bits(first.toLong(), 16)
            return
        }
        var best: Plan? = null
        for (order in 0..minOf(4, size - 1)) {
            val plan = plan(residual(samples, from, size, order), order, size)
            if (best == null || plan.bits < best.bits) best = plan
        }
        val chosen = best!!
        if (chosen.bits >= 16L * size) {
            out.bits(0b00000010, 8)      // padding, VERBATIM, no wasted bits
            for (at in from until from + size) out.bits(samples[at].toLong(), 16)
            return
        }
        out.bits(0, 1)
        out.bits((0b001000 or chosen.order).toLong(), 6)
        out.bits(0, 1)
        for (at in from until from + chosen.order) out.bits(samples[at].toLong(), 16)
        out.bits(0, 2)                   // Rice, four-bit parameters
        out.bits(chosen.partitionOrder.toLong(), 4)
        val partitions = 1 shl chosen.partitionOrder
        val length = size shr chosen.partitionOrder
        var at = 0
        for (partition in 0 until partitions) {
            val k = chosen.parameters[partition]
            out.bits(k.toLong(), 4)
            val count = if (partition == 0) length - chosen.order else length
            repeat(count) {
                val u = chosen.folded[at++]
                out.zeros(u ushr k)
                out.bits(1, 1)
                if (k > 0) out.bits(u.toLong(), k)
            }
        }
    }

    /** The residual of a fixed predictor, folded to unsigned: 0, -1, 1, -2 ... to 0, 1, 2, 3 ... */
    private fun residual(samples: ShortArray, from: Int, size: Int, order: Int): IntArray =
        IntArray(size - order) { index ->
            val i = from + order + index
            fun x(back: Int) = samples[i - back].toInt()
            val r = when (order) {
                0 -> x(0)
                1 -> x(0) - x(1)
                2 -> x(0) - 2 * x(1) + x(2)
                3 -> x(0) - 3 * x(1) + 3 * x(2) - x(3)
                else -> x(0) - 4 * x(1) + 6 * x(2) - 4 * x(3) + x(4)
            }
            (r shl 1) xor (r shr 31)
        }

    private class Plan(
        val order: Int,
        val partitionOrder: Int,
        val parameters: IntArray,
        val folded: IntArray,
        val bits: Long,
    )

    /**
     * The cheapest split of [folded] into partitions and of each into a Rice parameter.
     *
     * Costed exactly rather than estimated: the sums are taken once on the finest split and
     * added up for the coarser ones, so every partition order is weighed for the price of one.
     */
    private fun plan(folded: IntArray, order: Int, size: Int): Plan {
        var finest = 0
        while (finest < 8 && size % (2 shl finest) == 0 && (size shr (finest + 1)) > order) finest++
        val parts = 1 shl finest
        val length = size shr finest
        // cost[p][k]: sum of u >> k over partition p, plus its count, at the finest split.
        val shifted = Array(parts) { LongArray(MAX_K + 1) }
        val counts = IntArray(parts)
        var at = 0
        for (part in 0 until parts) {
            val count = if (part == 0) length - order else length
            counts[part] = count
            repeat(count) {
                val u = folded[at++]
                for (k in 0..MAX_K) shifted[part][k] += (u ushr k).toLong()
            }
        }
        var best: Plan? = null
        for (partitionOrder in 0..finest) {
            val merged = 1 shl (finest - partitionOrder)
            val parameters = IntArray(1 shl partitionOrder)
            var bits = 2L + 4L + 8L + 16L * order
            for (partition in parameters.indices) {
                var cheapest = Long.MAX_VALUE
                for (k in 0..MAX_K) {
                    var cost = 4L
                    for (fine in partition * merged until (partition + 1) * merged) {
                        cost += counts[fine].toLong() * (k + 1) + shifted[fine][k]
                    }
                    if (cost < cheapest) {
                        cheapest = cost
                        parameters[partition] = k
                    }
                }
                bits += cheapest
            }
            if (best == null || bits < best.bits) {
                best = Plan(order, partitionOrder, parameters, folded, bits)
            }
        }
        return best!!
    }

    /** The frame number, in the format's extended UTF-8. */
    private fun utf8(out: Bits, value: Int) {
        if (value < 0x80) {
            out.bits(value.toLong(), 8)
            return
        }
        var bytes = 2
        while (value >= (1 shl (5 * bytes + 1))) bytes++
        val lead = (0xFF shl (8 - bytes)) and 0xFF
        out.bits((lead or (value ushr (6 * (bytes - 1)))).toLong(), 8)
        for (index in bytes - 2 downTo 0) {
            out.bits((0x80 or ((value ushr (6 * index)) and 0x3F)).toLong(), 8)
        }
    }

    private fun crc8(bytes: ByteArray, from: Int, to: Int): Int {
        var crc = 0
        for (at in from until to) {
            crc = crc xor (bytes[at].toInt() and 0xFF)
            repeat(8) { crc = if (crc and 0x80 != 0) (crc shl 1) xor 0x07 else crc shl 1 }
            crc = crc and 0xFF
        }
        return crc
    }

    private fun crc16(bytes: ByteArray, from: Int, to: Int): Int {
        var crc = 0
        for (at in from until to) {
            crc = crc xor ((bytes[at].toInt() and 0xFF) shl 8)
            repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x8005 else crc shl 1 }
            crc = crc and 0xFFFF
        }
        return crc
    }

    /** Past this the four-bit parameter would be the escape code. */
    private const val MAX_K = 14

    /** A bit writer, most significant bit first, as the format is read. */
    private class Bits {
        private var buffer = ByteArray(1 shl 16)
        private var size = 0
        private var held = 0L
        private var count = 0

        /** The low [width] bits of [value], [width] up to 32. */
        fun bits(value: Long, width: Int) {
            held = (held shl width) or (value and ((1L shl width) - 1))
            count += width
            while (count >= 8) {
                count -= 8
                put(((held ushr count) and 0xFF).toInt())
            }
            held = held and ((1L shl count) - 1)
        }

        fun zeros(width: Int) {
            var left = width
            while (left > 0) {
                val step = minOf(left, 32)
                bits(0, step)
                left -= step
            }
        }

        fun align() {
            if (count > 0) bits(0, 8 - count)
        }

        /** Whole bytes written so far; only called when aligned. */
        fun length(): Int = size

        fun bytes(): ByteArray = buffer

        fun toByteArray(): ByteArray = buffer.copyOf(size)

        private fun put(byte: Int) {
            if (size == buffer.size) buffer = buffer.copyOf(buffer.size * 2)
            buffer[size++] = byte.toByte()
        }
    }
}
