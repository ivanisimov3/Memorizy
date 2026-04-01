package com.example.memorizy.domain.textcomparison

// Реализация сравнения текстов

class FuzzyTokenComparator(
    private val threshold: Double = 0.75
) : TextComparator {

    private val stemmer = RussianStemmer()

    private data class Token(
        val stem: String,
        val raw: String
    )

    override fun compare(expected: String, actual: String): Boolean {
        val expectedTokens = tokenize(expected)
        val actualTokens = tokenize(actual)

        if (expectedTokens.isEmpty() && actualTokens.isEmpty()) return true
        if (expectedTokens.isEmpty() || actualTokens.isEmpty()) return false

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
        return dice >= threshold
    }

    // Вычисляет балл похожести двух токенов от 0.0 до 1.0
    private fun getTokenSimilarity(exp: Token, act: Token): Double {
        val maxDistStem = maxDistance(minOf(exp.stem.length, act.stem.length))
        val distStem = levenshtein(exp.stem, act.stem)
        val scoreStem = when {
            distStem <= maxDistStem -> 1.0
            distStem == maxDistStem + 1 -> 0.5
            else -> 0.0
        }

        // Оценка по целым словам (raw)
        val maxDistRaw = maxDistance(minOf(exp.raw.length, act.raw.length))
        val distRaw = levenshtein(exp.raw, act.raw)
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
            else -> 2
        }
    }

    // Расстояние Левенштейна
    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length

        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,   // удаление
                    dp[i][j - 1] + 1,   // вставка
                    dp[i - 1][j - 1] + cost // замена
                )
            }
        }

        return dp[m][n]
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
            .replace(Regex("[^a-zа-яa-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}