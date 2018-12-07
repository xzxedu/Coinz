package com.example.xzx.coinz

import android.app.Activity
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.net.URL
import org.jetbrains.anko.*
import java.io.*
import java.net.HttpURLConnection

/**
 * This class extends from AsyncTask and downloads new songs and lyrics on the server asynchronously
 *
 * @param activity the activity calling this class
 * @param progressbar shows the progress of downloading
 * @param textview shows the progress of downloading
 *
 */
class DownloadTask(val activity : Activity,val progressbar: ProgressBar,val textview: TextView,
                   private val caller : DownloadCompleteListener): AsyncTask<String,Double,String>() {

    /**
     * Format all Int numbers under 10 to the String with "0i" type
     *
     * @param i Int number
     */
    private fun formatNum(i : Int): String =
            if(i<10)
                "0"+i.toString()
            else
                i.toString()

    override fun doInBackground(vararg urls: String): String = try {
        loadFileFromNetwork(urls[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection"
    }

    private fun loadFileFromNetwork(urlString: String): String {
        val stream: InputStream = downloadUrl(urlString)
        // Read input from stream, build result as a string
        val sb = StringBuilder()
        var line: String?
        val br = BufferedReader(InputStreamReader(stream))
        line = br.readLine()

        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        br.close()
        DownloadCompleteRunner.result = sb.toString()
        return DownloadCompleteRunner.result!!
    }

    // Given a string representation of a URL, sets up a connection and gets an input stream.
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        // Also available: HttpsURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect() // Starts the query
        return conn.inputStream
    }



    override fun onPreExecute() {
        progressbar.visibility = View.VISIBLE
        progressbar.progress = 0
        textview.text = "downloading "
    }

//    override fun doInBackground(vararg params: Int?): Int {
//            try {
//                // The directory for files to be downloaded
//                val songdir = File(root.absolutePath,  "songlist")
//
//                // Download the fourth and fifth version of maps not in local storage
//                for (i in current+1..newest) {
//                    val mapurl = url
//                    FileUtils.copyURLToFile(URL(mapurl), File(songdir.absolutePath, formatNum(i) + "/" + "map5.kml"))
//                    publishProgress(i.toDouble(), i.toDouble())
//                }
//            }catch (exception: IOException){
//                exception.printStackTrace()
//            }
//        return 1
//    }


    override fun onProgressUpdate(vararg values: Double?) {
        val progress = (values[0]!!*100).toInt()
        progressbar.progress = progress
        textview.text="downloading: ${values[1]!!.toInt()} "
    }

    override fun onPostExecute(result: String) {
        progressbar.progress = 100
        textview.text="complete!"
        // Finish UpdateSongActivity and go back to MapActivity
        activity.finish()
    }
}
//    override fun onPostExecute(result: String) {
//        super.onPostExecute(result)
//        caller.downloadComplete(result)
//    }

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}
object DownloadCompleteRunner : DownloadCompleteListener {
    var result: String? = null
    override fun downloadComplete(result: String) {
        // TODO: do whatever you want
        this.result = result
    }
}