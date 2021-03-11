package com.caner.android_location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission


fun checkPermissionGranted(@NonNull grantResults: IntArray): Boolean {
    return grantResults.isNotEmpty() &&
            !grantResults.any { it != PackageManager.PERMISSION_GRANTED }
}

fun AppCompatActivity.getLocationPermission(requestCode: Int): Boolean {
    return if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode
        )
        false
    } else {
        true
    }
}

fun AppCompatActivity.requestBackgroundLocation(requestCode: Int) {
    if (isBackgroundLocationPermissionAvailable) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            requestCode
        )
    }
}

fun Context.isGranted(permission: String): Boolean =
    checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

val isBackgroundLocationPermissionAvailable: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
