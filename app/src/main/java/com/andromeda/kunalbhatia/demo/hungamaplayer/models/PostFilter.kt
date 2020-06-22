package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import com.google.gson.annotations.SerializedName

class PostFilter {
    @SerializedName("search")
    var search: String? = null
    @SerializedName("limit")
    val limit: String? = null
    @SerializedName("offset")
    val offset: String? = null

    @SerializedName("score")
    var score: String? = null
    @SerializedName("posts_from")
    var postFrom: String? = null
    @SerializedName("measurement")
    var measurement: String? = null
    @SerializedName("group")
    var group: String? = null

    companion object{
        fun getDefaultPostFilter() : PostFilter{
            val postFilter = PostFilter()
            postFilter.postFrom = "public,followings,group"
            postFilter.score = "true"
            postFilter.measurement = "true"
            return postFilter
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

}