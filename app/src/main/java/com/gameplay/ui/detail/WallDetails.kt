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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gameplay.R
import com.gameplay.model.Wall
import com.gameplay.view_model.WallViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallDetails(
    wall: Wall?,
    navController: NavHostController,
    wallViewModel: WallViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filePath = wallViewModel.downloadImageCallback.value
    if(filePath == "-1"){
        //Error on downloading
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "Downloaded Failed! Try later",
                duration = SnackbarDuration.Short
            )
            wallViewModel.clearFilePath()
        }
    }else if (filePath != ""){
        coroutineScope.launch {
            val snackbarResult = snackbarHostState.showSnackbar(
                message = "Downloaded Success",
                actionLabel = "View",
                duration = SnackbarDuration.Long
            )
            when (snackbarResult) {
                SnackbarResult.ActionPerformed -> {
                    wallViewModel.clearFilePath()
                }
                else -> {
                    wallViewModel.clearFilePath()
                }
            }
        }
    }

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
            wallViewModel.downloadImageFromUrl(context, wall)
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text("${wall?.name}")
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
                        Log.e("Click", "WallDetails: ${Build.VERSION.SDK_INT} <= ${Build.VERSION_CODES.TIRAMISU} ", )
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                            checkAndRequestPermissions(
                                context,
                                PERMISSIONS,
                                launcherMultiplePermissions,
                                wall,
                                wallViewModel
                            )
                        } else {
                            wallViewModel.downloadImageFromUrl(context, wall)
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
                        .fillMaxWidth()
                        .fillMaxHeight()
                        /*.aspectRatio(16f / 9f)*/,
                    //contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wall?.image_url)
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
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    imagePath: Wall?,
    wallViewModel: WallViewModel,
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
        wallViewModel.downloadImageFromUrl(context, imagePath)
    } else {
        // Request permissions
        launcher.launch(permissions)
    }
}