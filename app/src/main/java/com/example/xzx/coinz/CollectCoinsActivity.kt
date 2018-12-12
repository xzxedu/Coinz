package com.example.xzx.coinz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import org.jetbrains.anko.toast



class CollectCoinsActivity: AppCompatActivity(){

    private var mAuth: FirebaseAuth? = null
    private var  UserCollectedCoins: DocumentReference? = null
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private val TAG = "CollectCoinsActivity"
    private val COLLECTION_KEY=currentUserDocRef.id
    private var DOCUMENT_KEY:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        // check location service permission
        if (ActivityCompat.checkSelfPermission(this, Manifest
                        .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var task = fusedLocationProviderClient.getLastLocation()
        task.addOnSuccessListener { currentlocation: Location? ->
            // Got last known location. In some rare situations this can be null.
            if (currentlocation != null) {
                var ifCollect: Boolean = false
                val intent: Intent = getIntent()
                val geoJson: String = intent.getStringExtra("geoJsonString")
                val fc = geoJson.let { FeatureCollection.fromJson(it) }
                val features = fc?.features()
                var PointNum = 0
                features?.let {
                    for (f in it) {
                        val j = f.properties() as JsonObject
                        if (f.geometry() is Point) {
                            val p = f.geometry() as Point
                            val temp = Location(LocationManager.GPS_PROVIDER)
                            temp.setLatitude(p.latitude())
                            temp.setLongitude(p.longitude())
                            val distance = currentlocation.distanceTo(temp)
                            if (distance <= 25.00 ){
                                ifCollect = true
                                PointNum += 1
                                updateCoinsFirestore(f,PointNum)
                                break
                            }
                        }
                    }
                    if (ifCollect == false)
                     Toast.makeText(this,"over 25 meters away! Collection fails! ",Toast.LENGTH_LONG).show()
                }
            }

        }
    }


    private fun updateCoinsFirestore(f: Feature,i:Int) {
        val j = f.properties() as JsonObject
        DOCUMENT_KEY = j.get("id").toString().replace('\"',' ').trim()
        var documentKey = DOCUMENT_KEY!!
        UserCollectedCoins = firestore?.collection(COLLECTION_KEY)?.document(documentKey)
        // record their id
        var PositionInf = mapOf(
                "id" to (j.get("id").toString()),
                "value" to (j.get("value").toString()),
                "currency" to (j.get("currency").toString())
        )
        // send the coins and listen for success or failure
        if (PositionInf!=null){
            var toastString = "Collect Coins Successfully! Rest Coins:" + (50-i).toString()
            UserCollectedCoins!!.set(PositionInf)
                    .addOnSuccessListener { Toast.makeText(this,
                            toastString,Toast.LENGTH_LONG).show()
                    } // anko
                    .addOnFailureListener { e -> Log.e(TAG, e.message) }
        }

    }

}