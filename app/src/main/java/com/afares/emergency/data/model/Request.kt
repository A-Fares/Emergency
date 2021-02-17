package com.afares.emergency.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class Request(
    val uId: String,
    val description: String,
    val location: String,
    @ServerTimestamp
    val status: String,
    val created: Date
)