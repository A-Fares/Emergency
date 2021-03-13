package com.afares.emergency.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid: String? = "",
    val token:String? = "",
    val name: String? = "",
    val ssn: String? = "",
    val phone: String? = "",
    val closePersonPhone: String? = "",
    val type: String? = "",
    val photoUrl: String? = "",
    val cityId: String? = ""
) : Parcelable
