package com.afares.emergency.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.afares.emergency.data.DataStoreRepository
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.CivilDefense
import com.afares.emergency.data.model.Hospital
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants.DEFAULT_USER_TYPE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.QueryDocumentSnapshot
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

    private val _userState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Empty())
    val userState: StateFlow<NetworkResult<User>> = _userState

    val readLoginStatus = dataStoreRepository.readLoginStatus.asLiveData()

    /** for retrieve user ssn*/

    private val ssnHasExist = MutableLiveData<Boolean>()
    fun ssnRegistered(userSSn: String): MutableLiveData<Boolean> {
        repository.checkSsnUniqueness(userSSn).addOnSuccessListener { task ->
            val empty = task.isEmpty
            ssnHasExist.postValue(!empty)
        }
        return ssnHasExist
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _userState.value = NetworkResult.Loading()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isNew = task.result.additionalUserInfo!!.isNewUser
                if (isNew) {
                    _userState.value = NetworkResult.Error("يرجى تسجيل الحساب اولا")
                } else {
                    repository.fetchUser().addOnSuccessListener { user ->
                        val userData = user.toObject(User::class.java)!!
                        _userState.value = NetworkResult.Success(userData)
                        saveLoginPreferences(true, userData.type.toString())
                    }
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

    fun saveUser(user: User) {
        repository.saveUser(user)
        saveLoginPreferences(true, user.type.toString())
    }

    private var _hospitalMutableList = MutableLiveData<List<Hospital>>()
    var hospitalMutableList: MutableLiveData<List<Hospital>> = _hospitalMutableList

    fun queryHospitalData() {
        val hospitalList = ArrayList<Hospital>()
        hospitalList.add(Hospital("none", "-- اختر --"))
        repository.queryHospitalData().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document: QueryDocumentSnapshot in task.result) {
                    val hospitalName = document.getString("name")
                    val hospitalId = document.getString("id")
                    hospitalList.add(
                        Hospital(
                            hospitalId.toString(),
                            hospitalName.toString()
                        )
                    )
                }
                _hospitalMutableList.postValue(hospitalList)
            }
        }
    }

    private var _civilDefenseMutableList = MutableLiveData<List<CivilDefense>>()
    var civilDefenseMutableList: MutableLiveData<List<CivilDefense>> = _civilDefenseMutableList

    fun queryCivilDefenseData() {
        val civilDefenseList = ArrayList<CivilDefense>()
        civilDefenseList.add(CivilDefense("none", "-- اختر --"))
        repository.queryCivilDefenseData().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document: QueryDocumentSnapshot in task.result) {
                    val civilDefenseName = document.getString("name")
                    val civilDefenseId = document.getString("id")
                    civilDefenseList.add(
                        CivilDefense(
                            civilDefenseId.toString(),
                            civilDefenseName.toString()
                        )
                    )
                }
                _civilDefenseMutableList.postValue(civilDefenseList)
            }
        }
    }


    fun signOut() {
        saveLoginPreferences(false, DEFAULT_USER_TYPE)
        repository.signOut()
    }

}

