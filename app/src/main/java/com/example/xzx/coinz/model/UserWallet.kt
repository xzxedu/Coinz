package com.example.xzx.coinz.model

data class UserWallet(val SHIL:String = "",
                      val DOLR:String = "",
                      val PENY:String = "",
                      val QUID:String = ""){
    constructor():this("","","","")}