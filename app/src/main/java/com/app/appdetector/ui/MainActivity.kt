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

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickImg: Button
    private lateinit var imgPicked: ImageView
    private lateinit var resultImg: ImageView
    private lateinit var btnStartDetection: Button
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val yoloDetector = YoloDetector(this)
        yoloDetector.setupModel("yolov8n_float16.tflite")
        yoloDetector.logModelInfo()

        btnPickImg = findViewById(R.id.btnSelectImg)
        imgPicked = findViewById(R.id.imgPickView)
        btnStartDetection = findViewById(R.id.btnYolo)
        resultImg = findViewById(R.id.imgViewResult)



        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

                if (uri != null) {
                    selectedImageUri = uri
                    Log.d("PhotoPicker", "Uri selecionada: $uri")

                    imgPicked.setImageURI(uri)
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

            if (bitmap != null) {
                Log.d(
                    "ImageConverter",
                    "Bitmap carregado com sucesso: ${bitmap.width}x${bitmap.height}"
                )

                resultImg.setImageBitmap(bitmap)
                Toast.makeText(this, "Imagem carregada na memória", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }
}