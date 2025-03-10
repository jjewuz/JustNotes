package com.jjewuz.justnotes.Utils

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.BufferedReader
import java.io.InputStreamReader


class MobileBertInterpreter(context: Context) {
    private var interpreter: Interpreter

    init {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        options.useXNNPACK = false
        interpreter = Interpreter(loadModelFile(context), options)

        Log.d("BERT", "Model input shape: ${interpreter.getInputTensor(0).shape().contentToString()}")
        Log.d("BERT", "Model output shape: ${interpreter.getOutputTensor(0).shape().contentToString()}")

    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("mobilebert.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun predict(input: Array<IntArray>): Array<Array<FloatArray>> {
        val output = Array(1) { Array(1) { FloatArray(512) } }
        interpreter.run(input, output)
        return output
    }
}

class WordPieceTokenizer(context: Context, vocabFile: String = "mobilebert_vocab.txt") {
    private val vocab: Map<String, Int>
    private val invVocab: Map<Int, String>

    init {
        vocab = loadVocabulary(context, vocabFile)
        invVocab = vocab.entries.associateBy({ it.value }, { it.key })
    }

    private fun loadVocabulary(context: Context, vocabFile: String): Map<String, Int> {
        val assetManager = context.assets
        val inputStream = assetManager.open(vocabFile)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val vocabMap = mutableMapOf<String, Int>()
        reader.useLines { lines ->
            lines.forEachIndexed { index, word ->
                vocabMap[word] = index
            }
        }
        return vocabMap
    }

    fun tokenize(text: String): IntArray {
        val tokens = mutableListOf<Int>()
        tokens.add(vocab["[CLS]"] ?: 101) // Начальный токен

        text.lowercase().split(" ").forEach { word ->
            if (vocab.containsKey(word)) {
                tokens.add(vocab[word]!!)
            } else {
                var subword = ""
                for (i in word.indices) {
                    val subToken = if (i == 0) word.substring(i) else "##" + word.substring(i)
                    if (vocab.containsKey(subToken)) {
                        subword = subToken
                        break
                    }
                }
                if (subword.isNotEmpty()) {
                    tokens.add(vocab[subword]!!)
                } else {
                    tokens.add(vocab["[UNK]"] ?: 100) // Неизвестный токен
                }
            }
        }

        tokens.add(vocab["[SEP]"] ?: 102) // Конечный токен
        return tokens.toIntArray()
    }

    fun detokenize(tokenIds: List<Int>): String {
        val tokens = mutableListOf<String>()
        for (tokenId in tokenIds) {
            tokens.add(invVocab[tokenId] ?: "[UNK]")
            val token = invVocab[tokenId] ?: "[UNK]"
            if (token == "[UNK]") {
                //println("Неизвестный токен: $tokenId")
            }
            tokens.add(token)
        }

        // Объединяем токены в строку, учитывая специальные токены и субтокены
        val text = StringBuilder()
        for (token in tokens) {
            when {
                token == "[CLS]" || token == "[SEP]" -> continue // Пропускаем служебные токены
                token.startsWith("##") -> text.append(token.substring(2)) // Субтокены
                else -> text.append(" ").append(token) // Обычные токены
            }
        }

        return text.toString().trim() // Убираем лишние пробелы в начале и конце
    }

}

