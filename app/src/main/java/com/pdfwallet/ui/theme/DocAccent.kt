package com.pdfwallet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pdfwallet.data.db.DocumentType

data class DocAccent(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color
)

fun getDocAccent(type: DocumentType, isDark: Boolean): DocAccent {
    return when (type) {
        DocumentType.GOVERNMENT_ID -> if (isDark) DocAccent(
            primary = Color(0xFFE8B264), onPrimary = Color(0xFF422C00),
            container = Color(0xFF5E4000), onContainer = Color(0xFFFFDFA6)
        ) else DocAccent(
            primary = Color(0xFF7E5700), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFFFDFA6), onContainer = Color(0xFF281900)
        )
        DocumentType.TRAIN -> if (isDark) DocAccent(
            primary = Color(0xFFB5C4FF), onPrimary = Color(0xFF072978),
            container = Color(0xFF264190), onContainer = Color(0xFFDCE1FF)
        ) else DocAccent(
            primary = Color(0xFF415AA9), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFDCE1FF), onContainer = Color(0xFF001550)
        )
        DocumentType.AIRLINE -> if (isDark) DocAccent(
            primary = Color(0xFFFFB4A9), onPrimary = Color(0xFF690002),
            container = Color(0xFF930005), onContainer = Color(0xFFFFDAD5)
        ) else DocAccent(
            primary = Color(0xFFC0000B), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFFFDAD5), onContainer = Color(0xFF410001)
        )
        DocumentType.BUS -> if (isDark) DocAccent(
            primary = Color(0xFF16A34A), onPrimary = Color(0xFF003915),
            container = Color(0xFF13291C), onContainer = Color(0xFFA8E6BE)
        ) else DocAccent(
            primary = Color(0xFF16A34A), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFA8E6BE), onContainer = Color(0xFF00210A)
        )
        DocumentType.HOTEL -> if (isDark) DocAccent(
            primary = Color(0xFFB45309), onPrimary = Color(0xFF421B00),
            container = Color(0xFF2B1D0E), onContainer = Color(0xFFF3C98A)
        ) else DocAccent(
            primary = Color(0xFFB45309), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFF3C98A), onContainer = Color(0xFF2E1100)
        )
        DocumentType.MEMBERSHIP -> if (isDark) DocAccent(
            primary = Color(0xFF8B5CF6), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFF2E1065), onContainer = Color(0xFFDDD6FE)
        ) else DocAccent(
            primary = Color(0xFF7C3AED), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFDDD6FE), onContainer = Color(0xFF2E1065)
        )
        DocumentType.TRANSIT -> if (isDark) DocAccent(
            primary = Color(0xFF06B6D4), onPrimary = Color(0xFF083344),
            container = Color(0xFF083344), onContainer = Color(0xFFCFFAFE)
        ) else DocAccent(
            primary = Color(0xFF0891B2), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFCFFAFE), onContainer = Color(0xFF083344)
        )
        DocumentType.CERTIFICATE -> if (isDark) DocAccent(
            primary = Color(0xFFF43F5E), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFF4C0519), onContainer = Color(0xFFFECDD3)
        ) else DocAccent(
            primary = Color(0xFFE11D48), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFFECDD3), onContainer = Color(0xFF4C0519)
        )
        DocumentType.OTHER -> if (isDark) DocAccent(
            primary = Color(0xFFC8C6CA), onPrimary = Color(0xFF303033),
            container = Color(0xFF464649), onContainer = Color(0xFFE4E2E6)
        ) else DocAccent(
            primary = Color(0xFF5E5E62), onPrimary = Color(0xFFFFFFFF),
            container = Color(0xFFE4E2E6), onContainer = Color(0xFF1B1B1E)
        )
    }
}

/**
 * Gets the chart color for this document type, respecting the provided dark mode flag
 * instead of directly querying isSystemInDarkTheme().
 */
fun DocumentType.getChartColor(isDark: Boolean): Color {
    return getDocAccent(this, isDark).primary
}

/**
 * Composable convenience that resolves dark mode from the composition.
 */
@Composable
fun DocumentType.getChartColor(): Color {
    return getDocAccent(this, isSystemInDarkTheme()).primary
}
