package com.example.memorizy.domain.textcomparison.semantic

import kotlin.math.sqrt

object CosineSimilarity {

    fun calculate(first: FloatArray, second: FloatArray): Float {
        require(first.size == second.size) {
            "Embedding sizes must match: ${first.size} != ${second.size}"
        }

        var dot = 0.0
        var firstNorm = 0.0
        var secondNorm = 0.0

        for (index in first.indices) {
            val a = first[index].toDouble()
            val b = second[index].toDouble()
            dot += a * b
            firstNorm += a * a
            secondNorm += b * b
        }

        if (firstNorm == 0.0 || secondNorm == 0.0) return 0f
        return (dot / (sqrt(firstNorm) * sqrt(secondNorm))).toFloat()
    }

    fun normalize(vector: FloatArray): FloatArray {
        var norm = 0.0
        for (value in vector) {
            norm += value.toDouble() * value.toDouble()
        }

        if (norm == 0.0) return vector

        val scale = sqrt(norm).toFloat()
        return FloatArray(vector.size) { index -> vector[index] / scale }
    }
}