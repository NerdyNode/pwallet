package com.pdfwallet.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PdfWalletWidgetReceiver : GlanceAppWidgetReceiver() {
    
    @Inject
    lateinit var pdfWalletWidget: PdfWalletWidget

    override val glanceAppWidget: GlanceAppWidget
        get() = pdfWalletWidget
}
