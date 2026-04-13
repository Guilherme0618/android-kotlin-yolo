package com.app.appdetector.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.appdetector.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickImg: Button
    private lateinit var imgpicked: ImageView
    private lateinit var resultImg: ImageView
    private lateinit var btnStartDetection: Button



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        btnPickImg = findViewById(R.id.btnSelectImg)
        imgpicked = findViewById(R.id.imgPickView)
        btnStartDetection = findViewById(R.id.btnYolo)
        resultImg = findViewById(R.id.imgViewResult)


        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri ->

            if (uri != null) {
                Log.d("PhotoPicker", "Uri selecionada : $uri")
                imgpicked.setImageURI(uri)
            } else {
                Log.d("PhotoPicker","No media selected")
            }


        }

        btnPickImg.setOnClickListener {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }

        btnStartDetection.setOnClickListener {



        }
    }


}