package app.saylune.ui

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.saylune.R
import app.saylune.capture.Reference

/**
 * Who recorded the sound of each symbol, and under what licence.
 *
 * **The screen exists because a licence asks for it.** Thirty-one of the recordings are
 * CC BY-SA, which allows every use -- including this one, including commercial ones -- on
 * the single condition that the author is named and the licence travels with the work. That
 * is a small price for a set nobody here could have recorded, and paying it is not optional.
 *
 * Each row is one work: the symbol it stands for, the phonetic name the recording was filed
 * under, its author, its licence, and a tap that opens the original. The original is what
 * makes the credit checkable rather than merely stated.
 *
 * Nothing is shown when nothing ships -- the release build carries no recordings, and a
 * heading over an empty list would suggest something failed to load.
 */
@Composable
fun SoundCredits(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val credits = remember { Reference.credits(context) }
    if (credits.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.credits_sounds),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            stringResource(R.string.credits_sounds_why),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        credits.forEach { credit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // The link is the whole point of the attribution: a name with no way
                        // back to the work credits nobody in particular.
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, credit.page.toUri())
                            )
                        }
                    }
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    credit.symbol,
                    modifier = Modifier.width(34.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        credit.title,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.Underline,
                    )
                    Text(
                        stringResource(R.string.credits_by, credit.author, credit.licence),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
