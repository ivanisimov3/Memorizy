package com.example.memorizy.data.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.json.JSONObject
import java.io.FileNotFoundException

@Singleton
class BertWordPieceTokenizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // vocab.txt устроен просто: номер строки в файле является id токена для модели.
    private val vocab: Map<String, Long> by lazy { loadVocab() }
    private val doLowerCase: Boolean by lazy { loadDoLowerCase() }

    fun encode(text: String, maxLength: Int): TokenizedText {
        // BERT-подобная модель не принимает строку напрямую: текст нужно разложить
        // на токены/подслова и заменить их числовыми id из vocab.txt.
        val pieces = basicTokenize(text)
            .flatMap(::wordPieceTokenize)

        val maxContentLength = maxLength - SPECIAL_TOKENS_COUNT
        val truncatedPieces = pieces.take(maxContentLength)

        // [CLS] и [SEP] - служебные токены BERT-архитектуры: начало и конец входа.
        val tokens = buildList {
            add(CLS_TOKEN)
            addAll(truncatedPieces)
            add(SEP_TOKEN)
        }

        // input_ids - сами id токенов; attention_mask говорит модели, где реальный
        // текст, а где padding; token_type_ids нужен BERT-моделям как технический вход.
        val inputIds = LongArray(maxLength) { padId }
        val attentionMask = LongArray(maxLength)
        val tokenTypeIds = LongArray(maxLength)

        tokens.forEachIndexed { index, token ->
            inputIds[index] = vocab[token] ?: unkId
            attentionMask[index] = 1L
        }

        return TokenizedText(
            inputIds = inputIds,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds
        )
    }

    private fun loadVocab(): Map<String, Long> {
        return context.assets.open(VOCAB_ASSET_PATH)
            .bufferedReader()
            .useLines { lines ->
                lines.mapIndexed { index, token -> token to index.toLong() }.toMap()
            }
    }

    private fun basicTokenize(text: String): List<String> {
        // Базовая токенизация отделяет слова от пунктуации. Более сложное разбиение
        // слова на подслова выполняется следующим этапом в wordPieceTokenize().
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
                char.isWhitespace() -> {
                    flushCurrent(current, tokens)
                }
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

    private fun wordPieceTokenize(token: String): List<String> {
        if (token.length > MAX_INPUT_CHARS_PER_WORD) return listOf(UNK_TOKEN)
        if (token in vocab) return listOf(token)

        // WordPiece использует greedy longest-match-first: идем слева направо и
        // каждый раз ищем самый длинный кусок слова, который есть в словаре.
        // Префикс ## означает, что подслово является продолжением текущего слова.
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

    private fun flushCurrent(current: StringBuilder, tokens: MutableList<String>) {
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
            current.clear()
        }
    }

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

    private fun loadDoLowerCase(): Boolean {
        return try {
            // У разных BERT/RuBERT-моделей разное отношение к регистру. Этот флаг
            // берется из tokenizer_config.json, чтобы не ломать токенизацию модели.
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
        private const val VOCAB_ASSET_PATH = "ml/rubert_tiny_lite/vocab.txt"
        private const val TOKENIZER_CONFIG_ASSET_PATH = "ml/rubert_tiny_lite/tokenizer_config.json"
        private const val MAX_INPUT_CHARS_PER_WORD = 100
        private const val SPECIAL_TOKENS_COUNT = 2

        private const val PAD_TOKEN = "[PAD]"
        private const val UNK_TOKEN = "[UNK]"
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
    }
}