package com.app.appdetector.detector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.app.appdetector.model.DetectionResult
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YoloDetector(private val context: Context) {

    private var interpreter: Interpreter? = null

    companion object {
        private const val MODEL_WIDTH = 640
        private const val MODEL_HEIGHT = 640
        private const val MODEL_CHANNELS = 3
        private const val FLOAT_SIZE = 4
    }

    fun setupModel(yoloPadrao: String) {
        try {
            val model = loadModelFile(yoloPadrao)
            interpreter = Interpreter(model)
            Log.d("YoloDetector", "modelo carregado!")
        } catch (e: Exception) {
            Log.e("YoloDetector", "erro ao carregar o modelo ${e.message}")
        }
    }

    fun prepareInput(bitmap: Bitmap): ByteBuffer{
        val resizedBitmap = resizeBitmap(bitmap)
        val inputBuffer = bitmapToByteBuffer(resizedBitmap)

        Log.d("YoloDetector", "Bitmap original: ${bitmap.width}x${bitmap.height}")
        Log.d("YoloDetector", "Bitmap redimensionado: ${resizedBitmap.width}x${resizedBitmap.height}")
        Log.d("YoloDetector", "Capacidade do buffer: ${inputBuffer.capacity()} bytes")

        return  inputBuffer
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, MODEL_WIDTH, MODEL_HEIGHT, true)
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = MODEL_WIDTH * MODEL_HEIGHT * MODEL_CHANNELS * FLOAT_SIZE

        val byteBuffer = ByteBuffer.allocateDirect(inputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(MODEL_WIDTH * MODEL_HEIGHT)
        bitmap.getPixels(
            pixels,
            0,
            MODEL_WIDTH,
            0,
            0,
            MODEL_WIDTH,
            MODEL_HEIGHT
        )

        var pixelIndex = 0

        for (y in 0 until MODEL_HEIGHT) {
            for (x in 0 until MODEL_WIDTH) {
                val pixelValue = pixels[pixelIndex++]

                val red = ((pixelValue shr 16) and 0xFF) / 255.0f
                val green = ((pixelValue shr 8) and 0xFF) / 255.0f
                val blue = (pixelValue and 0xFF) / 255.0f

                byteBuffer.putFloat(red)
                byteBuffer.putFloat(green)
                byteBuffer.putFloat(blue)
            }
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    fun runInference(inputBuffer: ByteBuffer): Array<Array<FloatArray>>? {
        val currentInterpreter = interpreter

        if (currentInterpreter == null) {
            return null
        }

        val output = Array(1) { Array(84) { FloatArray(8400) } }

        try {
            currentInterpreter.run(inputBuffer, output)
            Log.d("inferencia", "sucesso!")
            return output
        } catch (e: Exception) {
            Log.d("inferencia", "erro!!")
            return null
        }

    }

    fun parseOutput(output: Array<Array<FloatArray>>): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()

        val channels = output[0].size
        val predictions = output[0][0].size
        val confidenceThreshold = 0.25f

        if (channels < 6) {
            Log.e("YoloDetector", "Formato de saída inesperado")
            return detections
        }

        for (i in 0 until predictions) {
            val centerX = output[0][0][i]
            val centerY = output[0][1][i]
            val width = output[0][2][i]
            val height = output[0][3][i]

            var bestClassIndex = -1
            var bestScore = 0f

            for (classChannel in 4 until channels) {
                val score = output[0][classChannel][i]
                if (score > bestScore) {
                    bestScore = score
                    bestClassIndex = classChannel - 4
                }
            }

            if (bestScore > confidenceThreshold) {
                detections.add(
                    DetectionResult(
                        classIndex = bestClassIndex,
                        confidence = bestScore,
                        centerX = centerX,
                        centerY = centerY,
                        width = width,
                        height = height
                    )
                )
            }
        }

        return detections
    }


    private fun loadModelFile(yoloPadrao: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(yoloPadrao)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

}