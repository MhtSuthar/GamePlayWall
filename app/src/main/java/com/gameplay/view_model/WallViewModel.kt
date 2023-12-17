package com.gameplay.view_model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.gameplay.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class WallViewModel : ViewModel() {
    private var _downloadImageCallback = mutableStateOf("")
    val downloadImageCallback: State<String> = _downloadImageCallback

    private fun persistImage(context: Context, file: File, bitmap: Bitmap) {
        val os: OutputStream
        try {
            os = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            MediaScannerConnection.scanFile(
                context, arrayOf(file.toString()),
                null, null
            )
            _downloadImageCallback.value = file.path
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
            _downloadImageCallback.value = "-1"
        }
    }

     fun downloadImageFromUrl(context: Context, wall: Wall?) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e("TAG", "Download Started")
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(wall?.image_url)
                .allowHardware(false) // Disable hardware bitmaps.
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            if (!path.exists())
                path.mkdirs()

            val imageFile = File(path, "${wall?.image_url?.substring(wall?.image_url.lastIndexOf('/') + 1)}.jpg")
            Log.e("TAG", "imageFile Path: $imageFile")
            if (imageFile.exists()) {
                Log.e("TAG", "Download Image Present")
                //File Name Already Exist Do Whatever
                _downloadImageCallback.value = imageFile.path
            } else {
                persistImage(context, imageFile, bitmap)
            }
        }
    }

    fun clearFilePath() {
        _downloadImageCallback.value = ""
    }
}