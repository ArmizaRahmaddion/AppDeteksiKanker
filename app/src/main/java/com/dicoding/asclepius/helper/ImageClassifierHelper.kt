package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifierHelper(private val context: Context) {

    private lateinit var interpreter: Interpreter
    private val labels: List<String>

    init {
        interpreter = Interpreter(loadModelFile("ml/cancer_classification.tflite"))
        labels = loadLabels("ml/labels.txt")
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(filePath: String): List<String> {
        val labels = mutableListOf<String>()
        context.assets.open(filePath).bufferedReader().useLines { lines ->
            lines.forEach { labels.add(it) }
        }
        Log.d("ImageClassifierHelper", "Labels loaded: $labels")
        return labels
    }

    fun classifyStaticImage(bitmap: Bitmap): Pair<String, Float> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { FloatArray(2) } // Output layer has 2 classes
        interpreter.run(inputBuffer, output)

        // Get the highest confidence label
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = output[0][maxIndex]

        val label = if (maxIndex != -1) labels[maxIndex] else "Unknown"

        // Log output for debugging
        Log.d("ImageClassifierHelper", "Output: ${output[0].contentToString()}")
        Log.d("ImageClassifierHelper", "Predicted label: $label, confidence: $confidence")

        return Pair(label, confidence)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        var pixelIndex = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val pixel = intValues[pixelIndex++]

                // Convert to [-1, 1] range
                inputBuffer.putFloat(((pixel shr 16 and 0xFF) / 127.5f) - 1.0f)  // R
                inputBuffer.putFloat(((pixel shr 8 and 0xFF) / 127.5f) - 1.0f)   // G
                inputBuffer.putFloat(((pixel and 0xFF) / 127.5f) - 1.0f)           // B
            }
        }
        return inputBuffer
    }
}



