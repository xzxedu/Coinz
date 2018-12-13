package com.example.xzx.coinz.model

data class User(val bio:String = "",
                val name:String ="",
                val profilePicturePath:String? = null){
    constructor():this("","",null)
}