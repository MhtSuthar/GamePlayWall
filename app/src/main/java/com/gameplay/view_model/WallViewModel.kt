package com.gameplay.view_model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class WallViewModel : ViewModel() {
    var v1 by mutableStateOf("0")
    var v2 by mutableStateOf("0")
    /*val result = snapshotFlow { v1 to v2 }.mapLatest {
        sum(it.first, it.second)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0")*/

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
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
    }

     fun downloadImageFromUrl(context: Context, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Disable hardware bitmaps.
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            if (!path.exists())
                path.mkdirs()


            val imageFile = File(path, "${url.substring(url.lastIndexOf('/') + 1)}.jpg")
            if (imageFile.exists()) {
                //File Name Already Exist Do Whatever
            } else {
                persistImage(context, imageFile, bitmap)
            }
        }
    }
}