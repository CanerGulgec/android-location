package com.caner.android_location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.awaitLastLocation(locationFunc: (Location?) -> Unit) {
    lastLocation.addOnSuccessListener { location ->
        locationFunc.invoke(location)
    }.addOnFailureListener { e ->
        e.printStackTrace()
    }
}

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow(locationFunc: (Location?) -> Unit) {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result ?: return
            for (location in result.locations) {
                locationFunc.invoke(location)
            }
        }
    }

    requestLocationUpdates(createLocationRequest(), callback, Looper.getMainLooper())
        .addOnFailureListener { e ->
            e.printStackTrace()
            removeLocationUpdates(callback)
        }
}


fun createLocationRequest() = LocationRequest.create().apply {
    interval = 3000
    fastestInterval = 2000
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}