package com.example.expensemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PreferenceDataStore {
    suspend fun token(token: String)
    val token: Flow<String>
    suspend fun clearDataStore()
}

class PreferenceDataStoreDefault @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceDataStore {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
    }

    override suspend fun token(token: String) {
        dataStore.edit { preferences ->
            preferences[Keys.TOKEN] = token
        }
    }

    override val token: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[Keys.TOKEN].orEmpty()
        }

    override suspend fun clearDataStore() {
        dataStore.edit { it.clear() }
    }
}
