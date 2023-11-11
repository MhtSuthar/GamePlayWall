package com.gameplay

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.gameplay.model.Wall
import com.gameplay.ui.detail.WallDetails
import com.gameplay.ui.home.UpdateAvailDialog
import com.gameplay.ui.theme.GamePlayWallTheme
import com.gameplay.utils.navigated
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import me.nikhilchaudhari.library.neumorphic


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    var listOfWall = MutableStateFlow<MutableList<Wall>>(mutableListOf())
    private val firebaseRemoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    override fun onStart() {
        super.onStart()
        initFirebaseDatabase()
        initFirebaseConfig()
    }

    private fun initFirebaseConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        /*firebaseRemoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate : ConfigUpdate) {
                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys);

                if (configUpdate.updatedKeys.contains("isUpdateAvail")) {
                    firebaseRemoteConfig.activate().addOnCompleteListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Fetch and activate succeeded",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }

            override fun onError(error : FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error with code: " + error.code, error)
            }
        })*/
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    val isUpdateAvail = firebaseRemoteConfig.getBoolean("isUpdateAvail")
                    val isForceUpdate = firebaseRemoteConfig.getBoolean("isForceUpdate")
                    val appVersion = firebaseRemoteConfig.getString("appVersion")
                    val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                    Log.i(TAG, "Fetch Config update avail $isUpdateAvail App version $appVersion")
                    if (appVersion.toDouble() > pInfo.versionName.toDouble() && isUpdateAvail) {
                        showUpdateDialog(isForceUpdate)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Fetch failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun showUpdateDialog(isForceUpdate: Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(getString(R.string.update_avail))
            .setTitle(getString(R.string.download_app))
            .setPositiveButton(getString(R.string.download)) { dialog, _ ->
                dialog.cancel()
                openPlayStore()
            }
        if (!isForceUpdate) {
            builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.setCancelable(!isForceUpdate)
        dialog.show()
    }

    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    @Composable
    private fun ShowComposeUpdateDialog() {
        val showDialog = remember { mutableStateOf(true) }
        if (showDialog.value) {
            UpdateAvailDialog(setShowDialog = {
                showDialog.value = it
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            GamePlayWallTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        ShowHomeScreen(listOfWall, navController)
                    }
                    composable(
                        "wallDetails",
                        //arguments = listOf(navArgument("wallUrl") { type = NavType.StringType })
                    ) {
                        val wall = it.arguments?.getParcelable<Wall>("wall")
                        WallDetails(wall)
                    }
                }
            }
        }
    }

    private fun initFirebaseDatabase() {
        Log.d(TAG, "Firebase Called")
        val database = Firebase.database
        val myRef = database.reference
//        myRef.child("Test")
//        myRef.setValue("Hello, World!").addOnSuccessListener {
//            Log.d(TAG, "Succ Push Value")
//        }.addOnFailureListener {
//            Log.d(TAG, "Failed ${it.message}")
//        }

        myRef.child("walls").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //Need to convert in Data Classes
                var _listOfWall = mutableListOf<Wall>()
                dataSnapshot.children.forEach {
                    val value = it.getValue<Wall>()
                    value?.let { wall -> _listOfWall.add(wall) }
                    Log.d(TAG, "Value is: ${value?.name}")
                }
                listOfWall.value = _listOfWall
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }
}

@Composable
fun ShowHomeScreen(
    listOfWall: MutableStateFlow<MutableList<Wall>>,
    navController: NavHostController
) {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ShowStaggeredGrid(listOfWall, navController)
    }
}

@Composable
fun ShowStaggeredGrid(
    listOfWall: MutableStateFlow<MutableList<Wall>>,
    navController: NavHostController
) {
    //val viewModel = viewModel<CalculatorViewModel>()
    val list = listOfWall.collectAsState()
    println("List of Data ${list.value.size}")
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
    ) {
        itemsIndexed(items = list.value) { i, wall ->
            Box(
                Modifier
                    .padding(2.dp)
                    .fillMaxWidth()
                    .height(if (i % 2 == 0) 160.dp else 200.dp)
                    .background(Color.Black)
                    .neumorphic(
                        lightShadowColor = MaterialTheme.colorScheme.background,
                        darkShadowColor = Color.LightGray
                    )
                    .clickable {
                        val bundle = Bundle();
                        bundle.putParcelable("wall", wall)
                        navController.navigated("wallDetails", bundle)
                        //navController.navigate("wallDetails/${wall.image_url.toString()}")
                    },
            ) {
                AsyncImage(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.FillHeight,
                    model = wall.thumb_url,
                    contentDescription = "Translated description of what the image contains"
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    GamePlayWallTheme {
        //ShowStaggeredGrid()
    }
}