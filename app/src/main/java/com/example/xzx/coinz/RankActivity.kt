package com.example.xzx.coinz

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.xzx.coinz.model.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_rank.*

class RankActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var  UserCollectedCoins: DocumentReference? = null
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private val COLLECTION_KEY=currentUserDocRef.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)
        setSupportActionBar(toolbar)

        var RankList = arrayListOf<Float>()
        var GoldCoins: Float ?= null
        firestore.collection("BANK Account").get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        GoldCoins  =document.data.get("goldCoins").toString().toFloat()
                        Log.d("CoinsNUM!!",GoldCoins.toString())
                        RankList.add (GoldCoins!!)
                        Log.d("RANK list",RankList.toString())
                    }
        }
        RankList.sortDescending()



        // sort up each user's GOLD coins

    }

}
