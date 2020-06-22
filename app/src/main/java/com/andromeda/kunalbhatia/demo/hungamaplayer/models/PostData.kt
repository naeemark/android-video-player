package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostData() :Parcelable{
    @SerializedName("text")
    @Expose
    var text: String? = null
    @SerializedName("code")
    @Expose
    var code: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("like_count")
    @Expose
    var likeCount: Int? = null
    @SerializedName("comment_count")
    @Expose
    var commentCount: Int? = null
    @SerializedName("is_liked")
    @Expose
    var isLiked: Boolean? = null
    @SerializedName("is_muted")
    @Expose
    var isMuted: Boolean = false
    @SerializedName("media")
    @Expose
    var media: List<PostMedium>? = null
//
//    @SerializedName("test")
//    var shootingTest: UserScoreData? = null
//
//    @SerializedName("measurements")
//    var measurements: CombineMeasurements? = null

    constructor(parcel: Parcel) : this() {
        text = parcel.readString()
        created = parcel.readString()
        code = parcel.readString()
        likeCount = parcel.readValue(Int::class.java.classLoader) as? Int
        commentCount = parcel.readValue(Int::class.java.classLoader) as? Int
        isLiked = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isMuted = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeString(created)
        parcel.writeString(code)
        parcel.writeValue(likeCount)
        parcel.writeValue(commentCount)
        parcel.writeValue(isLiked)
        parcel.writeByte(if (isMuted) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostData> {
        override fun createFromParcel(parcel: Parcel): PostData {
            return PostData(parcel)
        }

        override fun newArray(size: Int): Array<PostData?> {
            return arrayOfNulls(size)
        }
    }
}