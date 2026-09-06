package app.speakup.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A colour as the palette states it: a lightness, a chroma and a hue, in OKLab's polar form.
 *
 * The palette is written in this space and never in sRGB, because the two rules it has to hold
 * are both perceptual. **A marked letter must be far enough from a bare one, and that distance
 * is paid in lightness, in chroma, or in both** -- measuring lightness alone called a perfectly
 * legible pale palette a failure. And a tone mixed toward the ground has to keep looking like
 * its own colour, which mixing in sRGB does not.
 */
@Immutable
data class Lch(val l: Float, val c: Float, val h: Float) {

    /** The nearest colour sRGB can actually show, with the hue kept. */
    val srgb: Color get() = Color(red = clipped.r, green = clipped.g, blue = clipped.b)

    /**
     * Toward [ground] by [t], **in OKLab**.
     *
     * Mixing sRGB toward a pale ground lightens too fast and desaturates crookedly, and the
     * softened edge stops looking like its own colour. This is how the halo is painted: the
     * outer pixels blend toward the ground and **stay opaque**, so a covering line still hides
     * what is under it -- an alpha halo would let the learner's contour show through the
     * model's at the very moment the covering has to be total.
     */
    fun mix(ground: Lch, t: Float): Lch {
        // The short way round the circle, so amber toward plum does not travel through green.
        val turn = (((ground.h - h) % 360f) + 540f) % 360f - 180f
        val turned = h + turn * t
        return Lch(l + (ground.l - l) * t, c + (ground.c - c) * t, ((turned % 360f) + 360f) % 360f)
    }

    /** How far apart two colours look, which is the one thing the marking rule measures. */
    infix fun apart(other: Lch): Float {
        val (a1, b1) = ab()
        val (a2, b2) = other.ab()
        val dl = l - other.l
        val da = a1 - a2
        val db = b1 - b2
        return sqrt(dl * dl + da * da + db * db)
    }

    private fun ab(): Pair<Float, Float> {
        val radians = h * PI.toFloat() / 180f
        return c * cos(radians) to c * sin(radians)
    }

    /**
     * **Gamut clipping reduces the chroma** until the colour exists in sRGB, instead of
     * clipping each channel on its own. Per-channel clipping shifts the hue as well as fading
     * it, and that is what made the red end of the ramp come out pink.
     */
    private val clipped: Rgb
        get() {
            val full = linear(c)
            if (full.inGamut) return full.encoded
            var low = 0f
            var high = c
            repeat(GAMUT_STEPS) {
                val middle = (low + high) / 2f
                if (linear(middle).inGamut) low = middle else high = middle
            }
            return linear(low).encoded
        }

    private fun linear(chroma: Float): Rgb {
        val radians = h * PI.toFloat() / 180f
        val a = chroma * cos(radians)
        val b = chroma * sin(radians)
        val lp = l + 0.3963377774f * a + 0.2158037573f * b
        val mp = l - 0.1055613458f * a - 0.0638541728f * b
        val sp = l - 0.0894841775f * a - 1.2914855480f * b
        val lc = lp * lp * lp
        val mc = mp * mp * mp
        val sc = sp * sp * sp
        return Rgb(
            +4.0767416621f * lc - 3.3077115913f * mc + 0.2309699292f * sc,
            -1.2684380046f * lc + 2.6097574011f * mc - 0.3413193965f * sc,
            -0.0041960863f * lc - 0.7034186147f * mc + 1.7076147010f * sc,
        )
    }
}

/** Linear-light channels, before the transfer function. */
private data class Rgb(val r: Float, val g: Float, val b: Float) {

    val inGamut: Boolean
        get() = listOf(r, g, b).all { it >= -SLACK && it <= 1f + SLACK }

    val encoded: Rgb get() = Rgb(gamma(r), gamma(g), gamma(b))

    private fun gamma(channel: Float): Float {
        val x = channel.coerceIn(0f, 1f)
        return if (x <= 0.0031308f) 12.92f * x else 1.055f * x.pow(1f / 2.4f) - 0.055f
    }
}

/** Rounding room, so a colour exactly on the gamut boundary is not walked back in. */
private const val SLACK = 0.0005f

/** Bisections of the chroma. Eighteen puts the answer well inside one 8-bit step. */
private const val GAMUT_STEPS = 18
