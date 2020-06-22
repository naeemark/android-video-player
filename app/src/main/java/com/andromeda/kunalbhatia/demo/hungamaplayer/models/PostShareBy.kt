package com.andromeda.kunalbhatia.demo.hungamaplayer.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostShareBy() : Parcelable{
    
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("last_name")
    @Expose
    var lastName: String? = null
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null
    @SerializedName("username")
    @Expose
    var username: String? = null
    @SerializedName("image_path")
    @Expose
    var imagePath: String? = null
    
    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        lastName = parcel.readString()
        firstName = parcel.readString()
        username = parcel.readString()
        imagePath = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(lastName)
        parcel.writeString(firstName)
        parcel.writeString(username)
        parcel.writeString(imagePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostShareBy> {
        override fun createFromParcel(parcel: Parcel): PostShareBy {
            return PostShareBy(parcel)
        }

        override fun newArray(size: Int): Array<PostShareBy?> {
            return arrayOfNulls(size)
        }
    }

}
