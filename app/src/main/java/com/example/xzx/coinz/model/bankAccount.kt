package com.example.xzx.coinz.model


data class bankAccount(val id:String = "",
                       val goldCoins:Float = 0f){
    constructor():this("",0f)
}