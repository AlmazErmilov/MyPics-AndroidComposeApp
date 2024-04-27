package com.example.mypics_androidcomposeapp

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.compose.AsyncImage
import com.example.mypics_androidcomposeapp.ui.theme.MyPicsAndroidComposeAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPicsAndroidComposeAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            val viewModel: MainViewModel = viewModel()
            MainScreen(viewModel, navController)
        }
        composable("detailScreen/{imageId}", arguments = listOf(navArgument("imageId") {
            type = NavType.IntType
        })) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getInt("imageId") ?: -1
            if (imageId != -1) {
                val viewModel: MainViewModel = viewModel()
                val image = viewModel.getImageById(imageId)
                if (image != null) {
                    ImageDetailScreen(image, navController)
                } else {
                    Text("Error: Image not found")
                }
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
        PlaceholderImagesList(viewModel, navController) // Now passing navController
        Text(text = "Saved images", modifier = Modifier
            .fillMaxWidth()
        )
        SavedImagesList(viewModel, navController)
    }
}

@Composable
fun SavedImagesList(viewModel: MainViewModel, navController: NavController) {
    when (val savedImagesState = viewModel.savedImages.collectAsState().value) {
        is DataState.Loading -> CircularProgressIndicator()
        is DataState.Success -> {
            LazyColumn {
                items(savedImagesState.data) { image ->
                    Row {
                        AsyncImage(
                            model = image.thumbnailUrl,
                            contentDescription = "Thumbnail",
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { viewModel.viewImage(image, navController) }) {
                            Text("View")
                        }
                        Button(onClick = { viewModel.deleteImage(image) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
        is DataState.Error -> Text("Failed to load images")
    }
}

@Composable
fun PlaceholderImagesList(viewModel: MainViewModel, navController: NavController) {
    val allImagesState = viewModel.allImages.collectAsState()

    when (val allImages = allImagesState.value) {
        is DataState.Loading -> CircularProgressIndicator()
        is DataState.Success -> {
            LazyColumn {
                items(allImages.data) { image ->
                    Row {
                        AsyncImage(
                            model = image.thumbnailUrl,
                            contentDescription = "Thumbnail",
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { viewModel.viewImage(image, navController) }) {
                            Text("View")
                        }
                        Button(onClick = { viewModel.saveImage(image) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
        is DataState.Error -> {
            Text(text = "Error: ${allImages.exception.localizedMessage}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(image: ImageModel?, navController: NavController) {
    if (image != null) {
        Column {
            TopAppBar(
                title = {
                    Row {
                        Text("<", modifier = Modifier.clickable {
                            navController.navigate("mainScreen")
                        })
                        Text(
                            "Photos",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.LightGray
                ),
            )
            Text(text = "Selected Image")
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = image.imageUrl,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Id: ${image.id}")
            Text(text = "Title: ${image.title}")
            Text(text = "Album number: ${image.albumId}")
            Text(text = "Album title: [Album Title Here]")  // Placeholder for album title
        }
    } else {
        Text("Error: Image not found")
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    MyPicsAndroidComposeAppTheme {
//        val viewModel: MainViewModel = viewModel()
//        MainScreen(viewModel, rememberNavController())
//    }
//}

sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val exception: Exception) : DataState<Nothing>()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _savedImages = MutableStateFlow<DataState<List<ImageModel>>>(DataState.Loading)
    val savedImages: StateFlow<DataState<List<ImageModel>>> = _savedImages

    private val photoService = ApiClient.instance.create(PhotoService::class.java)

    private val _allImages = MutableStateFlow<DataState<List<ImageModel>>>(DataState.Loading)
    val allImages: StateFlow<DataState<List<ImageModel>>> = _allImages

    private val imageDao = DatabaseBuilder.getDatabase(application).imageDao()

    init { loadImages() }

    // lateinit var navController: NavController // commented now pga viewImage

    private fun loadImages() {
        viewModelScope.launch {
            _allImages.value = DataState.Loading
            try {
                val fetchedImages = photoService.getPhotos()
                // Limit the number of images to the first 5
                val limitedImages = fetchedImages.take(5)
                _allImages.value = DataState.Success(limitedImages)
            } catch (e: Exception) {
                _allImages.value = DataState.Error(e)
            }
        }
    }

    fun viewImage(image: ImageModel, navController: NavController) {
        Log.d("ViewImage", "Navigating with image ID: ${image.id}")
        navController.navigate("detailScreen/${image.id}")
    }

    fun saveImage(image: ImageModel) {
        viewModelScope.launch {
            // Get the current list if it's a success state, otherwise empty list
            val currentList = (savedImages.value as? DataState.Success)?.data ?: listOf()
            // Update the state with the new image added
            _savedImages.value = DataState.Success(currentList + image)
            // Here, you should also interact with the Room database to save the image
            // Convert ImageModel to ImageEntity before saving
            val imageEntity = ImageModel.toEntity(image)
            imageDao.insertAll(imageEntity)
        }
    }

    fun deleteImage(image: ImageModel) {
        viewModelScope.launch {
            // Get the current list if it's a success state, otherwise empty list
            val currentList = (savedImages.value as? DataState.Success)?.data ?: listOf()
            // Update the state with the image removed
            _savedImages.value = DataState.Success(currentList.filter { it.id != image.id })
            // Here, you should also interact with the Room database to delete the image
            val imageEntity = ImageModel.toEntity(image)
            imageDao.delete(imageEntity)
        }
    }

    // Simulate a network fetch with a delay
    private suspend fun mockNetworkFetch(): List<ImageModel> {
        delay(2000)
        return listOf(
            ImageModel(1, 1, "Image 1", "https://via.placeholder.com/150/92c952", "https://via.placeholder.com/600/92c952"),
            ImageModel(2, 1, "Image 2", "https://via.placeholder.com/150/771796", "https://via.placeholder.com/600/771796")
            // Add more mock images as needed
        )
    }

    fun getImageById(imageId: Int?): ImageModel? {
        return allImages.value.let { state ->
            when (state) {
                is DataState.Success -> state.data.firstOrNull { it.id == imageId }
                else -> null
            }
        }
    }
}

//data classes are used by Retrofit to serialize and deserialize the data
data class ImageModel(
    val id: Int,
    val albumId: Int,
    val title: String,
    val thumbnailUrl: String,
    val imageUrl: String,
    val albumTitle: String? = null
){
    //It's often useful to have a method to convert a data class to and from the entity class
    companion object {
        fun toEntity(image: ImageModel) = ImageEntity(
            id = image.id,
            albumId = image.albumId,
            title = image.title,
            thumbnailUrl = image.thumbnailUrl,
            imageUrl = image.imageUrl,
            albumTitle = image.albumTitle
        )
    }
}

data class Album(
    val userId: Int,
    val id: Int,
    val title: String
)

//The interface that Retrofit uses to call the API.
//interface defines methods for each API endpoint.
interface PhotoService {
    @GET("photos")
    suspend fun getPhotos(): List<ImageModel>

    @GET("albums")
    suspend fun getAlbums(): List<Album>
}

// create and provide a Retrofit instance
object ApiClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

//an entity class that Room will use to create a table.
@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: Int,
    val albumId: Int,
    val title: String,
    val thumbnailUrl: String,
    val imageUrl: String?,  // nullable now
    val albumTitle: String? = null
)

//Data Access Object: an interface with the methods that need to access local data.
@Dao
interface ImageDao {
    @Query("SELECT * FROM images")
    fun getAllImages(): List<ImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg images: ImageEntity)

    @Delete
    suspend fun delete(image: ImageEntity)
}

//an abstract class that extends RoomDatabase and includes all DAOs
@Database(entities = [ImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}

// about to yse Hilt or another DI framework to provide instances of the database and DAOs.
object DatabaseBuilder {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "my_pics_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}