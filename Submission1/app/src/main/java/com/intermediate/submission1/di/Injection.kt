package com.intermediate.submission1.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.intermediate.submission1.data.database.StoryDatabase
import com.intermediate.submission1.data.preferences.AuthPreferences
import com.intermediate.submission1.data.network.ApiConfig
import com.intermediate.submission1.data.repository.AuthRepository
import com.intermediate.submission1.data.repository.StoryRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(database, apiService)
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        val authPreference = AuthPreferences.getInstance(context.dataStore)
        return AuthRepository.getInstance(authPreference, apiService)
    }
}