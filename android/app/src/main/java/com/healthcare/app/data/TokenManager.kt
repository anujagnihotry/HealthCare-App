package com.healthcare.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val DOCTOR_ID = stringPreferencesKey("doctor_id")
        private val PATIENT_ID = stringPreferencesKey("patient_id")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID] }
    val doctorId: Flow<String?> = context.dataStore.data.map { it[DOCTOR_ID] }
    val patientId: Flow<String?> = context.dataStore.data.map { it[PATIENT_ID] }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun getUserRole(): String? {
        return context.dataStore.data.first()[USER_ROLE]
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        role: String,
        doctorId: String?,
        patientId: String?,
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            prefs[USER_ID] = userId
            prefs[USER_ROLE] = role
            doctorId?.let { prefs[DOCTOR_ID] = it }
            patientId?.let { prefs[PATIENT_ID] = it }
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }
}
