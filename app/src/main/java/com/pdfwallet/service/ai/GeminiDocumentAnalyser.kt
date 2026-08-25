package com.pdfwallet.service.ai

import com.pdfwallet.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import com.pdfwallet.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Sends extracted document text to the Gemini API (via Firebase AI Logic) and returns a structured
 * [AiDocumentResult]. Enforces JSON schema output.
 */
@Singleton
class GeminiDocumentAnalyser @Inject constructor(
    private val logger: Logger
) {

    companion object {
        private const val TAG = "GeminiDocumentAnalyser"
        private const val DEFAULT_MODEL = "gemini-3.5-flash-lite"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val remoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(mapOf("gemini_model_name" to DEFAULT_MODEL))
            fetchAndActivate()
        }
    }

    private val generativeModel: GenerativeModel
        get() {
            val configModel = remoteConfig.getString("gemini_model_name")
            val modelName = configModel.takeIf { it.isNotBlank() } ?: DEFAULT_MODEL
            return GenerativeModel(
                    modelName = modelName,
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        maxOutputTokens = 1024
                    }
                )
        }

    /**
     * Analyse [bitmaps] with Gemini and return an [AiDocumentResult].
     * Throws an Exception if the extraction fails.
     */
    suspend fun analyse(bitmaps: List<android.graphics.Bitmap>): AiDocumentResult = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            logger.w(TAG, "Empty bitmaps received — returning default result")
            return@withContext AiDocumentResult()
        }

        val promptText = """
You are a document analysis engine for a digital wallet app.
Analyze the following document images and return ONLY valid JSON matching this structure:
{
  "documentType": "TRAIN_TICKET | FLIGHT_TICKET | BUS_TICKET | GOVERNMENT_ID | HOTEL_BOOKING | TRANSIT_PASS | MEMBERSHIP_CARD | CERTIFICATE | OTHER",
  "title": "Short human-readable title",
  "holderName": "Full name or null",
  "documentId": "ID/PNR number or null",
  "issueDate": "YYYY-MM-DD or null",
  "expiryDate": "YYYY-MM-DD or null",
  "journeyDate": "YYYY-MM-DD or null",
  "dateOfBirth": "YYYY-MM-DD or null",
  "fatherOrGuardianName": "Father/guardian name or null",
  "sourceLocation": "Origin or null",
  "destinationLocation": "Destination or null",
  "trainDetails": { "trainNumber": "", "trainName": "", "boardingStation": "", "destinationStation": "", "departureTime": "HH:mm", "arrivalTime": "HH:mm", "coach": "", "berth": "", "travelClass": "SL|3A|2A|1A", "quota": "GN|TQ|PT", "bookingStatus": "CONFIRMED|RAC|WAITLIST", "passengers": [ { "name": "", "age": 0, "seatOrBerth": "" } ] },
  "flightDetails": { "airlineName": "", "flightNumber": "", "departureTime": "HH:mm", "arrivalTime": "HH:mm", "seat": "", "gate": "", "terminal": "" },
  "busDetails": { "operator": "", "departureTime": "HH:mm", "seat": "" },
  "hotelDetails": { "hotelName": "", "checkIn": "", "checkOut": "", "roomDetails": "" },
  "transitDetails": { "operator": "", "route": "", "validity": "" },
  "membershipDetails": { "provider": "", "memberName": "", "validity": "" }
}

Rules:
- documentType must be exactly one of the allowed enum strings.
- Set details objects (e.g. trainDetails, hotelDetails) to null if not applicable.
- For GOVERNMENT_ID and CERTIFICATE: documentId = the ID/certificate number. Also extract dateOfBirth and fatherOrGuardianName if present.
- For TRAIN_TICKET: always fill trainDetails. Extract trainName alongside trainNumber. Extract departureTime and arrivalTime.
- For FLIGHT_TICKET: always fill flightDetails.
- For BUS_TICKET: always fill busDetails.
- For HOTEL_BOOKING: always fill hotelDetails.
- For TRANSIT_PASS: always fill transitDetails. (Includes metro cards, local passes)
- For MEMBERSHIP_CARD: always fill membershipDetails. (Includes gym, club cards)
- title should be meaningful (e.g. "IRCTC Train Ticket - PNR 123456" or "Taj Hotel Booking").
        """.trimIndent()

        var rawResponseText: String? = null

        return@withContext try {
            val response = generativeModel.generateContent(
                content {
                    for (bitmap in bitmaps) {
                        image(bitmap)
                    }
                    text(promptText)
                }
            )
            rawResponseText = response.text
            
            logger.d(TAG, "Gemini Raw Response: $rawResponseText")

            if (rawResponseText == null) {
                logger.e(TAG, "Gemini returned null response text")
                throw IllegalStateException("Gemini returned null response text")
            } else {
                parseGeminiResponse(rawResponseText)
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error generating or parsing content from Gemini: ${e.message}\nRaw response was: $rawResponseText", e)
            throw e
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun parseGeminiResponse(text: String): AiDocumentResult {
        // Even with JSON schema, trim potential accidental fences just in case
        val cleanJson = text.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
            
        return try {
            json.decodeFromString<AiDocumentResult>(cleanJson)
        } catch (e: Exception) {
            logger.e(TAG, "JSON parsing failed for clean text: $cleanJson", e)
            AiDocumentResult()
        }
    }
}
