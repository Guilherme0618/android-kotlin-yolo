package com.app.appdetector.detector

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YoloDetector(private val context: Context) {

    private var interpreter: Interpreter? = null

    fun setupModel(modelName: String) {
        try {
            val model = loadModelFile(modelName)
            interpreter = Interpreter(model)
            Log.d("YoloDetector", "modelo carregado!")
        } catch (e: Exception) {
            Log.e("YoloDetector", "erro ao carregar o modelo ${e.message}")
        }
    }

    fun logModelInfo() {
        val currentInterpreter = interpreter

        if (currentInterpreter == null) {
            Log.e("YoloDetector", "interpreter nulo")
            return
        }

        val inputCount = currentInterpreter.inputTensorCount
        Log.d("YoloDetector", "quantidade de entradas: $inputCount")

        for (i in 0 until inputCount) {
            val inputTensor = currentInterpreter.getInputTensor(i)
            Log.d(
                "YoloDetector",
                "Entrada[$i] : shape=${inputTensor.shape().contentToString()}, tipo=${inputTensor.dataType()}"
            )
        }

        val outputCount = currentInterpreter.outputTensorCount
        Log.d("YoloDetector", "quantidade de saidas $outputCount")

        for (i in 0 until outputCount) {
            val outputTensor = currentInterpreter.getOutputTensor(i)
            Log.d(
                "YoloDetector",
                "Saída[$i] shape=${outputTensor.shape().contentToString()}, tipo=${outputTensor.dataType()}"
            )
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}