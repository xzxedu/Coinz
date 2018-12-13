package com.example.xzx.coinz.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

data class Wallet(var id: String = "",
                  var currency:String="",
                  var values:String = " ")
