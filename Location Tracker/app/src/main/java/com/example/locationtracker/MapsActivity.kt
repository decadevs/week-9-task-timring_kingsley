package com.example.locationtracker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var partnerName: String
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        partnerName = intent.getStringExtra("Partner")!!

        //GET PARTNER'S LOCATION
        val firebaseDatabase = FirebaseDatabase.getInstance().getReference()
        firebaseDatabase.child(partnerName).addValueEventListener(getPartnerLocation())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationPermission()
    }

    //GET LOCATION PERMISSION
    private fun getLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates()
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                map.isMyLocationEnabled = true
            }
            else {
                Toast.makeText(this, "location access permission has not been granted!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun getLocationUpdates(){
        //GET CURRENT USER
        currentUser = intent.getStringExtra("currentUser")!!

        //SET INTERVAL OF RECEIVING LOCATION UPDATE
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        /** GET CURRENT LOCATION AND SEND TO FIREBASE DATABASE */
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                val latitude = location.latitude
                val longitude = location.longitude
                val firebaseDatabase = FirebaseDatabase.getInstance().getReference(currentUser)
                val bitmaps: Bitmap? = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources,R.drawable.tim_marker), 200, 200, true)

                if (location != null) {
                    /** SEND CURRENT LOCATION TO FIREBASE */
                    firebaseDatabase.child("Latitude").setValue(latitude)
                    firebaseDatabase.child("Longitude").setValue(longitude)

                    /** SET CURRENT LOCATION ON MAP */
                    val myCoordinates = LatLng(latitude, longitude)
                    map.apply {
                        addMarker(MarkerOptions().position(myCoordinates).title(currentUser))
                            .setIcon(BitmapDescriptorFactory.fromBitmap(bitmaps))
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, 18f))
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED ) {
            getLocationPermission()
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
        }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun getPartnerLocation(): ValueEventListener {
        //GET PARTNER NAME
        partnerName = intent.getStringExtra("Partner")!!

        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.clear()

                if(snapshot.exists()) {
                    /** GET AND SET COORDINATES */
                    val partnerLatitude = snapshot.child("Latitude").value as Double
                    val partnerLongitude = snapshot.child("Longitude").value as Double
                    val partnerCoordinate = LatLng(partnerLatitude, partnerLongitude)
                    val bitmaps: Bitmap? = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources,R.drawable.kingsley_marker), 200, 200, true)

                    Toast.makeText(this@MapsActivity, "Searching for $partnerName...", Toast.LENGTH_LONG).show()

                    /** SET CUSTOM MARKER ON MAP */
                    map.apply {
                        addMarker(MarkerOptions()
                            .position(partnerCoordinate)
                            .title(partnerName))
                            .setIcon(BitmapDescriptorFactory.fromBitmap(bitmaps))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(partnerCoordinate, 18f))
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "No coordinates found for $partnerName", Toast.LENGTH_LONG).show()
//                    val firebaseDatabase = FirebaseDatabase.getInstance().getReference()
//                    firebaseDatabase.child(partnerName).addValueEventListener(getPartnerLocation())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        return locationListener
    }

}