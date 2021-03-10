package com.afares.emergency.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afares.emergency.adapters.FirestorePagingSource
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.CivilDefense
import com.afares.emergency.data.model.Hospital
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {


    val requestAmbulance = MutableLiveData<PagingData<Request>?>()
    val requestFireFighter = MutableLiveData<PagingData<Request>>()

    val recipientMail = MutableLiveData<String>()

    fun getHospitalData(cityId: String) {
        repository.getHospitalData(cityId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val hospitalData = task.result.toObject(Hospital::class.java)
                val city = hospitalData?.city!!
                val mail = hospitalData.mail!!
                recipientMail.postValue(mail)
                val queryAmbulanceRequests = repository.queryAmbulanceRequests(city)

                val requestsAmbulanceFlow = Pager(
                    PagingConfig(pageSize = Constants.PAGE_SIZE)
                ) {
                    FirestorePagingSource(queryAmbulanceRequests)
                }.flow.cachedIn(viewModelScope)

                viewModelScope.launch {
                    requestsAmbulanceFlow.collectLatest { dataFlow ->
                        requestAmbulance.postValue(dataFlow)
                    }
                }
            }
        }
    }

    fun getCivilDefenseData(cityId: String) {
        repository.getCivilDefenseData(cityId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val civilDefenseData = task.result.toObject(CivilDefense::class.java)
                val city = civilDefenseData?.city!!
                val mail = civilDefenseData.mail!!
                recipientMail.postValue(mail)
                val queryFireFighterRequests = repository.queryFireFighterRequests(city)
                val requestsFireFighterFlow = Pager(
                    PagingConfig(pageSize = Constants.PAGE_SIZE)
                ) {
                    FirestorePagingSource(queryFireFighterRequests)
                }.flow.cachedIn(viewModelScope)

                viewModelScope.launch {
                    requestsFireFighterFlow.collectLatest { dataFlow ->
                        requestFireFighter.postValue(dataFlow)
                    }
                }
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
    private val _navigateToRequestInfo = MutableLiveData<Request>()
    val navigateToRequestInfo
        get() = _navigateToRequestInfo

    fun onRequestClicked(request: Request){
        _navigateToRequestInfo.value = request
    }

    fun onRequestNavigated() {
        _navigateToRequestInfo.value = null
    }


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

    fun getRequestsState() {
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