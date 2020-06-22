package com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.andromeda.kunalbhatia.demo.hungamaplayer.HomePostRepository
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.Resource
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.CreatePostResponse
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.NetState
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostsResponse
import okhttp3.RequestBody

class HomePostViewModel(application: Application) : AndroidViewModel(application) {

    val onStateUpdatedLiveData = MutableLiveData<NetState>()
    private val myRepository: HomePostRepository = HomePostRepository
    private var createPostLiveData = MutableLiveData<Resource<CreatePostResponse>>()

    init {
        // Initialize
    }

    // Load home feed
    fun loadHomePostFeeds(limit: String, offset: String) {
        onStateUpdatedLiveData.value = NetState.LOADING
        myRepository.loadHomePostFeeds(limit, offset, onStateUpdatedLiveData)
    }

    // observe loading
    fun observeOnStateUpdated(): MutableLiveData<NetState> {
        return onStateUpdatedLiveData
    }

    // observe home feed post
    fun observePostData(): MutableLiveData<PostsResponse?> {
        return myRepository.observePostsMutableLiveData()
    }

    // get home feed post
    fun getPostData(): PostsResponse? {
        return myRepository.observePostsMutableLiveData().value
    }

    fun setCreatePostLiveDataRepo(createPostData: Resource<CreatePostResponse>?, mapCreatePost: HashMap<String, RequestBody>?) {
        return myRepository.setCreatePostData(createPostData, mapCreatePost)
    }

    fun getCreatePostLiveDataRepo(): MutableLiveData<Resource<CreatePostResponse>> {
        return myRepository.getCreatePostLiveData()
    }
    fun createPostLiveData(postData: Map<String, RequestBody>) {
        createPostLiveData = myRepository.createPost(postData)
    }
}
