package com.afares.emergency.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import com.afares.emergency.util.Constants.DEFAULT_USER_TYPE
import com.afares.emergency.util.Constants.PREFERENCES_LOGIN_STATUS
import com.afares.emergency.util.Constants.PREFERENCES_NAME
import com.afares.emergency.util.Constants.PREFERENCES_USER_TYPE
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


@ActivityRetainedScoped
class DataStoreRepository @Inject constructor(@ApplicationContext context: Context) {

    private object PreferenceKeys {
        val loginStatus = booleanPreferencesKey(PREFERENCES_LOGIN_STATUS)
        val userType = stringPreferencesKey(PREFERENCES_USER_TYPE)
    }

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = PREFERENCES_NAME
    )

    suspend fun saveLoginStatus(isLogin: Boolean, userType: String) {
        dataStore.edit { preference ->
            preference[PreferenceKeys.loginStatus] = isLogin
            preference[PreferenceKeys.userType] = userType
        }
    }

    val readLoginStatus: Flow<LoginPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val isLogin = preferences[PreferenceKeys.loginStatus] ?: false
            val userType = preferences[PreferenceKeys.userType] ?: DEFAULT_USER_TYPE
            LoginPreferences(
                isLogin, userType
            )
        }
}

data class LoginPreferences(
    val isLogin: Boolean,
    val userType: String
)