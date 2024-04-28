package com.example.mypics_androidcomposeapp.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

//an abstract class that extends RoomDatabase and includes all DAOs
@Database(entities = [ImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
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
            Log.d("database", "Room database created")
            INSTANCE = instance
            instance
        }
    }
}
