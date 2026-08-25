package com.example.refractiveindexapp.ui.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import io.noties.markwon.Markwon

private val htmlTagPattern = Regex("</?[A-Za-z][^>]*>")

/** Renders legacy HTML, with Markdown as the fallback for current and future data. */
@Composable
fun DatabaseRichText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    val textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(it).apply {
                movementMethod = LinkMovementMethod.getInstance()
                linksClickable = true
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.maxLines = maxLines
            if (text.containsHtmlMarkup()) {
                textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                markwon.setMarkdown(textView, text)
            }
        }
    )
}

private fun String.containsHtmlMarkup(): Boolean = htmlTagPattern.containsMatchIn(this)
