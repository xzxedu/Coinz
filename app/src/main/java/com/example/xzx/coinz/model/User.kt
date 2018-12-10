package com.example.xzx.coinz.model

data class User(val bio:String = "",
                val name:String ="",
                val profilePicturePath:String? = null,
                val SHIL:String = "",
                val DOLR:String = "",
                val PENY:String = "",
                val QUID:String = "",
                val GOLDCoins:Double = 0.0){
    constructor():this("","",null,"","","","",0.0)
}