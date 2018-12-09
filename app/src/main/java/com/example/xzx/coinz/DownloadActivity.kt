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
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import java.io.IOException
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
                val currentDate= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                var DateUrl= "http://homepages.inf.ed.ac.uk/stg/coinz/"+currentDate +"/coinzmap.geojson"
                Log.i(tag,DateUrl)
                DownloadTask(this,progressbar!!, textview!!,
                              this).execute(DateUrl)

            } catch (e: IOException) {
                progressbar?.snackbar("download failed")
            }
        }else
            progressbar?.snackbar("try again.")

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
//        // required to check if you have downloaded todays map before
//        downloadDate = settings.getString("lastDownloadDate", "")
//        // Write a message to ”logcat” (for debugging purposes)
//        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")
    }

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
            var recycler_view_time = find<RecyclerView>(R.id.recycler_view_timeline)
            if (networkInfo == null || !networkInfo.isAvailable) {
                recycler_view_time.longSnackbar("network unavailable, please check your network")
            }
        }
    }
}




