package com.afares.emergency.data.model

data class User(
    val uId: String,
    val name: String,
    val photoUrl: String?,
    val type: String,
    val phone: String?,
    val closePersonPhone: String
)
