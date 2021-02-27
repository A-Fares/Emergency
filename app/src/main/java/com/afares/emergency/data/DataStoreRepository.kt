package com.afares.emergency.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import com.afares.emergency.util.Constants.PREFERENCES_LOGIN_STATUS
import com.afares.emergency.util.Constants.PREFERENCES_NAME
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
    }

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = PREFERENCES_NAME
    )

    suspend fun saveLoginStatus(isLogin: Boolean) {
        dataStore.edit { preference ->
            preference[PreferenceKeys.loginStatus] = isLogin
        }
    }

    val readLoginStatus: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val isLogin = preferences[PreferenceKeys.loginStatus] ?: false
            isLogin
        }
}