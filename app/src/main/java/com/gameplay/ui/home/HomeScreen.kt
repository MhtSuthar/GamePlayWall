package com.gameplay.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.gameplay.R
import com.gameplay.model.Wall
import com.gameplay.utils.navigated
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listOfWall: MutableStateFlow<MutableList<Wall>>,
    navController: NavHostController
) {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                //TopAppBar(title = { /*TODO*/ })
                RoundedCornerSearch()
            },
            content = {
                ShowStaggeredGrid(listOfWall, navController, it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedCornerSearch() {
    var text by rememberSaveable { mutableStateOf("") }
    Card(
        modifier = Modifier
            .padding(16.dp)
            //.background(color =MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                spotColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp), // Adjust the corner radius as needed
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        // Your content inside the card
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = {
                text = it
            },
            colors = TextFieldDefaults.textFieldColors(
                placeholderColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            placeholder = { Text("Search your favourite wallpaper") },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = stringResource(id = R.string.app_name),
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            singleLine = true
        )
    }
}

@Composable
fun ShowStaggeredGrid(
    listOfWall: MutableStateFlow<MutableList<Wall>>,
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    //val viewModel = viewModel<CalculatorViewModel>()
    val list = listOfWall.collectAsState()
    println("List of Data ${list.value.size}")
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = paddingValues
    ) {
        itemsIndexed(items = list.value) { i, wall ->
            Box(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20))
                    .height(if (i % 2 == 0) 160.dp else 200.dp)
                    /*.background(Color.Black)
                    .neumorphic(
                        lightShadowColor = MaterialTheme.colorScheme.background,
                        darkShadowColor = Color.LightGray
                    )*/
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
    Column {
        RoundedCornerSearch()
    }
}