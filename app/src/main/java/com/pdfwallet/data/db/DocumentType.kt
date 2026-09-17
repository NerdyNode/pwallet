package com.pdfwallet.data.db

enum class DocumentType {
    // Travel
    AIRLINE, TRAIN, BUS, HOTEL, CAB, TRANSIT,
    // Entertainment
    MOVIE, EVENT, AMUSEMENT_PARK,
    // Identity
    AADHAAR, PAN_CARD, PASSPORT, DRIVING_LICENSE, VOTER_ID, GOVERNMENT_ID,
    // Financial
    INVOICE, RECEIPT, INSURANCE_POLICY,
    // Medical
    PRESCRIPTION, MEDICAL_REPORT, LAB_REPORT,
    // Other / Legacy
    MEMBERSHIP, CERTIFICATE, OTHER,
    // Generic
    GENERIC, UNKNOWN
}
