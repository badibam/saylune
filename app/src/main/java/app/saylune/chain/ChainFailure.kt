package app.saylune.chain

/**
 * A link of the conversation chain could not do its job.
 *
 * One exception for the three links, and deliberately without a cause of its own -- no
 * subclass for the network, none for a spent quota, none for a rejected key. A caller that
 * could tell those apart would be a caller written against remote providers, and one of the
 * three links is meant to stop being remote: recognition's destination is the device
 * (`docs/reference.md`). A local implementation fails too -- weights absent, model refused
 * by the device -- and it must be able to say so through the same door.
 *
 * What the app does about a failure is decided per link and not here: the conversation
 * chain is repairable by construction, because the turn's audio file is kept and a failed
 * send is retried without saying the sentence again.
 */
class ChainFailure(message: String, cause: Throwable? = null) : Exception(message, cause)
