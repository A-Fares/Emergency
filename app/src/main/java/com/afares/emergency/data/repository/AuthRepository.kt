package com.afares.emergency.data.repository

import com.afares.emergency.data.FirebaseSource
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseSource: FirebaseSource) {


    fun signInWithGoogle(acct: GoogleSignInAccount) = firebaseSource.signInWithGoogle(acct)

    suspend fun saveUser(user: User) =
        firebaseSource.saveUser(user)

    suspend fun saveSavior(savior: Savior) =
        firebaseSource.saveSavior(savior)


}