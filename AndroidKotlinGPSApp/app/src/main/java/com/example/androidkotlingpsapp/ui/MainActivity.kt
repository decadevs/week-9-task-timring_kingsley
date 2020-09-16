package com.example.androidkotlingpsapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidkotlingpsapp.R
import kotlinx.android.synthetic.main.activity_main.*




class MainActivity : AppCompatActivity() {
    lateinit var currentUser: String
    lateinit var partnerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnMap.setOnClickListener {
            //GET CURRENT USER AND PARTNER NAMES
            currentUser = etCurrentUserName.text.toString()
            partnerName = etPartnerName.text.toString()
            Toast.makeText(this,"$currentUser",Toast.LENGTH_LONG).show()
            //GO TO MAP ACTIVITY
            val intent = Intent(this, MapActivity::class.java)
                .putExtra("partnerName", partnerName)
                .putExtra("currentUserName", currentUser)
            startActivity(intent)
        }
    }


}