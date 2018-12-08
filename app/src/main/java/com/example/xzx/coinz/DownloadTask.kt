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

public class DownloadTask(val activity : Activity,val progressbar: ProgressBar,val textview: TextView,
                                 private val caller : DownloadCompleteListener): AsyncTask<String, Double, String>()
{ //    private val root = Environment.getExternalStorageDirectory()

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
        var i = 0
        while (line != null) {
            sb.append(line)
            line = br.readLine()
            i += 1
            publishProgress(i.toDouble())
        }
        br.close()

        return sb.toString()
        // The directory for files to be downloaded
//        val mapdir = File(root.absolutePath,  "maplist")
//
//        FileUtils.copyURLToFile(URL(sb.toString()), File(mapdir.absolutePath))
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

    override fun onProgressUpdate(vararg values: Double?) {
        val progress = (values[0]!! * 100).toInt()
        progressbar.progress = progress
        textview.text = "downloading: ${progress} "
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        progressbar.progress = 100
        textview.text = "complete!"
        caller.downloadComplete(result)
        // Finish UpdateSongActivity and go back to MapActivity
        activity.finish()
    }
}

