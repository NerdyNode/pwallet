package com.pdfwallet.ui.pass

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector

enum class Emphasis { NORMAL, LARGE, HERO }

data class FieldSpec(
    val label: String,
    val value: String,
    val emphasis: Emphasis = Emphasis.NORMAL,
    val copyable: Boolean = true
)

data class FieldRow(val fields: List<FieldSpec>)

data class RouteSpec(
    val origin: FieldSpec,
    val destination: FieldSpec,
    val icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
)

enum class BarcodeDisplayFormat { QR, PDF417, AZTEC, CODE_128, CODE_39, UNKNOWN }

data class BarcodeConfig(
    val value: String,
    val format: BarcodeDisplayFormat = BarcodeDisplayFormat.QR,
    val label: String? = null
)

data class PassTemplate(
    val typeLabel: String,
    val title: String,
    val headerFields: List<FieldSpec>,
    val routeSection: RouteSpec? = null,
    val bodyRows: List<FieldRow> = emptyList(),
    val detailFields: List<FieldSpec> = emptyList(),
    val passengerList: List<FieldSpec> = emptyList(),
    val barcodeConfig: BarcodeConfig? = null
)
