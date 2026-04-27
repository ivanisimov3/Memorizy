package com.example.memorizy.data.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.json.JSONObject
import java.io.FileNotFoundException

@Singleton
class BertNliTokenizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val vocab: Map<String, Long> by lazy { loadVocab() }
    private val doLowerCase: Boolean by lazy { loadDoLowerCase() }

    fun encode(premise: String, hypothesis: String, maxLength: Int): TokenizedText {
        // BERT NLI получает оба текста в одной последовательности:
        val premisePieces = basicTokenize(premise)
            .flatMap(::wordPieceTokenize)
            .toMutableList()
        val hypothesisPieces = basicTokenize(hypothesis)
            .flatMap(::wordPieceTokenize)
            .toMutableList()

        // Лимит в 512 токенов общий для двух текстов и служебных токенов, поэтому
        // длинные пары сокращаются по принципу удаления из более длинной части.
        truncatePair(premisePieces, hypothesisPieces, maxLength - SPECIAL_TOKENS_COUNT)

        // [CLS] premise [SEP] hypothesis [SEP].
        // token_type_ids помечают посылку как сегмент 0, а гипотезу как сегмент 1.
        val tokens = mutableListOf<String>()
        val tokenTypeIdsList = mutableListOf<Long>()

        tokens.add(CLS_TOKEN)
        tokenTypeIdsList.add(0L)

        premisePieces.forEach { piece ->
            tokens.add(piece)
            tokenTypeIdsList.add(0L)
        }

        tokens.add(SEP_TOKEN)
        tokenTypeIdsList.add(0L)

        hypothesisPieces.forEach { piece ->
            tokens.add(piece)
            tokenTypeIdsList.add(1L)
        }

        tokens.add(SEP_TOKEN)
        tokenTypeIdsList.add(1L)

        val inputIds = LongArray(maxLength) { padId }   // Номера токенов из словаря
        val attentionMask = LongArray(maxLength)    // Показываем где токен, а где пусто
        val tokenTypeIds = LongArray(maxLength) // Показываем к какому тексту относится (premise или hypo)

        tokens.forEachIndexed { index, token ->
            inputIds[index] = vocab[token] ?: unkId
            attentionMask[index] = 1L
            tokenTypeIds[index] = tokenTypeIdsList[index]
        }

        return TokenizedText(
            inputIds = inputIds,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds
        )
    }

    // Подгружаем словарь
    private fun loadVocab(): Map<String, Long> {
        return context.assets.open(VOCAB_ASSET_PATH)
            .bufferedReader()
            .useLines { lines -> lines.mapIndexed { index, token -> token to index.toLong() }.toMap() }
    }

    // Делим предложения на токены (слова и знаки препинания)
    private fun basicTokenize(text: String): List<String> {
        val normalized = if (doLowerCase) {
            text.lowercase().trim()
        } else {
            text.trim()
        }

        if (normalized.isEmpty()) return emptyList()

        val tokens = mutableListOf<String>()
        val current = StringBuilder()

        for (char in normalized) {
            when {
                char.isWhitespace() -> flushCurrent(current, tokens)
                isPunctuation(char) -> {
                    flushCurrent(current, tokens)
                    tokens.add(char.toString())
                }
                else -> current.append(char)
            }
        }

        flushCurrent(current, tokens)
        return tokens
    }

    // Собираем токены (слова) из кусочков vocab.txt
    private fun wordPieceTokenize(token: String): List<String> {
        if (token.length > MAX_INPUT_CHARS_PER_WORD) return listOf(UNK_TOKEN)
        if (token in vocab) return listOf(token)

        val pieces = mutableListOf<String>()
        var start = 0

        while (start < token.length) {
            var end = token.length
            var currentPiece: String? = null

            while (start < end) {
                val substring = token.substring(start, end)
                val piece = if (start == 0) substring else "##$substring"
                if (piece in vocab) {
                    currentPiece = piece
                    break
                }
                end--
            }

            if (currentPiece == null) return listOf(UNK_TOKEN)

            pieces.add(currentPiece)
            start = end
        }

        return pieces
    }

    // Обрезаем сумму кусков эталона и ответа пользователя пока не получим 512 - 3 токена
    private fun truncatePair(
        premisePieces: MutableList<String>,
        hypothesisPieces: MutableList<String>,
        maxContentLength: Int
    ) {
        while (premisePieces.size + hypothesisPieces.size > maxContentLength) {
            if (premisePieces.size > hypothesisPieces.size) {
                premisePieces.removeAt(premisePieces.lastIndex)
            } else {
                hypothesisPieces.removeAt(hypothesisPieces.lastIndex)
            }
        }
    }

    // Убираем пробел и сохраняем токен
    private fun flushCurrent(current: StringBuilder, tokens: MutableList<String>) {
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
            current.clear()
        }
    }

    // Проверка на пунктуацию
    private fun isPunctuation(char: Char): Boolean {
        val type = Character.getType(char)
        return type == Character.CONNECTOR_PUNCTUATION.toInt() ||
            type == Character.DASH_PUNCTUATION.toInt() ||
            type == Character.START_PUNCTUATION.toInt() ||
            type == Character.END_PUNCTUATION.toInt() ||
            type == Character.INITIAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.FINAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.OTHER_PUNCTUATION.toInt()
    }

    // Читаем значение do_lower_case из tokenizer_config
    private fun loadDoLowerCase(): Boolean {
        return try {
            val json = context.assets.open(TOKENIZER_CONFIG_ASSET_PATH)
                .bufferedReader()
                .use { it.readText() }

            JSONObject(json).optBoolean("do_lower_case", false)
        } catch (_: FileNotFoundException) {
            false
        }
    }

    private val padId: Long get() = vocab[PAD_TOKEN] ?: 0L
    private val unkId: Long get() = vocab[UNK_TOKEN] ?: 1L

    companion object {
        private const val VOCAB_ASSET_PATH = "ml/rubert_base_cased_nli_threeway/onnx/vocab.txt"
        private const val TOKENIZER_CONFIG_ASSET_PATH =
            "ml/rubert_base_cased_nli_threeway/onnx/tokenizer_config.json"
        private const val MAX_INPUT_CHARS_PER_WORD = 100
        private const val SPECIAL_TOKENS_COUNT = 3

        private const val PAD_TOKEN = "[PAD]"
        private const val UNK_TOKEN = "[UNK]"
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
    }
}