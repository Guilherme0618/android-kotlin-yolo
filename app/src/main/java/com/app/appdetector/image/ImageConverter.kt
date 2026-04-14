package com.app.appdetector.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

class ImageConverter {

    fun loadBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)

            } else {

                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
        } catch (e: Exception) {
            Log.e("ImageLoader", "Erro ao converter Uri em Bitmap: ${e.message}")
            null

        }
    }
}