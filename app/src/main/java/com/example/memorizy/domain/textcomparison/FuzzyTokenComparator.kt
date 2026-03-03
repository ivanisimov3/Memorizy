package com.example.memorizy.domain.textcomparison

// Реализация сравнения текстов

class FuzzyTokenComparator(
    private val threshold: Double = 0.75
) : TextComparator {

    private val stemmer = RussianStemmer()

    override fun compare(expected: String, actual: String): Boolean {
        val expectedTokens = tokenize(expected)
        val actualTokens = tokenize(actual)

        if (expectedTokens.isEmpty() && actualTokens.isEmpty()) return true
        if (expectedTokens.isEmpty() || actualTokens.isEmpty()) return false

        val matchedExpected = mutableSetOf<String>()
        val matchedActual = mutableSetOf<String>()

        for ((actIdx, act) in actualTokens.withIndex()) {
            for ((expIdx, exp) in expectedTokens.withIndex()) {
                val expKey = "$expIdx:$exp"
                if (expKey in matchedExpected) continue
                if (isFuzzyMatch(exp, act)) {
                    matchedExpected.add(expKey)
                    matchedActual.add("$actIdx:$act")
                    break
                }
            }
        }

        val matchCount = matchedExpected.size
        val dice = (2.0 * matchCount) / (expectedTokens.size + actualTokens.size).toDouble()
        return dice >= threshold
    }

    // Проверка на совпадение текстов
    private fun isFuzzyMatch(a: String, b: String): Boolean {
        if (a == b) return true
        val maxAllowed = maxDistance(minOf(a.length, b.length))
        return levenshtein(a, b) <= maxAllowed
    }

    // Максимально допустимое число опечаток в слове в зависимости от длины
    private fun maxDistance(length: Int): Int {
        return when {
            length <= 3 -> 0
            length <= 8 -> 1
            else -> 2
        }
    }

    // Расстояние Левенштейна
    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length

        // Если разница длин больше максимально возможного допуска, то нет смысла считать
        if (Math.abs(m - n) > maxDistance(minOf(m, n)))
            return maxDistance(minOf(m, n)) + 1

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

    // Удаление стоп слов и упрощение слов до стемов
    private fun tokenize(text: String): List<String> {
        return normalize(text)
            .split(" ")
            .filter { it.isNotBlank() }
            .filter { !RussianStopWords.isStopWord(it) }
            .map { stemmer.stem(it) }
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
