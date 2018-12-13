package com.example.xzx.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.startActivity

//when the user has signed in , he will always take into the map directly until click the sign out.
class SplashActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build()
            firestore.setFirestoreSettings(settings)

            if (FirebaseAuth.getInstance().currentUser == null)
                startActivity<SignInActivity>()
            else
                startActivity<MainActivity>()
            finish()
    }
}
