package app.saylune.providers

import androidx.annotation.StringRes
import app.saylune.R

/**
 * How much reasoning a conversation model spends before it answers.
 *
 * **Not sending it is not a neutral default.** DeepSeek's own doc says thinking is on by
 * default at `high` effort, so a call that names nothing pays the fullest reasoning it has
 * without anyone having asked -- which is what the turns of 2026-09-06 measured: a median of
 * 13,2 s on the language link where the same model on the older contract took 4,4 s. That is
 * why this is declared and always sent, rather than left out when the user has not chosen.
 *
 * **The levels are per model and are read from the provider, never guessed.** They differ in
 * name and in number from one to the next, and a menu that offers a level the provider
 * silently maps onto another one lies about what was asked for.
 */
enum class Effort(val id: String, @StringRes val label: Int) {
    /** No reasoning at all. The fastest, and the one the latency measurement points at. */
    None("none", R.string.effort_none),
    Low("low", R.string.effort_low),
    Medium("medium", R.string.effort_medium),
    High("high", R.string.effort_high),
    Max("max", R.string.effort_max),
    ;

    companion object {
        fun of(id: String?): Effort? = entries.firstOrNull { it.id == id }
    }
}

/**
 * The effort in force for [provider], or the one the app asks for when nothing was chosen.
 *
 * **The default is the most sparing level the provider offers**, and it is a decision rather
 * than an absence: the language link is the project's first defect, the measurement of
 * 2026-09-06 says the enriched contract tripled it, and the lever the plan names against a bad
 * number is a model that does not reason for this link. What it costs in the judge's quality
 * is not measured at any bench and is read at use.
 *
 * That most sparing level is not the same thing everywhere: DeepSeek offers no reasoning at
 * all, OpenAI's floor here is `low`. Reading it off the declared list rather than naming one
 * is what keeps this from asking a provider for a level it does not have.
 *
 * A provider that has no notion of effort gets null and is sent nothing, which for it is not
 * a default but the absence of the parameter.
 */
fun effortFor(provider: Provider, values: Map<app.saylune.keys.Secret, String>): Effort? {
    val offered = provider.efforts
    if (offered.isEmpty()) return null
    return Effort.of(values[app.saylune.keys.Secret.ConversationEffort])?.takeIf { it in offered }
        ?: offered.first()
}
