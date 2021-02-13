package com.afares.emergency.data.model

data class User(
    val uId: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val type: String,
    val phone: String?,
    val closePersonPhone: String,
    val age: Int,
    val ssn: String,
    val bloodType: String
)
