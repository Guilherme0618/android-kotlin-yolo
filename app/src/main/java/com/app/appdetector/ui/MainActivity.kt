package com.app.appdetector.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.appdetector.R
import com.app.appdetector.image.ImageConverter
import com.app.appdetector.detector.YoloDetector
import com.app.appdetector.model.DetectionResult
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickImg: Button
    private lateinit var imgPicked: ImageView
    private lateinit var resultImg: ImageView
    private lateinit var btnStartDetection: Button
    private var selectedImageUri: Uri? = null
    private lateinit var yoloDetector: YoloDetector
    private lateinit var labels: List<String>

    private fun loadLabels(fileName: String): List<String> {
        return assets.open(fileName).bufferedReader().useLines { lines ->
            lines.toList()
        }
    }

    private fun drawBoxesOnBitmap(
        bitmap: Bitmap,
        detections: List<DetectionResult>
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)

        val boxPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 6f
            color = android.graphics.Color.BLUE
        }

        val textPaint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            textSize = 40f
            color = android.graphics.Color.BLUE
        }

        for (detection in detections) {

            val label = if (detection.classIndex in labels.indices) {
                labels[detection.classIndex]
            } else {
                "desconhecido"
            }

            val box = yoloDetector.convertToBox(
                centerX = detection.centerX,
                centerY = detection.centerY,
                width = detection.width,
                height = detection.height,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )

            canvas.drawRect(box[0], box[1], box[2], box[3], boxPaint)
            canvas.drawText(label, box[0], box[1] - 10f, textPaint)
        }

        return mutableBitmap
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        labels = loadLabels("fruitsLabels")
        yoloDetector = YoloDetector(this)
        yoloDetector.setupModel("modelFruits_float32.tflite")


        btnPickImg = findViewById(R.id.btnSelectImg)
        imgPicked = findViewById(R.id.imgPickView)
        btnStartDetection = findViewById(R.id.btnYolo)
        resultImg = findViewById(R.id.imgViewResult)



        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

                if (uri != null) {
                    selectedImageUri = uri
                    imgPicked.setImageURI(uri)
                    Log.d("PhotoPicker", "Uri selecionada: $uri")
                } else {
                    Log.d("PhotoPicker", "Nenhuma mídia selecionada")
                    selectedImageUri = null
                }
            }

        btnPickImg.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnStartDetection.setOnClickListener {
            val currentUri = selectedImageUri

            if (currentUri == null) {
                Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //ImageConverter  deve instanciar
            val imageConverter = ImageConverter()
            val bitmap: Bitmap? = imageConverter.loadBitmapFromUri(this, currentUri)

            if (bitmap == null) {
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultImg.setImageBitmap(bitmap)
            val inputBuffer: ByteBuffer = yoloDetector.prepareInput(bitmap)
            val output = yoloDetector.runInference(inputBuffer)


            if (output == null) {
                Toast.makeText(this, "Erro na inferência", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val detections = yoloDetector.parseOutput(output)

            Log.d("MainActivity", "Quantidade de detecções: ${detections.size}")

            if (detections.isNotEmpty()) {

                val bitmapWithBoxes = drawBoxesOnBitmap(bitmap, detections)
                resultImg.setImageBitmap(bitmapWithBoxes)

                Toast.makeText(this, "Detecções encontradas: ${detections.size}", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("MainActivity", "Nenhuma detecção acima do threshold")
                Toast.makeText(this, "Nenhuma detecção encontrada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}