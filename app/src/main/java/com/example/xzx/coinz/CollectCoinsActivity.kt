package com.example.xzx.coinz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.R.id.email
import com.firebase.ui.auth.R.id.password
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
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class CollectCoinsActivity: AppCompatActivity(){

    private var mAuth: FirebaseAuth? = null
    private var firestoreCoinz: DocumentReference? = null
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private val DOCUMENT_KEY= currentUserDocRef.id
    private var DocumentData= mutableMapOf<String, Any?>()
    private var DolrNum:Float = 0f
    private var QuidNum:Float = 0f
    private var ShilNum:Float = 0f
    private var PenyNum:Float = 0f

    companion object
    {
        private const val TAG = "CollectCoinsActivity"
        private const val COLLECTION_KEY="users"
        private val DOLR_FIELD = "DOLR"
        private val QUID_FIELD = "QUID"
        private val SHIL_FIELD = "SHIL"
        private val PENY_FIELD = "PENY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings
        firestoreCoinz = firestore?.collection(COLLECTION_KEY)?.document(DOCUMENT_KEY)
        getCurrentUser(currentUserDocRef)
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
                                updateCoinsFirestore(f)
                                break
                            }
                        }
                    }
                    if (ifCollect == false)
                     toast("over 25 meters away! Collection fails! ")
                }
            }

        }
    }

    fun getCurrentUser(currentUserDocRef: DocumentReference) {
        currentUserDocRef.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    DocumentData = document.data!!
                    Log.d("wallet", "DocumentSnapshot data: " + DocumentData)
                    DolrNum = DocumentData.get("DOLR").toString().toFloat()
                    ShilNum = DocumentData.get("SHIL").toString().toFloat()
                    PenyNum = DocumentData.get("PENY").toString().toFloat()
                    QuidNum = DocumentData.get("QUID").toString().toFloat()
                } else {
                    toast("No such document")
                }
            } else {
                Log.d("wallet", "get failed with ", task.exception)
            }
        })

    }

    private fun updateCoinsFirestore(f: Feature) {
        val j = f.properties() as JsonObject
        var currency :Map<String,String> ?= null
        var acquireValue = j.get("value").toString().replace('\"',' ').trim().toFloat()
        when(j.get("currency").toString().replace('\"',' ').trim()){
            "DOLR" ->{ currency = mapOf(DOLR_FIELD to (acquireValue.plus(DolrNum)).toString())}
            "SHIL" ->{ currency = mapOf(SHIL_FIELD to (acquireValue.plus(ShilNum)).toString())}
            "PENY" ->{ currency = mapOf(PENY_FIELD to  (acquireValue.plus(PenyNum)).toString())}
            "QUID" ->{ currency = mapOf(QUID_FIELD to  (acquireValue.plus(QuidNum)).toString())}
        }
        // send the coins and listen for success or failure
        if (currency !=  null){
            firestoreCoinz!!.update(currency)
                    .addOnSuccessListener { toast("Collect Coins Successfully!") } // anko
                    .addOnFailureListener { e -> Log.e(TAG, e.message) }
        }
    }

}