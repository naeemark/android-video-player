package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostsResponse : BaseResponse(){
    @SerializedName("filter")
    @Expose
    var filter: PostFilter? = null
    @SerializedName("_previous")
    @Expose
    var _previous: Page? = null
    @SerializedName("next")
    @Expose
    var next: String? = null
    @SerializedName("current")
    @Expose
    var current: Int? = null
    @SerializedName("_next")
    @Expose
    var _next: Page? = null
    @SerializedName("total")
    @Expose
    var total: Int? = null
    @SerializedName("data")
    @Expose
    var data: ArrayList<PostResponseData>? = null
    @SerializedName("previous")
    @Expose
    var previous: String? = null
}