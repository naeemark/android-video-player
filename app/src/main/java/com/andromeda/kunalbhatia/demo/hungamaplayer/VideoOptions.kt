package com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnCompressVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnCropVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnTrimVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class VideoOptions(private var ctx: Context) {
    companion object {
        const val TAG = "VideoOptions"
    }
    var videoLengthInSec : Long = 0

    fun trimVideo(startPosition: String, endPosition: String, inputPath: String, outputPath: String, outputFileUri: Uri, listener: OnTrimVideoListener?) {
        val ff = FFmpeg.getInstance(ctx)
        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Log.e("FFmpegLoad", "onFinish")
            }

            override fun onSuccess() {
                Log.e("FFmpegLoad", "onSuccess")
                val command = arrayOf("-y", "-i", inputPath, "-ss", startPosition, "-to", endPosition, "-c", "copy", outputPath)
                try {
                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
                        override fun onSuccess(message: String?) {
                            super.onSuccess(message)
                            Log.e(TAG, "onSuccess: " + message!!)
                        }

                        override fun onProgress(message: String?) {
                            super.onProgress(message)
                            Log.e(TAG, "onProgress: " + message!!)
                        }

                        override fun onFailure(message: String?) {
                            super.onFailure(message)
                            listener?.onError(message.toString())
                            Log.e(TAG, "onFailure: " + message!!)
                        }

                        override fun onStart() {
                            super.onStart()
                            Log.e(TAG, "onStart: ")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            listener?.getResult(outputFileUri)
                            Log.e(TAG, "onFinish: ")
                        }
                    })
                } catch (e: FFmpegCommandAlreadyRunningException) {
                    listener?.onError(e.toString())
                }
            }

            override fun onFailure() {
                Log.e("FFmpegLoad", "onFailure")
                listener?.onError("Failed")
            }

            override fun onStart() {
            }
        })
        listener?.onTrimStarted()
    }

    fun cropVideo(width: Int?, height: Int?, x: Int?, y: Int?, inputPath: String, outputPath: String, outputFileUri: Uri, listener: OnCropVideoListener?, frameCount: Int) {
        if (width == null || height == null || x == null || y == null) {
            return
        }
        val ff = FFmpeg.getInstance(ctx)
        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Log.e("FFmpegLoad", "onFinish")
            }

            override fun onSuccess() {
                Log.e("FFmpegLoad", "onSuccess")
//                val command = arrayOf("$inputPath -filter format=rgb24,crop=$width:$height:$x:$y $outputPath")
//                val command = arrayOf("-i", inputPath, "-filter:v", "crop=$width:$height:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", outputPath)
//                val cmde = arrayOf("-i", inputPath ,"-filter:v", "crop=" + width + ":" +
//                        width + ":" + x + ":" + y, "-c:a", "copy", outputPath)
                val command = arrayOf("-i", inputPath, "-filter:v", "crop=$width:$height:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", outputPath)
//                val command = arrayOf("-i", inputPath, "-filter:v", "crop=$width:$height:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", outputPath)
                try {
                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
                        override fun onSuccess(message: String?) {
                            super.onSuccess(message)
                            Log.e(TAG, "onSuccess: " + message!!)
                        }

                        override fun onProgress(message: String?) {
                            super.onProgress(message)
                            if (message != null) {
                                val messageArray = message.split("frame=")
                                if (messageArray.size >= 2) {
                                    val secondArray = messageArray[1].trim().split(" ")
                                    if (secondArray.isNotEmpty()) {
                                        val framesString = secondArray[0].trim()
                                        try {
                                            val frames = framesString.toInt()
                                            val progress = (frames.toFloat() / frameCount.toFloat()) * 100f
                                            listener?.onProgress(progress)
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            }
                            Log.e(TAG, "onProgress: " + message!!)
                        }

                        override fun onFailure(message: String?) {
                            super.onFailure(message)
                            listener?.onError(message.toString())
                            Log.e(TAG, "onFailure: " + message!!)
                        }

                        override fun onStart() {
                            super.onStart()
                            Log.e(TAG, "onStart: ")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            listener?.getResult(outputFileUri)
                            Log.e(TAG, "onFinish: ")
                        }
                    })
                } catch (e: FFmpegCommandAlreadyRunningException) {
                    listener?.onError(e.toString())
                }
            }

            override fun onFailure() {
                Log.e("FFmpegLoad", "onFailure")
                listener?.onError("Failed")
            }

            override fun onStart() {
            }
        })
        listener?.onCropStarted()
    }

    fun compressVideo(inputPath: String, outputPath: String, outputFileUri: Uri, videoLength : Long, width: String, height: String, listener: OnCompressVideoListener?) {
        videoLengthInSec = videoLength/1000
        var preset = ""
        if (FileUtils.checkFileSizeIfGreaterThenAllowedSize(Uri.parse(inputPath))) {
            preset = "ultrafast"
        } else {
            preset = "faster"
        }

        val ff = FFmpeg.getInstance(ctx)
        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Log.e("FFmpegLoad", "onFinish")
            }

            override fun onSuccess() {
                Log.e("FFmpegLoad", "onSuccess")
                val command = arrayOf("-i", inputPath, "-preset", preset, "-vf", "scale=$width:$height", outputPath) //iw:ih
//                val command = arrayOf("-i", inputPath, "-vf", "scale=$width:$height", outputPath) //iw:ih
                try {
                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
                        override fun onSuccess(message: String?) {
                            super.onSuccess(message)
                            Log.e(TAG, "onSuccess: " + message!!)
                        }

                        override fun onProgress(message: String?) {
                            Log.e(TAG, "onProgress: " + message!!)
                            listener?.onProgressUpdate(getProgress(message))
                            super.onProgress(message)
                            if (message != null) {
                                val messageArray = message.split("frame=")
                                if (messageArray.size >= 2) {
                                    val secondArray = messageArray[1].trim().split(" ")
                                    if (secondArray.isNotEmpty()) {
                                        val framesString = secondArray[0].trim()
                                        try {

                                            listener?.onProgress(framesString)
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            }

//                            listener?.onProgress(message.toString())
                            Log.e(TAG, "onProgress: " + message!!)
                        }

                        override fun onFailure(message: String?) {
                            super.onFailure(message)
                            listener?.onError(message.toString())
                            Log.e(TAG, "onFailure: " + message!!)
                        }

                        override fun onStart() {
                            super.onStart()
                            Log.e(TAG, "onStart: ")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            listener?.getResult(outputFileUri)
                            Log.e(TAG, "onFinish: ")
                        }
                    })
                } catch (e: FFmpegCommandAlreadyRunningException) {
                    listener?.onError(e.toString())
                }
            }

            override fun onFailure() {
                Log.e("FFmpegLoad", "onFailure")
                listener?.onError("Failed")
            }

            override fun onStart() {
            }
        })
        listener?.onCompressStarted()
    }

    fun getProgress(message : String?) :Long {
        if(message != null){
            val pattern = Pattern.compile("time=([\\d\\w:]+)")
            if (message.contains("speed")) {
                val matcher = pattern.matcher(message)
                matcher.find()
                val tempTime : String = (matcher.group(1).toString())
                Log.d("TAG", "getProgress: tempTime " + tempTime)
                val arrayTime  = tempTime.split(":")
                var currentTime : Long = TimeUnit.HOURS.toSeconds(arrayTime[0].toLong()) + TimeUnit.MINUTES.toSeconds(arrayTime[1].toLong()) + arrayTime[2].toLong()
                val percent : Long = 100 * currentTime/videoLengthInSec
                Log.d(TAG, "currentTime -> " + currentTime + "s % -> " + percent)
                return percent
            }
        }
        return 0;
    }

}