package com.example.memorizy.data.source.local.room

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// Файл конвертер для работы с полем definitionVariants: List<String>

class CardDefinitionVariantsConverter {

    private val json = Json

    //  В Json для Room
    @TypeConverter
    fun fromDefinitionVariants(value: List<String>): String {
        return json.encodeToString(ListSerializer(String.serializer()), value)
    }

    // В List<String>
    @TypeConverter
    fun toDefinitionVariants(value: String): List<String> {
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), value)
        }.getOrElse {
            emptyList()
        }
    }
}