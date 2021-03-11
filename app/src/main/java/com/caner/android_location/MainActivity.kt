package com.caner.android_location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private lateinit var mMap: GoogleMap
    private var centerPos = LatLng(0.0, 0.0)
    private var userLastLocation = LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationMapView.apply {
            onCreate(null)
            onResume()
            getMapAsync(this@MainActivity)
        }
        addGpsListener()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.apply {
            mMap = this
            setOnCameraIdleListener(this@MainActivity)
            uiSettings.isMapToolbarEnabled = false
        }
    }

    override fun onCameraIdle() {
        val lat = mMap.cameraPosition.target.latitude
        val lng = mMap.cameraPosition.target.longitude

        if (lat != 0.0 && lng != 0.0) {
            centerPos = LatLng(lat, lng)
        }
    }

    private fun addGpsListener() {
        GpsUtils(this).turnGPSOn(object : GpsUtils.OnGpsListener {
            override fun gpsStatusOn() {
                checkLocationPermission()
            }
        })
    }

    private fun checkLocationPermission() {
        if (getLocationPermission(Constants.PERMISSION_LOCATION)) {
            checkBackgroundLocationPermission()
        }
    }

    private fun checkBackgroundLocationPermission() {
        if (isBackgroundLocationPermissionAvailable) {
            if (!isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                requestBackgroundLocation(Constants.PERMISSION_BACKGROUND_LOCATION)
                return
            }
            getLastLocation()
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_LOCATION -> {
                val permissionGranted = checkPermissionGranted(grantResults)
                if (permissionGranted) {
                    checkLocationPermission()
                    return
                }
                //If user does not give location permission
                //take action here..
            }
            Constants.PERMISSION_BACKGROUND_LOCATION -> {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location ->
                location?.let {
                    setLocation(it)
                } ?: run { requestNewLocationData() }
            }
            ?.addOnFailureListener {
                it.printStackTrace()
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0

        mFusedLocationClient?.requestLocationUpdates(
            locationRequest, mLocationCallback,
            null
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            setLocation(locationResult.lastLocation)
            mFusedLocationClient?.removeLocationUpdates(this)
        }
    }

    private fun setLocation(location: Location) {
        userLastLocation = LatLng(location.latitude, location.longitude)
        val latLngZoom = CameraUpdateFactory.newLatLngZoom(userLastLocation, 15f)
        mMap.animateCamera(latLngZoom)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        checkLocationPermission()
                    }
                    Activity.RESULT_CANCELED -> {
                        //If user rejects turning gps status on
                        //take action here..
                    }
                }
            }
        }
    }

    fun buttonClickListener(v: View?) {
        when (v?.id) {
            R.id.ivCurrentLocation -> {
                if (userLastLocation.latitude != 0.0 && userLastLocation.longitude != 0.0) {
                    val latLngZoom = CameraUpdateFactory.newLatLngZoom(userLastLocation, 15f)
                    mMap.animateCamera(latLngZoom)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationMapView.onDestroy()
    }
}
