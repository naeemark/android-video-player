package com.video.trimmer.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.GeneralUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.cropping.InstaCropperCredentials_Model
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnCropVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel.VideoOptions
import com.video.trimmer.utils.BackgroundExecutor
import com.video.trimmer.utils.RealPathUtil
import kotlinx.android.synthetic.main.view_cropper.view.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class VideoCropper @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var mSrc: Uri
    private var thumbnailUri: Uri? =null
    private var mOnCropVideoListener: OnCropVideoListener? = null
    private var mFinalPath: String? = null
    private var mMinRatio: Float = 1f
    private var mMaxRatio: Float = 1.78f
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0

    private var screenWidth: Int = 0

    private var destinationPath: String
        get() {
            if (mFinalPath == null) {
                val folder = Environment.getExternalStorageDirectory()
                mFinalPath = folder.path + File.separator
            }
            return mFinalPath ?: ""
        }
        set(finalPath) {
            mFinalPath = finalPath
        }

    init {
        init(context)
    }

//    public fun getCropVideoView(): InstaCropperView{
//        return cropFrame
//    }

    public fun getThumbnailUri(): Uri?{
        return thumbnailUri
    }

    public fun reset(){
        cropFrame.resetDisplay()
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_cropper, this, true)
        setUpListeners()
    }

    private fun setUpListeners() {
        cropSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                onCropProgressChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        handlerTop.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                loadFrame(seekBar?.progress ?: 0)
            }
        })
    }

//    fun onCropProgressChanged(progress: Int) {
//        val width: Int
//        val height: Int
//        val progressRatio = mMinRatio + ((abs(mMinRatio - mMaxRatio) / cropSeekbar.max) * progress)
//        if (videoWidth > videoHeight) {
//            height = (videoWidth / progressRatio).toInt()
//            width = videoWidth
//        } else {
//            width = (progressRatio * videoHeight).toInt()
//            height = videoHeight
//        }
//        Log.d("CROPPER", "onCropProgressChanged AspectRatio width: " + width + ", height: " + height)
//        cropFrame.setAspectRatio(width, width)
//    }

    fun setVideoURI(videoURI: Uri, isTrimmerModeEnabled: Boolean, screenWidth: Int): VideoCropper {
        this.screenWidth = screenWidth

        mSrc = videoURI
//        createThumbnail(mSrc)

        val thumbUrl = createThumb(RealPathUtil.realPathFromUriApi19(context, mSrc), context)

        loadAsync(Uri.parse(thumbUrl))
        loadFrame(0)

        if (isTrimmerModeEnabled) {
            seekerFrame.visibility = View.VISIBLE
            timeLineView.setVideo(mSrc)
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, mSrc)
            videoWidth = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            videoHeight = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        }
        return this
    }

    fun createThumbnail(uri: Uri){

////        val videoFile = File(uri.toString())
//        val thumbnailFile = GeneralUtils.createImageFile(context)
////        val thumbnailFile = File(context.cacheDir.toString()+File.separator+"thumb.jpg")
////        val thumbnailFile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "thumb.jpg")
////        thumbnailUri = Uri.fromFile(thumbnailFile)
//        thumbnailUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", thumbnailFile)
//        thumbnailUri = context?.let {
//            FileProvider.getUriForFile(
//                it,
//                BuildConfig.APPLICATION_ID + ".provider",
//                thumbnailFile
//            )
//        }
//
//        val bmThumbnail = createBitmapFromUrl(uri)
//        val os: OutputStream = context.contentResolver.openOutputStream(thumbnailUri)
//
//        bmThumbnail.let {
//            bmThumbnail?.compress(Bitmap.CompressFormat.JPEG, 100, os)
//        }
//        os.flush()
//        os.close()
    }

    fun createBitmapFromUrl(uri: Uri) : Bitmap?{
        var bitmap : Bitmap ? = null
        bitmap = ThumbnailUtils.createVideoThumbnail(GeneralUtils.getRealPathFromUri(context, uri), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)
        }
        return bitmap
    }

    fun onSaveClicked() {
        var model : InstaCropperCredentials_Model? = null
        cropFrame.croppedImageCredentials?.let {
            model = it
        }

//        cropFrame.calculateCroppedImageCredentails(500, 500)

        Handler().postDelayed({
            /* Create an Intent that will start the Menu-Activity. */

//        val rect = cropFrame.cropRect
//        val width = abs(rect.left - rect.right)
//        val height = abs(rect.top - rect.bottom)
//        val x = rect.left
//        val y = rect.top
            val file = File(RealPathUtil.realPathFromUriApi19(context, mSrc))
//        val file = File(mSrc.path ?: "")
            val root = File(destinationPath)
            root.mkdirs()
            val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
            val outPutPath = RealPathUtil.realPathFromUriApi19(context, outputFileUri)
                ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + mSrc.path?.substring(mSrc.path!!.lastIndexOf("/") + 1)).absolutePath
            val extractor = MediaExtractor()
            var frameRate = 24
            try {
                extractor.setDataSource(file.path)
                val numTracks = extractor.trackCount
                for (i in 0..numTracks) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (mime.startsWith("video/")) {
                        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                            frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                        }
                    }
                }
            } catch (e: Exception) {
                e.message
            } finally {
                extractor.release()
            }
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, Uri.parse(file.path))
            val duration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
            val frameCount = duration / 1000 * frameRate

