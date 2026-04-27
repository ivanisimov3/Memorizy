package com.example.memorizy.domain.textcomparison.nli

/*
Результат трехклассовой NLI-модели: entailment означает, что эталонный ответ
следует из ответа пользователя; contradiction - что ответы противоречат друг другу;
neutral - что из ответа пользователя недостаточно вывести эталон.
*/
data class EntailmentComparisonResult(
    val isEntailed: Boolean,
    val entailment: Float,
    val contradiction: Float,
    val neutral: Float,
    val notEntailment: Float,
    val threshold: Float
)