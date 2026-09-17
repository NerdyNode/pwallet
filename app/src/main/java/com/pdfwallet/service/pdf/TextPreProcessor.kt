package com.pdfwallet.service.pdf

import com.pdfwallet.data.db.DocumentType

object TextPreProcessor {

    fun clean(raw: String): String = raw
        .replace(Regex("[ \t]{2,}"), " ")
        .replace(Regex("[^\u0020-\u007E\n\r]"), "")
        .replace(Regex("(?m)^[ \t]*$\n"), "")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    fun structure(text: String, suspectedType: DocumentType?): String {
        val typeHint = when (suspectedType) {
            DocumentType.AIRLINE -> "Document type hint: FLIGHT_TICKET\n"
            DocumentType.GOVERNMENT_ID -> "Document type hint: GOVERNMENT_ID\n"
            DocumentType.TRAIN -> "Document type hint: TRAIN_TICKET\n"
            DocumentType.BUS -> "Document type hint: BUS_TICKET\n"
            DocumentType.HOTEL -> "Document type hint: HOTEL_BOOKING\n"
            DocumentType.MOVIE -> "Document type hint: MOVIE_TICKET\n"
            DocumentType.TRANSIT -> "Document type hint: TRANSIT_PASS\n"
            DocumentType.MEMBERSHIP -> "Document type hint: MEMBERSHIP_CARD\n"
            DocumentType.CERTIFICATE -> "Document type hint: CERTIFICATE\n"
            else -> ""
        }
        
        // Truncate to ~3500 chars to avoid Gemini token exhaustion on massive PDFs.
        val maxChars = 3500
        val safeText = if (text.length > maxChars) text.substring(0, maxChars) + "\n...[TRUNCATED]" else text
        
        return "$typeHint\n$safeText"
    }

    fun preclassify(text: String): DocumentType? {
        val lower = text.lowercase()
        return when {
            lower.containsAny("flight", "boarding", "gate", "airline", "terminal") ->
                DocumentType.AIRLINE
            lower.containsAny("passport", "aadhaar", "pan card", "driving licence", "voter", "aadhar") ->
                DocumentType.GOVERNMENT_ID
            lower.containsAny("train", "irctc", "berth", "coach", "reservation", "railway") ->
                DocumentType.TRAIN
            lower.containsAny("bus", "boarding point", "redbus", "abhibus") ->
                DocumentType.BUS
            lower.containsAny("hotel", "check-in", "check-out", "booking confirmation", "resort") ->
                DocumentType.HOTEL
            lower.containsAny("movie", "cinema", "showtime", "screen", "pvr", "inox", "cinepolis") ->
                DocumentType.MOVIE
            lower.containsAny("metro", "transit", "pass", "local", "commuter") ->
                DocumentType.TRANSIT
            lower.containsAny("membership", "gym", "club", "subscriber", "member") ->
                DocumentType.MEMBERSHIP
            lower.containsAny("certificate", "awarded", "completion", "diploma") ->
                DocumentType.CERTIFICATE
            else -> null
        }
    }

    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it) }
}
