package com.afares.emergency.data.repository

import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.User
import com.afares.emergency.util.Constants
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val requestsId: DocumentReference
) {

    fun signOut() =
        firebaseAuth.signOut()

    fun saveUser(user: User) =
        fireStore.collection(Constants.COLLECTION_USERS)
            .document(firebaseAuth.currentUser!!.uid).set(user)


    fun fetchUser() =
        fireStore.collection(Constants.COLLECTION_USERS).document(firebaseAuth.currentUser!!.uid)
            .get()

    fun addMedicalHistory(userSsn: String, medicalHistory: MedicalHistory) =
        fireStore.collection(Constants.COLLECTION_MEDICAL_HISTORY)
            .document(userSsn).set(medicalHistory)


    fun addRequest(request: Request) =
        requestsId.set(request)

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

    fun getMedicalHistory(userSsn: String) =
        fireStore.collection(Constants.COLLECTION_MEDICAL_HISTORY)
            .document(userSsn).get()

    fun updateRequestStatus(currentRequest: String, status: String) =
        fireStore.collection(Constants.COLLECTION_REQUESTS)
            .document(currentRequest).update("status", status)


    fun queryHospitalData() =
        fireStore.collection(Constants.COLLECTION_HOSPITAL)
            .get()

    fun queryCivilDefenseData() =
        fireStore.collection(Constants.COLLECTION_CIVIL_DEFENSE)
            .get()
}
