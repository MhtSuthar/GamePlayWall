package com.gameplay.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.gameplay.R
import com.gameplay.model.Wall
import com.gameplay.view_model.WallViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallDetails(
    imagePath: Wall?,
    navController: NavHostController,
    wallViewModel: WallViewModel = viewModel()
) {
    val context = LocalContext.current
    val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            // Use location
            Log.d("TAG", "Permissions Accepted")
            wallViewModel.downloadImageFromUrl(context, imagePath?.image_url ?: "")
        } else {
            // Show dialog
            Log.d("TAG", "Permissions Not Accepted")
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("${imagePath?.name}")
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                            checkAndRequestPermissions(
                                context,
                                PERMISSIONS,
                                launcherMultiplePermissions
                            )
                        } else {
                            wallViewModel.downloadImageFromUrl(context, imagePath?.image_url ?: "")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_download),
                        contentDescription = "Download",
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                        /*modifier = Modifier.preferredHeightIn(160.dp, 260.dp)
                            .fillMaxWidth(),*/
                    )
                }
            },
        ) {
            Box() {
                /*val model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath?.image_url)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build()
                val painter = rememberAsyncImagePainter(model)
                Image(
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )*/
                AsyncImage(
                    modifier = Modifier
                        //.padding(2.dp)
                        .fillMaxWidth().fillMaxHeight()
                        /*.aspectRatio(16f / 9f)*/,
                    //contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath?.image_url)
                        .crossfade(true)
                        //.size(Size.ORIGINAL)
                        .placeholder(R.drawable.logo)
                        .build(),
                    contentDescription = "Translated description of what the image contains",
                )
            }
        }
    }
}

fun checkAndRequestPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    if (
        permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        // Use location because permissions are already granted
    } else {
        // Request permissions
        launcher.launch(permissions)
    }
}