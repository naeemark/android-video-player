package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import com.google.gson.annotations.SerializedName

class CreatePostResponse: BaseResponseStatus() {
    @SerializedName("data")
    var data: PostData? = null
//
//    @SerializedName("status")
//    var status: String? = null
//
//    @SerializedName("message")
//    var message: String? = null
//
//    @SerializedName("code")
//    var code: Int = 0
}