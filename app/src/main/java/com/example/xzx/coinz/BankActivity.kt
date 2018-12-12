package com.example.xzx.coinz

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.xzx.coinz.R
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_bank.*
import kotlinx.android.synthetic.main.activity_wallet.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import org.json.JSONTokener

class BankActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}
    private var ratesData= mutableMapOf<String, Any?>()
    private lateinit var PENY_rate:String
    private lateinit var DOLR_rate:String
    private lateinit var SHIL_rate:String
    private lateinit var QUID_rate:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)
        setSupportActionBar(toolbar_bank)
        val geoJsonString: String = getIntent().getStringExtra("geoJsonString")
        // upload rates to the firecloud
        var jsonObject = JSONTokener(geoJsonString).nextValue() as JSONObject
        val rates = jsonObject.getJSONObject("rates")
        Log.d("rates!!!",rates.toString())
        val rate = mapOf(
            "DOLR" to rates.get("DOLR"),
            "SHIL" to rates.get("SHIL"),
            "QUID" to rates.get("QUID"),
            "PENY" to rates.get("PENY")
        )
        firestore.collection("rates").document("currency").set(rates)
    }

//
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

//    fun getCurrentRates(){
//        val currentRatesDoc =  firestore
//                .collection("rates").document("currency")
//        currentRatesDoc.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
//            if (task.isSuccessful) {
//                val document = task.result
//                if (document != null) {
//                    ratesData = document.data!!
//                    PENY_rate = ratesData.get("PENY").toString()
//                    DOLR_rate = ratesData.get("DOLR").toString()
//                    QUID_rate = ratesData.get("QUID").toString()
//                    SHIL_rate = ratesData.get("SHIL").toString()
//                    val DOLR_Rate: TextView = findViewById(R.id.DOLR_Rate)
//                    val PENY_Rate: TextView = findViewById(R.id.PENY_Rate)
//                    val SHIL_Rate: TextView = findViewById(R.id.SHIL_Rate)
//                    val QUID_Rate: TextView = findViewById(R.id.QUID_Rate)
//                    DOLR_Rate.setText(PENY_rate)
//                    PENY_Rate.setText(DOLR_rate)
//                    QUID_Rate.setText(QUID_rate )
//                    SHIL_Rate.setText(SHIL_rate)
//                } else {
//                    toast("No such document")
//                }
//            } else {
//                Log.d("wallet", "get failed with ", task.exception)
//            }
//        })
//
//    }

}
