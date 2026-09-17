package com.pdfwallet.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.MultiFormatWriter

object BarcodeGenerator {
    fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap? {
        if (text.isEmpty()) return null
        return try {
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateBarcode(text: String, width: Int = 1024, height: Int = 300): Bitmap? {
        if (text.isEmpty()) return null
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generatePDF417(text: String, width: Int = 1024, height: Int = 300): Bitmap? {
        if (text.isEmpty()) return null
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.PDF_417, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
