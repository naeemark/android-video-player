package com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces

import android.graphics.Bitmap

interface ImageCompression_Interface {

    fun imageCompressionSuccessfull(compressedImagePath: String)

    fun imageCompressionFailed(errorMessage: String?)

}