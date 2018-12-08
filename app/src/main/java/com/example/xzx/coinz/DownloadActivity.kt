package com.example.xzx.coinz

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.alert
import java.io.BufferedReader
//import org.w3c.dom.Text
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DownloadActivity : AppCompatActivity(),DownloadCompleteListener {

    private val tag = "DownloadActivity"
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    var geoJsonString:String?= null

//    private var networkChangeReceiver:NetworkChangeReceiver?=null
    private var progressbar: ProgressBar?=null
    private var textview: TextView?=null
    private var networkChangeReceiver:NetworkChangeReceiver?=null


    override fun downloadComplete(result: String) {
        geoJsonString = result
        //Log.i(tag,geoJsonString)
        var intent = Intent() // Obtain values from previous activity
        intent.putExtra("geostring", geoJsonString)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        // Network monitoring
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        networkChangeReceiver =NetworkChangeReceiver()
        registerReceiver(networkChangeReceiver, intentFilter)

        // Initialize widgets
        progressbar = find(R.id.progressBar1)
        textview=find(R.id.textview_progress)
        val myToolBar = find<Toolbar>(R.id.toolbar_download_map)
        myToolBar.navigationIcon=resources.getDrawable(R.mipmap.back)
        myToolBar.setOnClickListener{
            finish()
        }

    }

    override fun onStart() {
        super.onStart()

        if(isExternalStorageWritable()) {
            try {
                // Start downloading
                progressbar!!.max = 100
                var DateUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/2018/01/01/coinzmap.geojson"
                Log.i(tag,"startdownloadmap")
                DownloadTask(this,progressbar!!, textview!!,
                              this).execute(DateUrl)

            } catch (e: IOException) {
                snackbar(find(R.id.progressBar1), "download failed")
            }
        }else
            snackbar(find(R.id.progressBar1), "try again.")

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
//        // required to check if you have downloaded todays map before
//        downloadDate = settings.getString("lastDownloadDate", "")
//        // Write a message to ”logcat” (for debugging purposes)
//        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")
    }

//    override fun onDestroy() {
//        super.onDestroy()
////        unregisterReceiver(networkChangeReceiver)
//    }

    override fun onStop() {
        super.onStop()
        //Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        //editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun isExternalStorageWritable():Boolean=
            Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    private inner class NetworkChangeReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo == null || !networkInfo.isAvailable) {
                longSnackbar(find<RecyclerView>(R.id.recycler_view_timeline), "network unavailable, please check your network")
            }
        }
    }
}




