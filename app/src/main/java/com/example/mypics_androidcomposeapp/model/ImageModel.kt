package com.example.mypics_androidcomposeapp.model

import com.example.mypics_androidcomposeapp.database.ImageEntity
import com.google.gson.annotations.SerializedName

data class ImageModel(
    val id: Int,
    val albumId: Int,
    val title: String,
    val thumbnailUrl: String,
    @SerializedName("url") val imageUrl: String,
    val albumTitle: String? = null
) {
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
