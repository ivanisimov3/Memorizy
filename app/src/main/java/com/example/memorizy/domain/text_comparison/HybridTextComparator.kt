package com.example.memorizy.domain.text_comparison

import com.example.memorizy.domain.text_comparison.algorithm.FuzzyTokenComparator
import com.example.memorizy.domain.text_comparison.nli.EntailmentTextClassifier
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class HybridTextComparator @Inject constructor(
    private val fuzzyTokenComparator: FuzzyTokenComparator,
    private val entailmentTextClassifier: EntailmentTextClassifier
) : TextComparator {

    override suspend fun compareDetailed(expected: String, actual: String): TextComparisonResult {
        if (actual.isBlank() && expected.isNotBlank()) {
            return TextComparisonResult(
                category = TextComparisonCategory.INCORRECT,
                isCorrect = false,
                fuzzyScore = 0f,
                entailmentScore = 0f,
                contradictionScore = 0f,
                neutralScore = 0f
            )
        }

        if (isExactMatch(expected, actual)) {
            return TextComparisonResult(
                category = TextComparisonCategory.CORRECT,
                isCorrect = true,
                fuzzyScore = 1f,
                entailmentScore = 1f,
                contradictionScore = 0f,
                neutralScore = 0f
            )
        }

        val fuzzyScore = fuzzyTokenComparator.score(expected, actual)   // Метрика совпадения токенов
        val nli = entailmentTextClassifier.classify(premise = actual, hypothesis = expected)
        val isEntailed = nli.isEntailed
        val hasLexicalOverlap = fuzzyScore >= FUZZY_THRESHOLD
        val isContradiction = nli.contradiction >= CONTRADICTION_THRESHOLD &&
            nli.contradiction > nli.entailment &&
            nli.contradiction > nli.neutral

        val category = when {
            isEntailed && hasLexicalOverlap -> TextComparisonCategory.CORRECT
            isEntailed -> TextComparisonCategory.CORRECT_PARAPHRASE
            isContradiction || hasLexicalOverlap -> TextComparisonCategory.SEMANTIC_ERROR
            else -> TextComparisonCategory.INCORRECT
        }

        return TextComparisonResult(
            category = category,
            isCorrect = isEntailed,
            fuzzyScore = fuzzyScore,
            entailmentScore = nli.entailment,
            contradictionScore = nli.contradiction,
            neutralScore = nli.neutral
        )
    }

    private fun isExactMatch(expected: String, actual: String): Boolean {
        val normalizedExpected = normalizeForExactMatch(expected)
        val normalizedActual = normalizeForExactMatch(actual)
        return normalizedExpected == normalizedActual
    }

    private fun normalizeForExactMatch(text: String): String {
        return text
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    companion object {
        private const val FUZZY_THRESHOLD = 0.5f
        private const val CONTRADICTION_THRESHOLD = 0.5f
    }
}