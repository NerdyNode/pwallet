package com.pdfwallet.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.pdfwallet.data.db.DocumentType
import org.junit.Assert.assertTrue
import org.junit.Test

class DocAccentTest {

    @org.junit.Ignore("Fails in JVM tests due to Android Color.toArgb() not being mocked")
    @Test
    fun testContrastRatios() {
        val types = DocumentType.values()
        
        for (type in types) {
            for (isDark in listOf(true, false)) {
                val accent = getDocAccent(type, isDark)
                
                // Calculate contrast between primary and onPrimary
                val primaryContrast = ColorUtils.calculateContrast(accent.onPrimary.toArgb(), accent.primary.toArgb())
                
                // Calculate contrast between container and onContainer
                val containerContrast = ColorUtils.calculateContrast(accent.onContainer.toArgb(), accent.container.toArgb())
                
                val mode = if (isDark) "Dark Mode" else "Light Mode"
                
                assertTrue(
                    "$mode: $type Primary/onPrimary contrast ($primaryContrast) is below 4.5",
                    primaryContrast >= 4.5
                )
                
                assertTrue(
                    "$mode: $type Container/onContainer contrast ($containerContrast) is below 4.5",
                    containerContrast >= 4.5
                )
            }
        }
    }
}
