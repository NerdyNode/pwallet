package com.pdfwallet.ui.pass

import com.pdfwallet.data.db.DocumentType
import com.pdfwallet.ui.theme.DocAccent

object TemplateRegistry {

    private val registry: Map<DocumentType, TemplateContract> = mapOf(
        DocumentType.AIRLINE          to AirlineTemplate,
        DocumentType.TRAIN            to TrainTemplate,
        DocumentType.BUS              to GenericTemplate,
        DocumentType.MOVIE            to MovieTemplate,
        DocumentType.EVENT            to MovieTemplate,
        DocumentType.AADHAAR          to GovernmentIdTemplate,
        DocumentType.PAN_CARD         to GovernmentIdTemplate,
        DocumentType.PASSPORT         to GovernmentIdTemplate,
        DocumentType.DRIVING_LICENSE  to GovernmentIdTemplate,
        DocumentType.INVOICE          to GenericTemplate,
        DocumentType.PRESCRIPTION     to GenericTemplate,
        DocumentType.GENERIC          to GenericTemplate,
        DocumentType.UNKNOWN          to GenericTemplate,
    )

    fun resolve(type: DocumentType): TemplateContract =
        registry[type] ?: GenericTemplate

    fun accentColors(type: DocumentType, isDark: Boolean): DocAccent =
        resolve(type).accentColors(isDark)
}
