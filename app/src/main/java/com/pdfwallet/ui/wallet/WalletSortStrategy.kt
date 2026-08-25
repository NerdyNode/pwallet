package com.pdfwallet.ui.wallet

import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.PassState
import com.pdfwallet.service.PassLifecycleManager

enum class WalletSortStrategy {
    RELEVANCE,
    DATE_NEWEST,
    DATE_OLDEST,
    TYPE,
    NAME;

    companion object {
        val DEFAULT = RELEVANCE
    }
}

fun List<Document>.sortByStrategy(strategy: WalletSortStrategy): List<Document> =
    when (strategy) {
        WalletSortStrategy.RELEVANCE -> sortedWith(
            compareBy<Document> {
                when (PassLifecycleManager.computeState(it)) {
                    PassState.ACTIVE -> 0
                    PassState.UPCOMING -> 1
                    PassState.USED -> 2
                    PassState.EXPIRED -> 3
                    PassState.ARCHIVED -> 4
                }
            }.thenBy { it.journeyDate ?: it.expiryDateEpoch ?: Long.MAX_VALUE }
        )
        WalletSortStrategy.DATE_NEWEST -> sortedByDescending { it.journeyDate ?: it.importDate }
        WalletSortStrategy.DATE_OLDEST -> sortedBy { it.journeyDate ?: it.importDate }
        WalletSortStrategy.TYPE -> sortedBy { it.documentType.name }
        WalletSortStrategy.NAME -> sortedBy { it.title.lowercase() }
    }
