package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afares.emergency.data.Resource
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    private val gmailUserLiveData = MutableLiveData<Resource<User>>()
    private val saveUserLiveData = MutableLiveData<Resource<User>>()

   /* fun signInWithGoogle(acct: GoogleSignInAccount): LiveData<Resource<User>> {

        repository.signInWithGoogle(acct).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                gmailUserLiveData.postValue(
                    Resource.success(
                        User(
                            firebaseAuth.currentUser!!.uid,
                            firebaseAuth.currentUser?.displayName!!,
                            firebaseAuth.currentUser?.email!!,
                            firebaseAuth.currentUser?.photoUrl.toString()
                        )
                    )
                )
            } else {
                gmailUserLiveData.postValue(Resource.error(null, "couldn't sign in user"))
            }

        }
        return gmailUserLiveData
    }

    fun saveUser(id: String, name: String, email: String, photoUrl: String) {
        repository.saveUser(id, name, email, photoUrl).addOnCompleteListener {
            if (it.isSuccessful) {
                saveUserLiveData.postValue(Resource.success(User(id, name, email, photoUrl)))
            } else {
                saveUserLiveData.postValue(Resource.error(null, it.exception?.message.toString()))
            }
        }
    }*/
}