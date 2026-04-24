package com.example.memorizy.domain.textcomparison

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
class HybridTextComparator @Inject constructor(
    private val fuzzyTokenComparator: FuzzyTokenComparator,
    private val semanticTextComparator: SemanticTextComparator
) : TextComparator {

    override suspend fun compare(expected: String, actual: String): Boolean {
        if (actual.isBlank()) return false  // Если ответ пустой
        if (isExactMatch(expected, actual)) return true // После нормализации полностью совпали
        if (!numbersAreCompatible(expected, actual)) return false   // Числа в эталоне и ответе не совпадают

        val fuzzyScore = fuzzyTokenComparator.score(expected, actual)
        if (fuzzyScore >= FUZZY_STRONG_THRESHOLD) return true   // Все проверки кроме семантики дали больше порога

        val semanticScore = semanticTextComparator.compare(expected, actual).similarity

        return if (isShortExpected(expected)) {
            false
        } else {
            semanticScore >= SEMANTIC_THRESHOLD &&
                fuzzyScore >= MIN_FUZZY_FOR_SEMANTIC
        }
    }

    private fun isExactMatch(expected: String, actual: String): Boolean {
        val normalizedExpected = normalizeForExactMatch(expected)
        val normalizedActual = normalizeForExactMatch(actual)
        return normalizedExpected.isNotEmpty() && normalizedExpected == normalizedActual
    }

    private fun isShortExpected(expected: String): Boolean {
        val significantWords = normalizeForExactMatch(expected)
            .split(" ")
            .filter { it.isNotBlank() }
            .filter { !RussianStopWords.isStopWord(it) }

        return significantWords.size <= SHORT_EXPECTED_WORD_LIMIT ||
            normalizeForExactMatch(expected).length <= SHORT_EXPECTED_CHAR_LIMIT
    }

    private fun normalizeForExactMatch(text: String): String {
        return text
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^a-zа-я0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun numbersAreCompatible(expected: String, actual: String): Boolean {
        val expectedNumbers = extractNormalizedNumbers(expected)
        if (expectedNumbers.isEmpty()) return true

        val actualNumbers = extractNormalizedNumbers(actual)
        return expectedNumbers == actualNumbers
    }

    private fun extractNormalizedNumbers(text: String): List<String> {
        return NUMBER_REGEX.findAll(text)
            .map { match -> normalizeNumber(match.value) }
            .toList()
    }

    private fun normalizeNumber(rawNumber: String): String {
        val normalized = rawNumber.replace(',', '.')
        return BigDecimal(normalized)
            .stripTrailingZeros()   // Убрать конечные нули
            .toPlainString()    // Убрать научную нотацию
    }

    companion object {
        private val NUMBER_REGEX = Regex("""\d+(?:[.,]\d+)?""")

        private const val FUZZY_STRONG_THRESHOLD = 0.88f
        private const val SEMANTIC_THRESHOLD = 0.82f
        private const val MIN_FUZZY_FOR_SEMANTIC = 0.30f
        private const val SHORT_EXPECTED_WORD_LIMIT = 2
        private const val SHORT_EXPECTED_CHAR_LIMIT = 12
    }
}