package com.afares.emergency.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.afares.emergency.adapters.FirestorePagingSource
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.Resource
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

    private val _hasRequests = MutableStateFlow(false)
    val hasRequests: StateFlow<Boolean> = _hasRequests

    fun checkRequestHistory() {
        queryRequestsHistory.addSnapshotListener { value, error ->
            _hasRequests.value = !value!!.isEmpty
        }
    }

    val hasMedicalHistory = MutableLiveData<Boolean>()

    val historyRequestsFlow = Pager(
        PagingConfig(pageSize = PAGE_SIZE)
    ) {
        FirestorePagingSource(queryRequestsHistory)
    }.flow.cachedIn(viewModelScope)

    fun getUserInfo(userID: String) {
        _userData.value = NetworkResult.Loading()
        repository.getUserInfo(userID).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = task.result.toObject(User::class.java)!!
                _userData.value = NetworkResult.Success(userData)
            } else {
                _userData.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    fun addMedicalHistory(medicalHistory: MedicalHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMedicalHistory(medicalHistory)
        }
    }

    fun getMedicalHistory(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMedicalHistory(userId).addOnSuccessListener { task ->
                if (task.exists()) {
                    hasMedicalHistory.postValue(true)
                } else {
                    hasMedicalHistory.postValue(false)
                }
            }
        }
    }

    fun addRequest(request: Request) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRequest(request)
        }
    }


    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}