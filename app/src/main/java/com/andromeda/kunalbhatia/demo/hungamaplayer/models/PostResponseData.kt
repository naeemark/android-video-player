package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostResponseData() : Parcelable{
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("post")
    @Expose
    var postData: PostData? = null
    @SerializedName("share_by")
    @Expose
    var shareBy: PostShareBy? = null
    @SerializedName("group")
    @Expose
    var group: PostGroup? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("share_with")
    @Expose
    var shareWith: String? = null
    @SerializedName("admin")
    @Expose
    var admin: Boolean? = null

//    @SerializedName("comments")
//    @Expose
//    var comments: ArrayList<PostDataComments>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        postData = parcel.readParcelable(PostData::class.java.classLoader)
        shareBy = parcel.readParcelable(PostShareBy::class.java.classLoader)
        group = parcel.readParcelable(PostGroup::class.java.classLoader)
        created = parcel.readString()
        admin = parcel.readValue(Boolean::class.java.classLoader) as? Boolean

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeParcelable(postData, flags)
        parcel.writeParcelable(shareBy, flags)
        parcel.writeParcelable(group, flags)
        parcel.writeString(created)
        parcel.writeValue(admin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostResponseData> {
        override fun createFromParcel(parcel: Parcel): PostResponseData {
            return PostResponseData(parcel)
        }

        override fun newArray(size: Int): Array<PostResponseData?> {
            return arrayOfNulls(size)
        }
    }

}