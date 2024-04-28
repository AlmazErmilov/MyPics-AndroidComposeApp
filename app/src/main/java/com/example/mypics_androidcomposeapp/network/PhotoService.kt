package com.example.mypics_androidcomposeapp.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mypics_androidcomposeapp.database.DatabaseBuilder
import com.example.mypics_androidcomposeapp.model.ImageModel
import retrofit2.http.GET

//The interface that Retrofit uses to call the API.
//interface defines methods for each API endpoint.
interface PhotoService {
    @GET("photos")
    suspend fun getPhotos(): List<ImageModel>

    @GET("albums")
    suspend fun getAlbums(): List<Album>
}

data class Album(
    val userId: Int,
    val id: Int,
    val title: String
)

//Worker class will handle fetching the images in the background and storing them in the database.
class DownloadImagesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val photoService = ApiClient.instance.create(PhotoService::class.java)
            val images = photoService.getPhotos()
            val imageEntities = images.map { ImageModel.toEntity(it) }.toTypedArray()
            val database = DatabaseBuilder.getDatabase(applicationContext)
            database.imageDao().insertAll(*imageEntities)
            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadImagesWorker", "Error downloading images", e)
            Result.failure()
        }
    }
}
