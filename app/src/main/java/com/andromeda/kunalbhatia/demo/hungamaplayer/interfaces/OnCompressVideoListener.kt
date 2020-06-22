package com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces

import android.net.Uri

interface OnCompressVideoListener {
    fun onCompressStarted()
    fun getResult(uri: Uri)
    fun onProgress(message: String?)
    fun onError(message: String)
    fun onProgressUpdate(progress : Long){}

}