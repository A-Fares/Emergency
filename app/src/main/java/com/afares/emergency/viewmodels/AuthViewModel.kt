package com.afares.emergency.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.afares.emergency.data.DataStoreRepository
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.afares.emergency.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    private val userType = MutableLiveData<String>()
    var isLogin = false

    val readLoginStatus = dataStoreRepository.readLoginStatus.asLiveData()

    private fun saveLoginStatus(isLogin: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveLoginStatus(isLogin)
        }

    fun saveUser(user: User) {
        repository.saveUser(user)
    }

    fun saveSavior(savior: Savior) {
        repository.saveSavior(savior)
    }

    fun fetchUserType(): LiveData<String> {
        repository.fetchUser().addOnSuccessListener { userTask ->
            userType.postValue(userTask.getString("type"))
        }
        return userType
    }

}