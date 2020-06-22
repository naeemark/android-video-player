package com.andromeda.kunalbhatia.demo.hungamaplayer.models


import com.google.gson.annotations.SerializedName

open class BaseResponseStatus {
    @SerializedName("status")
    var status: Boolean? = null

    @SerializedName("message")
    var message: String? = null


    @SerializedName("code")
    var code: Int = 0

    @SerializedName("count")
    var count: Int = 0

}