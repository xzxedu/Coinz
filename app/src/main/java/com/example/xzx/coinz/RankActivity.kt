package com.example.xzx.coinz

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.example.xzx.coinz.model.bankAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_rank.*

class RankActivity : AppCompatActivity() {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private val COLLECTION_KEY=currentUserDocRef.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)
        setSupportActionBar(toolbar_rank)

        var RankList = arrayListOf<bankAccount>()
        firestore.collection("BANK Account").get()
                .addOnSuccessListener { documents ->
                    var userCoin: bankAccount
                    for (document in documents){
                        if (document.exists()){
                            if (document.data.get("goldCoins") == null){
                                userCoin = bankAccount(document.id,0f)
                            }
                        else {
                                userCoin = bankAccount(document.id,document.data.get("goldCoins").toString().toFloat())
                            }
                        RankList.add (userCoin)
                        Log.d("RANK list",RankList.toString())}
                        else RankList.add(bankAccount("start your first game",0f))
                    }
        }
        // sort up each user's GOLD coins
        var ranklist = RankList.sortedWith(compareBy({it.goldCoins}))
        Log.d("ranklist!!",ranklist.toString())
        for (i in -1..-3) {
            when (i){
                -1 -> {
                    var firstid = findViewById<TextView>(R.id.firstid)
                    firstid.setText(ranklist[i].id)
                    Log.d("ranlist11",ranklist[i].id)
                    var firstCoins = findViewById<TextView>(R.id.firstCoins)
                    firstCoins.setText(ranklist[i].goldCoins.toString())}
                -2 -> {
                    var secondid = findViewById<TextView>(R.id.secondid)
                    secondid.setText(ranklist[i].id)
                    var secondCoins = findViewById<TextView>(R.id.secondcoins)
                    secondCoins.setText(ranklist[i].goldCoins.toString())}
                -3 -> {
                    var thirdid = findViewById<TextView>(R.id.thirdid)
                    thirdid.setText(ranklist[i].id)
                    var thirdCoins = findViewById<TextView>(R.id.thirdcoins)
                    thirdCoins.setText(ranklist[i].goldCoins.toString())}
            }
        }

    }

}
