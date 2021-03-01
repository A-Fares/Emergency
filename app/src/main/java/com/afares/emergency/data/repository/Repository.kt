package com.afares.emergency.data.repository

import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.User
import com.afares.emergency.util.Constants
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class Repository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) {

    fun saveUser(user: User) =
        fireStore.collection(Constants.COLLECTION_USERS)
            .document(firebaseAuth.currentUser!!.uid).set(user)


    fun fetchUser() =
        fireStore.collection(Constants.COLLECTION_USERS).document(firebaseAuth.currentUser!!.uid)
            .get()

    fun addMedicalHistory(medicalHistory: MedicalHistory) =
        fireStore.collection(Constants.COLLECTION_MEDICAL_HISTORY)
            .document(firebaseAuth.currentUser!!.uid).set(medicalHistory)


    fun addRequest(request: Request) =
        fireStore.collection(Constants.COLLECTION_REQUESTS)
            .add(request)

    fun queryUserRequests() =
        fireStore.collection(Constants.COLLECTION_REQUESTS)
            .whereEqualTo("uid", firebaseAuth.currentUser!!.uid)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(Constants.PAGE_SIZE.toLong())

    fun queryAmbulanceRequests() =
        fireStore.collection(Constants.COLLECTION_REQUESTS)
            .whereEqualTo("type", Constants.AMBULANCE)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(Constants.PAGE_SIZE.toLong())

    fun queryFireFighterRequests() =
        fireStore.collection(Constants.COLLECTION_REQUESTS)
            .whereEqualTo("type", Constants.FIRE_FIGHTER)
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(Constants.PAGE_SIZE.toLong())

    fun getUserInfo(userID: String) =
        fireStore.collection(Constants.COLLECTION_USERS)
            .document(userID).get()

    fun getMedicalHistory(userID: String) =
        fireStore.collection(Constants.COLLECTION_MEDICAL_HISTORY)
            .document(userID).get()

}