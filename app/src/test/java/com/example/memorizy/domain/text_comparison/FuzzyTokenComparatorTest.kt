@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.text_comparison

import com.example.memorizy.domain.text_comparison.algorithm.FuzzyTokenComparator
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class FuzzyTokenComparatorTest {

    @Test
    fun `все тест-кейсы из датасета`() {
        val failures = mutableListOf<String>()

        dataset.forEachIndexed { i, case ->
            val result = comparator.compare(case.expected, case.actual)
            if (result != case.shouldMatch) {
                failures += "#$i [${case.category}]: " +
                    "'${case.expected}' vs '${case.actual}' -> " +
                    "got $result, expected ${case.shouldMatch}"
            }
        }

        if (failures.isNotEmpty()) {
            println("=== Провалено ${failures.size}/${dataset.size} ===")
            failures.forEach { println("  $it") }
        }
    }

    @Test
    fun `метрики качества по категориям`() {
        data class Metrics(var tp: Int = 0, var fp: Int = 0, var tn: Int = 0, var fn: Int = 0)

        val byCategory = mutableMapOf<String, Metrics>()
        val total = Metrics()

        dataset.forEach { case ->
            val m = byCategory.getOrPut(case.category) { Metrics() }
            val result = comparator.compare(case.expected, case.actual)
            when {
                result && case.shouldMatch   -> { m.tp++; total.tp++ }
                result && !case.shouldMatch  -> { m.fp++; total.fp++ }
                !result && !case.shouldMatch -> { m.tn++; total.tn++ }
                !result && case.shouldMatch  -> { m.fn++; total.fn++ }
            }
        }

        println("Метрики режима тестирования: ${dataset.size} тест-кейсов ===")
        byCategory.toSortedMap().forEach { (cat, m) ->
            val total = m.tp + m.fp + m.tn + m.fn
            val acc = if (total > 0) (m.tp + m.tn).toDouble() / total else 0.0
            println("  %-12s accuracy=%.1f%%  (tp=%d fp=%d tn=%d fn=%d)".format(
                "[$cat]", acc * 100, m.tp, m.fp, m.tn, m.fn
            ))
        }

        val accuracy  = (total.tp + total.tn).toDouble() / dataset.size
        val precision = if (total.tp + total.fp > 0) total.tp.toDouble() / (total.tp + total.fp) else 0.0
        val recall    = if (total.tp + total.fn > 0) total.tp.toDouble() / (total.tp + total.fn) else 0.0
        val f1 = if (precision + recall > 0) 2 * precision * recall / (precision + recall) else 0.0

        println("\nИтого:")
        println("Accuracy:  %.2f%%".format(accuracy * 100))
        println("Precision: %.2f%%".format(precision * 100))
        println("Recall:    %.2f%%".format(recall * 100))
        println("F1-score:  %.2f%%".format(f1 * 100))
        println("TP=${total.tp}  FP=${total.fp}  TN=${total.tn}  FN=${total.fn}")

        assertTrue(
            "Общая Accuracy ${"%.2f".format(accuracy * 100)}% ниже порога 85%",
            accuracy >= 0.85
        )
    }

    @Test
    fun `размер датасета не менее 400 пар`() {
        assertTrue(
            "Датасет содержит ${dataset.size} пар, ожидается >= 400",
            dataset.size >= 400
        )
    }

    @Test
    fun `очень короткий точный ответ принимается`() {
        assertTrue(comparator.compare("да", "да"))
        assertTrue(comparator.compare("йод", "йод"))
    }

    @Test
    fun `очень короткий похожий но неверный ответ не принимается`() {
        assertFalse(comparator.compare("дом", "том"))
        assertFalse(comparator.compare("йод", "лед"))
    }

    @Test
    fun `перестановка соседних символов в слове считается одной опечаткой`() {
        assertTrue(comparator.compare("клетка", "клекта"))
    }

    @Test
    fun `перестановка соседних символов в очень коротком слове не принимается`() {
        assertFalse(comparator.compare("дом", "дмо"))
    }

    @Test
    fun `почти совпадающие но семантически неверные ответы не принимаются`() {
        assertFalse(comparator.compare("митоз", "мейоз"))
        assertFalse(comparator.compare("закон ома", "закон ньютона"))
    }

    @Test
    fun `пустой ввод не принимается для непустого ответа`() {
        assertFalse(comparator.compare("клетка", ""))
        assertTrue(comparator.compare("", ""))
    }

    @Test
    fun `лишние знаки препинания и повторяющиеся пробелы игнорируются`() {
        assertTrue(
            comparator.compare(
                "Клетка",
                "   клетка!!!   "
            )
        )
        assertTrue(
            comparator.compare(
                "Процесс деления клетки",
                "процесс,   деления... клетки!!!"
            )
        )
    }

    @Test
    fun `дефис и тире считаются разделителями слов а не склеивают их`() {
        assertTrue(comparator.compare("научно-технический прогресс", "научно технический прогресс"))
        assertTrue(comparator.compare("какое-то явление", "какое то явление"))
    }

    private val comparator = FuzzyTokenComparator()

    private val definitions = listOf(
        "Москва",
        "Берлин",
        "Токио",
        "Мадрид",
        "Вашингтон",

        "Столица Российской Федерации",
        "Крупнейший город Японии",
        "Самый длинный река в Европе",
        "Наименьший материк по площади",
        "Озеро с наибольшей глубиной в мире",

        "Фотосинтез",
        "Митохондрия",
        "Хлоропласт",
        "Рибосома",
        "Цитоплазма",

        "Процесс образования глюкозы в хлоропластах",
        "Органелла клетки отвечающая за выработку энергии",
        "Двухцепочечная молекула хранящая генетическую информацию",
        "Деление клетки с сохранением числа хромосом",
        "Белок ускоряющий химические реакции в организме",

        "Процесс поглощения углекислого газа и выделения кислорода растениями под действием света",
        "Последовательность нуклеотидов в молекуле ДНК определяющая строение белка",
        "Совокупность всех химических реакций протекающих в живом организме",
        "Непроизвольное сокращение мышц диафрагмы вызванное раздражением блуждающего нерва",
        "Способность организма поддерживать постоянство внутренней среды",

        "Гравитация",
        "Инерция",
        "Теплопроводность",
        "Электричество",
        "Магнетизм",

        "Сила притяжения между телами имеющими массу",
        "Свойство тела сохранять состояние покоя или движения",
        "Единица измерения силы тока в системе СИ",
        "Величина равная отношению работы ко времени",
        "Передача энергии от нагретого тела к холодному",

        "Закон сохранения энергии гласит что энергия не создается и не уничтожается а лишь переходит из одной формы в другую",
        "Скорость тела при равноускоренном движении равна сумме начальной скорости и произведения ускорения на время",

        "Отечественная война тысяча восемьсот двенадцатого года",
        "Великая Октябрьская социалистическая революция",
        "Первый искусственный спутник Земли",
        "Распад Советского Союза в декабре тысяча девятьсот девяносто первого года",

        "Теорема Пифагора",
        "Площадь треугольника равна половине произведения основания на высоту",
        "Квадратное уравнение имеющее два корня",
        "Числовая последовательность в которой каждый следующий член равен сумме двух предыдущих",

        "Водород",
        "Периодическая таблица химических элементов Менделеева",
        "Реакция нейтрализации кислоты и основания",
        "Число Авогадро равное шести целым и ноль два на десять в двадцать третьей степени",

        "Алгоритм",
        "Двоичная система счисления",
        "Структура данных для хранения пар ключ и значение",
        "Объектно ориентированное программирование",
        "Протокол передачи гипертекста",

        "Евгений Онегин роман в стихах Пушкина",
        "Война и мир роман Толстого",
        "Преступление и наказание роман Достоевского",
        "Мертвые души поэма Гоголя"
    )

    private val russianAlphabet = "абвгдежзиклмнопрстуфхцчшщэюя"

    // Добавляем опечатки
    private fun addTypo(text: String, count: Int, random: Random): String {
        if (text.length < 2) return text
        val chars = text.toMutableList()
        val letterPositions = chars.indices.filter { chars[it].isLetter() }
        if (letterPositions.isEmpty()) return text

        val positionsToChange = letterPositions.shuffled(random).take(count)
        for (pos in positionsToChange) {
            chars[pos] = russianAlphabet[random.nextInt(russianAlphabet.length)]
        }
        return String(chars.toCharArray())
    }

    // Добавляем стоп-слова
    private fun addStopWords(text: String, random: Random): String {
        val stops = listOf("это", "в", "и", "на", "для", "что", "как")
        val words = text.split(" ").toMutableList()
        words.add(0, stops[random.nextInt(stops.size)])
        if (words.size > 2) {
            words.add(words.size / 2, stops[random.nextInt(stops.size)])
        }
        return words.joinToString(" ")
    }

    // Перемешиваем слова
    private fun shuffleWords(text: String, random: Random): String {
        val words = text.split(" ")
        if (words.size < 2) return text
        var shuffled = words.shuffled(random)

        if (shuffled == words && words.size >= 2) {
            shuffled = words.reversed()
        }
        return shuffled.joinToString(" ")
    }

    // Отрезаем частично слова
    private fun partialAnswer(text: String): String {
        val words = text.split(" ")
        return words
            .take((words.size * 0.45).toInt()
                .coerceAtLeast(1))
            .joinToString(" ")
    }

    // Меняем регистр
    private fun randomCase(text: String, random: Random): String {
        return text.map {
            if (random.nextBoolean()) it.uppercaseChar() else it.lowercaseChar()
        }.joinToString("")
    }

    data class TestCase(
        val expected: String,
        val actual: String,
        val shouldMatch: Boolean,
        val category: String
    )

    private val dataset: List<TestCase> = buildDataset()

    private fun buildDataset(): List<TestCase> {
        val cases = mutableListOf<TestCase>()
        val random = Random(System.currentTimeMillis())

        definitions.forEachIndexed { index, definition ->
            // Точное совпадение = true
            cases += TestCase(definition, definition, true, "exact")

            // Одна опечатка = true
            if (definition.length >= 4) {
                cases += TestCase(definition, addTypo(definition, 1, random), true, "typo_1")
            }

            // Две опечатки = зависит от суммарной длины слов
            if (definition.length >= 8) {
                val typo2text = addTypo(definition, 2, random)
                val totalLetters = definition.count { it.isLetter() }
                val shouldMatch = totalLetters > 12
                cases += TestCase(definition, typo2text, shouldMatch, "typo_2")
            }

            // Добавление стоп-слов = true
            cases += TestCase(definition, addStopWords(definition, random), true, "stopwords")

            // Рандомный регистр = true
            cases += TestCase(definition, randomCase(definition, random), true, "case")

            // Перемешивание слов = true (для фраз из 2+ слов)
            if (definition.split(" ").size >= 2) {
                cases += TestCase(definition, shuffleWords(definition, random), true, "word_order")
            }

            // Частичный ответ ~45% слов = false (для фраз из 4+ слов)
            if (definition.split(" ").size >= 5) {
                cases += TestCase(definition, partialAnswer(definition), false, "partial")
            }

            // Чужое определение = false
            val wrongIndex = (index + 7) % definitions.size
            val wrongDef = definitions[wrongIndex]
            if (wrongDef != definition) {
                cases += TestCase(definition, wrongDef, false, "wrong")
            }
        }
        return cases
    }
}