package com.afares.emergency.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.afares.emergency.adapters.FirestorePagingSource
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants
import com.afares.emergency.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val city: String = "Alexandria Governorate"
    private val queryAmbulanceRequests = repository.queryAmbulanceRequests(city)
    private val queryFireFighterRequests = repository.queryFireFighterRequests(city)

    fun getA() {
        repository.queryAmbulanceRequests(city).get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("SSS", "Success")
            } else {
                Log.d("SSS", "Failed" + it.exception)
            }
        }
    }

    private val _medicalData =
        MutableStateFlow<NetworkResult<MedicalHistory>>(NetworkResult.Empty())
    val medicalData: StateFlow<NetworkResult<MedicalHistory>> = _medicalData

    private val _requestState = MutableStateFlow<NetworkResult<Request>>(NetworkResult.Empty())
    val requestState: StateFlow<NetworkResult<Request>> = _requestState

    private val _requestStatus = MutableStateFlow("تم الطلب")
    val requestStatus: StateFlow<String> = _requestStatus

    fun checkRequestStatus(status: String) {
        viewModelScope.launch {
            _requestStatus.emit(status)
        }
    }

    val requestsAmbulanceFlow = Pager(
        PagingConfig(pageSize = Constants.PAGE_SIZE)
    ) {
        FirestorePagingSource(queryAmbulanceRequests)
    }.flow.cachedIn(viewModelScope)


    val requestsFireFighterFlow = Pager(
        PagingConfig(pageSize = Constants.PAGE_SIZE)
    ) {
        FirestorePagingSource(queryFireFighterRequests)
    }.flow.cachedIn(viewModelScope)

    fun getMedicalHistory(userSnn: String) {
        _medicalData.value = NetworkResult.Loading()
        repository.getMedicalHistory(userSnn).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val medicalData = task.result.toObject(MedicalHistory::class.java)!!
                _medicalData.value = NetworkResult.Success(medicalData)
            } else {
                _medicalData.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    fun getRequestsStatus() {
        _requestState.value = NetworkResult.Loading()
        repository.queryUserRequests().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _requestState.value = NetworkResult.Success(null)
            } else {
                _requestState.value = NetworkResult.Error("No Internet Connection.")
            }
        }
    }

    fun updateRequestStatus(currentRequest: String, status: String) =
        repository.updateRequestStatus(currentRequest, status)

}