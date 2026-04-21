package com.example.memorizy.domain.textcomparison

// Интерфейс для сравнения двух текстов

interface TextComparator {

    fun compare(expected: String, actual: String): Boolean
}