package com.intermediate.submission1.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.database.StoryDatabase
import com.intermediate.submission1.data.local.StoryRemoteMediator
import com.intermediate.submission1.data.network.response.AddStoryResponse
import com.intermediate.submission1.data.network.ApiService
import com.intermediate.submission1.data.models.Story
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) {
    fun getStoriesWithLocation(token: String): LiveData<Result<List<Story>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoriesWithLocation(token)
            val stories = response.listStory.map {
                Story(it.id, it.name, it.createdAt, it.description, it.photoUrl)
            }
            emit(Result.Success(stories))
        } catch (e: Exception) {
            Log.d(TAG, "getStories: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getPagedStories(token: String): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 1
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun addStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody
    ): LiveData<Result<AddStoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.uploadImage(token, imageMultipart, description)
            emit(Result.Success(response))
        } catch (e: Exception) {
            Log.d(TAG, "addStory: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        private const val TAG = "StoryRepository"

        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            storyDatabase: StoryDatabase,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(storyDatabase, apiService)
            }.also { instance = it }
    }
}