package com.example.memorizy.domain.importer.usecase

import android.content.Context
import android.net.Uri
import com.example.memorizy.domain.importer.model.CardImportError
import com.example.memorizy.domain.importer.model.ParseResult
import com.example.memorizy.domain.importer.model.ParsedCard
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Парсер входного файла
 *
 * Принцип работы:
 * - Используется библиотека kotlin-csv
 * - Отделяем термины и определения с помощью delimiter, quoteChar, escapeChar
 * - Разбираем каждую строку на карточки или ошибки
 */

class ParseCardsFileUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        val successfulCards = mutableListOf<ParsedCard>()
        val errors = mutableListOf<CardImportError>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = csvReader {
                    delimiter = ','
                    quoteChar = '"'
                    escapeChar = '\\'
                }
                
                var lineNumber = 1
                try {
                    val rows = reader.readAll(inputStream)
                    for (row in rows) {
                        try {
                            if (row.size >= 2) {
                                val term = row[0].trim()
                                val definition = row[1].trim()

                                if (term.isEmpty()) {
                                    errors.add(CardImportError(lineNumber, "Пустой термин"))
                                } else if (definition.isEmpty()) {
                                    errors.add(CardImportError(lineNumber, "Пустое определение"))
                                } else if (term.length > 500 || definition.length > 500) {
                                     errors.add(CardImportError(lineNumber, "Длина текста превышает 500 символов"))
                                } else {
                                    successfulCards.add(ParsedCard(term, definition))
                                }
                            } else if (row.isNotEmpty() && row[0].isNotBlank()) {
                                errors.add(CardImportError(lineNumber, "Недостаточно данных в строке (нужно минимум 2: термин и определение)"))
                            }
                        } catch (e: Exception) {
                            errors.add(CardImportError(lineNumber, "Ошибка чтения строки: ${e.message}"))
                        }
                        lineNumber++
                    }
                } catch (e: Exception) {
                    errors.add(CardImportError(0, "Ошибка парсинга CSV: файл имеет неверный формат"))
                }
            } ?: run {
                errors.add(CardImportError(0, "Не удалось открыть файл. Проверьте права доступа."))
            }
        } catch (e: Exception) {
             errors.add(CardImportError(0, "Не удалось прочитать файл или файл поврежден"))
        }

        ParseResult(successfulCards, errors)
    }
}
