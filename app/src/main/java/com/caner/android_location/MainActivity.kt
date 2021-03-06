package com.caner.android_location

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private val mFusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private lateinit var mMap: GoogleMap
    private var centerPos = LatLng(0.0, 0.0)
    private var userLastLocation = LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    private suspend fun getLastKnownLocation() {
        try {
            val lastLocation = mFusedLocationClient?.awaitLastLocation()
            lastLocation?.let {
                setLocation(it)
            } ?: startUpdatingLocation()
        } catch (e: Exception) {
            Log.d("TAG", "Unable to get location", e)
        }
    }

    private fun startUpdatingLocation() {
/*
       // LiveData
        mFusedLocationClient.locationFlow()
            .conflate()
            .catch { e ->
                Log.d("TAG", "Unable to get location", e)
            }
            .asLiveData()
            .observe(this, { location ->
                setLocation(location)
            })
*/

        // when you have only one flow to collect
/*        mFusedLocationClient.locationFlow()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                // New location! Update the map
                setLocation(it)
            }
            .launchIn(lifecycleScope)*/

        addRepeatingJob(Lifecycle.State.STARTED) {
            mFusedLocationClient.locationFlow()
                .conflate()
                .catch { e ->
                    Log.d("TAG", "Unable to get location", e)
                }.collect {
                    setLocation(it)
                }
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
        GpsUtils(this).turnGPSOn {
            lifecycleScope.launch {
                getLastKnownLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
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
                        lifecycleScope.launch {
                            getLastKnownLocation()
                        }
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

    override fun onStart() {
        super.onStart()
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationMapView.onDestroy()
    }
}
