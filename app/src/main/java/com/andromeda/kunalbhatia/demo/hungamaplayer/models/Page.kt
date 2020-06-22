package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Page {

    @SerializedName("search")
    @Expose
    var search: String? = null
    @SerializedName("limit")
    @Expose
    val limit: Int? = null
    @SerializedName("offset")
    @Expose
    val offset: Int? = null

}