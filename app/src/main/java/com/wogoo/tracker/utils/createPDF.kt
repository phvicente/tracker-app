package com.wogoo.tracker.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.wogoo.tracker.model.entity.Reminder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createAndDownloadPdf(context: Context, reminders: List<Reminder>) {
    val groupedReminders = reminders.groupBy { it.horario }

    val pdfDocument = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    var currentPageNumber = 1
    var yPosition = 40f

    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    val titlePaint = Paint().apply {
        textSize = 24f
        isFakeBoldText = true
    }
    val headerPaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
    }
    val contentPaint = Paint().apply {
        textSize = 18f
    }
    val linePaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        strokeWidth = 1f
    }

    canvas.drawText("Relatório de Remédios", 30f, yPosition, titlePaint)
    yPosition += 30f
    canvas.drawLine(30f, yPosition, (pageWidth - 30).toFloat(), yPosition, linePaint)
    yPosition += 20f

    groupedReminders.forEach { (horario, remindersForTime) ->
        val formattedTime = formatTime(horario)
        val headerText = "Hora: $formattedTime"
        if (yPosition + 30f > pageHeight) {
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 40f
        }
        canvas.drawText(headerText, 30f, yPosition, headerPaint)
        yPosition += 20f
        canvas.drawLine(30f, yPosition, (pageWidth - 30).toFloat(), yPosition, linePaint)
        yPosition += 15f

        remindersForTime.forEach { reminder ->
            val reminderText = "- ${reminder.nomeRemedio}"
            if (yPosition + 20f > pageHeight) {
                pdfDocument.finishPage(page)
                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 40f
            }
            canvas.drawText(reminderText, 40f, yPosition, contentPaint)
            yPosition += 20f
        }
        yPosition += 20f
    }
    pdfDocument.finishPage(page)

    val fileName = "relatorio_lembretes_${System.currentTimeMillis()}.pdf"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
    }

    val resolver = context.contentResolver
    val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

    try {
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outStream ->
                pdfDocument.writeTo(outStream)
            }
            Toast.makeText(context, "PDF gerado em: $uri", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

private fun formatTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}