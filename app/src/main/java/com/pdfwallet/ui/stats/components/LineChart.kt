package com.pdfwallet.ui.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pdfwallet.ui.theme.Dimens

@Composable
fun LineChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(Dimens.SpacingMedium)
    ) {
        val maxValue = dataPoints.maxOrNull() ?: 1f
        val width = size.width
        val height = size.height
        
        val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width
        
        val path = Path()
        
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxValue * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw points
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxValue * height)
            
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
