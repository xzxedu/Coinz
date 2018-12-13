package com.example.xzx.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback

import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import org.jetbrains.anko.startActivityForResult
import java.util.ArrayList

/**
 * Use the Location component to easily add a device location "puck" to a Mapbox map.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener{
    private val tag ="MapActivity"
    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    private var p: Point? = null
    var markerInit :Marker?= null
//    var markerList = listOf(markerInit)
    var markerList = arrayListOf(markerInit)

    private lateinit var geoJsonString:String

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
                    var marker = mapboxMap!!.addMarker(MarkerOptions()
                                          .title(j.get("currency").toString())
                                          .snippet(j.get("marker-symbol").toString())
                                          .icon(icon)
                                          .position(LatLng(p!!.latitude(), p!!.longitude())))
                    markerList .add(marker)
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

        var mToolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.CollectButton -> {
                //TODO ARRAYLIST OR LIST? PARCEBLE

                val i: Intent = Intent(this, CollectCoinsActivity::class.java)
//                i.putExtra("geoJsonString", geoJsonString)
//                i.putExtra("markerList",markerList)
                startActivity(i)
                return super.onOptionsItemSelected(item)
            }
            R.id.action_settings -> {
                //TODO: intent startactivity to settings
                return super.onOptionsItemSelected(item)
            }
            R.id.Wallet -> {
                var i:Intent = Intent(this,WalletActivity::class.java)
                i.putExtra("geoJsonString", geoJsonString)
                startActivity(i)
                return super.onOptionsItemSelected(item)
            }

            R.id.backgroundMode -> {
                //TODO: intent startactivity to backgroundMode
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
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