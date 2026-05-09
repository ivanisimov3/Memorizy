package com.example.memorizy.data.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.memorizy.domain.text_comparison.nli.EntailmentComparisonResult
import com.example.memorizy.domain.text_comparison.nli.EntailmentTextClassifier
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.LongBuffer
import kotlin.math.exp

@Singleton
class OnnxEntailmentTextClassifier @Inject constructor(
    private val modelAssetProvider: ModelAssetProvider,
    private val tokenizer: BertNliTokenizer
) : EntailmentTextClassifier {

    /*
    To start a scoring session, first create the OrtEnvironment,
    then open a session using the OrtSession class,
    passing in the file path to the model as a parameter.
    */
    private val environment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val session: OrtSession by lazy {
        val modelFile = modelAssetProvider.materializeAsset(MODEL_ASSET_PATH)
        environment.createSession(
            modelFile.absolutePath,
            OrtSession.SessionOptions()
        )
    }

    override suspend fun classify(
        premise: String, hypothesis: String
    ): EntailmentComparisonResult = withContext(Dispatchers.IO) {
        classifyInternal(premise, hypothesis)
    }

    private fun classifyInternal(premise: String, hypothesis: String): EntailmentComparisonResult {
        // NLI проверяет, следует ли гипотеза из посылки.
        // В проверке ответа посылка - это ответ пользователя, а гипотеза - эталонный ответ.
        val tokenizedText = tokenizer.encode(
            premise = premise,
            hypothesis = hypothesis,
            maxLength = MAX_SEQUENCE_LENGTH
        )

        // Формируем входные данные
        val inputs = buildMap<String, OnnxTensor> {
            putTensor(INPUT_IDS, tokenizedText.inputIds)
            putTensor(ATTENTION_MASK, tokenizedText.attentionMask)
            if (session.inputNames.contains(TOKEN_TYPE_IDS)) {
                putTensor(TOKEN_TYPE_IDS, tokenizedText.tokenTypeIds)
            }
        }

        // Запускаем модель и получаем сырые оценки
        val logits = inputs.useTensors { tensorInputs ->
            session.run(tensorInputs).use { result ->
                outputToLogits(selectLogitsOutput(result))
            }
        }

        // Модель возвращает сырые оценки классов: [entailment, contradiction, neutral].
        // Softmax переводит их в сопоставимые вероятности в диапазоне 0..1.
        val probabilities = softmax(logits)
        val entailment = probabilities.getOrElse(ENTAILMENT_INDEX) { 0f }
        val contradiction = probabilities.getOrElse(CONTRADICTION_INDEX) { 0f }
        val neutral = probabilities.getOrElse(NEUTRAL_INDEX) { 0f }
        val notEntailment = maxOf(contradiction, neutral)

        return EntailmentComparisonResult(
            isEntailed = entailment >= ENTAILMENT_THRESHOLD &&
                entailment > contradiction &&
                entailment > neutral,
            entailment = entailment,
            contradiction = contradiction,
            neutral = neutral,
            notEntailment = notEntailment,
            threshold = ENTAILMENT_THRESHOLD
        )
    }

    private fun MutableMap<String, OnnxTensor>.putTensor(name: String, values: LongArray) {
        put(
            name,
            OnnxTensor.createTensor(
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
            values.forEach { it.close() }
        }
    }

    // Какой выход модели взять
    private fun selectLogitsOutput(result: OrtSession.Result): OnnxValue {
        for (name in PREFERRED_OUTPUT_NAMES) {
            val output = result.get(name)
            if (output.isPresent) return output.get()
        }

        return result.get(0)
    }

    // Как превратить выход в FloatArray
    private fun outputToLogits(output: OnnxValue): FloatArray {
        val value = output.value

        @Suppress("UNCHECKED_CAST")
        return when (value) {
            is Array<*> -> {
                val first = value.firstOrNull()
                when (first) {
                    is FloatArray -> first
                    is Array<*> -> (first as Array<FloatArray>).first()
                    else -> error("Unsupported ONNX logits array type: ${first?.javaClass?.name}")
                }
            }
            is FloatArray -> value
            else -> error("Unsupported ONNX logits output type: ${value?.javaClass?.name}")
        }
    }

    // Из сырых в нормальные коэффициенты
    // https://docs.pytorch.org/docs/stable/generated/torch.nn.Softmax.html
    private fun softmax(logits: FloatArray): FloatArray {
        if (logits.isEmpty()) return logits

        val maxLogit = logits.max()
        val exps = FloatArray(logits.size)
        var sum = 0.0

        for (index in logits.indices) {
            val value = exp((logits[index] - maxLogit).toDouble())
            exps[index] = value.toFloat()
            sum += value
        }

        if (sum == 0.0) return FloatArray(logits.size)

        return FloatArray(logits.size) { index ->
            (exps[index] / sum).toFloat()
        }
    }

    companion object {
        private const val MODEL_ASSET_PATH = "ml/rubert_base_cased_nli_threeway/onnx/model_uint8.onnx"
        private const val MAX_SEQUENCE_LENGTH = 512

        private const val INPUT_IDS = "input_ids"
        private const val ATTENTION_MASK = "attention_mask"
        private const val TOKEN_TYPE_IDS = "token_type_ids"

        private const val ENTAILMENT_INDEX = 0
        private const val CONTRADICTION_INDEX = 1
        private const val NEUTRAL_INDEX = 2
        private const val ENTAILMENT_THRESHOLD = 0.50f

        private val PREFERRED_OUTPUT_NAMES = listOf("logits", "output", "output_0")
    }
}