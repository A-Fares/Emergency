package com.afares.emergency.data

import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.afares.emergency.util.Constants.AMBULANCE
import com.afares.emergency.util.Constants.COLLECTION_MEDICAL_HISTORY
import com.afares.emergency.util.Constants.COLLECTION_REQUESTS
import com.afares.emergency.util.Constants.COLLECTION_USERS
import com.afares.emergency.util.Constants.FIRE_FIGHTER
import com.afares.emergency.util.Constants.PAGE_SIZE
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class FirebaseSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) {


    suspend fun signInWithGoogle(acct: GoogleSignInAccount) = firebaseAuth.signInWithCredential(
        GoogleAuthProvider.getCredential(acct.idToken, null)
    )

    fun saveUser(user: User) =
        fireStore.collection(COLLECTION_USERS)
            .document(firebaseAuth.currentUser!!.uid).set(user)


    fun saveSavior(savior: Savior) =
        fireStore.collection(COLLECTION_USERS)
            .document(firebaseAuth.currentUser!!.uid).set(savior)


    suspend fun fetchUser() =
        fireStore.collection(COLLECTION_USERS).document(firebaseAuth.currentUser!!.uid).get()

    suspend fun addMedicalHistory(medicalHistory: MedicalHistory) =
        fireStore.collection(COLLECTION_MEDICAL_HISTORY)
            .document(firebaseAuth.currentUser!!.uid).set(medicalHistory)


    suspend fun getMedicalHistory() =
        fireStore.collection(COLLECTION_MEDICAL_HISTORY)
            .document(firebaseAuth.currentUser!!.uid).get()


    suspend fun addRequest(request: Request) =
        fireStore.collection(COLLECTION_REQUESTS)
            .add(request)

    fun queryUserRequests() =
        fireStore.collection(COLLECTION_REQUESTS)
            .whereEqualTo("uid", firebaseAuth.currentUser!!.uid)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

    fun queryAmbulanceRequests() =
        fireStore.collection(COLLECTION_REQUESTS)
            .whereEqualTo("type", AMBULANCE)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

    fun queryFireFighterRequests() =
        fireStore.collection(COLLECTION_REQUESTS)
            .whereEqualTo("type", FIRE_FIGHTER)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())
}