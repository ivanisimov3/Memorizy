@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.importer.usecase

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.memorizy.domain.importer.model.ParsedCard
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.Charset

class ParseCardsFileUseCaseTest {

    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()

    private lateinit var useCase: ParseCardsFileUseCase

    @Before
    fun setUp() {
        every { context.contentResolver } returns contentResolver
        useCase = ParseCardsFileUseCase(context)
    }

    @Test
    fun `legacy_two_columns успешно импортируется без ошибок`() = runTest {
        val result = parseDocFile("import_test_01_legacy_two_columns.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertCard(
            expected = ParsedCard(
                term = "Клетка",
                definition = "Базовая единица живого организма",
                definitionVariants = emptyList()
            ),
            actual = result.successfulCards[0]
        )
        assertCard(
            expected = ParsedCard(
                term = "Фотосинтез",
                definition = "Процесс образования органических веществ на свету",
                definitionVariants = emptyList()
            ),
            actual = result.successfulCards[1]
        )
    }

    @Test
    fun `additional_definitions_semicolon корректно собирает дополнительные определения`() = runTest {
        val result = parseDocFile("import_test_02_additional_definitions_semicolon.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertCard(
            expected = ParsedCard(
                term = "Клетка",
                definition = "Базовая единица живого организма",
                definitionVariants = listOf(
                    "Структурная единица организма",
                    "Элементарная живая система"
                )
            ),
            actual = result.successfulCards[0]
        )
        assertCard(
            expected = ParsedCard(
                term = "Глагол",
                definition = "Часть речи, обозначающая действие",
                definitionVariants = listOf(
                    "Слово, называющее действие или состояние",
                    "Изменяемая часть речи"
                )
            ),
            actual = result.successfulCards[1]
        )
    }

    @Test
    fun `header_and_empty_columns корректно пропускает заголовок и пустые дополнительные колонки`() = runTest {
        val result = parseDocFile("import_test_03_header_and_empty_columns.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertCard(
            expected = ParsedCard(
                term = "Молекула",
                definition = "Наименьшая частица вещества, сохраняющая его свойства",
                definitionVariants = emptyList()
            ),
            actual = result.successfulCards[0]
        )
        assertCard(
            expected = ParsedCard(
                term = "Гипотеза",
                definition = "Научное предположение",
                definitionVariants = listOf("Предварительное объяснение явления")
            ),
            actual = result.successfulCards[1]
        )
    }

    @Test
    fun `comma_delimiter корректно распознается автоматически`() = runTest {
        val result = parseDocFile("import_test_04_comma_delimiter.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertEquals(
            listOf(
                "Структурная единица организма",
                "Элементарная живая система"
            ),
            result.successfulCards[0].definitionVariants
        )
        assertEquals(
            listOf("Единая природная система"),
            result.successfulCards[1].definitionVariants
        )
    }

    @Test
    fun `quoted_delimiters не ломается на разделителях внутри текста`() = runTest {
        val result = parseDocFile("import_test_05_quoted_delimiters.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertEquals(
            "Изменение высоты, силы; темпа и окраски голоса",
            result.successfulCards[0].definition
        )
        assertEquals(
            "Обмен информацией, мыслями, чувствами",
            result.successfulCards[1].definition
        )
    }

    @Test
    fun `multiline_definition корректно собирает многострочные определения`() = runTest {
        val result = parseDocFile("import_test_06_multiline_definition.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertEquals(
            "Последовательность процессов деления клетки.\n" +
                "В результате образуются две дочерние клетки.",
            result.successfulCards[0].definition
        )
        assertEquals(
            "Краткий анализ произведения.\n" +
                "Обычно содержит оценку, аргументы и вывод.",
            result.successfulCards[1].definition
        )
    }

    @Test
    fun `errors возвращает успешные строки и ошибки с номерами строк`() = runTest {
        val result = parseDocFile("import_test_07_errors.csv")

        assertEquals(1, result.successfulCards.size)
        assertEquals(3, result.errors.size)

        assertCard(
            expected = ParsedCard(
                term = "Клетка",
                definition = "Базовая единица живого организма",
                definitionVariants = listOf("Структурная единица организма")
            ),
            actual = result.successfulCards[0]
        )

        assertEquals(3, result.errors[0].lineNumber)
        assertEquals("Пустой термин", result.errors[0].reason)

        assertEquals(4, result.errors[1].lineNumber)
        assertEquals("Пустое определение", result.errors[1].reason)

        assertEquals(5, result.errors[2].lineNumber)
        assertEquals(
            "Недостаточно данных в строке (нужно минимум 2: термин и определение)",
            result.errors[2].reason
        )
    }

    @Test
    fun `duplicate_variants убирает повторы и основное определение из дополнительных`() = runTest {
        val result = parseDocFile("import_test_08_duplicate_variants.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.successfulCards.size)
        assertEquals(
            listOf("Структурная единица организма"),
            result.successfulCards[0].definitionVariants
        )
        assertEquals(
            listOf("Слово, называющее действие"),
            result.successfulCards[1].definitionVariants
        )
    }

    @Test
    fun `tab delimiter корректно распознается`() = runTest {
        val result = parseDocFile("import_test_09_tab_delimiter.csv")

        assertTrue(result.errors.isEmpty())
        assertEquals(1, result.successfulCards.size)
        assertCard(
            expected = ParsedCard(
                term = "Атом",
                definition = "Наименьшая частица химического элемента",
                definitionVariants = listOf("Частица вещества")
            ),
            actual = result.successfulCards[0]
        )
    }

    @Test
    fun `UTF-8 BOM корректно нормализуется`() = runTest {
        val bytes = byteArrayOf(
            0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()
        ) + "Термин;Определение\nОрбита;Траектория движения тела".toByteArray(Charsets.UTF_8)

        val result = parseBytes("bom_utf8.csv", bytes)

        assertTrue(result.errors.isEmpty())
        assertEquals(1, result.successfulCards.size)
        assertEquals("Орбита", result.successfulCards[0].term)
    }

    @Test
    fun `windows_1251 файл корректно декодируется`() = runTest {
        val bytes = "Термин;Определение\nСинтаксис;Правила построения предложений"
            .toByteArray(Charset.forName("windows-1251"))

        val result = parseBytes("win1251.csv", bytes)

        assertTrue(result.errors.isEmpty())
        assertEquals(1, result.successfulCards.size)
        assertCard(
            expected = ParsedCard(
                term = "Синтаксис",
                definition = "Правила построения предложений",
                definitionVariants = emptyList()
            ),
            actual = result.successfulCards[0]
        )
    }

    private suspend fun parseDocFile(fileName: String) =
        parseBytes(fileName, repoFile("docs/$fileName").readBytes())

    private suspend fun parseBytes(fileName: String, bytes: ByteArray) =
        useCase(createUri(fileName, bytes))

    private fun createUri(fileName: String, bytes: ByteArray): Uri {
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://test/$fileName"
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(bytes)
        return uri
    }

    private fun repoFile(relativePath: String): File {
        return generateSequence(File(".").canonicalFile) { it.parentFile }
            .map { File(it, relativePath) }
            .firstOrNull { it.exists() }
            ?: error("Не удалось найти файл $relativePath")
    }

    private fun assertCard(expected: ParsedCard, actual: ParsedCard) {
        assertEquals(expected.term, actual.term)
        assertEquals(expected.definition, actual.definition)
        assertEquals(expected.definitionVariants, actual.definitionVariants)
    }
}
