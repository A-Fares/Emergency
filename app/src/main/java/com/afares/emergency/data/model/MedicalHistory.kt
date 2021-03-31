package com.afares.emergency.data.model

data class MedicalHistory(
    val uid: String? = "",
    val diabetic: Boolean? = false,
    val heartPatient: Boolean? = false,
    val pressurePatient: Boolean? = false,
    val height: String? = "",
    val weight: String? = "",
    val age: String? = "",
    val bloodType: String? = "",
    val gender: String? = ""
)

