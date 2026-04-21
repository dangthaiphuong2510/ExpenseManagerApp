package com.example.expensemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
        private val IS_CURRENCY_SET_KEY = booleanPreferencesKey("is_currency_set")
    }

    val currencySymbol: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] ?: "₫"
        }

    val isCurrencySet: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_CURRENCY_SET_KEY] ?: false
        }

    suspend fun saveCurrency(symbol: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = symbol
            preferences[IS_CURRENCY_SET_KEY] = true
        }
    }

    suspend fun clearCurrencySettings() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENCY_SYMBOL_KEY)
            preferences.remove(IS_CURRENCY_SET_KEY)
        }
    }
}
