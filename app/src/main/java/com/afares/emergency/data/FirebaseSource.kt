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


    suspend fun signInWithGoogle(acct: GoogleSignInAccount) = firebaseAuth.signInWithCredential(
        GoogleAuthProvider.getCredential(acct.idToken, null)
    )

    suspend fun saveUser(user: User) =
        fireStore.collection("users")
            .document(firebaseAuth.currentUser!!.uid).set(user)


    suspend fun saveSavior(savior: Savior) =
        fireStore.collection("users")
            .document(firebaseAuth.currentUser!!.uid).set(savior)


    suspend fun addMedicalHistory(medicalHistory: MedicalHistory) =
        fireStore.collection("Medical History")
            .document(firebaseAuth.currentUser!!.uid).set(medicalHistory)

    suspend fun getMedicalHistory() =
        fireStore.collection("Medical History")
            .document(firebaseAuth.currentUser!!.uid).get()


    suspend fun fetchUser() =
        fireStore.collection("users").document(firebaseAuth.currentUser!!.uid).get()


    suspend fun addRequest(request: Request) =
        fireStore.collection("Requests")
            .add(request)

    suspend fun getRequests() =
        fireStore.collection("Requests")
            .get()


}