package com.afares.emergency.data

import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
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

    fun saveUser(user: User) =
        user.uId?.let {
            fireStore.collection("users")
                .document(it).set(user)
        }

    fun saveSavior(savior: Savior) =
        fireStore.collection("users")
            .document(savior.uId).set(savior)


    fun addMedicalHistory(medicalHistory: MedicalHistory) =
        fireStore.collection("Medical History")
            .document(medicalHistory.uId).set(medicalHistory)


    fun fetchUser() =
        fireStore.collection("users").document(firebaseAuth.currentUser!!.uid).get()


    fun addRequest(request: Request) =
        fireStore.collection("Requests")
            .add(request)
}