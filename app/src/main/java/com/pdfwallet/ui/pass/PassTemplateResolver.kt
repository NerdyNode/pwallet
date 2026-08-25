package com.pdfwallet.ui.pass

import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.data.db.TicketMetadata

object PassTemplateResolver {

    fun resolve(doc: Document): PassTemplate {
        return when (val meta = doc.additionalMeta) {
            is TicketMetadata.Train -> trainTemplate(doc, meta)
            is TicketMetadata.Airline -> airlineTemplate(doc, meta)
            is TicketMetadata.Bus -> busTemplate(doc, meta)
            is TicketMetadata.Hotel -> hotelTemplate(doc, meta)
            is TicketMetadata.GovernmentId -> governmentIdTemplate(doc, meta)
            is TicketMetadata.Transit -> transitTemplate(doc, meta)
            is TicketMetadata.Membership -> membershipTemplate(doc, meta)
            null -> genericTemplate(doc)
        }
    }

    private fun trainTemplate(doc: Document, meta: TicketMetadata.Train) = PassTemplate(
        typeLabel = "TRAIN TICKET",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("PNR", doc.documentId ?: "--", Emphasis.LARGE),
            FieldSpec("STATUS", meta.bookingStatus.name)
        ),
        routeSection = RouteSpec(
            origin = FieldSpec("FROM", meta.boardingStation, Emphasis.HERO),
            destination = FieldSpec("TO", meta.destinationStation, Emphasis.HERO)
        ),
        bodyRows = listOf(
            FieldRow(listOf(
                FieldSpec("DEPARTURE", meta.departureTime ?: "--"),
                FieldSpec("ARRIVAL", meta.arrivalTime ?: "--")
            )),
            FieldRow(listOf(
                FieldSpec("TRAIN", "${meta.trainNumber} ${meta.trainName ?: ""}".trim()),
                FieldSpec("CLASS", meta.travelClass),
                FieldSpec("QUOTA", meta.quota),
                FieldSpec("DATE", meta.journeyDate)
            ))
        ),
        passengerList = meta.passengers.map { p ->
            FieldSpec(p.name, p.seatOrBerth ?: "--")
        },
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )

    private fun airlineTemplate(doc: Document, meta: TicketMetadata.Airline) = PassTemplate(
        typeLabel = "BOARDING PASS",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("PNR", doc.documentId ?: "--", Emphasis.LARGE),
            FieldSpec("FLIGHT", meta.flightNumber ?: "--")
        ),
        routeSection = RouteSpec(
            origin = FieldSpec("FROM", meta.source ?: "--", Emphasis.HERO),
            destination = FieldSpec("TO", meta.destination ?: "--", Emphasis.HERO)
        ),
        bodyRows = listOf(
            FieldRow(listOf(
                FieldSpec("DEPARTURE", meta.departureTime ?: "--"),
                FieldSpec("ARRIVAL", meta.arrivalTime ?: "--")
            )),
            FieldRow(listOf(
                FieldSpec("SEAT", meta.seat ?: "--"),
                FieldSpec("GATE", meta.gate ?: "--"),
                FieldSpec("TERMINAL", meta.terminal ?: "--")
            ))
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )

    private fun busTemplate(doc: Document, meta: TicketMetadata.Bus) = PassTemplate(
        typeLabel = "BUS TICKET",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("OPERATOR", meta.operator ?: "--", Emphasis.LARGE),
            FieldSpec("SEAT", meta.seat ?: "--")
        ),
        routeSection = if (meta.source != null || meta.destination != null) RouteSpec(
            origin = FieldSpec("FROM", meta.source ?: "--", Emphasis.HERO),
            destination = FieldSpec("TO", meta.destination ?: "--", Emphasis.HERO)
        ) else null,
        bodyRows = listOf(
            FieldRow(listOf(
                FieldSpec("DEPARTURE", meta.departureTime ?: "--")
            ))
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )

    private fun hotelTemplate(doc: Document, meta: TicketMetadata.Hotel) = PassTemplate(
        typeLabel = "HOTEL BOOKING",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("HOTEL", meta.hotelName ?: "--", Emphasis.LARGE)
        ),
        bodyRows = listOf(
            FieldRow(listOf(
                FieldSpec("CHECK-IN", meta.checkIn ?: "--"),
                FieldSpec("CHECK-OUT", meta.checkOut ?: "--")
            )),
            FieldRow(listOf(
                FieldSpec("ROOM", meta.roomDetails ?: "--")
            ))
        ),
        detailFields = listOfNotNull(
            doc.holderName?.let { FieldSpec("GUEST", it) },
            doc.documentId?.let { FieldSpec("BOOKING ID", it) }
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )

    private fun governmentIdTemplate(doc: Document, meta: TicketMetadata.GovernmentId) = PassTemplate(
        typeLabel = "GOVERNMENT ID",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("ID NUMBER", doc.documentId ?: "--", Emphasis.LARGE)
        ),
        bodyRows = listOf(
            FieldRow(listOfNotNull(
                doc.holderName?.let { FieldSpec("NAME", it) },
                meta.dateOfBirth?.let { FieldSpec("DOB", it) }
            )),
            FieldRow(listOfNotNull(
                meta.fatherOrGuardianName?.let { FieldSpec("FATHER/GUARDIAN", it) },
                doc.issueDate?.let { FieldSpec("ISSUED", it) }
            ))
        ),
        detailFields = listOfNotNull(
            doc.expiryDate?.let { FieldSpec("EXPIRES", it) }
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )

    private fun transitTemplate(doc: Document, meta: TicketMetadata.Transit) = PassTemplate(
        typeLabel = "TRANSIT PASS",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("OPERATOR", meta.operator ?: "--", Emphasis.LARGE)
        ),
        bodyRows = listOf(
            FieldRow(listOfNotNull(
                meta.route?.let { FieldSpec("ROUTE", it) },
                meta.validity?.let { FieldSpec("VALID", it) }
            ))
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR)
        }
    )

    private fun membershipTemplate(doc: Document, meta: TicketMetadata.Membership) = PassTemplate(
        typeLabel = "MEMBERSHIP",
        title = doc.title,
        headerFields = listOf(
            FieldSpec("PROVIDER", meta.provider ?: "--", Emphasis.LARGE)
        ),
        bodyRows = listOf(
            FieldRow(listOfNotNull(
                meta.memberName?.let { FieldSpec("MEMBER", it) },
                meta.validity?.let { FieldSpec("VALID", it) }
            ))
        ),
        detailFields = listOfNotNull(
            doc.documentId?.let { FieldSpec("MEMBER ID", it) }
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR)
        }
    )

    private fun genericTemplate(doc: Document) = PassTemplate(
        typeLabel = doc.documentType.name,
        title = doc.title,
        headerFields = listOfNotNull(
            doc.documentId?.let { FieldSpec("ID", it, Emphasis.LARGE) }
        ),
        bodyRows = listOf(
            FieldRow(listOfNotNull(
                doc.holderName?.let { FieldSpec("NAME", it) },
                doc.issueDate?.let { FieldSpec("ISSUED", it) }
            ))
        ).filter { it.fields.isNotEmpty() },
        detailFields = listOfNotNull(
            doc.sourceLocation?.let { FieldSpec("FROM", it) },
            doc.destinationLocation?.let { FieldSpec("TO", it) },
            doc.expiryDate?.let { FieldSpec("EXPIRES", it) }
        ),
        barcodeConfig = doc.contentHash.let {
            BarcodeConfig(it, BarcodeDisplayFormat.QR, doc.documentId)
        }
    )
}
