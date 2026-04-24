package com.example.memorizy.data.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.memorizy.domain.textcomparison.semantic.CosineSimilarity
import com.example.memorizy.domain.textcomparison.semantic.TextEmbeddingModel
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.LongBuffer

@Singleton
class OnnxTextEmbeddingModel @Inject constructor(
    private val modelAssetProvider: ModelAssetProvider,
    private val tokenizer: BertWordPieceTokenizer
) : TextEmbeddingModel {

    // Окружение ONNX Runtime
    private val environment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val session: OrtSession by lazy {
        // ONNX Runtime удобнее открывать модель как обычный файл. Поэтому модель
        // сначала копируется из assets во внутреннюю папку приложения.
        val modelFile = modelAssetProvider.materializeAsset(MODEL_ASSET_PATH)
        environment.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
    }

    // Инференс модели запускается на IO-диспетчере, чтобы первый прогрев модели
    // и последующие расчеты embedding-а не блокировали UI-поток.
    override suspend fun embed(text: String): FloatArray = withContext(Dispatchers.IO) {
        embedInternal(text)
    }

    private fun embedInternal(text: String): FloatArray {
        // На вход ONNX-модель получает не текст, а три числовых массива формата BERT.
        val tokenizedText = tokenizer.encode(text, MAX_SEQUENCE_LENGTH)

        val inputs = buildMap<String, OnnxTensor> {
            putTensor(INPUT_IDS, tokenizedText.inputIds)
            putTensor(ATTENTION_MASK, tokenizedText.attentionMask)
            if (session.inputNames.contains(TOKEN_TYPE_IDS)) {
                putTensor(TOKEN_TYPE_IDS, tokenizedText.tokenTypeIds)
            }
        }

        return inputs.useTensors { tensorInputs ->
            session.run(tensorInputs).use { result ->
                val output = selectEmbeddingOutput(result)
                // У rubert-tiny-lite выход last_hidden_state содержит вектор каждого
                // токена. Для сравнения ответов нужен один вектор всего текста.
                val embedding = outputToEmbedding(output, tokenizedText.attentionMask)
                CosineSimilarity.normalize(embedding)
            }
        }
    }

    private fun MutableMap<String, OnnxTensor>.putTensor(name: String, values: LongArray) {
        // Форма [1, 512]: один текст в batch (за раз) и максимум 512 токенов
        put(
            name,
            OnnxTensor.createTensor(    // Тензор - многомерный массив чисел
                environment,
                LongBuffer.wrap(values),
                longArrayOf(1L, MAX_SEQUENCE_LENGTH.toLong())
            )
        )
    }

    private fun <T> Map<String, OnnxTensor>.useTensors(block: (Map<String, OnnxTensor>) -> T): T {
        try {
            return block(this)
        } finally {
            values.forEach { it.close() }   // Закрываем каждый тензор, чтобы не было утечек памяти
        }
    }

    private fun selectEmbeddingOutput(result: OrtSession.Result): OnnxValue {
        // Разные экспортированные модели могут называть выход по-разному. Для этой
        // модели фактически используется last_hidden_state, но список делает код устойчивее.
        for (name in PREFERRED_OUTPUT_NAMES) {
            val output = result.get(name)
            if (output.isPresent) return output.get()
        }

        return result.get(0)
    }

    private fun outputToEmbedding(output: OnnxValue, attentionMask: LongArray): FloatArray {
        val value = output.value

        @Suppress("UNCHECKED_CAST")
        return when (value) {
            is Array<*> -> {
                val first = value.firstOrNull()
                when (first) {
                    is FloatArray -> first
                    // Если модель вернула векторы всех токенов, усредняем только
                    // реальные токены. Padding отбрасывается через attention_mask.
                    is Array<*> -> meanPool(first as Array<FloatArray>, attentionMask)
                    else -> error("Unsupported ONNX output array type: ${first?.javaClass?.name}")
                }
            }
            is FloatArray -> value
            else -> error("Unsupported ONNX output type: ${value?.javaClass?.name}")
        }
    }

    private fun meanPool(tokenEmbeddings: Array<FloatArray>, attentionMask: LongArray): FloatArray {
        // Mean pooling превращает набор token embeddings в один sentence embedding:
        // суммируем векторы всех настоящих токенов и делим на их количество.
        val hiddenSize = tokenEmbeddings.firstOrNull()?.size ?: 0
        val pooled = FloatArray(hiddenSize)
        var tokenCount = 0

        for (index in tokenEmbeddings.indices) {
            if (attentionMask.getOrElse(index) { 0L } == 0L) continue

            val tokenEmbedding = tokenEmbeddings[index]
            for (hiddenIndex in 0 until hiddenSize) {
                pooled[hiddenIndex] += tokenEmbedding[hiddenIndex]
            }
            tokenCount++
        }

        if (tokenCount == 0) return pooled

        for (index in pooled.indices) {
            pooled[index] /= tokenCount.toFloat()
        }

        return pooled
    }

    companion object {
        private const val MODEL_ASSET_PATH = "ml/rubert_tiny_lite/model.onnx"
        private const val MAX_SEQUENCE_LENGTH = 512

        private const val INPUT_IDS = "input_ids"
        private const val ATTENTION_MASK = "attention_mask"
        private const val TOKEN_TYPE_IDS = "token_type_ids"

        private val PREFERRED_OUTPUT_NAMES = listOf(
            "sentence_embedding",
            "embeddings",
            "pooler_output",
            "last_hidden_state"
        )
    }
}