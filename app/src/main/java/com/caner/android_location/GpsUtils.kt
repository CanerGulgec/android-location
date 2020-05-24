package com.caner.android_location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

class GpsUtils(private val context: Activity) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var settingsBuilder : LocationSettingsRequest.Builder? = null

    init {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        settingsBuilder?.setAlwaysShow(true)
    }

    fun turnGPSOn(onGpsListener: OnGpsListener?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGpsListener?.gpsStatusOn()
        } else {
            val result = LocationServices.getSettingsClient(context).checkLocationSettings(settingsBuilder?.build())
            result.addOnCompleteListener { task ->
                try {
                    task.getResult(ApiException::class.java)
                } catch (ex: ApiException) {
                    when (ex.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            Log.e("gpsUtils", "Gps status off")
                            val resolvableApiException = ex as ResolvableApiException
                            resolvableApiException.startResolutionForResult(context, Constants.REQUEST_CHECK_SETTINGS
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e("gpsUtils", "PendingIntent unable to execute request.")
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Log.e("gpsUtils", "Something is wrong in your GPS")
                        }
                    }
                }
            }
        }
    }

    interface OnGpsListener {
        fun gpsStatusOn()
    }
}