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
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickImg: Button
    private lateinit var imgPicked: ImageView
    private lateinit var resultImg: ImageView
    private lateinit var btnStartDetection: Button
    private var selectedImageUri: Uri? = null
    private lateinit var yoloDetector: YoloDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        yoloDetector = YoloDetector(this)
        yoloDetector.setupModel("yolov8n_float16.tflite")


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
                val first = detections[0]
                Log.d(
                    "MainActivity",
                    "Primeira detecção -> classe=${first.classIndex}, confiança=${first.confidence}, x=${first.centerX}, y=${first.centerY}, w=${first.width}, h=${first.height}"
                )
                Toast.makeText(this, "Detecções encontradas: ${detections.size}", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("MainActivity", "Nenhuma detecção acima do threshold")
                Toast.makeText(this, "Nenhuma detecção encontrada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}