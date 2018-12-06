package com.example.xzx.coinz

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.xzx.coinz.R

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
import com.example.xzx.coinz.R.string.access_token
import com.google.gson.JsonArray
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.light.Position
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory


/**
 * Use the Location component to easily add a device location "puck" to a Mapbox map.
 */
class MapActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener  {

    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    private var p : Point ?= null
    private var geoJsonString: String?= null

    private val tag = "MapActivity"
    private var downloadDate = "" // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences

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
    }
    // to show the user location, you need to update your Google Play to the latest version
    override fun onMapReady(mapboxMap: MapboxMap) {
        this@MapActivity.mapboxMap = mapboxMap
        enableLocationComponent()
        // Create an Icon object for the marker to use
        val icon = IconFactory.getInstance(this@MapActivity).fromResource(R.mipmap.marker_icon_blue)

        //where geoJsonString is the string with your GeoJson data.
        // The method addLayer will generate and add a new LineLayer
        val source = GeoJsonSource("geojson", geoJsonString)
        mapboxMap.addSource(source)
        mapboxMap.addLayer(LineLayer("geojson", "geojson"))
        val fc = geoJsonString?.let { FeatureCollection.fromJson(it) }
        val features = fc?.features()
        var symbol: String
        // f is a Feature. get f's coordinates :
        features?.let{
            for (f in it) {
                val j = f.properties() as JsonObject
                symbol =j.get("marker-symbol") as String
                if (f.geometry() is Point) {
                    p = f.geometry() as Point
                    mapboxMap.addMarker(MarkerOptions()
                            //.position(LatLng(55.93863, -3.17603))
                            .title(getString(R.string.draw_marker_options_title))
                            .snippet(symbol)
                            .icon(icon)
                            .position(LatLng(p!!.latitude(),
                                    p!!.longitude())))
                }
            }
        }
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

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        // required to check if you have downloaded todays map before
        downloadDate = settings.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")

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

        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
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
//   //  a String with geojson data in it.
//   // Use those two methods to convert the content of a .geojson file at a specific URI to a String.
//    @Throws(Exception::class)
//    private fun convertStreamToString(`is`: InputStream):String {
//        val reader = BufferedReader(InputStreamReader(`is`))
//        val sb = StringBuilder()
//        val line:String ?= null
//        while ((line = reader.readLine()) != null)
//        {
//            sb.append(line).append("\n")
//        }
//        reader.close()
//        return sb.toString()
//    }
//    @Throws(Exception::class)
//    fun getStringFromFile(fileUri:Uri, context:Context):String {
//        val fin = context.getContentResolver().openInputStream(fileUri)
//        val ret = convertStreamToString(fin)
//        fin.close()
//        return ret
//    }
    // ******************************** download today's map *************************************//
