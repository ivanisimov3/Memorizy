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
import java.nio.charset.Charset
import javax.inject.Inject

/**
 * Парсер входного файла
 *
 * Принцип работы:
 * - Используется библиотека kotlin-csv
 * - Дополнительно определяется кодировка файла и разделитель колонок
 * - Поддерживаются CSV из Excel и других табличных редакторов
 */

class ParseCardsFileUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        val successfulCards = mutableListOf<ParsedCard>()
        val errors = mutableListOf<CardImportError>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                var lineNumber = 1

                try {
                    val normalizedFileBytes = normalizeCsvBytes(inputStream.readBytes())    // Получаем содержимое файла в чистом виде UTF-8
                    val delimiter = detectDelimiter(normalizedFileBytes)    // Определяем разделитель
                    val rows = parseRows(normalizedFileBytes, delimiter)

                    for ((rowIndex, row) in rows.withIndex()) {
                        if (rowIndex == 0 && isHeaderRow(row)) {
                            lineNumber++
                            continue
                        }

                        try {
                            if (row.size >= 2) {
                                val term = row[0].trim()
                                val definition = row[1].trim()
                                val definitionVariants = normalizeDefinitionVariants(
                                    primaryDefinition = definition,
                                    rawVariants = row.drop(2)
                                )

                                if (term.isEmpty()) {
                                    errors.add(CardImportError(
                                        lineNumber,
                                        "Пустой термин")
                                    )
                                } else if (definition.isEmpty()) {
                                    errors.add(CardImportError(
                                        lineNumber,
                                        "Пустое определение")
                                    )
                                } else if (term.length > 500 || definition.length > 500) {
                                    errors.add(CardImportError(
                                        lineNumber,
                                        "Длина текста превышает 500 символов")
                                    )
                                } else if (definitionVariants.any { it.length > 500 }) {
                                    errors.add(CardImportError(
                                        lineNumber,
                                        "Одно из дополнительных определений превышает 500 символов")
                                    )
                                } else {
                                    successfulCards.add(
                                        ParsedCard(
                                            term = term,
                                            definition = definition,
                                            definitionVariants = definitionVariants
                                        )
                                    )
                                }
                            } else if (row.isNotEmpty() && row[0].isNotBlank()) {
                                errors.add(CardImportError(
                                    lineNumber,
                                    "Недостаточно данных в строке (нужно минимум 2: термин и определение)")
                                )
                            }
                        } catch (e: Exception) {
                            errors.add(CardImportError(
                                lineNumber,
                                "Ошибка чтения строки: ${e.message}")
                            )
                        }

                        lineNumber++
                    }
                } catch (e: Exception) {
                    errors.add(CardImportError(
                        0,
                        "Ошибка парсинга CSV: файл имеет неверный формат")
                    )
                }
            } ?: run {
                errors.add(CardImportError(
                    0,
                    "Не удалось открыть файл. Проверьте права доступа.")
                )
            }
        } catch (e: Exception) {
            errors.add(CardImportError(
                0,
                "Не удалось прочитать файл или файл поврежден")
            )
        }

        ParseResult(successfulCards, errors)
    }

    private fun normalizeCsvBytes(fileBytes: ByteArray): ByteArray {
        val normalizedText = decodeCsvContent(fileBytes).removePrefix("\uFEFF") // Декодинг файла + убираем Byte Order Mark вначале
        return normalizedText.toByteArray(Charsets.UTF_8)
    }

    private fun decodeCsvContent(fileBytes: ByteArray): String {
        if (fileBytes.isEmpty()) return ""

        return when {
            // UTF-8
            fileBytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())) ->
                fileBytes.copyOfRange(3, fileBytes.size).toString(Charsets.UTF_8)

            // UTF-16 LE
            fileBytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte())) ->
                fileBytes.copyOfRange(2, fileBytes.size).toString(Charsets.UTF_16LE)

            // UTF-16 BE
            fileBytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte())) ->
                fileBytes.copyOfRange(2, fileBytes.size).toString(Charsets.UTF_16BE)

            else -> {
                val utf8Text = fileBytes.toString(Charsets.UTF_8)
                if ('\uFFFD' !in utf8Text) {    // Текст успешно прочитан
                    utf8Text
                } else {    // Тогда работаем с Win 1251
                    fileBytes.toString(Charset.forName("windows-1251"))
                }
            }
        }
    }

    private fun detectDelimiter(fileBytes: ByteArray): Char {
        val candidates = listOf(';', ',', '\t')

        val bestCandidate = candidates
            .map { delimiter ->
                val rows = runCatching {
                    parseRows(fileBytes, delimiter)
                }.getOrDefault(emptyList())

                val nonEmptyRows = rows.filter { row -> row.any { it.isNotBlank() } }
                val rowsWithData = nonEmptyRows.count { it.size >= 2 }
                val averageColumns = if (nonEmptyRows.isEmpty()) {  // Число колонок в среднем
                    0.0
                } else {
                    nonEmptyRows.sumOf { it.size }.toDouble() / nonEmptyRows.size
                }

                Triple(delimiter, rowsWithData, averageColumns)
            }
            .maxWithOrNull(compareBy<Triple<Char, Int, Double>> { it.second }.thenBy { it.third })

        return if (bestCandidate == null || bestCandidate.second == 0) ',' else bestCandidate.first
    }

    private fun parseRows(
        fileBytes: ByteArray,
        delimiter: Char
    ): List<List<String>> {
        val reader = csvReader {
            this.delimiter = delimiter
            quoteChar = '"'
            escapeChar = '\\'
            charset = Charsets.UTF_8.name()
            skipEmptyLine = true
        }

        return reader.open(fileBytes.inputStream()) {
            buildList {
                while (true) {
                    val row = readNext() ?: break
                    add(row)
                }
            }
        }
    }

    // Базовая нормализация дополнительных определений
    private fun normalizeDefinitionVariants(
        primaryDefinition: String,
        rawVariants: List<String>
    ): List<String> {
        val primaryNormalized = primaryDefinition.trim()

        return rawVariants
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it != primaryNormalized }
            .distinct()
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        if (row.size < 2) return false

        val normalizedCells = row.map { cell ->
            cell.trim()
                .lowercase()
                .removePrefix("\uFEFF")
        }

        val firstCell = normalizedCells.getOrNull(0).orEmpty()
        val secondCell = normalizedCells.getOrNull(1).orEmpty()

        val matchesSemanticHeader =
            firstCell in setOf("термин", "term", "слово") &&
                secondCell in setOf("определение", "definition", "значение")

        val matchesGeneratedExcelHeader = normalizedCells.all { cell ->
            Regex("""(column|столбец)\s*\d+""").matches(cell)
        }

        return matchesSemanticHeader || matchesGeneratedExcelHeader
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false    // Файл меньше по размеру, чем искомый префикс
        return prefix.indices.all { index -> this[index] == prefix[index] }
    }
}