package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.afares.emergency.data.DataStoreRepository
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants.DEFAULT_USER_TYPE
import com.afares.emergency.util.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    private val userType = MutableLiveData<String>()

    private val _userState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Empty())
    val userState: StateFlow<NetworkResult<User>> = _userState

    val readLoginStatus = dataStoreRepository.readLoginStatus.asLiveData()

    fun signUpWithPhoneAuthCredential(user: User, credential: PhoneAuthCredential) {
        _userState.value = NetworkResult.Loading()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = User(
                    firebaseAuth.currentUser!!.uid,
                    user.name, user.ssn, user.phone, user.closePersonPhone, user.type, null
                )
                saveLoginPreferences(true, userData.type.toString())
                saveUser(userData)
                _userState.value = NetworkResult.Success(userData)
            } else {
                // Show Error
                _userState.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _userState.value = NetworkResult.Loading()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                repository.fetchUser().addOnSuccessListener { user ->
                    val userData = user.toObject(User::class.java)!!
                    _userState.value = NetworkResult.Success(userData)
                    saveLoginPreferences(true, userData.type.toString())
                }

            } else {
                // Show Error
                _userState.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    private fun saveLoginPreferences(loginState: Boolean, userType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveLoginStatus(loginState, userType)
        }
    }

    private fun saveUser(user: User) {
        repository.saveUser(user)
    }


    fun signOut() {
        saveLoginPreferences(false, DEFAULT_USER_TYPE)
        repository.signOut()
    }

}