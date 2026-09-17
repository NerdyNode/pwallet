package com.pdfwallet.service.pdf

object TextQualityChecker {

    enum class Quality { GOOD, LOW, POOR }

    data class QualityReport(
        val quality: Quality,
        val wordCount: Int,
        val garbledRatio: Float,
        val reason: String
    )

    fun assess(text: String): QualityReport {
        if (text.isBlank()) return QualityReport(
            Quality.POOR, 0, 1f, "Empty text"
        )

        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size
        val garbledRatio = countGarbledChars(text).toFloat() / text.length.coerceAtLeast(1)

        return when {
            wordCount < 15 || garbledRatio > 0.35f -> QualityReport(
                Quality.POOR, wordCount, garbledRatio,
                "Too short or too many garbled characters"
            )
            wordCount < 80 || garbledRatio > 0.15f -> QualityReport(
                Quality.LOW, wordCount, garbledRatio,
                "Marginal quality — re-OCR recommended"
            )
            else -> QualityReport(
                Quality.GOOD, wordCount, garbledRatio,
                "Acceptable"
            )
        }
    }

    private fun countGarbledChars(text: String): Int =
        text.count { char ->
            char.code > 127 &&
            !char.isLetterOrDigit() &&
            char !in ".,;:!?-\u2013\u2014()[]{}\"'@#\u20B9\$%&*/\\ \n\t"
        }
}
