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
                provider.ready(filled),
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
     * **Every host is marked as one**, so none is required by accident.
     *
     * This is the one that catches the real mistake, and it is the only one here that does:
     * the two above read the flag, this one holds it against the naming, so a host added and
     * left unmarked fails here rather than on the screen where the provider is missing.
     */
    @Test
    fun `every endpoint is marked overridable`() {
        Secret.entries.filter { it.id.endsWith(".endpoint") }.forEach {
            assertTrue("${it.id} is not marked overridable", it.overridable)
        }
    }
}
