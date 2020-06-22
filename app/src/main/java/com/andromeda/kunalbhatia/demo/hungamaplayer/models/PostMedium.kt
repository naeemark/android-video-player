package com.andromeda.kunalbhatia.demo.hungamaplayer.models


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostMedium() : Parcelable{

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("label")
    @Expose
    var label: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("media")
    @Expose
    var media: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("thumbnail")
    @Expose
    var thumbnail: String? = null
    @SerializedName("width")
    @Expose
    var width: Int? = null
    @SerializedName("height")
    @Expose
    var height: Int? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        label = parcel.readString()
        type = parcel.readString()
        media = parcel.readString()
        created = parcel.readString()
        thumbnail = parcel.readString()
        width = parcel.readValue(Int::class.java.classLoader) as? Int
        height = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(label)
        parcel.writeString(type)
        parcel.writeString(media)
        parcel.writeString(created)
        parcel.writeString(thumbnail)
        parcel.writeValue(width)
        parcel.writeValue(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostMedium> {
        override fun createFromParcel(parcel: Parcel): PostMedium {
            return PostMedium(parcel)
        }

        override fun newArray(size: Int): Array<PostMedium?> {
            return arrayOfNulls(size)
        }
    }
}