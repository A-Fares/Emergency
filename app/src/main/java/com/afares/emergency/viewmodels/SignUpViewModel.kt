package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.afares.emergency.data.Resource
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    application: Application,
    private val repository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    private val gmailUserLiveData = MutableLiveData<Resource<User>>()
    private val gmailSaviorLiveData = MutableLiveData<Resource<Savior>>()


    fun signUpUserWithGoogle(
        acct: GoogleSignInAccount,
        personalPhone: String,
        userType: String,
        closePersonPhone: String,
        age: Int,
        ssn: String,
        bloodType: String
    ): LiveData<Resource<User>> {

        repository.signInWithGoogle(acct).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                gmailUserLiveData.postValue(
                        Resource.success(

                            User(
                                firebaseAuth.currentUser!!.uid,
                                firebaseAuth.currentUser?.displayName!!,
                                firebaseAuth.currentUser?.email!!,
                                firebaseAuth.currentUser?.photoUrl.toString(),
                                userType,
                                personalPhone,
                                closePersonPhone,
                                age,
                                ssn,
                                bloodType
                            )
                        )
                )
            } else {
                gmailUserLiveData.postValue(Resource.error(null, "couldn't sign in user"))
            }

        }
        return gmailUserLiveData
    }

    fun signUpSaviorWithGoogle(
        acct: GoogleSignInAccount,
        userType: String
    ): LiveData<Resource<Savior>> {

        repository.signInWithGoogle(acct).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                gmailSaviorLiveData.postValue(
                    Resource.success(
                        Savior(
                            firebaseAuth.currentUser!!.uid,
                            firebaseAuth.currentUser?.displayName!!,
                            firebaseAuth.currentUser?.email!!,
                            firebaseAuth.currentUser?.photoUrl.toString(),
                            userType
                        )
                    )
                )

            } else {
                gmailSaviorLiveData.postValue(Resource.error(null, "couldn't sign in user"))
            }

        }
        return gmailSaviorLiveData
    }


    fun saveUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUser(user)
        }
    }

    fun saveSavior(savior: Savior) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSavior(savior)
        }
    }
}