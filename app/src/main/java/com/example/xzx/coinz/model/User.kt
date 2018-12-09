package com.example.xzx.coinz.model

data class User(val name:String = "",
                val bio:String = "",
                val profilePicturePath:String? = null){
    constructor():this("","",null)
}