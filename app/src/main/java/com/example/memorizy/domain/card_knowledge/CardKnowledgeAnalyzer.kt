package com.example.memorizy.domain.card_knowledge

object CardKnowledgeAnalyzer {

    private const val MAX_LEVEL = 7
    private const val LEVEL_WEIGHT = 0.4f
    private const val STABILITY_WEIGHT = 0.6f
    private const val RECENT_ANSWER_LIMIT = 5

    fun calculateKnowledgeScore(level: Int, recentAnswerHistory: String): Float {
        val levelScore = level.coerceIn(0, MAX_LEVEL).toFloat() / MAX_LEVEL
        val recentAnswers = recentAnswerHistory
            .filter { it == '0' || it == '1' }
            .takeLast(RECENT_ANSWER_LIMIT)
        val stabilityScore = recentAnswers.count { it == '1' }.toFloat() / RECENT_ANSWER_LIMIT

        return (LEVEL_WEIGHT * levelScore + STABILITY_WEIGHT * stabilityScore)
            .coerceIn(0f, 1f)
    }
}