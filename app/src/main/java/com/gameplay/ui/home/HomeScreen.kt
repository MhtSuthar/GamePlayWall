package com.gameplay.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gameplay.R
import com.gameplay.model.Wall
import com.gameplay.utils.navigated
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listOfWall: SharedFlow<MutableList<Wall>>,
    navController: NavHostController,
    loadMoreWall: (wall: MutableList<Wall>) -> Unit
) {
    // A surface container using the 'background' color from the theme
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                //TopAppBar(title = { /*TODO*/ })
                RoundedCornerSearch(textState)
            },
            content = {
                ShowStaggeredGrid(listOfWall, navController, it, textState, loadMoreWall)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedCornerSearch(textState: MutableState<TextFieldValue>) {
    //var text by rememberSaveable { mutableStateOf("") }
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
            value = textState.value,
            onValueChange = {
                textState.value = it
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
            trailingIcon = {
                if (textState.value != TextFieldValue("")) {
                    IconButton(
                        onClick = {
                            textState.value =
                                TextFieldValue("") // Remove text from TextField when you press the 'X' icon
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                        )
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
fun ShowStaggeredGrid(
    listOfWall: SharedFlow<MutableList<Wall>>,
    navController: NavHostController,
    paddingValues: PaddingValues,
    textState: MutableState<TextFieldValue>,
    loadMoreWall: (wall: MutableList<Wall>) -> Unit
) {
    //val viewModel = viewModel<CalculatorViewModel>()
    val listOfWallState = listOfWall.collectAsState(initial = mutableListOf())
    Log.e("TAG", "ShowStaggeredGrid: ${listOfWallState.value.size}" )

    val list = remember {
        mutableListOf<Wall>()
    }
    list.addAll(listOfWallState.value)

    if (list.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                /* modifier = Modifier.drawBehind {
                     drawCircle(
                         Color.Black,
                         //radius = size.width / 2 - strokeWidth.toPx() / 2,
                         //style = Stroke(5.dp))
                     )
                 },*/
                color = Color.LightGray,
                strokeWidth = 5.dp
            )
        }
    }

    val searchedText = textState.value.text
    val filteredItems = if (searchedText.isEmpty()) {
        list//.reversed()
    } else {
        val resultList = mutableListOf<Wall>()
        for (item in list) {
            if (item.name?.lowercase(Locale.getDefault())
                    ?.contains(searchedText.lowercase(Locale.getDefault())) == true
            ) {
                resultList.add(item)
            }
        }
        resultList
    }

    val listState = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = paddingValues,
        state = listState
    ) {
        itemsIndexed(items = filteredItems) { i, wall ->
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
                    contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wall.thumb_url)
                        .crossfade(true)
                        .placeholder(R.drawable.logo)
                        .build(),
                    contentDescription = "Translated description of what the image contains"
                )
                AsyncImage(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wall.image_url)
                        .crossfade(true)
                        //.placeholder(R.drawable.logo)
                        .build(),
                    contentDescription = "Translated description of what the image contains"
                )
            }
        }
    }
    listState.OnBottomReached {
        Log.e("TAG", "Load More: ${list.size}")
        if (list.isNotEmpty()) {
            loadMoreWall(list)
        }
    }
}

@Composable
fun LazyStaggeredGridState.OnBottomReached(
    loadMore: (count: Int) -> Unit
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1
        }
    }

    // Convert the state into a cold flow and collect
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect {
                Log.e("TAG", "Item Count: ${layoutInfo.totalItemsCount}")
                // if should load more, then invoke loadMore
                if (it) loadMore(layoutInfo.totalItemsCount)
            }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Column {
        //RoundedCornerSearch()
    }
}