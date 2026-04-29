package com.example.memorizy.domain.data_exchange.exporter.model

import android.net.Uri

data class ExportedCsvFile(
    val uri: Uri,
    val fileName: String
)