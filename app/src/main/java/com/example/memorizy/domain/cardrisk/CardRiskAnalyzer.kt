package com.example.memorizy.domain.cardrisk

object CardRiskAnalyzer {

    enum class CardRisk {
        HIGH,
        MEDIUM,
        LOW
    }

    fun calculateRisk(level: Int): CardRisk {
        return when (level) {
            in 0..2 -> CardRisk.HIGH
            in 3..4 -> CardRisk.MEDIUM
            else -> CardRisk.LOW
        }
    }
}