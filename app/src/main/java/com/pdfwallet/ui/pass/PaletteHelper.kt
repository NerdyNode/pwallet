package com.pdfwallet.ui.pass

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberDominantColor(imagePath: String?, defaultColor: Color): State<Color> {
    return produceState(initialValue = defaultColor, key1 = imagePath) {
        if (!imagePath.isNullOrBlank()) {
            val extractedColor = withContext(Dispatchers.IO) {
                try {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val defaultArgb = defaultColor.toArgb()
                        val colorArgb = palette.getVibrantColor(palette.getDominantColor(defaultArgb))
                        Color(colorArgb)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (extractedColor != null) {
                value = extractedColor
            }
        }
    }
}
