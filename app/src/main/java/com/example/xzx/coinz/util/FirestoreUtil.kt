package com.example.xzx.coinz.util

import android.content.ContentValues.TAG
import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import com.example.xzx.coinz.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


object FirestoreUtil {

        private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

        private val currentUserDocRef: DocumentReference
            get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw NullPointerException("UID is null.")}")


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "", null)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
                // the first time register will acquire 100 GOLD coins
                var goldCoins = mapOf("goldCoins" to 100)
                firestoreInstance.collection("BANK Account").document(currentUserDocRef.id).set(goldCoins)
            }
            else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        currentUserDocRef.update(userFieldMap)
    }


    fun getCurrentUser(onComplete: (User) -> Unit) {
        Log.d("currentDocId", currentUserDocRef.id)
        Log.d("currentDocpath", currentUserDocRef.path)
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }

    }
}



