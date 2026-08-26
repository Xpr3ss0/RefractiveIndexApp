package com.example.refractiveindexapp.ui.components

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.TextPaint
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import io.noties.markwon.Markwon
import kotlin.math.roundToInt

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
                includeFontPadding = true
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
 * Android's default subscript shift is too low for compact chemical formulas.
 * We retain the smaller glyphs but use a gentler baseline shift to prevent clipping.
 */
private fun parseDatabaseHtml(text: String): Spanned {
    val styledText = SpannableString(
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    )
    styledText.getSpans(0, styledText.length, SubscriptSpan::class.java).forEach { span ->
        val start = styledText.getSpanStart(span)
        val end = styledText.getSpanEnd(span)
        styledText.removeSpan(span)
        styledText.setSpan(
            RelativeSizeSpan(0.7f),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        styledText.setSpan(
            RaisedSubscriptSpan(),
            start,
            end,
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

/** A subscript shift smaller than Android's default half-ascent shift. */
private class RaisedSubscriptSpan : MetricAffectingSpan() {
    override fun updateDrawState(textPaint: TextPaint) = applyShift(textPaint)

    override fun updateMeasureState(textPaint: TextPaint) = applyShift(textPaint)

    private fun applyShift(textPaint: TextPaint) {
        textPaint.baselineShift -= (textPaint.ascent() * 0.2f).roundToInt()
    }
}
