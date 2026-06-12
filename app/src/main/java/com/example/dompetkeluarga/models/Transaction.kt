package com.example.dompetkeluarga.models

class Transaction (
    val userId:String?=null,
    val id:String?=null,
    val type : String? = null,
    val category:String? = null,
    val amount : Long? =null,
    val date: String?=null,
    val note: String?=null,
    val imageLocation: String?=null
)