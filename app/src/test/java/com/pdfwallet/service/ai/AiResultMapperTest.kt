package com.pdfwallet.service.ai

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AiResultMapperTest {

    @Test
    fun testExpiryDateEpochMapping() {
        // Arrange
        val expiryDateString = "2026-10-14"
        val expectedEpoch = LocalDate.parse(expiryDateString)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond() * 1000L

        val result = AiDocumentResult(
            documentType = "GOVERNMENT_ID",
            title = "Test Doc",
            expiryDate = expiryDateString,
            sourceLocation = null,
            destinationLocation = null,
            journeyDate = null,
            holderName = null,
            documentId = null,
            issueDate = null,
            trainDetails = null,
            flightDetails = null,
            busDetails = null
        )

        // Act
        val mappedResult = AiResultMapper.map(result)

        // Assert
        assertEquals(expectedEpoch, mappedResult.expiryDateEpoch)
    }
}
