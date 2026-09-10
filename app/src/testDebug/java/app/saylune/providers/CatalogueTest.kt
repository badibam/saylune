package app.saylune.providers

import app.saylune.keys.Secret
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueTest {

    /**
     * **An overridable entry left blank never holds a provider back.** This says what `ready`
     * does with the flag; what checks the flag itself is the last test here, and that is the
     * one that bites -- a host nobody marked is a provider that never appears in its link's
     * menu, with nothing on screen saying why.
     */
    @Test
    fun `an overridable entry left blank does not hold a provider back`() {
        Provider.entries.forEach { provider ->
            val filled = provider.needs
                .filterNot { it.overridable }
                .associateWith { "filled" }
            assertTrue(
                "${provider.label} is not offered with " +
                    provider.needs.filter { it.overridable }.map { it.id } + " left blank",
                // The weights granted, this being about what is typed in and not about what
                // is downloaded: the one provider that asks for them asks for no credential
                // at all, so left false it would fail here for the wrong reason.
                provider.ready(filled, weights = true),
            )
        }
    }

    /** And a credential left out really does hold it back, or the test above proves nothing. */
    @Test
    fun `a missing credential holds a provider back`() {
        Provider.entries.forEach { provider ->
            val credentials = provider.needs.filterNot { it.overridable }
            credentials.forEach { missing ->
                val short = credentials.filterNot { it == missing }.associateWith { "filled" }
                assertTrue(
                    "${provider.label} is offered without ${missing.id}",
                    !provider.ready(short),
                )
            }
        }
    }

    /**
     * **Every host that replaces a published one is marked as one**, so none is required by
     * accident.
     *
     * This is the one that catches the real mistake, and it is the only one here that does:
     * the two above read the flag, this one holds it against the naming, so a host added and
     * left unmarked fails here rather than on the screen where the provider is missing.
     *
     * **The analysis server is the exception, and it is one by construction.** Every other
     * host stands in for an address the provider publishes, so blank is the ordinary case;
     * that one has nothing behind it, and blank means the link is simply not offered. The
     * address *is* the credential there, which is what keeps the app from being tied to one
     * instance (`keys/Secret.kt`). Named rather than derived: a second host of that kind
     * should have to be admitted here on purpose.
     */
    @Test
    fun `every endpoint that stands in for a published one is marked overridable`() {
        Secret.entries
            .filter { it.id.endsWith(".endpoint") && it != Secret.AnalysisEndpoint }
            .forEach {
                assertTrue("${it.id} is not marked overridable", it.overridable)
            }
    }
}
