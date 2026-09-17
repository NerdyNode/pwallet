package com.pdfwallet.service.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    val membershipDetails: AiMembershipDetails? = null,
    val governmentIdDetails: AiGovernmentIdDetails? = null,
    val movieDetails: AiMovieDetails? = null
)

@Serializable
data class AiTrainDetails(
    val trainNumber: String? = null,
    val trainName: String? = null,
    val passengerName: String? = null,
    val boardingStation: String? = null,
    val destinationStation: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val coach: String? = null,
    val berth: String? = null,
    val travelClass: String? = null,
    val quota: String? = null,
    val bookingStatus: String? = null,
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
    @SerialName("passenger_name") val passengerName: String? = null,
    @SerialName("airline_name") val airlineName: String? = null,
    @SerialName("flight_number") val flightNumber: String? = null,
    @SerialName("origin_airport_code") val originAirportCode: String? = null,
    @SerialName("destination_airport_code") val destinationAirportCode: String? = null,
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("departure_time") val departureTime: String? = null,
    @SerialName("arrival_time") val arrivalTime: String? = null,
    @SerialName("seat_number") val seat: String? = null,
    val gate: String? = null,
    val terminal: String? = null,
    @SerialName("booking_reference") val bookingReference: String? = null
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

@Serializable
data class AiGovernmentIdDetails(
    val idType: String? = null,
    val fullName: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val fatherOrGuardianName: String? = null,
    val address: String? = null
)

@Serializable
data class AiMovieDetails(
    val cinemaName: String? = null,
    val movieName: String? = null,
    val showDate: String? = null,
    val showTime: String? = null,
    val screen: String? = null,
    val seats: String? = null
)
