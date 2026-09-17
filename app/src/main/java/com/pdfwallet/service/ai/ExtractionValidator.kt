package com.pdfwallet.service.ai

import com.pdfwallet.data.db.DocumentType
import java.time.LocalDate
import java.time.format.DateTimeParseException

object ExtractionValidator {

    sealed class ValidationResult {
        data object OK : ValidationResult()
        data class NeedsReview(val issues: List<String>) : ValidationResult()
        data class Failed(val reason: String) : ValidationResult()
    }

    fun validate(
        fields: Map<String, String?>,
        docType: DocumentType?
    ): ValidationResult {
        val issues = mutableListOf<String>()

        // Date field validation
        listOf("departure_date", "journey_date", "issue_date", "check_in_date", "check_out_date")
            .forEach { key ->
                fields[key]?.let { dateStr ->
                    if (!isValidDate(dateStr)) {
                        issues.add("'$key' has invalid format: $dateStr")
                    }
                }
            }

        // Expiry date sanity check
        fields["expiry_date"]?.let { expiry ->
            if (isValidDate(expiry)) {
                val date = LocalDate.parse(expiry)
                if (date.isBefore(LocalDate.of(2000, 1, 1))) {
                    issues.add("Expiry date '$expiry' seems too far in the past")
                }
                if (date.isAfter(LocalDate.now().plusYears(50))) {
                    issues.add("Expiry date '$expiry' seems too far in the future")
                }
            }
        }

        // Flight-specific validation
        if (docType == DocumentType.AIRLINE) {
            fields["flight_number"]?.let { flight ->
                if (!flight.matches(Regex("[A-Z0-9]{2,3}\\d{1,4}[A-Z]?"))) {
                    issues.add("Flight number format unexpected: $flight")
                }
            }
            fields["origin_airport_code"]?.let { iata ->
                if (!iata.matches(Regex("[A-Z]{3}"))) {
                    issues.add("Origin IATA code invalid: $iata")
                }
            }
        }

        // Check that at least one primary field has a value
        val primaryFields = listOf(
            "passenger_name", "full_name", "primary_name",
            "guest_name", "holder_name", "member_name",
            "recipient_name", "document_title"
        )
        val hasPrimaryField = primaryFields.any { fields[it]?.isNotBlank() == true }
        if (!hasPrimaryField) {
            return ValidationResult.Failed("No primary name/title field extracted")
        }

        return if (issues.isEmpty()) ValidationResult.OK
        else ValidationResult.NeedsReview(issues)
    }

    private fun isValidDate(dateStr: String): Boolean = try {
        LocalDate.parse(dateStr)
        true
    } catch (e: DateTimeParseException) {
        false
    }
}
