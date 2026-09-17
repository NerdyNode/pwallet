package com.pdfwallet.service.ai

import com.pdfwallet.data.db.BookingStatus
import com.pdfwallet.data.db.DocumentMetadata
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.Passenger
import java.text.SimpleDateFormat
import java.util.Locale

object AiResultMapper {

    private fun format12Hour(timeStr: String?): String? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf24.parse(timeStr)
            if (date != null) sdf12.format(date) else timeStr
        } catch (e: Exception) {
            timeStr
        }
    }

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
        val journeyDate: Long?,
        val bookingStatus: BookingStatus?,
        val ticketMetadata: DocumentMetadata?
    )

    fun map(result: AiDocumentResult): MappedResult {
        val docType = when (result.documentType.uppercase()) {
            "TRAIN_TICKET", "TRAIN"  -> DocumentType.TRAIN
            "FLIGHT_TICKET", "AIRLINE" -> DocumentType.AIRLINE
            "BUS_TICKET", "BUS"    -> DocumentType.BUS
            "GOVERNMENT_ID" -> parseGovIdType(result.governmentIdDetails?.idType)
            "HOTEL_BOOKING", "HOTEL" -> DocumentType.GENERIC // Or DocumentType.HOTEL if it exists in DocumentType
            "MOVIE_TICKET", "MOVIE" -> DocumentType.MOVIE
            "TRANSIT_PASS", "TRANSIT" -> DocumentType.GENERIC
            "MEMBERSHIP_CARD", "MEMBERSHIP" -> DocumentType.GENERIC
            "CERTIFICATE" -> DocumentType.GENERIC
            else            -> DocumentType.UNKNOWN
        }

        val ticketMetadata: DocumentMetadata? = when (docType) {
            DocumentType.TRAIN   -> mapTrainMetadata(result)
            DocumentType.AIRLINE -> mapFlightMetadata(result)
            DocumentType.BUS     -> mapBusMetadata(result)
            DocumentType.AADHAAR, DocumentType.PAN_CARD, DocumentType.PASSPORT,
            DocumentType.DRIVING_LICENSE, DocumentType.VOTER_ID -> mapGovernmentIdMetadata(result)
            DocumentType.MOVIE   -> mapMovieMetadata(result)
            DocumentType.GENERIC -> {
                if (result.documentType.uppercase() == "TRANSIT_PASS" || result.documentType.uppercase() == "TRANSIT") {
                    mapTransitMetadata(result)
                } else if (result.documentType.uppercase() == "MEMBERSHIP_CARD" || result.documentType.uppercase() == "MEMBERSHIP") {
                    mapMembershipMetadata(result)
                } else if (result.documentType.uppercase() == "HOTEL_BOOKING" || result.documentType.uppercase() == "HOTEL") {
                    mapHotelMetadata(result)
                } else null
            }
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

    private fun parseGovIdType(typeString: String?): DocumentType {
        if (typeString == null) return DocumentType.GENERIC
        val up = typeString.uppercase()
        return when {
            up.contains("AADHAAR") -> DocumentType.AADHAAR
            up.contains("PAN") -> DocumentType.PAN_CARD
            up.contains("PASSPORT") -> DocumentType.PASSPORT
            up.contains("DRIVING") -> DocumentType.DRIVING_LICENSE
            up.contains("VOTER") -> DocumentType.VOTER_ID
            else -> DocumentType.GENERIC
        }
    }

    private fun mapTrainMetadata(result: AiDocumentResult): DocumentMetadata.Train? {
        val d = result.trainDetails ?: return null
        return DocumentMetadata.Train(
            passengerName = d.passengerName ?: result.holderName,
            trainNumber = d.trainNumber ?: "Unknown",
            trainName = d.trainName,
            journeyDate = result.journeyDate ?: "Unknown",
            boardingStation = d.boardingStation ?: result.sourceLocation ?: "Unknown",
            destinationStation = d.destinationStation ?: result.destinationLocation ?: "Unknown",
            departureTime = format12Hour(d.departureTime),
            arrivalTime = format12Hour(d.arrivalTime),
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

    private fun mapFlightMetadata(result: AiDocumentResult): DocumentMetadata.Airline? {
        val d = result.flightDetails ?: return null
        return DocumentMetadata.Airline(
            passengerName = d.passengerName,
            airlineName = d.airlineName,
            flightNumber = d.flightNumber,
            departureTime = format12Hour(d.departureTime),
            arrivalTime = format12Hour(d.arrivalTime),
            origin = DocumentMetadata.AirportInfo(iataCode = d.originAirportCode, cityName = result.sourceLocation, airportName = null),
            destination = DocumentMetadata.AirportInfo(iataCode = d.destinationAirportCode, cityName = result.destinationLocation, airportName = null),
            seat = d.seat,
            gate = d.gate,
            terminal = d.terminal,
            pnr = d.bookingReference ?: result.documentId
        )
    }

    private fun mapBusMetadata(result: AiDocumentResult): DocumentMetadata.Bus? {
        val d = result.busDetails ?: return null
        return DocumentMetadata.Bus(
            operatorName = d.operator,
            departureTime = format12Hour(d.departureTime),
            origin = result.sourceLocation,
            destination = result.destinationLocation,
            seatNumber = d.seat
        )
    }

    private fun mapGovernmentIdMetadata(result: AiDocumentResult): DocumentMetadata.GovernmentId {
        val d = result.governmentIdDetails
        
        val idType = when (parseGovIdType(d?.idType)) {
            DocumentType.AADHAAR -> DocumentMetadata.IdType.AADHAAR
            DocumentType.PAN_CARD -> DocumentMetadata.IdType.PAN
            DocumentType.PASSPORT -> DocumentMetadata.IdType.PASSPORT
            DocumentType.DRIVING_LICENSE -> DocumentMetadata.IdType.DRIVING_LICENSE
            DocumentType.VOTER_ID -> DocumentMetadata.IdType.VOTER_ID
            else -> DocumentMetadata.IdType.OTHER
        }

        return DocumentMetadata.GovernmentId(
            idType = idType,
            fullName = d?.fullName ?: result.holderName,
            idNumber = d?.idNumber ?: result.documentId,
            gender = d?.gender,
            dateOfBirth = d?.dateOfBirth ?: result.dateOfBirth,
            fatherName = d?.fatherOrGuardianName ?: result.fatherOrGuardianName,
            address = d?.address,
            issueDate = result.issueDate,
            expiryDate = result.expiryDate,
            issuingAuthority = null
        )
    }

    private fun mapMovieMetadata(result: AiDocumentResult): DocumentMetadata.Movie? {
        val d = result.movieDetails ?: return null
        return DocumentMetadata.Movie(
            cinemaName = d.cinemaName,
            movieName = d.movieName,
            showDate = d.showDate ?: result.journeyDate,
            showTime = format12Hour(d.showTime),
            screenName = d.screen,
            seatNumbers = d.seats?.split(",")?.map { it.trim() }
        )
    }

    private fun mapHotelMetadata(result: AiDocumentResult): DocumentMetadata.Hotel? {
        val d = result.hotelDetails ?: return null
        return DocumentMetadata.Hotel(
            hotelName = d.hotelName,
            checkIn = d.checkIn,
            checkOut = d.checkOut,
            roomDetails = d.roomDetails
        )
    }

    private fun mapTransitMetadata(result: AiDocumentResult): DocumentMetadata.Transit? {
        val d = result.transitDetails ?: return null
        return DocumentMetadata.Transit(
            operator = d.operator,
            route = d.route,
            validity = d.validity
        )
    }

    private fun mapMembershipMetadata(result: AiDocumentResult): DocumentMetadata.Membership? {
        val d = result.membershipDetails ?: return null
        return DocumentMetadata.Membership(
            provider = d.provider,
            memberName = d.memberName,
            validity = d.validity
        )
    }

    private fun parseToEpochMillis(dateStr: String): Long? {
        return try {
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val year  = parts[0].toInt()
            val month = parts[1].toInt() - 1  
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
