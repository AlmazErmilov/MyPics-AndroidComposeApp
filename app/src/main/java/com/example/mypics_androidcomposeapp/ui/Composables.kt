package com.example.mypics_androidcomposeapp.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.mypics_androidcomposeapp.model.ImageModel
import com.example.mypics_androidcomposeapp.util.DataState
import com.example.mypics_androidcomposeapp.viewmodel.MainViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(viewModel, navController)
        }
        composable("detailScreen/{imageId}", arguments = listOf(navArgument("imageId") {
            type = NavType.IntType
        })) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getInt("imageId") ?: -1
            if (imageId != -1) {
                ImageDetailScreenWrapper(viewModel, navController, imageId)
            } else {
                Text("Error: Invalid image ID")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, navController: NavController) {
    Column {
        TopAppBar(
            title = { Text(
                "Home screen",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
            )},
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.LightGray)
        )
        PlaceholderImagesList(viewModel, navController)
        SavedImagesList(viewModel, navController)
    }
}

@Composable
fun SavedImagesList(viewModel: MainViewModel, navController: NavController) {
    val savedImagesState = viewModel.savedImages.collectAsState().value  // Collecting the current state of saved images

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Saved Images",
            // style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(4.dp)
        )
        when (savedImagesState) {
            is DataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()  // Show loading indicator while images are loading
                }
            }
            is DataState.Success -> {
                if (savedImagesState.data.isNotEmpty()) {
                    LazyColumn {
                        items(savedImagesState.data) { image ->
                            ImageRow(image, viewModel, navController)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No saved images available.")  // Show message if no images are saved
                    }
                }
            }
            is DataState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load saved images: ${savedImagesState.exception.localizedMessage}")  // Display error message
                }
            }
        }
    }
}

@Composable
fun ImageRow(image: ImageModel, viewModel: MainViewModel, navController: NavController, actionText: String = "Delete") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.viewImage(image, navController) }
            .padding(2.dp)
    ) {
        AsyncImage(
            model = image.thumbnailUrl,
            contentDescription = "Thumbnail of image",
            modifier = Modifier
                .size(70.dp)
                .padding(2.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(image.title)
        }
        Button(
            onClick = {
                if (actionText == "Save") viewModel.saveImage(image)
                else viewModel.deleteImage(image)
            },
            modifier = Modifier.padding(2.dp)
        ) {
            Text(actionText)
        }
    }
}

@Composable
fun PlaceholderImagesList(viewModel: MainViewModel, navController: NavController) {
    val allImagesState = viewModel.allImages.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Images from jsonplaceholder.typicode.com",
            modifier = Modifier.padding(4.dp)
        )
        when (val allImages = allImagesState.value) {
            is DataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                if (allImages.data.isNotEmpty()) {
                    LazyColumn {
                        items(allImages.data) { image ->
                            ImageRow(image, viewModel, navController, "Save")
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No available images.")
                    }
                }
            }
            is DataState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${allImages.exception.localizedMessage}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreenWrapper(viewModel: MainViewModel, navController: NavController, imageId: Int) {
    Column (modifier = Modifier.verticalScroll(ScrollState(0))) {
        TopAppBar(
            title = {
                Row {
                    Text("<", modifier = Modifier.clickable {
                        navController.popBackStack()  // Ensure proper back navigation
                    })
                    Text("Photos", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.LightGray)
        )
        // Retrieve the image details for the given imageId
        ImageDetailScreen(viewModel.getImageById(imageId), navController)
    }
}

@Composable
fun ImageDetailScreen(image: ImageModel?, navController: NavController) {
    if (image != null) {
        Column {
            Spacer(modifier = Modifier.height(2.dp))
            AsyncImage(
                model = image.imageUrl,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Selected Image")
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Id: ${image.id}")
            Text(text = "Title: ${image.title}")
            Text(text = "Album number: ${image.albumId}")
            Text(text = "Album title: [Album Title Here]")  // Placeholder for album title
        }
    } else {
        Text("Error: Image not found")
    }
}

/*@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyPicsAndroidComposeAppTheme {
        val viewModel: MainViewModel = viewModel()
        MainScreen(viewModel, rememberNavController())
    }
}*/
