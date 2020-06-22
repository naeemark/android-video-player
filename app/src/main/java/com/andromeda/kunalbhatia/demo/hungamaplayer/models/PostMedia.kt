package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostMedia() : Parcelable{

    @SerializedName("width")
    @Expose
    var width: Int? = null
    @SerializedName("height")
    @Expose
    var height: Int? = null

    var path: Uri? = null

    var mediaType: String = ""

    var isEditMode : Boolean = false

    constructor(parcel: Parcel) : this() {
        width = parcel.readValue(Int::class.java.classLoader) as? Int
        height = parcel.readValue(Int::class.java.classLoader) as? Int
        path = parcel.readParcelable(Uri::class.java.classLoader)
        mediaType = parcel.readString().toString()
        isEditMode = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeParcelable(path, flags)
        parcel.writeString(mediaType)
        parcel.writeByte(if (isEditMode) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostMedia> {
        override fun createFromParcel(parcel: Parcel): PostMedia {
            return PostMedia(parcel)
        }

        override fun newArray(size: Int): Array<PostMedia?> {
            return arrayOfNulls(size)
        }
    }
}