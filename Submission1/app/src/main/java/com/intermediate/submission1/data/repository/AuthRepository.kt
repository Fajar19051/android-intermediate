package com.intermediate.submission1.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.preferences.AuthPreferences
import com.intermediate.submission1.data.network.response.LoginResponse
import com.intermediate.submission1.data.network.response.LoginResult
import com.intermediate.submission1.data.network.response.SignupResponse
import com.intermediate.submission1.data.network.ApiService
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val pref: AuthPreferences,
    private val apiService: ApiService
) {
    fun login(email: String, password: String): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)

            pref.saveCurrentUser(
                response.loginResult.name,
                response.loginResult.userId,
                response.loginResult.token
            )

            emit(Result.Success(response))
        } catch (e: Exception) {
            Log.d(TAG, "login: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun signup(name: String, email: String, password: String): LiveData<Result<SignupResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.signup(name, email, password)
            emit(Result.Success(response))
        } catch (e: Exception) {
            Log.d(TAG, "signup: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    suspend fun logout() {
        pref.saveCurrentUser(
            "",
            "",
            ""
        )
    }

    val user: Flow<LoginResult>
        get() = pref.getCurrentUser()

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var instance: AuthRepository? = null
        fun getInstance(
            pref: AuthPreferences,
            apiService: ApiService
        ): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(pref, apiService)
            }.also { instance = it }
    }
}