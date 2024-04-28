package com.example.mypics_androidcomposeapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mypics_androidcomposeapp.database.DatabaseBuilder
import com.example.mypics_androidcomposeapp.model.ImageModel
import com.example.mypics_androidcomposeapp.network.ApiClient
import com.example.mypics_androidcomposeapp.network.PhotoService
import com.example.mypics_androidcomposeapp.util.DataState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _savedImages = MutableStateFlow<DataState<List<ImageModel>>>(DataState.Loading)
    val savedImages: StateFlow<DataState<List<ImageModel>>> = _savedImages

    private val photoService = ApiClient.instance.create(PhotoService::class.java)

    private val _allImages = MutableStateFlow<DataState<List<ImageModel>>>(DataState.Loading)
    val allImages: StateFlow<DataState<List<ImageModel>>> = _allImages

    private val imageDao = DatabaseBuilder.getDatabase(application).imageDao()

    init { loadImages() }

    // late init var navController: NavController // commented now pga viewImage

    private fun loadImages() {
        viewModelScope.launch {
            _allImages.value = DataState.Loading
            try {
                val fetchedImages = photoService.getPhotos()
                // Limit the number of images to the first 5
                val limitedImages = fetchedImages.take(3)
                _allImages.value = DataState.Success(limitedImages)
            } catch (e: Exception) {
                _allImages.value = DataState.Error(e)
            }
        }
    }

    fun viewImage(image: ImageModel, navController: NavController) {
        //Log.d("ViewImage", "Navigating with image ID: ${image.id}")
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
            Log.d("database", "insertAll method was called")
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
            Log.d("database", "delete method was called")
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
