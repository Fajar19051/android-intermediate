package com.intermediate.submission1.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.intermediate.submission1.data.network.response.LoginResult
import com.intermediate.submission1.data.preferences.SettingPreferences
import com.intermediate.submission1.data.repository.AuthRepository
import com.intermediate.submission1.di.Injection

class MainViewModel(private val pref: SettingPreferences, authRepository: AuthRepository) :
    ViewModel() {
    val user: LiveData<LoginResult> = authRepository.user.asLiveData()
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }
}

class MainViewModelFactory(private val pref: SettingPreferences, private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(pref, Injection.provideAuthRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}