package com.afares.emergency.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class Request(
    val uId: String,
    val type:String,
    val description: String,
    val location: String,
    val status: String,
    @ServerTimestamp
    val created: Date?
)