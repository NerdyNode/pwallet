package com.pdfwallet.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class BookingStatus { 
    CONFIRMED, 
    RAC, 
    WAITLIST, 
    UNKNOWN 
}

@Serializable
data class Passenger(
    val name: String, 
    val age: Int?, 
    val seatOrBerth: String?
)

@Serializable
sealed class TicketMetadata {

    @Serializable
    data class Train(
        val trainNumber: String,
        val trainName: String? = null,
        val journeyDate: String,
        val boardingStation: String,
        val destinationStation: String,
        val departureTime: String? = null,
        val arrivalTime: String? = null,
        val coach: String?,
        val berth: String?,
        val travelClass: String, // SL, 3A, 2A, 1A
        val quota: String, // GN, TQ, PT...
        val bookingStatus: BookingStatus,
        val passengers: List<Passenger>
    ) : TicketMetadata()

    @Serializable
    data class Airline(
        val airlineName: String? = null,
        val flightNumber: String? = null,
        val departureTime: String? = null,
        val arrivalTime: String? = null,
        val source: String? = null,
        val destination: String? = null,
        val seat: String? = null,
        val gate: String? = null,
        val terminal: String? = null
    ) : TicketMetadata()

    @Serializable
    data class Bus(
        val operator: String? = null,
        val departureTime: String? = null,
        val source: String? = null,
        val destination: String? = null,
        val seat: String? = null
    ) : TicketMetadata()

    @Serializable
    data class GovernmentId(
        val dateOfBirth: String? = null,
        val fatherOrGuardianName: String? = null
    ) : TicketMetadata()

    @Serializable
    data class Hotel(
        val hotelName: String? = null,
        val checkIn: String? = null,
        val checkOut: String? = null,
        val roomDetails: String? = null
    ) : TicketMetadata()

    @Serializable
    data class Transit(
        val operator: String? = null,
        val route: String? = null,
        val validity: String? = null
    ) : TicketMetadata()

    @Serializable
    data class Membership(
        val provider: String? = null,
        val memberName: String? = null,
        val validity: String? = null
    ) : TicketMetadata()
}

class MetadataConverter {
    @TypeConverter
    fun fromMetadata(meta: TicketMetadata?): String? {
        return meta?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toMetadata(json: String?): TicketMetadata? {
        return json?.let { Json.decodeFromString<TicketMetadata>(it) }
    }
}
