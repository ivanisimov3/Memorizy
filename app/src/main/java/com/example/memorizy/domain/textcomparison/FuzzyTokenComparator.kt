package com.example.memorizy.domain.textcomparison

/**
 * Компаратор с нечётким сопоставлением токенов через расстояние Левенштейна
 * и стемминг Портера для русского языка.
 *
 * 1. Нормализация (lowercase, ё→е, удаление пунктуации, схлопывание пробелов)
 * 2. Токенизация (разбиение на слова)
 * 3. Стемминг каждого токена (приведение к основе слова)
 * 4. Нечёткое сопоставление: два стема считаются совпавшими,
 *    если расстояние Левенштейна между ними ≤ допуска:
 *      - длина ≤ 3:  допуск 0
 *      - длина 4–8:  допуск 1
 *      - длина > 8:  допуск 2
 * 5. Подсчёт коэффициента Сёренсена-Дайса по нечётким совпадениям
 *
 * @param threshold порог коэффициента Сёренсена-Дайса (по умолчанию 0.75)
 */
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

    /**
     * Проверяет, совпадают ли два токена с учётом допуска по Левенштейну.
     */
    private fun isFuzzyMatch(a: String, b: String): Boolean {
        if (a == b) return true
        val maxAllowed = maxDistance(minOf(a.length, b.length))
        return levenshtein(a, b) <= maxAllowed
    }

    /**
     * Максимально допустимое расстояние в зависимости от длины слова.
     */
    private fun maxDistance(length: Int): Int {
        return when {
            length <= 3 -> 0
            length <= 8 -> 1
            else -> 2
        }
    }

    /**
     * Расстояние Левенштейна между двумя строками.
     */
    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length

        // Быстрая проверка: если разница длин больше максимально возможного допуска — нет смысла считать
        if (Math.abs(m - n) > maxDistance(minOf(m, n))) return maxDistance(minOf(m, n)) + 1

        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // удаление
                    dp[i][j - 1] + 1,      // вставка
                    dp[i - 1][j - 1] + cost // замена
                )
            }
        }

        return dp[m][n]
    }

    private fun tokenize(text: String): List<String> {
        return normalize(text)
            .split(" ")
            .filter { it.isNotBlank() }
            .filter { !RussianStopWords.isStopWord(it) }
            .map { stemmer.stem(it) }
    }

    private fun normalize(text: String): String {
        return text
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^a-zа-яa-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
