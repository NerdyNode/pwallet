package com.pdfwallet.service

import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.PassState

object PassLifecycleManager {

    private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    fun computeState(doc: Document): PassState {
        val now = System.currentTimeMillis()

        // Check user-set states first
        if (doc.passState == PassState.ARCHIVED || doc.passState == PassState.USED) {
            return doc.passState
        }

        val journeyEpoch = doc.journeyDate
        val expiryEpoch = doc.expiryDateEpoch

        return when {
            // Has a journey date (tickets)
            journeyEpoch != null -> when {
                now < journeyEpoch -> PassState.UPCOMING
                now < journeyEpoch + ONE_DAY_MS -> PassState.ACTIVE
                else -> PassState.EXPIRED
            }
            // Has an expiry date (IDs, memberships)
            expiryEpoch != null -> when {
                now < expiryEpoch -> PassState.ACTIVE
                else -> PassState.EXPIRED
            }
            // No dates — always active
            else -> PassState.ACTIVE
        }
    }
}
