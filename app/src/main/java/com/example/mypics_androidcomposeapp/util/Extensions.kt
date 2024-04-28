package com.example.mypics_androidcomposeapp.util

import android.content.Context
import android.widget.Toast

// Extension function to show toast messages easily throughout the app
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
