package com.example.xzx.coinz

import android.content.Context
import android.content.Intent
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
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import org.json.JSONTokener

class BankActivity : AppCompatActivity() {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)
        setSupportActionBar(toolbar_bank)
        val sharedPref = getSharedPreferences("Downloadmap", Context.MODE_PRIVATE)
        var peny = sharedPref.getFloat("PENY",1f)
        var dolr = sharedPref.getFloat("DOLR",1f)
        var shil = sharedPref.getFloat("SHIL",1f)
        var quid = sharedPref.getFloat("QUID",1f)
        var dolrRate=findViewById<TextView>(R.id.DOLR_Rate)
        dolrRate.setText(dolr.toString())
        var penyRate=findViewById<TextView>(R.id.PENY_Rate)
        penyRate.setText(peny.toString())
        var shilRate=findViewById<TextView>(R.id.SHIL_Rate)
        shilRate.setText(shil.toString())
        var quidRate=findViewById<TextView>(R.id.QUID_Rate)
        quidRate.setText(quid.toString())
        var userId = findViewById<TextView>(R.id.myuserID)
        userId.setText(FirebaseAuth.getInstance().currentUser?.uid)
        var goldCoins = findViewById<TextView>(R.id.mygoldCoins)
        firestoreInstance.collection("BANK Account").document(currentUserDocRef.id)
                .get()
                .addOnCompleteListener({task ->
                    if(task.isSuccessful){
                        val document = task.result
                        goldCoins.setText(document?.data?.get("goldCoins").toString())
                    }
                })
        bankButton.setOnClickListener {
            var i= Intent(this,WalletActivity::class.java)
            startActivity(i)
        }
    }
}
