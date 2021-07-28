package com.test.publisher

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNReconnectionPolicy
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {

    private var executorService: ScheduledExecutorService? = null
    private var startTime: Long? = null

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var pubnub: PubNub

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById(R.id.btn)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_sub) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val pnConfiguration = PNConfiguration().apply {
            subscribeKey = Constants.PUBNUB_SUBSCRIBE_KEY
            publishKey = Constants.PUBNUB_PUBLISH_KEY
            secure = true
            uuid = Constants.DEMO_USER
            reconnectionPolicy = PNReconnectionPolicy.LINEAR
            maximumReconnectionRetries = 5
        }
        pubnub = PubNub(pnConfiguration)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        btn.setOnClickListener {
            if (hasPermission()) {
                scheduleLocationPublish()
            } else {
                Toast.makeText(this, "Please give location access to view this", Toast.LENGTH_SHORT)
                    .show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }

    private fun getNewLocationMessage(
        lat: Double,
        lng: Double,
        userName: String = Constants.DEMO_USER
    ): Map<String, String> {
        return mapOf("who" to userName, "lat" to lat.toString(), "lng" to lng.toString())
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        pubnub.addListener(
            LocationPublishCallback(
                LocationSubscribeMapAdapter(
                    this,
                    map
                ), Constants.SUBSCRIBE_CHANNEL_NAME
            )
        )

        if (hasPermission()) {
            scheduleLocationPublish()
        } else {
            Toast.makeText(this, "Please give location access to view this", Toast.LENGTH_SHORT)
                .show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        pubnub.subscribe(
            channels = (listOf(Constants.SUBSCRIBE_CHANNEL_NAME))
        )
    }

    @SuppressLint("MissingPermission")
    private fun scheduleLocationPublish() {

        executorService = Executors.newSingleThreadScheduledExecutor()
        startTime = System.currentTimeMillis()
        executorService?.scheduleAtFixedRate({
            (runOnUiThread {

                Log.i("Publishing", "xoxo")
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    val message: Map<String, String> = getNewLocationMessage(
                        it.latitude,
                        it.longitude
                    )

                    Log.i("Publish", "Message: $message")

                    pubnub.publish(
                        channel = Constants.SUBSCRIBE_CHANNEL_NAME,
                        message = message
                    ).async { result, status ->
                        if (status.error)
                            Log.i("Publish", "Error: ${status.error}")
                        else
                            Log.i("Publish", "Result: ${result!!.timetoken}")
                    }
                }.addOnFailureListener {
                    Log.i(this.localClassName, "scheduleLocationPublish: $it")
                }
            })
        }, 0, 5, TimeUnit.SECONDS)
    }


    private fun hasPermission() = (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)

    override fun onLocationChanged(location: Location) {}

}
