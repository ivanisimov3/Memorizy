package com.example.memorizy.domain.textcomparison.algorithm

import jakarta.inject.Inject

// Реализация сравнения текстов

class FuzzyTokenComparator @Inject constructor() : TextScorer {

    private val stemmer = RussianStemmer()

    private data class Token(
        val stem: String,
        val raw: String
    )

    override fun score(expected: String, actual: String): Float {
        val expectedTokens = tokenize(expected)
        val actualTokens = tokenize(actual)

        if (expectedTokens.isEmpty() && actualTokens.isEmpty()) return 1f
        if (expectedTokens.isEmpty() || actualTokens.isEmpty()) return 0f

        val matchedExpectedIndices = mutableSetOf<Int>()
        val matchedActualIndices = mutableSetOf<Int>()

        var totalScore = 0.0

        // Ищем идеальные совпадения
        for ((actIdx, act) in actualTokens.withIndex()) {
            for ((expIdx, exp) in expectedTokens.withIndex()) {
                if (expIdx in matchedExpectedIndices) continue

                if (exp.stem == act.stem || exp.raw == act.raw) {
                    totalScore += 1.0
                    matchedExpectedIndices.add(expIdx)
                    matchedActualIndices.add(actIdx)
                    break
                }
            }
        }

        // Ищем частичные совпадения
        val partialMatches = mutableListOf<Triple<Int, Int, Double>>() // expIdx, actIdx, score

        for ((actIdx, act) in actualTokens.withIndex()) {
            if (actIdx in matchedActualIndices) continue

            for ((expIdx, exp) in expectedTokens.withIndex()) {
                if (expIdx in matchedExpectedIndices) continue

                val score = getTokenSimilarity(exp, act)
                if (score > 0.0) {
                    partialMatches.add(Triple(expIdx, actIdx, score))
                }
            }
        }

        partialMatches.sortByDescending { it.third }

        for (match in partialMatches) {
            val (expIdx, actIdx, score) = match
            if (expIdx !in matchedExpectedIndices && actIdx !in matchedActualIndices) {
                totalScore += score
                matchedExpectedIndices.add(expIdx)
                matchedActualIndices.add(actIdx)
            }
        }

        val dice = (2.0 * totalScore) / (expectedTokens.size + actualTokens.size).toDouble()
        return dice.toFloat()
    }

    // Вычисляет балл похожести двух токенов от 0.0 до 1.0
    private fun getTokenSimilarity(exp: Token, act: Token): Double {
        // Оценка по стемам (stem)
        val maxDistStem = maxDistance(minOf(exp.stem.length, act.stem.length))
        val distStem = damerauLevenshtein(exp.stem, act.stem)
        val scoreStem = when {
            distStem <= maxDistStem -> 1.0
            distStem == maxDistStem + 1 -> 0.5
            else -> 0.0
        }

        // Оценка по целым словам (raw)
        val maxDistRaw = maxDistance(minOf(exp.raw.length, act.raw.length))
        val distRaw = damerauLevenshtein(exp.raw, act.raw)
        val scoreRaw = when {
            distRaw <= maxDistRaw -> 1.0
            distRaw == maxDistRaw + 1 -> 0.5
            else -> 0.0
        }

        // Возвращаем лучший результат (вдруг стеммер отрезал лишнего из-за опечатки)
        return maxOf(scoreStem, scoreRaw)
    }

    // Максимально допустимое число опечаток в слове в зависимости от длины
    private fun maxDistance(length: Int): Int {
        return when {
            length <= 3 -> 0
            length <= 7 -> 1
            length <= 12 -> 2
            else -> 3
        }
    }

    // Расстояние Дамерау-Левенштейна
    private fun damerauLevenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length

        if (m == 0) return n
        if (n == 0) return m

        val maxDistance = m + n // Максимальное локальное расстояние между сравниваемыми словами
        val lastRowByChar = mutableMapOf<Char, Int>()
        val dp = Array(m + 2) { IntArray(n + 2) }

        dp[0][0] = maxDistance

        for (i in 0..m) {
            dp[i + 1][0] = maxDistance
            dp[i + 1][1] = i
        }

        for (j in 0..n) {
            dp[0][j + 1] = maxDistance
            dp[1][j + 1] = j
        }

        for (i in 1..m) {
            var lastMatchingColumn = 0

            for (j in 1..n) {
                val lastMatchingRow = lastRowByChar[b[j - 1]] ?: 0
                val lastSwapColumn = lastMatchingColumn

                val cost = if (a[i - 1] == b[j - 1]) {
                    lastMatchingColumn = j
                    0
                } else {
                    1
                }

                dp[i + 1][j + 1] = minOf(
                    dp[i][j] + cost, // замена
                    dp[i + 1][j] + 1, // вставка
                    dp[i][j + 1] + 1, // удаление
                    dp[lastMatchingRow][lastSwapColumn] + (i - lastMatchingRow - 1) + 1 +
                        (j - lastSwapColumn - 1) // перестановка соседних символов
                )
            }

            lastRowByChar[a[i - 1]] = i
        }

        return dp[m + 1][n + 1]
    }

    // Токенизация: нормализация, удаление стоп-слов, стемминг
    private fun tokenize(text: String): List<Token> {
        val normalized = normalize(text)
            .split(" ")
            .filter { it.isNotBlank() }
            .filter { !RussianStopWords.isStopWord(it) }

        return normalized.map { raw -> Token(stem = stemmer.stem(raw), raw = raw) }
    }

    // Базовые преобразования над текстом
    private fun normalize(text: String): String {
        return text
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /* ----------------------------------------- */

    // Legacy код для тестирования алгоритмов и вывода Boolean значения уверенности
    private val threshold: Double = 0.9
    fun compare(expected: String, actual: String): Boolean {
        return score(expected, actual) >= threshold
    }
}