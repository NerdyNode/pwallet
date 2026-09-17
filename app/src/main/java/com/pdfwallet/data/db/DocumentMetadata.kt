package com.pdfwallet.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Passenger(
    val name: String, 
    val age: Int?, 
    val seatOrBerth: String?
)

@Serializable
sealed class DocumentMetadata {

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Airline")
    data class Airline(
        val passengerName: String? = null,
        val airlineCode: String? = null,
        val airlineName: String? = null,
        val flightNumber: String? = null,
        val origin: AirportInfo? = null,
        val destination: AirportInfo? = null,
        val departureTime: String? = null,
        val arrivalTime: String? = null,
        val duration: String? = null,
        val travelClass: String? = null,
        val seat: String? = null,
        val gate: String? = null,
        val terminal: String? = null,
        val baggageAllowance: String? = null,
        val pnr: String? = null,
        val sequenceNumber: String? = null,
        val fareClass: String? = null,
        val frequentFlyerNumber: String? = null,
        val mealPreference: String? = null,
        // Legacy
        val source: String? = null,
        val departureDate: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    data class AirportInfo(
        val iataCode: String?,
        val cityName: String?,
        val airportName: String?
    )

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Train")
    data class Train(
        val passengerName: String? = null,
        val trainNumber: String? = null,
        val trainName: String? = null,
        val origin: StationInfo? = null,
        val destination: StationInfo? = null,
        val departureTime: String? = null,
        val arrivalTime: String? = null,
        val duration: String? = null,
        val travelClass: String? = null,
        val coachNumber: String? = null,
        val seatNumber: String? = null,
        val berthType: String? = null,
        val pnr: String? = null,
        val quota: String? = null,
        val chartStatus: String? = null,
        val mealIncluded: Boolean? = null,
        val distance: String? = null,
        val fare: String? = null,
        // Legacy
        val journeyDate: String? = null,
        val boardingStation: String? = null,
        val destinationStation: String? = null,
        val coach: String? = null,
        val berth: String? = null,
        val bookingStatus: BookingStatus = BookingStatus.UNKNOWN,
        val passengers: List<Passenger> = emptyList(),
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    data class StationInfo(
        val stationCode: String?,
        val stationName: String?,
        val cityName: String?
    )

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Bus")
    data class Bus(
        val passengerName: String? = null,
        val operatorName: String? = null,
        val busType: String? = null,
        val origin: String? = null,
        val destination: String? = null,
        val departureTime: String? = null,
        val arrivalTime: String? = null,
        val seatNumber: String? = null,
        val pickupPoint: String? = null,
        val dropPoint: String? = null,
        val pnr: String? = null,
        val fare: String? = null,
        // Legacy
        val operator: String? = null,
        val source: String? = null,
        val seat: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Movie")
    data class Movie(
        val movieName: String? = null,
        val certification: String? = null,
        val genre: String? = null,
        val format: String? = null,
        val language: String? = null,
        val cinemaName: String? = null,
        val cinemaAddress: String? = null,
        val screenName: String? = null,
        val showDate: String? = null,
        val showTime: String? = null,
        val row: String? = null,
        val seatNumbers: List<String>? = null,
        val ticketNumber: String? = null,
        val bookingId: String? = null,
        val amount: String? = null,
        val convenienceFee: String? = null,
        val totalAmount: String? = null,
        val bookedVia: String? = null,
        // Legacy
        val screen: String? = null,
        val seats: String? = null,
        val cinemaAddressLegacy: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Event")
    data class Event(
        val eventName: String? = null,
        val eventType: String? = null,
        val venue: String? = null,
        val city: String? = null,
        val eventDate: String? = null,
        val eventTime: String? = null,
        val doorOpenTime: String? = null,
        val category: String? = null,
        val seatInfo: String? = null,
        val ticketNumber: String? = null,
        val bookingId: String? = null,
        val amount: String? = null,
        val restrictions: List<String>? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.GovernmentId")
    data class GovernmentId(
        val idType: IdType? = null,
        val fullName: String? = null,
        val dateOfBirth: String? = null,
        val gender: String? = null,
        val idNumber: String? = null,
        val address: String? = null,
        val issueDate: String? = null,
        val expiryDate: String? = null,
        val issuingAuthority: String? = null,
        val fatherName: String? = null,
        val nationality: String? = null,
        // Legacy
        val fatherOrGuardianName: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    enum class IdType { AADHAAR, PAN, PASSPORT, DRIVING_LICENSE, VOTER_ID, OTHER }

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Invoice")
    data class Invoice(
        val vendorName: String? = null,
        val vendorGstin: String? = null,
        val customerName: String? = null,
        val invoiceNumber: String? = null,
        val invoiceDate: String? = null,
        val dueDate: String? = null,
        val lineItems: List<LineItem>? = null,
        val subtotal: String? = null,
        val taxAmount: String? = null,
        val totalAmount: String? = null,
        val currency: String? = null,
        val paymentStatus: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    data class LineItem(val description: String?, val quantity: String?, val amount: String?)

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.MedicalReport")
    data class MedicalReport(
        val patientName: String? = null,
        val hospitalName: String? = null,
        val doctorName: String? = null,
        val reportType: String? = null,
        val reportDate: String? = null,
        val referenceNumber: String? = null,
        val summary: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Generic")
    data class Generic(
        val extractedTitle: String? = null,
        val keyFields: List<KeyValuePair>? = null,
        val summary: String? = null,
        val detectedLanguage: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    data class KeyValuePair(val label: String, val value: String)
    
    // Legacy wrappers (to avoid breaking completely if database contains them)
    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Hotel")
    data class Hotel(
        val hotelName: String? = null,
        val checkIn: String? = null,
        val checkOut: String? = null,
        val roomDetails: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Transit")
    data class Transit(
        val operator: String? = null,
        val route: String? = null,
        val validity: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()

    @Serializable
    @SerialName("com.pdfwallet.data.db.TicketMetadata.Membership")
    data class Membership(
        val provider: String? = null,
        val memberName: String? = null,
        val validity: String? = null,
        val customFields: Map<String, String> = emptyMap()
    ) : DocumentMetadata()
}

class MetadataConverter {
    @TypeConverter
    fun fromMetadata(meta: DocumentMetadata?): String? {
        val json = Json { encodeDefaults = true; classDiscriminator = "type" }
        return meta?.let { json.encodeToString(DocumentMetadata.serializer(), it) }
    }

    @TypeConverter
    fun toMetadata(jsonString: String?): DocumentMetadata? {
        if (jsonString.isNullOrBlank()) return null
        val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }
        return try {
            json.decodeFromString(DocumentMetadata.serializer(), jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for corrupted/legacy JSON that cannot be mapped to the new sealed class structure
            null
        }
    }
}
