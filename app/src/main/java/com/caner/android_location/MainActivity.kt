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
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

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

    override fun onCameraIdle() {
        val lat = mMap.cameraPosition.target.latitude
        val lng = mMap.cameraPosition.target.longitude

        if (lat != 0.0 && lng != 0.0) {
            centerPos = LatLng(lat, lng)
        }
    }

    private fun addGpsListener() {
        GpsUtils(this).turnGPSOn {
            getLocation()
        }
    }

    private fun getLocation() {
        try {
            mFusedLocationClient.awaitLastLocation { location ->
                location?.let {
                    setLocation(it)
                } ?: mFusedLocationClient.locationFlow { currentLocation ->
                    currentLocation?.let { setLocation(currentLocation) }
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "Unable to get location", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
                        getLocation()
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
