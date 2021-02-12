package com.afares.emergency.data

import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirebaseSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) {


    fun signInWithGoogle(acct: GoogleSignInAccount) = firebaseAuth.signInWithCredential(
        GoogleAuthProvider.getCredential(acct.idToken, null)
    )

    suspend fun saveUser(user: User) =
        fireStore.collection("users")
            .document(user.uId).set(user)

    suspend fun saveSavior(savior: Savior) =
        fireStore.collection("users")
            .document(savior.uId).set(savior)


    fun fetchUser() = fireStore.collection("users").get()
}