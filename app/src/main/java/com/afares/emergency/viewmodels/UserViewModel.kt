package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.afares.emergency.data.Resource
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val repository: AuthRepository
) : AndroidViewModel(application) {

    private val medicalHistory = MutableLiveData<MedicalHistory>()
    private val user = MutableLiveData<User>()

    private val readUserLiveData = MutableLiveData<Resource<User>>()

    fun fetchUser(): LiveData<Resource<User>> {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchUser().addOnSuccessListener { userData ->
                val uId = userData.getString("uId")
                val name = userData.getString("name")
                val photoUrl = userData.getString("photoUrl")
                val phone = userData.getString("phone")
                val closePersonPhone = userData.getString("closePersonPhone")
                val type = userData.getString("type")

                readUserLiveData.postValue(
                    Resource.success(
                        User(uId, name, photoUrl, type, phone, closePersonPhone)
                    )
                )
            }
        }
        return readUserLiveData
    }

    fun addMedicalHistory(medicalHistory: MedicalHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMedicalHistory(medicalHistory)
        }
    }

}