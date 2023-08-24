package com.gameplay

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gameplay.model.Wall
import com.gameplay.ui.theme.GamePlayWallTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
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
                    Toast.makeText(
                        this,
                        "Fetch and activate succeeded $isUpdateAvail",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Fetch failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GamePlayWallTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowStaggeredGrid(listOfWall)
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
fun ShowStaggeredGrid(listOfWall: MutableStateFlow<MutableList<Wall>>) {
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
                    .background(Color.Cyan)
                    .neumorphic(),
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