//        cropFrame.calculateCroppedImageCredentails(600,600)

//            VideoOptions(context).cropVideo(cropFrame.instaCropperCredentials_model.width, cropFrame.instaCropperCredentials_model.height, cropFrame.instaCropperCredentials_model.x,cropFrame.instaCropperCredentials_model.y, file.path, outPutPath, outputFileUri, mOnCropVideoListener, frameCount.toInt())
            VideoOptions(context).cropVideo(model?.width,model?.height, model?.x,model?.y, file.path, outPutPath, outputFileUri, mOnCropVideoListener, frameCount.toInt())
        }, 200)


    }

    fun onCancelClicked() {
        mOnCropVideoListener?.cancelAction()
    }

    fun setOnCropVideoListener(onTrimVideoListener: OnCropVideoListener): VideoCropper {
        mOnCropVideoListener = onTrimVideoListener
        return this
    }

    fun setDestinationPath(path: String): VideoCropper {
        destinationPath = path
        return this
    }

    fun setMinMaxRatios(minRatio: Float, maxRatio: Float): VideoCropper {
        mMinRatio = minRatio
        mMaxRatio = maxRatio
//        cropFrame.setRatios(minRatio,minRatio,maxRatio)
//        cropFrame
//        onCropProgressChanged(50)
//        cropSeekbar.progress = 50
        return this
    }

    private fun loadFrame(progress: Int) {
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mSrc)
                    val videoLengthInMs = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000).toLong()
                    val seekDuration = (videoLengthInMs * progress) / 1000
                    val bitmap = mediaMetadataRetriever.getFrameAtTime(seekDuration * 10, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    if (bitmap != null) {
                        try {
//                            context.runOnUiThread {
//                                cropFrame.setImageUri(thumbnailUri)
//                                cropFrame.setImageBitmap(bitmap)
//                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                }
            }
        })
    }

    fun createThumb(
        inputPath: String?,
        context: Context?
    ): String? {
        var thumbnailPath : String? = ""
        val thumbnailBmp: Bitmap? = getVideoThumbnail(inputPath)
        thumbnailPath = try {
            saveBitmap(thumbnailBmp, context, true)
        } catch (e: Exception) {
            e.printStackTrace()
            return thumbnailPath
        }
        return thumbnailPath
    }

    fun getVideoThumbnail(path: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, Uri.parse(path))
            val width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            val height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()

            bitmap = mediaMetadataRetriever.getFrameAtTime(1)
//            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
//            scaledBitmap = ScalingUtilities.createScaledBitmap(
//                bitmap,
//                width,
//                height,
//                ScalingUtilities.ScalingLogic.FIT,
//                ScalingUtilities.getFileOrientation(path)
//            )
//            scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
//            scaledBitmap = getResizedBitmap(bitmap ,width, height)
        } catch (e: Exception) {
            bitmap = null
        } finally { //            fmmr.release();
        }
        return bitmap
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int) : Bitmap{

        val maxWidth : Int= newWidth
        val maxHeight : Int= newHeight
        val scale : Float = Math.min((maxHeight.toFloat() / bm.getWidth()), (maxWidth.toFloat() / bm.getHeight()))

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val bitmap: Bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true)
        return bitmap

