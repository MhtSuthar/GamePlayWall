package com.gameplay.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gameplay.model.Wall

@Composable
fun WallDetails(imagePath: Wall?) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box {
            AsyncImage(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentScale = ContentScale.Inside,
                model = imagePath?.image_url,
                contentDescription = "Translated description of what the image contains"
            )
        }
    }
}