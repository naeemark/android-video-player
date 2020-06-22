package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostGroup() : Parcelable {
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("cover_image")
    @Expose
    var coverImage: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(this.id)
        dest.writeString(this.title)
        dest.writeString(this.coverImage)
    }

    constructor(parcel: Parcel) : this() {
        this.id = parcel.readValue(Int::class.java.classLoader) as Int
        this.title = parcel.readString()
        this.coverImage = parcel.readString()
    }

    companion object CREATOR : Parcelable.Creator<PostGroup> {
        override fun createFromParcel(parcel: Parcel): PostGroup {
            return PostGroup(parcel)
        }

        override fun newArray(size: Int): Array<PostGroup?> {
            return arrayOfNulls(size)
        }
    }

}
