package com.example.memorizy.domain.data_exchange.exporter.usecase

import android.content.Context
import androidx.core.content.FileProvider
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.domain.data_exchange.exporter.model.ExportedCsvFile
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ExportCardsCsvUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        studySet: StudySet,
        cards: List<Card>
    ): ExportedCsvFile = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, EXPORT_DIR_NAME).apply { // Создаем папку для файлов экспорта
            mkdirs()
        }
        val fileName = CardsCsvFormatter.buildFileName(studySet.name)
        val file = File(exportDir, fileName)    // Creates a new File instance from a parent abstract pathname and a child pathname string
        val rows = CardsCsvFormatter.buildRows(cards)   // Формируем строки файла csv

        csvWriter {
            delimiter = ';'
            charset = Charsets.UTF_8.name()
            lineTerminator = "\n"
        }.writeAll(rows, file, append = false)

        val uri = FileProvider.getUriForFile(   // Передача через безопасный URI
            context,
            "${context.packageName}.fileprovider",  // Совпадает с тем же, что описан в манифесте
            file
        )

        ExportedCsvFile(
            uri = uri,
            fileName = fileName
        )
    }

    private companion object {
        const val EXPORT_DIR_NAME = "exports"
    }
}