package com.example.mypics_androidcomposeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mypics_androidcomposeapp.network.DownloadImagesWorker
import com.example.mypics_androidcomposeapp.ui.AppNavigation
import com.example.mypics_androidcomposeapp.ui.theme.MyPicsAndroidComposeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWorkManager()
        setContent {
            MyPicsAndroidComposeAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
    private fun setupWorkManager() {
        val workRequest = OneTimeWorkRequestBuilder<DownloadImagesWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
