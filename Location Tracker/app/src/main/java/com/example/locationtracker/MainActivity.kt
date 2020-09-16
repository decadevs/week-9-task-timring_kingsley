package com.example.locationtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            //GET CURRENT USER AND PARTNER NAMES
            val currentUser = currentUser.text.toString()
            val partnerName = partnerName.text.toString()

            //ASK FOR LOCATION PERMISSION AND SEND DATA TO FIREBASE
            if(currentUser.trim().isEmpty() || partnerName.trim().isEmpty()) {
                Toast.makeText(this, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                //GO TO MAP ACTIVITY
                val intent = Intent(this, MapsActivity::class.java)
                    .putExtra("Partner", partnerName)
                    .putExtra("currentUser", currentUser)
                startActivity(intent)
            }
        }
    }
}