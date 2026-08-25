package com.pdfwallet.service.ai

import com.pdfwallet.data.db.BookingStatus
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.Passenger
import com.pdfwallet.data.db.TicketMetadata

/**
 * Maps [AiDocumentResult] fields to:
 *  - A [DocumentType] for the database column
 *  - A [TicketMetadata] sealed-class value for rich card rendering
 *
 * All fields have safe defaults so nothing can cause a crash.
 */
object AiResultMapper {

    data class MappedResult(
        val documentType: DocumentType,
        val title: String,
        val holderName: String?,
        val documentId: String?,
        val issueDate: String?,
        val expiryDate: String?,
        val expiryDateEpoch: Long?,
        val sourceLocation: String?,
        val destinationLocation: String?,
        val journeyDate: Long?,          // epoch millis or null
        val bookingStatus: BookingStatus?,
        val ticketMetadata: TicketMetadata?
    )

    fun map(result: AiDocumentResult): MappedResult {
        val docType = when (result.documentType.uppercase()) {
            "TRAIN_TICKET", "TRAIN"  -> DocumentType.TRAIN
            "FLIGHT_TICKET", "AIRLINE" -> DocumentType.AIRLINE
            "BUS_TICKET", "BUS"    -> DocumentType.BUS
            "GOVERNMENT_ID" -> DocumentType.GOVERNMENT_ID
            "HOTEL_BOOKING", "HOTEL" -> DocumentType.HOTEL
            "TRANSIT_PASS", "TRANSIT" -> DocumentType.TRANSIT
            "MEMBERSHIP_CARD", "MEMBERSHIP" -> DocumentType.MEMBERSHIP
            "CERTIFICATE" -> DocumentType.CERTIFICATE
            else            -> DocumentType.OTHER
        }

        val ticketMetadata: TicketMetadata? = when (docType) {
            DocumentType.TRAIN   -> mapTrainMetadata(result)
            DocumentType.AIRLINE -> mapFlightMetadata(result)
            DocumentType.BUS     -> mapBusMetadata(result)
            DocumentType.GOVERNMENT_ID, DocumentType.CERTIFICATE -> mapGovernmentIdMetadata(result)
            DocumentType.HOTEL   -> mapHotelMetadata(result)
            DocumentType.TRANSIT -> mapTransitMetadata(result)
            DocumentType.MEMBERSHIP -> mapMembershipMetadata(result)
            else -> null
        }

        val bookingStatus: BookingStatus? = result.trainDetails?.bookingStatus?.let {
            when (it.uppercase()) {
                "CONFIRMED" -> BookingStatus.CONFIRMED
                "RAC"       -> BookingStatus.RAC
                "WAITLIST"  -> BookingStatus.WAITLIST
                else        -> BookingStatus.UNKNOWN
            }
        }

        // Parse journeyDate string ("YYYY-MM-DD") to epoch millis
        val journeyEpoch = result.journeyDate?.let { parseToEpochMillis(it) }

        val expiryEpoch = result.expiryDate?.let {
            try {
                java.time.LocalDate.parse(it).atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000L
            } catch (e: Exception) {
                null
            }
        }

        return MappedResult(
            documentType = docType,
            title = result.title.ifBlank { "Imported Document" },
            holderName = result.holderName,
            documentId = result.documentId,
            issueDate = result.issueDate,
            expiryDate = result.expiryDate,
            expiryDateEpoch = expiryEpoch,
            sourceLocation = result.sourceLocation,
            destinationLocation = result.destinationLocation,
            journeyDate = journeyEpoch,
            bookingStatus = bookingStatus,
            ticketMetadata = ticketMetadata
        )
    }

    // ── Metadata builders ────────────────────────────────────────────────────

    private fun mapTrainMetadata(result: AiDocumentResult): TicketMetadata.Train? {
        val d = result.trainDetails ?: return null
        return TicketMetadata.Train(
            trainNumber = d.trainNumber ?: "Unknown",
            trainName = d.trainName,
            journeyDate = result.journeyDate ?: "Unknown",
            boardingStation = d.boardingStation ?: result.sourceLocation ?: "Unknown",
            destinationStation = d.destinationStation ?: result.destinationLocation ?: "Unknown",
            departureTime = d.departureTime,
            arrivalTime = d.arrivalTime,
            coach = d.coach,
            berth = d.berth,
            travelClass = d.travelClass ?: "Unknown",
            quota = d.quota ?: "GN",
            bookingStatus = when (d.bookingStatus?.uppercase()) {
                "CONFIRMED" -> BookingStatus.CONFIRMED
                "RAC"       -> BookingStatus.RAC
                "WAITLIST"  -> BookingStatus.WAITLIST
                else        -> BookingStatus.UNKNOWN
            },
            passengers = d.passengers.map { p ->
                Passenger(
                    name = p.name ?: "Unknown",
                    age = p.age,
                    seatOrBerth = p.seatOrBerth
                )
            }
        )
    }

    private fun mapFlightMetadata(result: AiDocumentResult): TicketMetadata.Airline? {
        val d = result.flightDetails ?: return null
        return TicketMetadata.Airline(
            airlineName = d.airlineName,
            flightNumber = d.flightNumber,
            departureTime = d.departureTime,
            arrivalTime = d.arrivalTime,
            source = result.sourceLocation,
            destination = result.destinationLocation,
            seat = d.seat,
            gate = d.gate,
            terminal = d.terminal
        )
    }

    private fun mapBusMetadata(result: AiDocumentResult): TicketMetadata.Bus? {
        val d = result.busDetails ?: return null
        return TicketMetadata.Bus(
            operator = d.operator,
            departureTime = d.departureTime,
            source = result.sourceLocation,
            destination = result.destinationLocation,
            seat = d.seat
        )
    }

    private fun mapGovernmentIdMetadata(result: AiDocumentResult): TicketMetadata.GovernmentId {
        return TicketMetadata.GovernmentId(
            dateOfBirth = result.dateOfBirth,
            fatherOrGuardianName = result.fatherOrGuardianName
        )
    }

    private fun mapHotelMetadata(result: AiDocumentResult): TicketMetadata.Hotel? {
        val d = result.hotelDetails ?: return null
        return TicketMetadata.Hotel(
            hotelName = d.hotelName,
            checkIn = d.checkIn,
            checkOut = d.checkOut,
            roomDetails = d.roomDetails
        )
    }

    private fun mapTransitMetadata(result: AiDocumentResult): TicketMetadata.Transit? {
        val d = result.transitDetails ?: return null
        return TicketMetadata.Transit(
            operator = d.operator,
            route = d.route,
            validity = d.validity
        )
    }

    private fun mapMembershipMetadata(result: AiDocumentResult): TicketMetadata.Membership? {
        val d = result.membershipDetails ?: return null
        return TicketMetadata.Membership(
            provider = d.provider,
            memberName = d.memberName,
            validity = d.validity
        )
    }

    // ── Date parsing ─────────────────────────────────────────────────────────

    private fun parseToEpochMillis(dateStr: String): Long? {
        return try {
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val year  = parts[0].toInt()
            val month = parts[1].toInt() - 1  // Calendar months are 0-based
            val day   = parts[2].toInt()
            val cal = java.util.Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}
