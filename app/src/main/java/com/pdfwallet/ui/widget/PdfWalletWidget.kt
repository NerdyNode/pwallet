package com.pdfwallet.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import com.pdfwallet.data.repository.DocumentRepository
import com.pdfwallet.data.db.DocumentType
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import com.pdfwallet.ui.MainActivity
import kotlinx.coroutines.runBlocking
import android.content.Intent

class PdfWalletWidget @Inject constructor(
    private val documentRepository: DocumentRepository
) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val documents = documentRepository.allDocuments.first()
        val upcomingTickets = documents.filter { 
            it.documentType == DocumentType.AIRLINE || it.documentType == DocumentType.TRAIN || it.documentType == DocumentType.BUS 
        }

        provideContent {
            WidgetContent(context, upcomingTickets.size)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, ticketCount: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.WHITE))
                .padding(16.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PDF Wallet",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Text(
                text = if (ticketCount > 0) "$ticketCount Upcoming Tickets" else "No upcoming tickets",
                modifier = GlanceModifier.padding(top = 8.dp)
            )
        }
    }
}
