package com.example.xzx.coinz

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
//import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback

import com.example.xzx.coinz.R.id.mapView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonArray
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.light.Position
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 * Use the Location component to easily add a device location "puck" to a Mapbox map.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener{
    private val tag ="MapActivity"
    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    private var p: Point? = null

    private lateinit var geoJsonString:String
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this@MapActivity.mapboxMap = mapboxMap
        this@MapActivity.geoJsonString = data!!.getStringExtra("geostring")
        // Create an Icon object for the marker to use
        val source = GeoJsonSource("geojson", geoJsonString)
        mapboxMap!!.addSource(source)
        mapboxMap!!.addLayer(LineLayer("geojson", "geojson"))
        val fc = geoJsonString!!.let { FeatureCollection.fromJson(it) }
        val features = fc?.features()
        var icon:Icon?= null
        var color :String
        // f is a Feature. get f's coordinates :
        features?.let {
            for (f in it) {
                val j = f.properties() as JsonObject
                if (f.geometry() is Point) {
                    p = f.geometry() as Point
                     // Create an Icon object for the marker to use
                    color = j.get("marker-color").toString()
                    color = color.replace('\"',' ').trim()
                    when (color){
                        "#ff0000" -> icon = IconFactory.getInstance(this@MapActivity).fromResource(R.drawable.mapbox_marker_icon_default)
                        "#008000" -> icon = IconFactory.getInstance(this@MapActivity).fromResource(R.mipmap.marker_icon_green)
                        "#0000ff" -> icon = IconFactory.getInstance(this@MapActivity).fromResource(R.mipmap.marker_icon_blue)
                        "#ffdf00" -> icon = IconFactory.getInstance(this@MapActivity).fromResource(R.mipmap.marker_icon_yellow)
                    }
                    mapboxMap!!.addMarker(MarkerOptions()
                            .title(j.get("currency").toString())
                            .snippet(j.get("marker-symbol").toString())
                            .icon(icon)
                            .position(LatLng(p!!.latitude(), p!!.longitude())))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        // click the "coin" button ,if within 25 meters, the coins are collected
        CollectButton.setOnClickListener{
          var i:Intent = Intent(this,CollectCoinsActivity::class.java)
          i.putExtra("geoJsonString",geoJsonString)
          startActivity(i)
        }

    }

    // to show the user location, you need to update your Google Play to the latest version
    override fun onMapReady(mapboxMap: MapboxMap) {
        this@MapActivity.mapboxMap = mapboxMap
        enableLocationComponent()
        startActivityForResult<DownloadActivity>(1)
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            val options = LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                    .build()
            // Get an instance of the component
            val locationComponent = mapboxMap?.locationComponent
            // Activate with options
            locationComponent?.activateLocationComponent(this, options)
            // Enable to make component visible
            locationComponent?.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent?.cameraMode = CameraMode.TRACKING
            locationComponent?.renderMode = RenderMode.COMPASS
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

}