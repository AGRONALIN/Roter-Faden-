package com.example.ui

import android.net.Uri
import androidx.core.content.FileProvider

class RoterFadenFileProvider : FileProvider() {
    override fun getType(uri: Uri): String? {
        val path = uri.path
        return if (path != null && path.endsWith(".roterfaden")) {
            "application/x-roterfaden"
        } else {
            super.getType(uri)
        }
    }
}
