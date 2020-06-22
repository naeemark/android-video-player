package com.andromeda.kunalbhatia.demo.hungamaplayer

import androidx.lifecycle.MutableLiveData
import com.andromeda.kunalbhatia.demo.hungamaplayer.rest.APIs
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.Resource
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.CreatePostResponse
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.NetState
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostsResponse
import com.andromeda.kunalbhatia.demo.hungamaplayer.rest.ApiClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object HomePostRepository {

    private var postsMutableLiveData = MutableLiveData<PostsResponse?>()
    private var createPostLiveData = MutableLiveData<Resource<CreatePostResponse>>()
    private var createPostMapLiveData = MutableLiveData<HashMap<String, RequestBody>>()

    // fetching home feed post from network
    fun loadHomePostFeeds(limit: String, offset: String, onStateUpdatedLiveData : MutableLiveData<NetState>) {
        val api: APIs? = ApiClient.getClient()?.create(APIs::class.java)

        api?.getHomeFeedPosts("social_service", limit, offset, "public", "true", "false")
            ?.enqueue(object : Callback<PostsResponse> {
                override fun onResponse(call: Call<PostsResponse>?, response: Response<PostsResponse>?) {

                    val postResponse = response?.body()
                    if (response != null && response.isSuccessful && response.code() == 200 && postResponse != null && postResponse.code != 401 ) {
                        onStateUpdatedLiveData.postValue(NetState.LOADED)
                        val postDataResponse = response.body()
                        if (postDataResponse?.previous != null) {
                            if (!postsMutableLiveData.value?.data.isNullOrEmpty()) {
                                postDataResponse.data?.addAll(0, postsMutableLiveData.value?.data?: emptyList())
                                postsMutableLiveData.value = postDataResponse
                            }
                        }else{
                            postsMutableLiveData.value = postDataResponse
                        }

                    } else {
                        var message = ""
                        response?.message()?.let { message = it }
                        var code = -1
                        postResponse?.code?.let { code = it }
                        onStateUpdatedLiveData.postValue(NetState(NetState.Status.ERROR, message, code))
                    }
                }

                override fun onFailure(call: Call<PostsResponse>?, t: Throwable?) {
                    onStateUpdatedLiveData.postValue(t?.message?.let { NetState(NetState.Status.ERROR, it) })
                }
            })
    }

    fun observePostsMutableLiveData(): MutableLiveData<PostsResponse?> {
        return postsMutableLiveData
    }

    fun setCreatePostData(createPostData : Resource<CreatePostResponse>?, mapCreatePost: HashMap<String, RequestBody>?){
        createPostLiveData.value = createPostData
        createPostMapLiveData.value = mapCreatePost
    }
    fun getCreatePostLiveData() : MutableLiveData<Resource<CreatePostResponse>>{
        return createPostLiveData
    }

    fun createPost(postData: Map<String, RequestBody>): MutableLiveData<Resource<CreatePostResponse>> {

        val api: APIs = ApiClient.getClient()!!.create(APIs::class.java)
        val createPostCall = api.createPost(postData)
        createPostCall.enqueue(object : Callback<CreatePostResponse> {
            override fun onFailure(call: Call<CreatePostResponse>, t: Throwable) {
                createPostLiveData.value = Resource.error("Something went wrong " , null)
            }

            override fun onResponse(call: Call<CreatePostResponse>, response: Response<CreatePostResponse>) {
                if (response != null && response.isSuccessful && response.body() != null && (response.body()?.code == 200 || response.body()?.code == 201)) {
                    createPostLiveData.value = Resource.success(response.body())
                } else {
                    createPostLiveData.value = Resource.error(response.body()?.message?:"Something went wrong..", null)
                }
            }
        })
        return createPostLiveData

    }
}
