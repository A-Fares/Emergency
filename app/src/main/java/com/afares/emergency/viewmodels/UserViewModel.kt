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
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.afares.emergency.adapters.FirestorePagingSource
import com.afares.emergency.data.Resource
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.data.model.Request
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import com.afares.emergency.util.Constants.PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    private val queryRequestsHistory = repository.queryUserRequests()

    private val readUserLiveData = MutableLiveData<Resource<User>>()
    val readRequestsHistory = MutableLiveData<Resource<Request>>()
    val hasMedicalHistory = MutableLiveData<Boolean>()


    val flow = Pager(
        PagingConfig(
            pageSize = PAGE_SIZE
        )
    ) {
        FirestorePagingSource(queryRequestsHistory)
    }.flow.cachedIn(viewModelScope)


    fun getUserRequestsHistory() {
        if (hasInternetConnection()) {
            readRequestsHistory.postValue(Resource.loading(null))
            try {

                readRequestsHistory.postValue(Resource.success(null))
            } catch (e: Exception) {
                readRequestsHistory.postValue(Resource.error(null, "No Requests History"))
            }
        } else {
            readRequestsHistory.postValue(Resource.error(null, "No Internet Connection."))
        }
    }
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

    fun getMedicalHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMedicalHistory().addOnSuccessListener { task ->
                if (task.exists()) {
                    //     readMedicalHistory.postValue(task.result.toObject(MedicalHistory::class.java))
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