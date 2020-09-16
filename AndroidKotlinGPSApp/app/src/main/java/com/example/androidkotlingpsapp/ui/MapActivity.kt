package com.example.androidkotlingpsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidkotlingpsapp.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback{
    private val PERMISSION_REQUEST = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var request: LocationRequest
    private lateinit var database:DatabaseReference
    private lateinit var currentUserName:String
    private lateinit var locationCallback:LocationCallback
    private lateinit var partnerName:String

    private lateinit var map:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // ...
        locationCallback= LocationCallback()
        //initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment=supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //get my other partner location from the database and display in map
        database= Firebase.database.reference
        partnerName= intent.getStringExtra("partnerName").toString()
        currentUserName= intent.getStringExtra("currentUserName").toString()
        if (partnerName != null) {
            database.child(partnerName).addValueEventListener(getLocationFromPartner())
        database.child("")
        }

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        //check that i have requested permisioon to get my location tracked and saved to the database
        if (googleMap != null) {
            map=googleMap
            requestPermission()
        }
    }

    private fun getLocationFromPartner():ValueEventListener{
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                map?.clear()
                // Get Post object and use the values to update the UI
                if(dataSnapshot.exists()) {
                    val locationLat = dataSnapshot.child("Latitude").value
                    val locationLong = dataSnapshot.child("Longitude").value
                    val teamLat: Double = locationLat as Double
                    val teamLong:Double= locationLong as Double
                    if(teamLat!=null && teamLong!=null) {
                        val teamLoc = LatLng(teamLat, teamLong)
                        val bitmaps: Bitmap? = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources,R.drawable.tim_marker), 200, 200, true)
                        map?.apply {
                            addMarker(
                                MarkerOptions()
                                    .position(teamLoc)
                                    .title("$partnerName")
                            )
                                .setIcon(BitmapDescriptorFactory.fromBitmap(bitmaps))
                        }
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(teamLoc, 18f))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun requestPermission(){
            //Check whether this app has access to the location permission//
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            //If the location permission has been granted, then start the TrackerService//
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission granted",Toast.LENGTH_LONG).show()
                requestLocationUpdates()
                startLocationUpdates()
            } else {

                //If the app doesn’t currently have access to the user’s location, then request access//
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST
                );
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==PERMISSION_REQUEST){
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                map.isMyLocationEnabled=true
            }
            else{
                Toast.makeText(this,"User did not grant location access permission",Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun requestLocationUpdates() {
        request = LocationRequest()
        /**interval for receiving location updates**/
        request.interval = 10000
        /**shortest interval for receiving location callBack**/
        request.fastestInterval = 10000
        //Get the most accurate location data available//
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            //...then request location updates//
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                val latitude = location.latitude
                val longitude = location.longitude
                val firebaseDatabase = FirebaseDatabase.getInstance().getReference("$currentUserName")
                if (location != null) {
                    //SEND CURRENT LOCATION TO FIREBASE
                    firebaseDatabase.child("Latitude").setValue(latitude)
                    firebaseDatabase.child("Longitude").setValue(longitude)
                    val currentUserLoc=LatLng(latitude,longitude)

                    val bitmaps: Bitmap? = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources,R.drawable.kingsley_marker), 200, 200, true)
                    map?.apply {
                        addMarker(
                            MarkerOptions()
                                .position(currentUserLoc)
                                .title("$currentUserName")
                        )
                            .setIcon(BitmapDescriptorFactory.fromBitmap(bitmaps))
                    }
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLoc, 18f))
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }
        fusedLocationClient.requestLocationUpdates(request,locationCallback, Looper.myLooper())
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            null
        )
    }
}
