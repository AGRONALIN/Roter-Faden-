package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareHelper {
    fun shareTextAsFile(context: Context, jsonStr: String, defaultTitle: String) {
        try {
            val cacheDir = context.cacheDir
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            // Export using our custom extension .roterfaden
            val fileName = "debattenbank_export_${dateFormat.format(Date())}.roterfaden"
            val file = File(cacheDir, fileName)
            file.writeText(jsonStr)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/x-roterfaden"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, defaultTitle)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Inhalte teilen"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to raw text sharing if file creation or provider access fails
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, jsonStr)
                putExtra(Intent.EXTRA_SUBJECT, defaultTitle)
            }
            context.startActivity(Intent.createChooser(intent, "Inhalte teilen"))
        }
    }
}
