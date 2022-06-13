package com.intermediate.submission1.ui.addstory

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.intermediate.submission1.data.network.response.LoginResult
import com.intermediate.submission1.data.repository.AuthRepository
import com.intermediate.submission1.data.repository.StoryRepository
import com.intermediate.submission1.di.Injection
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(
    authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    val user: LiveData<LoginResult> = authRepository.user.asLiveData()

    fun addStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ) =
        storyRepository.addStory("Bearer $token", imageMultipart, description, lat, lon)
}

class AddStoryViewModelFactory(private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
            return AddStoryViewModel(
                Injection.provideAuthRepository(context),
                Injection.provideStoryRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}