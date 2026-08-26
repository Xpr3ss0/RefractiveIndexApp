package com.example.refractiveindexapp.ui.components

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.RelativeSizeSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
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
    maxLines: Int = Int.MAX_VALUE,
    linksEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    val textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(it).apply {
                isClickable = false
                isFocusable = false
                isSingleLine = false
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.maxLines = maxLines
            textView.ellipsize = null
            textView.movementMethod = if (linksEnabled) LinkMovementMethod.getInstance() else null
            textView.linksClickable = linksEnabled
            textView.setOnClickListener(
                if (linksEnabled || onClick == null) null else { _ -> onClick() }
            )
            textView.isClickable = !linksEnabled && onClick != null
            if (text.containsHtmlMarkup()) {
                textView.text = parseDatabaseHtml(text)
            } else {
                markwon.setMarkdown(textView, text)
            }
        }
    )
}

private fun String.containsHtmlMarkup(): Boolean = htmlTagPattern.containsMatchIn(this)

/**
 * Android lowers HTML subscripts but leaves their glyphs at the surrounding size.
 * Scaling them here keeps chemical formulas such as BaB₂O₄ visually balanced.
 */
private fun parseDatabaseHtml(text: String): Spanned {
    val styledText = SpannableString(
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    )
    styledText.getSpans(0, styledText.length, SubscriptSpan::class.java).forEach { span ->
        styledText.setSpan(
            RelativeSizeSpan(0.7f),
            styledText.getSpanStart(span),
            styledText.getSpanEnd(span),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    styledText.getSpans(0, styledText.length, SuperscriptSpan::class.java).forEach { span ->
        styledText.setSpan(
            RelativeSizeSpan(0.7f),
            styledText.getSpanStart(span),
            styledText.getSpanEnd(span),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return styledText
}
