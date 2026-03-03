package com.example.memorizy.domain.textcomparison

// Реализация стеммера Портера
// snowballstem.org/algorithms/russian/stemmer.html

class RussianStemmer {

    companion object {
        private val VOWELS = setOf('а', 'е', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я')

        private val PERFECTIVE_GERUND_GROUP1 = listOf("вшись", "вши", "в")
        private val PERFECTIVE_GERUND_GROUP2 = listOf("ившись", "ывшись", "ивши", "ывши", "ив", "ыв")

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

        private val PARTICIPLE_GROUP1 = listOf("ющ", "вш", "нн", "ем", "щ")
        private val PARTICIPLE_GROUP2 = listOf("ующ", "ивш", "ывш")

        private val REFLEXIVE = listOf("ся", "сь")

        private val VERB_GROUP1 = listOf(
            "ете", "йте", "нно",
            "ешь", "ла", "на", "ли",
            "ло", "но", "ем", "ны",
            "ть", "ют", "ет",
            "й", "л", "н"
        )
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

        private val SUPERLATIVE = listOf("ейше", "ейш")

        private val DERIVATIONAL = listOf("ость", "ост")
    }

    // Нахождение стема (основы) слова
    fun stem(word: String): String {
        var w = word.lowercase().replace('ё', 'е')

        if (w.length < 2) return w

        val rv = findRV(w)
        val r2 = findR2(w)

        if (rv >= w.length) return w

        w = step1(w, rv)

        // Шаг 2 алгоритма
        if (w.endsWith("и") && w.length - 1 >= rv) {
            w = w.dropLast(1)
        }

        w = step3(w, r2)

        w = step4(w, rv)

        return w
    }

    private fun step1(word: String, rv: Int): String {
        removePerfectiveGerund(word, rv)?.let { return it }

        var w = removeReflexive(word, rv) ?: word

        removeAdjectival(w, rv)?.let { return it }
        removeVerb(w, rv)?.let { return it }
        removeNoun(w, rv)?.let { return it }

        return w
    }

    private fun step3(word: String, r2: Int): String {
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

    private fun step4(word: String, rv: Int): String {
        var w = word

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

        if (w.endsWith("нн")) {
            w = undoubleN(w, rv)
            return w
        }

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
            val pos = word.length - 1
            if (pos >= rv) {
                return word.dropLast(1)
            }
        }
        return word
    }

    private fun removePerfectiveGerund(word: String, rv: Int): String? {
        for (suffix in PERFECTIVE_GERUND_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
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
        for (adjSuffix in ADJECTIVE) {
            if (word.endsWith(adjSuffix)) {
                val adjPos = word.length - adjSuffix.length
                if (adjPos >= rv) {
                    val wordWithoutAdj = word.dropLast(adjSuffix.length)
                    val result = removeParticiple(wordWithoutAdj, rv) ?: wordWithoutAdj
                    return result
                }
            }
        }
        return null
    }

    private fun removeParticiple(word: String, rv: Int): String? {
        for (suffix in PARTICIPLE_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
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
        for (suffix in VERB_GROUP2) {
            if (word.endsWith(suffix)) {
                val pos = word.length - suffix.length
                if (pos >= rv) {
                    return word.dropLast(suffix.length)
                }
            }
        }
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

    private fun findRV(word: String): Int {
        for (i in word.indices) {
            if (word[i] in VOWELS) {
                return i + 1
            }
        }
        return word.length
    }

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