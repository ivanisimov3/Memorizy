package com.example.memorizy.domain.textcomparison

/**
 * Стеммер Портера для русского языка.
 *
 * Реализация строго по спецификации Snowball:
 * https://snowballstem.org/algorithms/russian/stemmer.html
 *
 * Алгоритм:
 * 1. Находим регионы RV, R1, R2 (один раз, на исходном слове)
 * 2. Шаг 1: Удаление окончания (PERFECTIVE GERUND / REFLEXIVE + ADJECTIVAL / VERB / NOUN)
 * 3. Шаг 2: Удаление «и» на конце
 * 4. Шаг 3: Удаление деривационного суффикса в R2
 * 5. Шаг 4: Undouble «н», удаление SUPERLATIVE, удаление «ь»
 *
 * Все проверки суффиксов выполняются в зоне RV (маркеры фиксируются один раз).
 */
class RussianStemmer {

    companion object {
        private val VOWELS = setOf('а', 'е', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я')

        // ── PERFECTIVE GERUND ──────────────────────────────────────────────
        // group 1: must be preceded by 'а' or 'я' (which must also be in RV)
        private val PERFECTIVE_GERUND_GROUP1 = listOf("вшись", "вши", "в")
        // group 2: no precondition
        private val PERFECTIVE_GERUND_GROUP2 = listOf("ившись", "ывшись", "ивши", "ывши", "ив", "ыв")

        // ── ADJECTIVE ──────────────────────────────────────────────────────
        private val ADJECTIVE = listOf(
            "ими", "ыми", "его", "ого", "ему", "ому",
            "ее", "ие", "ые", "ое",
            "ей", "ий", "ый", "ой",
            "ем", "им", "ым", "ом",
            "их", "ых",
            "ую", "юю",
            "ая", "яя",
            "ою", "ею"
        )

        // ── PARTICIPLE ─────────────────────────────────────────────────────
        // group 1: must be preceded by 'а' or 'я' (which must also be in RV)
        private val PARTICIPLE_GROUP1 = listOf("ющ", "вш", "нн", "ем", "щ")
        // group 2: no precondition
        private val PARTICIPLE_GROUP2 = listOf("ующ", "ивш", "ывш")

        // ── REFLEXIVE ──────────────────────────────────────────────────────
        private val REFLEXIVE = listOf("ся", "сь")

        // ── VERB ───────────────────────────────────────────────────────────
        // group 1: must be preceded by 'а' or 'я' (which must also be in RV)
        private val VERB_GROUP1 = listOf(
            "ете", "йте", "нно",
            "ешь", "ла", "на", "ли",
            "ло", "но", "ем", "ны",
            "ть", "ют", "ет",
            "й", "л", "н"
        )
        // group 2: no precondition
        private val VERB_GROUP2 = listOf(
            "ейте", "уйте",
            "ила", "ыла", "ена", "ите", "или", "ыли",
            "ило", "ыло", "ено", "уют", "ует",
            "ены", "ить", "ыть", "ишь",
            "ей", "уй",
            "ил", "ыл", "им", "ым", "ен",
            "ят", "ит", "ыт",
            "ую", "ю"
        )

        // ── NOUN ───────────────────────────────────────────────────────────
        private val NOUN = listOf(
            "иями", "ями", "ами",
            "ией",
            "иям", "ием",
            "иях",
            "ев", "ов", "ие", "ье",
            "еи", "ии", "ий", "ей", "ой",
            "ам", "ом", "ем",
            "ах", "ях",
            "ию", "ью",
            "ия", "ья",
            "а", "е", "и", "й", "о", "у", "ы", "ь", "ю", "я"
        )

        // ── SUPERLATIVE ────────────────────────────────────────────────────
        private val SUPERLATIVE = listOf("ейше", "ейш")

        // ── DERIVATIONAL ───────────────────────────────────────────────────
        private val DERIVATIONAL = listOf("ость", "ост")
    }

    /**
     * Возвращает стем (основу) слова.
     */
    fun stem(word: String): String {
        var w = word.lowercase().replace('ё', 'е')

        if (w.length < 2) return w

        // Фиксируем маркеры один раз на исходном слове
        val rv = findRV(w)
        val r2 = findR2(w)

        if (rv >= w.length) return w

        // Шаг 1
        w = step1(w, rv)

        // Шаг 2: Удаление «и» на конце (в RV)
        if (w.endsWith("и") && w.length - 1 >= rv) {
            w = w.dropLast(1)
        }

        // Шаг 3: Удаление деривационного суффикса в R2
        w = step3(w, r2)

        // Шаг 4: Undouble н, SUPERLATIVE, ь
        w = step4(w, rv)

        return w
    }

    // ── Шаг 1 ────────────────────────────────────────────────────────────────

    private fun step1(word: String, rv: Int): String {
        // Попытка 1: PERFECTIVE GERUND
        removePerfectiveGerund(word, rv)?.let { return it }

        // Иначе: попробовать REFLEXIVE
        var w = removeReflexive(word, rv) ?: word

        // Затем попробовать по очереди: ADJECTIVAL, VERB, NOUN
        removeAdjectival(w, rv)?.let { return it }
        removeVerb(w, rv)?.let { return it }
        removeNoun(w, rv)?.let { return it }

        return w
    }

    // ── Шаг 3 ────────────────────────────────────────────────────────────────

    private fun step3(word: String, r2: Int): String {
        // Удаление DERIVATIONAL окончания, если оно целиком лежит в R2
        for (suffix in DERIVATIONAL) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= r2) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        return word
    }

