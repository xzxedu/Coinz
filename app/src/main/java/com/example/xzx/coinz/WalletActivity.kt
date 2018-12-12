package com.example.xzx.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.xzx.coinz.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_wallet.*

class WalletActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestore.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")
    private var DocumentData= mutableMapOf<String, Any?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        setSupportActionBar(toolbar_wallet)
        mAuth = FirebaseAuth.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore?.firestoreSettings = settings

        getCurrentUser(currentUserDocRef)

        val bankButton: Button = findViewById(R.id.BankButton)
        bankButton.setOnClickListener{
            val geoJsonString: String = getIntent().getStringExtra("geoJsonString")
            val intent = Intent(this, BankActivity::class.java)
            intent.putExtra("geoJsonString", geoJsonString)
            startActivity(intent)
        }
    }

    fun getCurrentUser(currentUserDocRef: DocumentReference){
        currentUserDocRef.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    DocumentData = document.data!!
                    Log.d("wallet", "DocumentSnapshot data: " + DocumentData)
                    val DolrId:TextView = findViewById(R.id.DOLR)
                    val PenyId:TextView = findViewById(R.id.PENY)
                    val ShilId:TextView = findViewById(R.id.SHIL)
                    val QuidId:TextView = findViewById(R.id.QUID)
                    Log.d("wallet","keykeykey!!!!"+  (DocumentData!!.keys))
                    Log.d("wallet","CurentUser!!!!"+  (DocumentData))
                    DolrId.setText(DocumentData!!.get("DOLR").toString())
                    PenyId.setText(DocumentData!!.get("PENY").toString())
                    QuidId.setText(DocumentData!!.get("QUID").toString())
                    ShilId.setText(DocumentData!!.get("SHIL").toString())

                } else {
                    Log.d("wallet", "No such document")
                }
            } else {
                Log.d("wallet", "get failed with ", task.exception)
            }
        })
    }

}