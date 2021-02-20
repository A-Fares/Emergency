package com.afares.emergency.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class Request(
    val uId: String? = null,
    val type: String? = null,
    val description: String? = null,
    val location: String? = null,
    val status: String? = null,
    @ServerTimestamp
    val created: Date? = null
)