    // ── Шаг 4 ────────────────────────────────────────────────────────────────

    private fun step4(word: String, rv: Int): String {
        var w = word

        // (2) Если заканчивается на SUPERLATIVE — удалить и undouble н
        for (suffix in SUPERLATIVE) {
            if (w.endsWith(suffix)) {
                val pos = w.length - suffix.length
                if (pos >= rv) {
                    w = w.dropLast(suffix.length)
                    w = undoubleN(w, rv)
                    return w
                }
            }
        }

        // (1) Undouble н
        if (w.endsWith("нн")) {
            w = undoubleN(w, rv)
            return w
        }

        // (3) Если заканчивается на ь — удалить
        if (w.endsWith("ь")) {
            val pos = w.length - 1
            if (pos >= rv) {
                w = w.dropLast(1)
            }
        }

        return w
    }

    private fun undoubleN(word: String, rv: Int): String {
        if (word.endsWith("нн")) {
            val pos = word.length - 1  // позиция последней 'н'
            if (pos >= rv) {
                return word.dropLast(1)
            }
        }
        return word
    }

    // ── Удаление окончаний ───────────────────────────────────────────────────

    private fun removePerfectiveGerund(word: String, rv: Int): String? {
        // Group 2 (no precondition) — проверяем сначала (более длинные суффиксы)
        for (suffix in PERFECTIVE_GERUND_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        // Group 1 (must be preceded by 'а' or 'я', which must also be in RV)
        for (suffix in PERFECTIVE_GERUND_GROUP1) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos > rv && pos > 0) {
                    val preceding = word[pos - 1]
                    if (preceding == 'а' || preceding == 'я') {
                        return word.dropLast(suffix.length)
                    }
                }
            }
        }
        return null
    }

    private fun removeAdjectival(word: String, rv: Int): String? {
        // ADJECTIVAL = ADJECTIVE + optional PARTICIPLE перед ним
        // Сначала ищем ADJECTIVE
        for (adjSuffix in ADJECTIVE) {
            if (word.endsWith(adjSuffix)) {
                val adjPos = word.length - adjSuffix.length
                if (adjPos >= rv) {
                    // Нашли ADJECTIVE, теперь пробуем PARTICIPLE перед ним
                    val wordWithoutAdj = word.dropLast(adjSuffix.length)
                    val result = removeParticiple(wordWithoutAdj, rv) ?: wordWithoutAdj
                    return result
                }
            }
        }
        return null
    }

    private fun removeParticiple(word: String, rv: Int): String? {
        // Group 2 (no precondition) — сначала длинные
        for (suffix in PARTICIPLE_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        // Group 1 (must be preceded by 'а' or 'я', which must also be in RV)
        for (suffix in PARTICIPLE_GROUP1) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos > rv && pos > 0) {
                    val preceding = word[pos - 1]
                    if (preceding == 'а' || preceding == 'я') {
                        return word.dropLast(suffix.length)
                    }
                }
            }
        }
        return null
    }

    private fun removeReflexive(word: String, rv: Int): String? {
        for (suffix in REFLEXIVE) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        return null
    }

    private fun removeVerb(word: String, rv: Int): String? {
        // Group 2 (no precondition) — сначала длинные
        for (suffix in VERB_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        // Group 1 (must be preceded by 'а' or 'я', which must also be in RV)
        for (suffix in VERB_GROUP1) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos > rv && pos > 0) {
                    val preceding = word[pos - 1]
                    if (preceding == 'а' || preceding == 'я') {
                        return word.dropLast(suffix.length)
                    }
                }
            }
        }
        return null
    }

    private fun removeNoun(word: String, rv: Int): String? {
        for (suffix in NOUN) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
        return null
    }

    // ── Нахождение регионов ──────────────────────────────────────────────────

    /**
     * RV — регион после первой гласной.
     */
    private fun findRV(word: String): Int {
        for (i in word.indices) {
            if (word[i] in VOWELS) {
                return i + 1
            }
        }
        return word.length
    }

    /**
     * R1 — регион после первой согласной, следующей за гласной.
     */
    private fun findR1(word: String): Int {
        var seenVowel = false
        for (i in word.indices) {
            if (word[i] in VOWELS) {
                seenVowel = true
            } else if (seenVowel) {
                return i + 1
            }
        }
        return word.length
    }

    /**
     * R2 — регион после первой согласной, следующей за гласной в R1.
     */
    private fun findR2(word: String): Int {
        val r1 = findR1(word)
        if (r1 >= word.length) return word.length

        var seenVowel = false
        for (i in r1 until word.length) {
            if (word[i] in VOWELS) {
                seenVowel = true
            } else if (seenVowel) {
                return i + 1
            }
        }
        return word.length
    }
}
