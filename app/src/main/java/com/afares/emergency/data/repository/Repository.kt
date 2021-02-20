package com.afares.emergency.data.repository

import com.afares.emergency.data.FirebaseSource
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

class Repository @Inject constructor(private val firebaseSource: FirebaseSource) {


    suspend fun signInWithGoogle(acct: GoogleSignInAccount) = firebaseSource.signInWithGoogle(acct)

    suspend fun saveUser(user: User) =
        firebaseSource.saveUser(user)

    suspend fun saveSavior(savior: Savior) =
        firebaseSource.saveSavior(savior)

    suspend fun fetchUser() = firebaseSource.fetchUser()

    suspend fun addMedicalHistory(medicalHistory: MedicalHistory) =
        firebaseSource.addMedicalHistory(medicalHistory)

    suspend fun getMedicalHistory() =
        firebaseSource.getMedicalHistory()

    suspend fun addRequest(request: Request) =
        firebaseSource.addRequest(request)

     fun queryUserRequests() =
        firebaseSource.queryUserRequests()

}