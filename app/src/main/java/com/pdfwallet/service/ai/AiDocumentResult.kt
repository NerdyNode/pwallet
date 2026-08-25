package com.pdfwallet.service.ai

import kotlinx.serialization.Serializable

/**
 * The exact JSON shape that Gemini returns after analysing a document.
 * All fields are nullable — Gemini fills only what it can confidently detect.
 */
@Serializable
data class AiDocumentResult(
    val documentType: String = "OTHER",       // TRAIN_TICKET, FLIGHT_TICKET, BUS_TICKET, GOVERNMENT_ID, HOTEL_BOOKING, TRANSIT_PASS, MEMBERSHIP_CARD, CERTIFICATE, OTHER
    val title: String = "Imported Document",
    val holderName: String? = null,
    val documentId: String? = null,           // PNR, ticket number, ID number, etc.
    val issueDate: String? = null,            // YYYY-MM-DD
    val expiryDate: String? = null,           // YYYY-MM-DD
    val journeyDate: String? = null,          // YYYY-MM-DD
    val dateOfBirth: String? = null,          // For Government IDs
    val fatherOrGuardianName: String? = null, // For Government IDs
    val sourceLocation: String? = null,
    val destinationLocation: String? = null,
    val trainDetails: AiTrainDetails? = null,
    val flightDetails: AiFlightDetails? = null,
    val busDetails: AiBusDetails? = null,
    val hotelDetails: AiHotelDetails? = null,
    val transitDetails: AiTransitDetails? = null,
    val membershipDetails: AiMembershipDetails? = null
)

@Serializable
data class AiTrainDetails(
    val trainNumber: String? = null,
    val trainName: String? = null,
    val boardingStation: String? = null,
    val destinationStation: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val coach: String? = null,
    val berth: String? = null,
    val travelClass: String? = null,   // SL, 3A, 2A, 1A
    val quota: String? = null,         // GN, TQ, PT
    val bookingStatus: String? = null, // CONFIRMED, RAC, WAITLIST, UNKNOWN
    val passengers: List<AiPassenger> = emptyList()
)

@Serializable
data class AiPassenger(
    val name: String? = null,
    val age: Int? = null,
    val seatOrBerth: String? = null
)

@Serializable
data class AiFlightDetails(
    val airlineName: String? = null,
    val flightNumber: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val seat: String? = null,
    val gate: String? = null,
    val terminal: String? = null
)

@Serializable
data class AiBusDetails(
    val operator: String? = null,
    val departureTime: String? = null,
    val seat: String? = null
)

@Serializable
data class AiHotelDetails(
    val hotelName: String? = null,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val roomDetails: String? = null
)

@Serializable
data class AiTransitDetails(
    val operator: String? = null,
    val route: String? = null,
    val validity: String? = null
)

@Serializable
data class AiMembershipDetails(
    val provider: String? = null,
    val memberName: String? = null,
    val validity: String? = null
)
