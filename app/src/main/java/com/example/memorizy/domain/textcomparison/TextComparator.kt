package com.example.memorizy.domain.textcomparison

/**
 * Стратегия сравнения двух текстов.
 *
 * Реализации определяют, считаются ли [expected] и [actual] эквивалентными.
 * Используется в мобильном приложении для проверки ответов пользователя в режиме тестирования.
 */
interface TextComparator {

    /**
     * Сравнивает эталонный текст с ответом пользователя.
     *
     * @param expected эталонное определение из системы
     * @param actual   ответ пользователя
     * @return true, если ответ считается верным
     */
    fun compare(expected: String, actual: String): Boolean
}
