package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.afares.emergency.data.Resource
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {


    val userType = MutableLiveData<String>()


    private
    val gmailUserLiveData = MutableLiveData<Resource<User>>()

    fun signInWithGoogle(acct: GoogleSignInAccount): LiveData<Resource<User>> {
        repository.signInWithGoogle(acct).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                gmailUserLiveData.postValue(
                    Resource.success(null)
                )
            } else {
                gmailUserLiveData.postValue(Resource.error(null, "couldn't sign in user"))
            }

        }
        return gmailUserLiveData
    }

    fun fetchUserType(): LiveData<String> {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchUser().addOnSuccessListener { userTask ->
                userType.postValue(userTask.getString("type"))
            }
        }
        return userType
    }

}