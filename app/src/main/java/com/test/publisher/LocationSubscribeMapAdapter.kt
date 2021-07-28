package com.test.publisher

import android.app.Activity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

/**
 * @Author: Shubham Rimjha
 * @Date: 25-07-2021
 */
class LocationSubscribeMapAdapter(private val activity: Activity, private val map: GoogleMap) {

    private var marker: Marker? = null

    fun locationUpdated(newLocation: LinkedHashMap<*, *>?) {
        if (newLocation?.containsKey("lat") == true && newLocation.containsKey("lng")) {
            val lat = newLocation["lat"] as String
            val lng = newLocation["lng"] as String
            doUiUpdate(
                LatLng(
                    lat.toDouble(),
                    lng.toDouble()
                )
            )
        } else {
            Log.w(
                TAG,
                "message ignored: $newLocation"
            )
        }
    }

    private fun doUiUpdate(location: LatLng) {

        activity.runOnUiThread {
            if (marker != null) {
                marker!!.position = location
            } else {
                marker = map.addMarker(
                    MarkerOptions().position(location).anchor(0.5f, 1f)
                )
            }
            map.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }

    companion object {
        private val TAG = LocationSubscribeMapAdapter::class.java.name
    }

}
