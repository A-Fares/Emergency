package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.afares.emergency.adapters.FirestorePagingSource
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants.PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    private val queryRequestsHistory = repository.queryUserRequests()

    private val _userData = MutableStateFlow<NetworkResult<User>>(NetworkResult.Empty())
    val userData: StateFlow<NetworkResult<User>> = _userData

    private val _hasRequests = MutableLiveData<Boolean?>()
    val hasRequests
        get() = _hasRequests

    fun onRequestsHistoryNavigated() {
        _hasRequests.value = null
    }

    fun checkRequestHistory() {
        queryRequestsHistory.get().addOnSuccessListener {
            val hasRequest = !it.isEmpty
            _hasRequests.value = hasRequest
        }
    }

    val historyRequestsFlow = Pager(
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        FirestorePagingSource(queryRequestsHistory)
    }.flow.cachedIn(viewModelScope)

    fun getUserInfo(userID: String) {
        _userData.value = NetworkResult.Loading()
        repository.getUserInfo(userID).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = task.result.toObject(User::class.java)
                _userData.value = NetworkResult.Success(userData)
            } else {
                _userData.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    fun addMedicalHistory(userSsn: String, medicalHistory: MedicalHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMedicalHistory(userSsn, medicalHistory)
        }
    }

    private val _hasMedicalHistory = MutableLiveData<Boolean?>()
    val hasMedicalHistory
        get() = _hasMedicalHistory


    fun onMedicalHistoryNavigated() {
        _hasMedicalHistory.value = null
    }

    fun getMedicalHistory(userSsn: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMedicalHistory(userSsn).addOnSuccessListener { task ->
                _hasMedicalHistory.postValue(task.exists())
            }
        }
    }

    fun addRequest(request: Request) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRequest(request)
        }
    }

    fun updateUserImage(photoUrl: String) =
        repository.updateUserImage(photoUrl)

}