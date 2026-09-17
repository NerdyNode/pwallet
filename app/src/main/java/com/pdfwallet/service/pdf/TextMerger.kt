package com.pdfwallet.service.pdf

object TextMerger {

    data class ExtractionResult(
        val text: String,
        val source: Source,
        val confidence: Float  // 0.0 - 1.0
    )

    enum class Source { PDFBOX, OCR, MERGED }

    fun merge(
        pdfBoxResult: ExtractionResult?,
        ocrResult: ExtractionResult?
    ): ExtractionResult {
        // Both failed
        if (pdfBoxResult == null && ocrResult == null) {
            return ExtractionResult("", Source.MERGED, 0f)
        }

        // Only one available
        if (pdfBoxResult == null) return ocrResult!!
        if (ocrResult == null) return pdfBoxResult

        val pdfText = pdfBoxResult.text.trim()
        val ocrText = ocrResult.text.trim()

        return when {
            // PdfBox gave substantial structured text — trust it
            pdfText.length > ocrText.length * 1.5 && pdfText.wordCount() > 50 ->
                pdfBoxResult.copy(source = Source.MERGED)

            // OCR gave substantially more — scanned doc
            ocrText.length > pdfText.length * 1.5 ->
                ocrResult.copy(source = Source.MERGED)

            // Both partial — combine, dedup
            else -> ExtractionResult(
                text = combineTexts(pdfText, ocrText),
                source = Source.MERGED,
                confidence = maxOf(pdfBoxResult.confidence, ocrResult.confidence)
            )
        }
    }

    private fun combineTexts(primary: String, secondary: String): String {
        val primaryLines = primary.lines().map { it.trim() }.toSet()
        val uniqueSecondary = secondary.lines()
            .map { it.trim() }
            .filter { line -> line.isNotBlank() && !primaryLines.any { it.contains(line) } }
        return (primary.lines() + uniqueSecondary).joinToString("\n")
    }

    private fun String.wordCount() = this.split(Regex("\\s+")).size
}
