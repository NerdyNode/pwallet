package com.pdfwallet.service.ai

import com.pdfwallet.data.db.DocumentType

object GeminiPromptBuilder {

    fun build(text: String, docType: DocumentType?, firstPageBitmap: android.graphics.Bitmap? = null): String {
        val fields = fieldsFor(docType)
        val formatInstruction = """
            |Respond ONLY with a valid JSON object. No markdown, no explanation.
            |If a field is not found, use null.
        """.trimMargin()

        return """
            |You are a document field extraction system for a digital wallet app.
            |$formatInstruction
            |
            |Extract the following fields from the document text below:
            |${fields.joinToString("\n") { "- $it" }}
            |
            |Also determine the documentType from: TRAIN_TICKET, FLIGHT_TICKET, BUS_TICKET, GOVERNMENT_ID, HOTEL_BOOKING, MOVIE_TICKET, TRANSIT_PASS, MEMBERSHIP_CARD, CERTIFICATE, OTHER
            |
            |Return a JSON object matching this structure EXACTLY. Do NOT return arrays for the details fields. If there are multiple flights/trains, pick the first one:
            |{
            |  "documentType": "<type>",
            |  "title": "Short human-readable title",
            |  "holderName": "Full name or null",
            |  "documentId": "ID/PNR number or null",
            |  "issueDate": "YYYY-MM-DD or null",
            |  "expiryDate": "YYYY-MM-DD or null",
            |  "journeyDate": "YYYY-MM-DD or null",
            |  "dateOfBirth": "YYYY-MM-DD or null",
            |  "fatherOrGuardianName": "Father/guardian name or null",
            |  "sourceLocation": "Origin or null",
            |  "destinationLocation": "Destination or null",
            |  "trainDetails": { "trainNumber": "", "trainName": "", "passengerName": "", "boardingStation": "", "destinationStation": "", "departureTime": "HH:mm", "arrivalTime": "HH:mm", "coach": "", "berth": "", "travelClass": "", "quota": "", "bookingStatus": "", "passengers": [{"name":"","age":0,"seatOrBerth":""}] },
            |  "flightDetails": { "airlineName": "", "flightNumber": "", "departureTime": "HH:mm", "arrivalTime": "HH:mm", "seat": "", "gate": "", "terminal": "" },
            |  "busDetails": { "operator": "", "departureTime": "HH:mm", "seat": "" },
            |  "hotelDetails": { "hotelName": "", "checkIn": "", "checkOut": "", "roomDetails": "" },
            |  "transitDetails": { "operator": "", "route": "", "validity": "" },
            |  "membershipDetails": { "provider": "", "memberName": "", "validity": "" },
            |  "governmentIdDetails": { "idType": "Aadhaar|PAN|Passport|DrivingLicense|VoterID", "fullName": "", "idNumber": "", "gender": "Male|Female", "dateOfBirth": "YYYY-MM-DD", "fatherOrGuardianName": "", "address": "" },
            |  "movieDetails": { "cinemaName": "", "movieName": "", "showDate": "YYYY-MM-DD", "showTime": "HH:mm", "screen": "", "seats": "" }
            |}
            |
            |Rules:
            |- Set detail objects to null if not applicable.
            |- For GOVERNMENT_ID: fill governmentIdDetails. Also set holderName = fullName, documentId = idNumber.
            |- For TRAIN_TICKET: fill trainDetails. Set passengerName from Passenger Details.
            |- For MOVIE_TICKET: fill movieDetails with showDate and showTime.
            |
            |Document text:
            |---
            |$text
            |---
        """.trimMargin()
    }

    private fun fieldsFor(docType: DocumentType?): List<String> = when (docType) {
        DocumentType.AIRLINE -> listOf(
            "passenger_name",
            "flight_number",
            "origin_airport_code (IATA, e.g. DEL)",
            "destination_airport_code (IATA, e.g. BOM)",
            "departure_date (ISO 8601, e.g. 2026-08-18)",
            "departure_time (HH:mm)",
            "arrival_time (HH:mm)",
            "seat_number",
            "booking_reference (PNR)",
            "airline_name",
            "terminal",
            "gate"
        )
        DocumentType.GOVERNMENT_ID -> listOf(
            "full_name",
            "document_number",
            "document_type (e.g. Aadhaar, PAN, Passport)",
            "date_of_birth (ISO 8601)",
            "father_or_guardian_name",
            "expiry_date (ISO 8601, null if no expiry)",
            "issuing_authority",
            "address"
        )
        DocumentType.MOVIE -> listOf(
            "movie_name",
            "cinema_name",
            "show_time (ISO 8601 or HH:mm)",
            "screen",
            "seats",
            "booking_reference"
        )
        DocumentType.TRAIN -> listOf(
            "passenger_name",
            "pnr_number",
            "train_number",
            "train_name",
            "origin_station",
            "destination_station",
            "journey_date (ISO 8601)",
            "departure_time (HH:mm)",
            "arrival_time (HH:mm)",
            "coach",
            "seat_berth_number",
            "class (e.g. 3AC, SL)",
            "quota (e.g. GN, TQ, PT)",
            "booking_status (CONFIRMED, RAC, or WAITLIST)",
            "passengers (array of {name, age, seatOrBerth})"
        )
        DocumentType.BUS -> listOf(
            "passenger_name",
            "operator",
            "origin",
            "destination",
            "departure_time (HH:mm)",
            "seat_number",
            "booking_reference"
        )
        DocumentType.HOTEL -> listOf(
            "guest_name",
            "hotel_name",
            "check_in_date (ISO 8601)",
            "check_out_date (ISO 8601)",
            "room_details",
            "booking_reference"
        )
        DocumentType.TRANSIT -> listOf(
            "holder_name",
            "operator",
            "route",
            "validity",
            "pass_number"
        )
        DocumentType.MEMBERSHIP -> listOf(
            "member_name",
            "provider",
            "membership_id",
            "validity",
            "type (e.g. Gold, Silver, Premium)"
        )
        DocumentType.CERTIFICATE -> listOf(
            "recipient_name",
            "certificate_title",
            "issuing_organization",
            "issue_date (ISO 8601)",
            "certificate_number"
        )
        else -> listOf(
            "document_title",
            "primary_name",
            "reference_number",
            "issue_date (ISO 8601)",
            "expiry_date (ISO 8601)",
            "issuing_authority",
            "document_type"
        )
    }
}
