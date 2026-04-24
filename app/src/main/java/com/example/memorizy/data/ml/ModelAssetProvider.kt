package com.example.memorizy.data.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.File

@Singleton
class ModelAssetProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Переносим модель из assets во внутреннюю папку приложения
    fun materializeAsset(assetPath: String): File {
        val outputFile = File(context.filesDir, assetPath)
        if (outputFile.exists() && outputFile.length() > 0L) return outputFile

        outputFile.parentFile?.mkdirs()

        context.assets.open(assetPath).use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }
}