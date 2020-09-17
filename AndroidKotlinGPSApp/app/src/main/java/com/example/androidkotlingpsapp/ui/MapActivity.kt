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
    //permission request can be of any constant
    private val PERMISSION_REQUEST = 100

    //declare fused location, the location api that intelligently combines signal and provides user information
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //define request as location request which will be used for setting location request based on interval, priority
    private lateinit var request: LocationRequest

    //define database as database reference
    private lateinit var database:DatabaseReference

    //define locationCallback as LocationCallback which will be used to retrieve notification of the changes of the device location whether it has changed
    //or cannot be determined
    private lateinit var locationCallback:LocationCallback


    //define the currentUsername as string
    private lateinit var currentUserName:String
    //define partnerName as string
    private lateinit var partnerName:String

    //define map as GoogleMap
    private lateinit var map:GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        //initialize locationCallback
        locationCallback= LocationCallback()

        //initialize fusedLocationClient by using the LocationServices FusedLocationProvider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        //get the map fragment by referencing the id from the google provided map fragment xml
        val mapFragment=supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //initialise the database to hold Firebase  database reference
        database= Firebase.database.reference

        //retrive the partner and currentUser name from the intent
        partnerName= intent.getStringExtra("partnerName").toString()
        currentUserName= intent.getStringExtra("currentUserName").toString()

        //0. point reference to the partner name, adding a value event listener to trigger and get his location when value has changed
        database.child(partnerName).addValueEventListener(getLocationFromPartner())

    }

    //0. when the app is created display the users location on the map, pointing the latestLocation and listening for value change
    //from the databse snapshot
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
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(teamLoc, 20f))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
    }


    //1. when the map is ready and not null, initialize map to googleMap and requestPermission
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map=googleMap
            requestPermission()
        }
    }

    //1a. request permission from the user, in other to access location
    private fun requestPermission(){
        //Check whether this app has access to the location permission//
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //If the location permission has been granted, then start receiving location updates //
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Permission granted",Toast.LENGTH_LONG).show()
            //a1.
            requestLocationUpdates()
            //a2.
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

    //1b. based on the permission, check the result wether to proceed, or finish
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

    //1a1 recieve current user location with intervals and display on the map
    private fun requestLocationUpdates() {

        request = LocationRequest()
        /**interval for receiving location updates**/
        request.interval = 1000
        /**shortest interval for receiving location callBack**/
        request.fastestInterval = 5000

        //et user location using high accurate settings
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //set location call back to receive updates to location change
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val location = locationResult.lastLocation
                val latitude = location.latitude
                val longitude = location.longitude
                map.clear()
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
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLoc, 20f))
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

        //save the settings using fusedLocationClient, specifying the unit process to take place in the System
        fusedLocationClient.requestLocationUpdates(request,locationCallback, Looper.myLooper())
    }


    //1a2 update the location
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
