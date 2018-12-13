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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.mapbox.geojson.FeatureCollection
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DownloadActivity : AppCompatActivity(),DownloadCompleteListener {

    private val tag = "DownloadActivity" // for storing preferences
    var geoJsonString:String?= null

    //    private var networkChangeReceiver:NetworkChangeReceiver?=null
    private var progressbar: ProgressBar?=null
    private var textview: TextView?=null
    private var networkChangeReceiver:NetworkChangeReceiver?=null

    override fun downloadComplete(result: String) {
        geoJsonString = result
        val sharedPref = getSharedPreferences("Downloadmap",Context.MODE_PRIVATE)
        with (sharedPref.edit()){
            if (sharedPref.contains("geoJsonString")){
                remove("geoJsonString")
            }
            putString("geoJsonString",geoJsonString)
            commit()

        }
        //save today's currency in the shared Preference
        val geoJsonString: String = getIntent().getStringExtra("geoJsonString")
        // upload rates to the firecloud
        var jsonObject = JSONTokener(geoJsonString).nextValue() as JSONObject
        val rates = jsonObject.getJSONObject("rates")
        with (sharedPref.edit()){
               putFloat("DOLR", rates.get("DOLR").toString().toFloat())
               putFloat("SHIL", rates.get("SHIL").toString().toFloat())
               putFloat("QUID",rates.get("QUID").toString().toFloat())
               putFloat("PENY" , rates.get("PENY").toString().toFloat())
            commit()
        }
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
                val sharedPref = getSharedPreferences("Downloadmap",Context.MODE_PRIVATE)
                // Restore preferences
                var downloadDate =sharedPref.getString("lastDownloadDate", "")
                // if this is today first time downloading from URL else read SharedPreferences.
                progressbar!!.max = 100
                val currentDate= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                if (downloadDate.equals(currentDate)){
                    if ((sharedPref.contains("geoJsonString")) == false){
                        Log.d("downloadingFail!!!!",sharedPref.toString())
                    }
                    geoJsonString = getSharedPreferences("Downloadmap",Context.MODE_PRIVATE).
                                                             getString("geoJsonString","")
                    var intent = Intent() // Obtain values from previous activity
                    intent.putExtra("geostring", geoJsonString)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else {
                    var DateUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson"
                    Log.i(tag, DateUrl)
                    DownloadTask(this, progressbar!!, textview!!,
                            this).execute(DateUrl)
                    }
            } catch (e: IOException) {
                progressbar?.snackbar("download failed")
            }
        }else
            progressbar?.snackbar("try again.")
    }

    override fun onStop() {
        super.onStop()
        // All objects are from android.context.Context
        val settings = getSharedPreferences("Downloadmap", Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
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
            var recycler_view_time = find<RecyclerView>(R.id.recycler_view_timeline)
            if (networkInfo == null || !networkInfo.isAvailable) {
                recycler_view_time.longSnackbar("network unavailable, please check your network")
            }
        }
    }
}




