package com.example.xzx.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.xzx.coinz.R.id.snap
import com.example.xzx.coinz.model.Wallet
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.admin.v1beta1.Index
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.share_dialog.view.*
import org.jetbrains.anko.commit
import java.util.Collections.list

class WalletAdapter(val context: Context,val DATA:ArrayList<Wallet>) :
        RecyclerView.Adapter<WalletAdapter.MyViewHolder>() {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private var data = DATA

    companion object {
        val TAG: String = WalletAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("dataSize",data.size.toString())
        return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val wallet = data.get(position)
        Log.d("currentWallet",wallet.toString())
        holder.setData(wallet,position)
    }

    inner class MyViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        var currentWallet =Wallet("","","")
        var currentPosition: Int = 0

        init {
            // get today's currency
            val sharedPref = context.getSharedPreferences("Downloadmap",Context.MODE_PRIVATE)
            var peny = sharedPref.getFloat("PENY",1f)
            var dolr = sharedPref.getFloat("DOLR",1f)
            var shil = sharedPref.getFloat("SHIL",1f)
            var quid = sharedPref.getFloat("QUID",1f)
            itemView.setOnClickListener {
                Toast.makeText(context,"Please click the share or deposit buttons on the bar!",Toast.LENGTH_LONG).show()
            }
            var equalGold:Float =0f
            when (currentWallet!!.currency){
                "DOLR" -> equalGold = currentWallet!!.value.toFloat() * dolr
                "PENY" -> equalGold = currentWallet!!.value.toFloat() * peny
                "SHIL" -> equalGold = currentWallet!!.value.toFloat() * shil
                "QUID" -> equalGold = currentWallet!!.value.toFloat() * quid
            }
            itemView.imgShare.setOnClickListener {
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.share_dialog,null)
                //AlertDialogBuilder
                val mBuilder= AlertDialog.Builder(context)
                        .setView(mDialogView)
                        .setTitle("Share COINS")
                //show dialog
                val mAlertDialog = mBuilder.show()
                mDialogView.dialogShareBtn.setOnClickListener{
                    mAlertDialog.dismiss()
                    val userID= mDialogView.dialogID.text.toString()
                    // transfer the money to entered userID
                    firestoreInstance.collection("BANK Account")
                            .document(userID).get()
                            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null) {
                                        var newGoldNum = document.data?.get("goldCoins").toString().toFloat().plus(equalGold)
                                        var map = mapOf("goldCoins" to newGoldNum)
                                        firestoreInstance.collection("BANK Account").document(userID).update(map)
                                                .addOnSuccessListener{
                                                    removeItem(currentPosition)
                                                    Toast.makeText(context, "Send successfully", Toast.LENGTH_LONG).show()}
                                                .addOnFailureListener { Toast.makeText(context, "Sharing fails! Try again!", Toast.LENGTH_LONG).show()
                                                }
                                    }
                                }
                            })
                    //delete shared coins information on the firestore
                    firestoreInstance.collection(currentUserDocRef.id).document(currentWallet!!.id)
                            .delete()
                            .addOnSuccessListener { Toast.makeText(context, "Sharing successfully", Toast.LENGTH_LONG).show() }
                            .addOnFailureListener { Toast.makeText(context, "Sharing fails! Try again!", Toast.LENGTH_LONG).show() }
                }
            }
            itemView.imgSave.setOnClickListener{
                val sharedPref = context.getSharedPreferences("Downloadmap",Context.MODE_PRIVATE)
                var saveNum = sharedPref.getInt("SavedCoins",0)
                if (saveNum <=25 ) {
                    firestoreInstance.collection("BANK Account")
                            .document(currentUserDocRef.id).get()
                            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null) {
                                        var newGoldNum = document.data!!.get("goldCoins").toString().toFloat().plus(equalGold)
                                        var map = mapOf("goldCoins" to newGoldNum)
                                        firestoreInstance.collection("BANK Account").document(currentUserDocRef.id).update(map)
                                                .addOnSuccessListener{
                                                    removeItem(currentPosition)
                                                    Toast.makeText(context, "Deposit successfully", Toast.LENGTH_LONG).show()}
                                                .addOnFailureListener { Toast.makeText(context, "Try again!", Toast.LENGTH_LONG).show()
                                                }
                                    }
                                }
                            })
                    Log.d("currentWalletId",currentWallet.id)
                    firestoreInstance.collection(currentUserDocRef.id).document(currentWallet!!.id)
                            .delete()
                            .addOnSuccessListener { Toast.makeText(context, "Deposit successfully", Toast.LENGTH_LONG).show() }
                            .addOnFailureListener { Toast.makeText(context, "Try again!", Toast.LENGTH_LONG).show() }
                    saveNum +=1
                    sharedPref.edit().putInt("SavedCoins",saveNum)
                    sharedPref.edit().commit()
                }
                else
                    Toast.makeText(context,"You have already saved 25 Coins today!",Toast.LENGTH_LONG).show()
            }
            //TODO 让操作完 的item消失
        }
    fun setData(wallet: Wallet, pos: Int) {
        wallet?.let {
                itemView.txvTitle.text = wallet.currency
                itemView.textview_currency.text = wallet.value
            }
            this.currentWallet = wallet
            this.currentPosition = pos
        }
    fun removeItem(position: Int){
        DATA.removeAt(position)
    }
    }
}