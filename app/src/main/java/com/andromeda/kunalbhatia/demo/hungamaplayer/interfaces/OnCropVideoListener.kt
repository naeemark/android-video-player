package com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces

import android.net.Uri

interface OnCropVideoListener {
    fun onCropStarted()
    fun getResult(uri: Uri)
    fun cancelAction()
    fun onError(message: String)
    fun onProgress(progress: Float)
}