//        val width = bm.getWidth();
//        val height = bm.getHeight();
//        val scaleWidth : Float = newWidth.toFloat() / width;
//        val scaleHeight : Float = newHeight.toFloat() / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        var matrix : Matrix = Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        var resizedBitmap : Bitmap = Bitmap.createBitmap(
//            bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
//    return resizedBitmap;
}

    ///// save Bitmap function
    fun saveBitmap(
        bitmap: Bitmap?,
        context: Context?,
        isCamera: Boolean
    ): String? {
//        val file_path =
//            Environment.getExternalStorageDirectory().absolutePath

        val dir = File(FileUtils.storageImagePath)
        if (!dir.exists()) dir.mkdirs()
        val date = Date()
        val file = File(dir, "Img_"+System.currentTimeMillis() + ".jpg")
        thumbnailUri = Uri.fromFile(file)
        var fOut: FileOutputStream? = null
        return try {
            fOut = FileOutputStream(file)
            /* if (isCamera) {
                    Bitmap bmp = AppUtils.getRotatedBmp(context, file.getPath(), bitmap);
                    if (bmp != null)
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
                    else
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fOut);

                } else*/bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.path
        } catch (e: Exception) {
            Log.d("TAG", "SaveBitmap Exp:\n"+e.message)
            ""
        }
    }

    private fun loadAsync(uri: Uri) {
        Log.i("TAG", "loadAsync: $uri")
        val task = DownloadAsync()
        task.execute(uri)
    }


    internal inner class DownloadAsync : AsyncTask<Uri, Void, Bitmap>(), DialogInterface.OnCancelListener {

        private var mUri: Uri? = null

        override fun doInBackground(vararg params: Uri): Bitmap? {
            mUri = params[0]

            var bitmap: Bitmap? = null
            mUri?.let {
                bitmap = BitmapFactory.decodeFile(it.toString())
//                bitmap = BitmapLoadUtils.decode(it.toString(), 1000, 1000)
            }

            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            result?.let {
                mUri?.let { uri ->
                    setImageURI(uri, result)
                }
            } ?: run {
                Log.d("TAG", "Failed to load image " + mUri?.toString())
            }
        }

        override fun onCancel(dialog: DialogInterface) {
            Log.i("TAG", "onProgressCancel")
            this.cancel(true)
        }

        override fun onCancelled() {
            super.onCancelled()
            Log.i("TAG", "onCancelled")
        }

    }

    private fun setImageURI(uri: Uri, bitmap: Bitmap) {
        Log.d("TAG", "image size: " + bitmap.width + "x" + bitmap.height)
        cropFrame.layoutParams = RelativeLayout.LayoutParams(screenWidth, screenWidth)
        cropFrame.setImageBitmap(bitmap)
        cropFrame.setBackgroundDrawable(null)

        thumbnailUri = uri
    }

//    private fun bitmapConvertToFile(bitmap: Bitmap): File? {
//        var fileOutputStream: FileOutputStream? = null
//        var bitmapFile: File? = null
//        try {
//            val file = File(FileUtils.storageImagePath)
//            if (!file.exists()) {
//                file.mkdir()
//            }
//
//            bitmapFile = File(file, "cropped_" + System.currentTimeMillis() + ".jpg")
//            fileOutputStream = FileOutputStream(bitmapFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//            MediaScannerConnection.scanFile(context, arrayOf(bitmapFile.absolutePath), null, object : MediaScannerConnection.MediaScannerConnectionClient {
//                override fun onMediaScannerConnected() {
//
//                }
//
//                override fun onScanCompleted(path: String, uri: Uri) {
//                    Log.d("TAG", "file saved")
//                    context?.let {
//                        (it as CropMediaActivity).cropMediaSuccessful(uri, PHOTO)
//                    }
//                }
//            })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            if (fileOutputStream != null) {
//                try {
//                    fileOutputStream.flush()
//                    fileOutputStream.close()
//                } catch (e: Exception) {
//                }
//
//            }
//        }
//
//        return bitmapFile
//    }
}