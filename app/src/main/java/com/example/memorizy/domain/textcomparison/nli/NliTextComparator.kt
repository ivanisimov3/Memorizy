package com.example.memorizy.domain.textcomparison.nli

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NliTextComparator @Inject constructor(
    private val entailmentTextClassifier: EntailmentTextClassifier
) {

    suspend fun compare(expected: String, actual: String): Boolean {
        val normalizedExpected = expected.trim()
        val normalizedActual = actual.trim()

        if (normalizedExpected.isEmpty() || normalizedActual.isEmpty()) return false

        // Проверяем, достаточно ли ответа пользователя, чтобы из него следовал эталон.
        // Обратное направление могло бы засчитывать правдивые, но слишком неполные ответы.
        val result = entailmentTextClassifier.classify(
            premise = normalizedActual,
            hypothesis = normalizedExpected
        )

        return result.isEntailed
    }
}