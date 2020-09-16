package com.example.androidkotlingpsapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidkotlingpsapp.data.LOCATION_NODE
import com.google.firebase.database.FirebaseDatabase


class LocationViewModel: ViewModel(){
    private val dbLocation=FirebaseDatabase.getInstance().getReference(LOCATION_NODE)


}