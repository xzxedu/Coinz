package com.example.xzx.coinz

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.example.xzx.coinz.model.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_wallet.*
import org.jetbrains.anko.startActivity

class WalletActivity : AppCompatActivity() {
    companion object {
        val TAG: String = WalletActivity::class.java.simpleName
    }

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        // TODO turn into bank activity
        setupRecyclerView()
    }

    fun downloadCollectInf():List<Wallet>{
        var wallet = Wallet("","","")
        var data = listOf(wallet)
        var newWallet: Wallet?= null
        firestoreInstance.collection(currentUserDocRef.id).get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        newWallet =document.toObject(Wallet::class.java)
                        data += (newWallet)!!
                    }
                }
        return data
    }


    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        var data = downloadCollectInf()
        val adapter= WalletAdapter(this,data)
        recyclerView?.adapter = adapter
    }

}