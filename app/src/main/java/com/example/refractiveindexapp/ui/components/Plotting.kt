package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DispersionPlot(
    wavelengths: DoubleArray?,
    values: DoubleArray?,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {

        if (wavelengths == null || wavelengths.isEmpty()) return@Canvas
        if (values == null || values.isEmpty()) return@Canvas

        val xMin = wavelengths.first()
        val xMax = wavelengths.last()

        val yMin = values.min()
        val yMax = values.max()

        fun mapX(x: Double): Float =
            ((x - xMin) / (xMax - xMin) * size.width).toFloat()

        fun mapY(y: Double): Float =
            (size.height * (1.0 - (y - yMin) / (yMax - yMin))).toFloat()

        val path = Path()

        path.moveTo(
            mapX(wavelengths[0]),
            mapY(values[0])
        )

        for (i in 1 until wavelengths.size) {
            path.lineTo(
                mapX(wavelengths[i]),
                mapY(values[i])
            )
        }

        drawPath(
            path,
            color = Color.Blue,
            style = Stroke(width = 4f)
        )
    }
}