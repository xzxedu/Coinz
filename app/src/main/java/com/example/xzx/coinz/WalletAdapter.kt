package com.example.xzx.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.xzx.coinz.R.id.snap
import com.example.xzx.coinz.model.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_item.view.*
import java.util.Collections.list

class WalletAdapter(val context: Context) :
        RecyclerView.Adapter<WalletAdapter.MyViewHolder>() {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private  var i = 0

    companion object {
        val TAG: String = WalletAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return (50)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var j = 0
        var hobby:Wallet ?= null
        firestoreInstance.collection(currentUserDocRef.id).get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        if (position ==j)
                        {hobby =document.toObject(Wallet::class.java)
                        holder.setData(hobby,position)}
                    }
                    i += 1
                }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var currentHobby: Wallet? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                Toast.makeText(context,currentHobby!!.id+"Clicked!",Toast.LENGTH_LONG).show()
            }

            itemView.imgShare.setOnClickListener {

                currentHobby?.let {
                    val message: String = "My hobby is: " + currentHobby!!.id

                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, message)
                    intent.type = "text/plain"

                    context.startActivity(Intent.createChooser(intent, "Please select app: "))
                }
            }
        }

        fun setData(hobby: Wallet?, pos: Int) {
            hobby?.let {
                itemView.txvTitle.text = hobby.currency
                itemView.textview_currency.text = hobby.values
                // TODO record id and act with firestore
            }

            this.currentHobby = hobby
            this.currentPosition = pos
        }
    }
}