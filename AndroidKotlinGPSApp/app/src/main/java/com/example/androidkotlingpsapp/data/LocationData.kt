package com.example.androidkotlingpsapp.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

data class LocationData(var latitutde:Double,
                        var longitude:Double,
                        @get:Exclude
                        var id:String?=null):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitutde)
        parcel.writeDouble(longitude)
    }

    override fun hashCode(): Int {
        var result = latitutde?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationData> {
        override fun createFromParcel(parcel: Parcel): LocationData {
            return LocationData(parcel)
        }

        override fun newArray(size: Int): Array<LocationData?> {
            return arrayOfNulls(size)
        }
    }
}