package com.example.memorizy.domain.textcomparison.semantic

import com.example.memorizy.domain.textcomparison.SemanticComparisonResult
import com.example.memorizy.domain.textcomparison.SemanticTextComparator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class RubertTinyLiteTextComparator @Inject constructor(
    private val textEmbeddingModel: TextEmbeddingModel
) : SemanticTextComparator {

    private val threshold = 0.75f
    private val expectedEmbeddingCache = ConcurrentHashMap<String, FloatArray>()

    override suspend fun compare(expected: String, actual: String): SemanticComparisonResult {
        val normalizedExpected = expected.trim()
        val normalizedActual = actual.trim()

        if (normalizedExpected.isEmpty() || normalizedActual.isEmpty()) {
            return SemanticComparisonResult(
                isSimilar = false,
                similarity = 0f,
                threshold = threshold
            )
        }

        val expectedEmbedding = expectedEmbeddingCache[normalizedExpected]
            ?: textEmbeddingModel.embed(normalizedExpected).also { embedding ->
                expectedEmbeddingCache.putIfAbsent(normalizedExpected, embedding)
            }
        val actualEmbedding = textEmbeddingModel.embed(actual)
        val similarity = CosineSimilarity.calculate(expectedEmbedding, actualEmbedding)

        return SemanticComparisonResult(
            isSimilar = similarity >= threshold,
            similarity = similarity,
            threshold = threshold
        )
    }
}