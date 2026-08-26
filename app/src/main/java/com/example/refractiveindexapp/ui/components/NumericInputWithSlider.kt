package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

@Composable
fun NumericInputWithSlider(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    range: ClosedFloatingPointRange<Double>?,
    error: String? = null,
    unit: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            supportingText = error?.let { message -> { Text(message) } }
        )
        range?.takeIf { it.start.isFinite() && it.endInclusive.isFinite() && it.start < it.endInclusive }?.let { validRange ->
            val parsed = value.replace(',', '.').toDoubleOrNull()
            val sliderValue = (parsed ?: validRange.start).coerceIn(validRange.start, validRange.endInclusive)
            val logarithmic = validRange.start > 0.0 && validRange.endInclusive / validRange.start >= 10.0
            Slider(
                value = toSliderFraction(sliderValue, validRange, logarithmic).toFloat(),
                onValueChange = { fraction ->
                    onValueChange(formatSliderValue(fromSliderFraction(fraction.toDouble(), validRange, logarithmic)))
                },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${formatSliderValue(validRange.start)}–${formatSliderValue(validRange.endInclusive)}${unit?.let { " $it" }.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun toSliderFraction(value: Double, range: ClosedFloatingPointRange<Double>, logarithmic: Boolean): Double =
    if (logarithmic) (kotlin.math.ln(value) - kotlin.math.ln(range.start)) / (kotlin.math.ln(range.endInclusive) - kotlin.math.ln(range.start))
    else (value - range.start) / (range.endInclusive - range.start)

private fun fromSliderFraction(fraction: Double, range: ClosedFloatingPointRange<Double>, logarithmic: Boolean): Double =
    if (logarithmic) kotlin.math.exp(kotlin.math.ln(range.start) + fraction * (kotlin.math.ln(range.endInclusive) - kotlin.math.ln(range.start)))
    else range.start + fraction * (range.endInclusive - range.start)

private fun formatSliderValue(value: Double): String = "%.5g".format(java.util.Locale.US, value)
