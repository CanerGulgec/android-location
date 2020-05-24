package com.caner.android_location

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper {

    companion object {
        fun checkPermissionGranted(@NonNull grantResults: IntArray): Boolean {
            return grantResults.isNotEmpty() &&
                    !grantResults.any { it != PackageManager.PERMISSION_GRANTED }
        }

        fun getLocationPermission(activity: AppCompatActivity, requestCode: Int): Boolean {
            return if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
                false
            } else {
                true
            }
        }
    }
}