package com.afares.emergency.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.util.*

@Parcelize
data class Request(
    val id: String? = "",
    val uid: String? = "",
    val type: String? = "",
    val description: String? = "",
    val location: String? = "",
    val status: String? = "",
    @ServerTimestamp
    val created: Date? = null
) : Parcelable