package com.andromeda.kunalbhatia.demo.hungamaplayer.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.andromeda.kunalbhatia.demo.hungamaplayer.FileCompressor
import com.andromeda.kunalbhatia.demo.hungamaplayer.ImageCompression
import com.andromeda.kunalbhatia.demo.hungamaplayer.PermissionsChecker
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.activities.CreatePostActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.activities.CropMediaActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.activities.PermissionsActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.Status
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.CropMedia_Interface
import com.andromeda.kunalbhatia.demo.hungamaplayer.*
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.Constants
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.MediaTypes
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.ImageCompression_Interface
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnCompressVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostMedia
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.compressedVideoFilePath
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.getVideoWidthOrHeight
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.rootPath
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.storageImagePath
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.GeneralUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.ImageUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel.HomePostViewModel
import com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel.VideoOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import com.video.trimmer.utils.RealPathUtil
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.GalleryVideoFilter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import io.github.lizhangqu.coreprogress.ProgressHelper
import io.github.lizhangqu.coreprogress.ProgressUIListener
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.fragment_create_post.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class CreatePostFragment : Fragment(),
    ModeSelectDialogFragment.ModeSelectListener,
    CropMedia_Interface {

    lateinit var rxPermissions: RxPermissions
    private var currentTempProgress: Float = 0.0f
    private var currentProgressOfProgressBar: Float = 0.0f
    private var numberOfFiles: Int = 0
    private var progressPerChunk: Int = 0
    private var totalProgress: Int = 0
    private var indexOfFileInProcess: Int = 0
    private var uploadingTextView: AppCompatTextView? = null
    private var postUploadingProgress: ProgressBar? = null
    private var createPostDialog: Dialog? = null
    private var SHARE: Boolean = false
    private var IsGIF: Boolean = false
    var isGallerySelected: Boolean = false
    val TAKE_PHOTO_CODE = 100
    val REQUEST_CODE_PERMISSIONS_CAMERA = 102
    val REQUEST_CODE_PERMISSIONS_GALLERY = 103
    lateinit var mPhotoFile: File
    lateinit var mCompressor: FileCompressor


    private lateinit var rootView: View
    private val REQUEST_CODE_CHOOSE = 23
    var postText = ""
    lateinit var mChecker: PermissionsChecker
    lateinit var postsAdapter: PostMediaAdapter
    lateinit var mediaList: ArrayList<PostMedia>
    val mapCreatePost = LinkedHashMap<String, RequestBody>()
    private lateinit var postViewModel: HomePostViewModel
    var compressedVideoPath = ""
    private var groupId: Int? = null
    private var groupVerified = false
    private var isEditPost = false
    var mimeType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle()
    }

    private fun getBundle() {
        arguments?.let {
            if (it.containsKey("SHARE")) {
                SHARE = it.getBoolean("SHARE")
                IsGIF = it.getBoolean("IsGIF")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_create_post, container, false)
        if (groupId != null) {
            rootView.tv_privacy.text = getString(R.string.who_see_group)
            rootView.rb_everyone.text = getString(R.string.group_feed)
            rootView.rb_followers.text = getString(R.string.group_home_feed)
            rootView.tv_formats_txt.text =  if(groupVerified){
                String.format(Locale.ENGLISH, resources.getString(R.string.text_file_format_value), 150)
            }else{
                String.format(Locale.ENGLISH, resources.getString(R.string.text_file_format_value), 60)
            }
        } else {
            rootView.tv_privacy.text = getString(R.string.who_see_post)
            rootView.rb_everyone.text = getString(R.string.everyone)
            rootView.rb_followers.text = getString(R.string.only_followers)
            rootView.tv_formats_txt.text = String.format(Locale.ENGLISH, resources.getString(
                R.string.text_file_format_value
            ), 60)
        }

        rxPermissions = RxPermissions(this)
        postViewModel = ViewModelProviders.of(this).get(HomePostViewModel::class.java)
        return rootView
    }

    private fun openUploadingPopup(isCompression : Boolean) {
        val createPostDialogBuilder = AlertDialog.Builder(context!!)
        val inflater = activity?.getLayoutInflater()
        val dialogView = if(isCompression) inflater?.inflate(R.layout.compress_popup_layout, null) else inflater?.inflate(
            R.layout.post_uploading_popup, null)
        createPostDialogBuilder.setView(dialogView)

        postUploadingProgress = dialogView?.findViewById<ProgressBar>(R.id.progressPostUploading)
        uploadingTextView = dialogView?.findViewById<AppCompatTextView>(R.id.uploadingText)
        createPostDialog = createPostDialogBuilder.create()
        createPostDialog?.setCancelable(false)
        createPostDialog?.show()
    }

    private fun updateFileUploadProgressText(isCompression : Boolean, fileIndex: Int){
        uploadingTextView?.text = if(isCompression) ("$fileIndex %")
        else context?.resources?.getString(R.string.uploadingAttachmentText) + " " + fileIndex.toString() + " of " + mediaList.size
    }

    private fun calculateProgressDefaultValues(){
            numberOfFiles = mediaList.size
            totalProgress = 100
            progressPerChunk = totalProgress / numberOfFiles
    }

    private fun calculateProgressValues(currentFileProgress : Float){

        if (currentFileProgress > 0 && progressPerChunk > 0) {
            if (currentTempProgress == 1.0f) {
                currentTempProgress = 0.0f
            }

            currentProgressOfProgressBar = currentProgressOfProgressBar + ( (currentFileProgress-currentTempProgress) * progressPerChunk ) //3.3
            if( currentFileProgress > currentTempProgress ){
                currentTempProgress = currentFileProgress // 0.1
            }

        }

        updateProgress(currentProgressOfProgressBar.toInt())
    }

    private fun updateProgress(progress: Int){
        postUploadingProgress?.setProgress(progress)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress_bar.bringToFront()
        mChecker = PermissionsChecker(context)
        mCompressor = FileCompressor(context)
        init()
        setListeners()
        if (SHARE) {
            val extension = arguments?.getString("extension")?:""
            val postMedia = PostMedia()
            postMedia.path = arguments?.getParcelable<Uri>("fileUri")
            postMedia.mediaType = extension
            setMediaAdapter(postMedia)
            if (extension.contains("gif")) {
                enableEditMedia(true)
                enable_textField_radioBtns_buttons(true)
            } else {
                createIntent()
            }
        }
    }

    fun createIntent() {
        val share = arguments?.get("SHARE") as Boolean
        val step = arguments?.get("STEP") as String
        val task = arguments?.get("TASK") as String
        val fileUri = arguments?.get("fileUri") as Uri
        //go to crop media here
        val intent = Intent(
            activity,
            CropMediaActivity::class.java
        )
        CropMediaActivity.cropMediaInterface = this
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("SHARE", share)
        intent.putExtra("STEP", step)
        intent.putExtra("TASK", task)
        intent.putExtra("fileUri", fileUri)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if  (mediaList!=null)
        {
            if  (mediaList.size < 1)
            {
                enable_textField_radioBtns_buttons(false)
            }
        }
    }
    private fun init() {
        mediaList = ArrayList()
        postViewModel.setCreatePostLiveDataRepo(null, null)
        postViewModel.getCreatePostLiveDataRepo().observe(this, Observer {

            if ((it?.data?.code == 200 || it?.data?.code == 201) && it.status == Status.SUCCESS) {
                progress_bar?.visibility = View.GONE
                enablePreviewAndSharePost(true)
                postViewModel.getCreatePostLiveDataRepo().value = null
                FileUtils.deleteFolderForMedia(File(rootPath))
                activity?.finish()
                if (createPostDialog != null && createPostDialog?.isShowing?:false) {
                    createPostDialog?.dismiss()
                }
                if (SHARE) {
                    val intent = Intent(
                        activity,
                        CreatePostActivity::class.java
                    )
                    startActivity(intent)
                }

            } else if (it?.message != null && it.status == Status.ERROR) {
                progress_bar?.visibility = View.GONE
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
        })
        enablePreviewAndSharePost(false)
        etComment.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                p0?.parent?.requestDisallowInterceptTouchEvent(true)
                if (MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP || p1?.action == MotionEvent.ACTION_UP) {
                    view?.parent?.requestDisallowInterceptTouchEvent(false)
                }
                return false
            }
        })
    }

    private fun setMediaAdapter(postMedia: PostMedia?) {

        if (postMedia != null) {
            mediaList.add(postMedia)
            rv_media.visibility = View.VISIBLE
            postsAdapter = context?.let {
                PostMediaAdapter(mediaList, it) {

                    if (mediaList.size == 0) {
                        tv_edit.text =
                            getString(R.string.edit_post)
                        enableEditButton(false)
                        enablePreviewAndSharePost(false)
                        enable_textField_radioBtns_buttons(false)
                        enableAddMedia(true)
                    } else {
                        tv_edit.text =
                            getString(R.string.cancel).toUpperCase()
                        enableEditButton(true)
                        enablePreviewAndSharePost(false)
                        enable_textField_radioBtns_buttons(false)
                        enableAddMedia(false)
                    }
                }
            }!!
            rv_media.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rv_media.adapter = postsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_CHOOSE || requestCode == TAKE_PHOTO_CODE) && resultCode == Activity.RESULT_OK) {
            setMediaItem(requestCode, data, false, null)
        } else if (requestCode == REQUEST_CODE_PERMISSIONS_CAMERA && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
                if (mChecker.lacksPermissions(*Constants.PERMISSIONS)) {
                    PermissionsActivity.startActivityForResult(
                        activity,
                        REQUEST_CODE_PERMISSIONS_CAMERA,
                        *Constants.PERMISSIONS
                    )
                }else{
                    FileUtils.deleteFolderForMedia(File(rootPath))
                    FileUtils.createFolderForMedia()
                    launchCamera()
                }
            }
        }
        else if (requestCode == REQUEST_CODE_PERMISSIONS_GALLERY && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
                FileUtils.deleteFolderForMedia(File(rootPath))
                FileUtils.createFolderForMedia()
                launchPicker(false)
            }
        }
    }

    private fun setMediaItem(code: Int, data: Intent?, isCropped: Boolean, resultUri: Uri?) {
        var uri: List<Uri>? = null
        var postMedia = PostMedia()
        enablePreviewAndSharePost(true)
        if (code == REQUEST_CODE_CHOOSE || isGallerySelected) {
            if (!isCropped) {
                uri = Matisse.obtainResult(data)
                if (uri.get(0).getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                    val cr = context?.getContentResolver()
                    mimeType = cr?.getType(uri.get(0)).toString()
                } else {
                    val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                        uri.toString()
                    )
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        fileExtension.toLowerCase()
                    ).toString()
                }
                postMedia?.path = uri.get(0)
            } else {
                postMedia?.path = resultUri
            }


        } else {
            postMedia.path = Uri.fromFile(mPhotoFile)
            mimeType = getMimeType(mPhotoFile.path)

            if (resultUri != null && resultUri.toString().isNotEmpty()) {
                postMedia?.path = resultUri
            }
        }

        if (mimeType.equals(MimeType.JPEG.toString(), true)
            || mimeType.equals(MimeType.PNG.toString(), true)
        ) {
            if (!isCropped) {
                val intent = Intent(activity, CropMediaActivity::class.java)
                CropMediaActivity.cropMediaInterface = this
                intent.putExtra("SHARE", false)
                intent.putExtra("STEP", "CROP")
                intent.putExtra("TASK", "PHOTO_CROPPING")
                intent.putExtra("fileUri", postMedia.path)
                startActivity(intent)
            }
            postMedia.mediaType = MediaTypes.PHOTO.toString()
        }
        else if (mimeType.equals(MimeType.MP4.toString(), true)
            || mimeType.equals(MimeType.QUICKTIME.toString(), true)
            || mimeType.equals(MimeType.MPEG.toString(), true)
        ) {
            val f =
                File(Environment.getExternalStorageDirectory().absolutePath + "/" + context?.getPackageName() + "/media/videos")
            postMedia.mediaType = MediaTypes.VIDEO.toString()
            if (f.mkdirs() || f.isDirectory) {
                if (uri != null && uri.isNotEmpty()) {

                    if (!isCropped) {
                        val intent = Intent(activity, CropMediaActivity::class.java)
                        CropMediaActivity.cropMediaInterface = this
                        intent.putExtra("SHARE", false)
                        intent.putExtra("STEP", "CROP")
                        intent.putExtra("TASK", "VIDEO_CROPPING")
                        intent.putExtra("fileUri", postMedia.path)
                        startActivity(intent)
                    }else{
                        compressedVideoPath = resultUri.toString()
                        postMedia.path = resultUri
                    }
                }
            }

        } else if (mimeType.equals(MimeType.GIF.toString(), true))
            postMedia.mediaType = MediaTypes.GIF.toString()
        setMediaAdapter(postMedia)
        enableDisableButtons()
    }

    fun getMimeType(path: String): String {
        var type = "image/jpeg" // Default Value
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }

    private fun setMediaToMap() {
        if (mediaList.size != 0) {
            var index = 0
            mediaList.forEach {
                var fileBody: RequestBody
                if (it.mediaType.equals(MediaTypes.PHOTO.toString(), true)) {
                    val uriString = it.path.toString()
                    val extension = uriString.substring(uriString.lastIndexOf(".")+1)
                    val file = File(RealPathUtil.realPathFromUriApi19(activity!!, it.path!!))
                    fileBody = file.asRequestBody("image/$extension".toMediaTypeOrNull())
                    fileBody = SetupUploadDataTrackerListener(fileBody)
                    mapCreatePost.put("media\"; filename=\"post$index.$extension\"", fileBody)
                } else if (it.mediaType.equals(MediaTypes.GIF.toString(), true)) {
                    val realPath =
                        it.path?.let {
                            GeneralUtils.getRealPathFromUri(
                                activity,
                                it
                            )
                        }
                    val file = File(realPath)
                    fileBody = file.asRequestBody("image/gif".toMediaTypeOrNull())
                    fileBody = SetupUploadDataTrackerListener(fileBody)
                    mapCreatePost.put("media\"; filename=\"post$index.gif\"", fileBody)
                } else if (it.mediaType.equals(MediaTypes.VIDEO.toString(), ignoreCase = true)) {
                    if (!isEditPost) {
                        val uriString = it.path.toString()
                        if (!uriString.isNullOrEmpty()) {

                            val file = File(RealPathUtil.realPathFromUriApi19(activity!!, Uri.parse(uriString)))
                            val extension =
                                uriString.substring(uriString.lastIndexOf(".")+1)
                            var fileBodyTemp = file.asRequestBody("video/$extension".toMediaTypeOrNull())
                            fileBodyTemp = SetupUploadDataTrackerListener(fileBodyTemp)
                            mapCreatePost.put("media\"; filename=\"post$index.$extension", fileBodyTemp)
                        } else {
                            Toast.makeText(
                                context,
                                "Video is not processed successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                index++
            }
        }
    }

    private fun SetupUploadDataTrackerListener(requestBody: RequestBody): RequestBody{
        val body = ProgressHelper.withProgress(requestBody, object: ProgressUIListener(){
            override fun onUIProgressStart(totalBytes: Long) {
                super.onUIProgressStart(totalBytes)
                Log.d("TAG", "onProgressStart totalBytes: " + totalBytes)
                indexOfFileInProcess++
                if (indexOfFileInProcess == 1) {
                    calculateProgressDefaultValues()
                }
            }

            override fun onUIProgressChanged(
                numBytes: Long,
                totalBytes: Long,
                percent: Float,
                speed: Float
            ) {
                updateFileUploadProgressText(false, indexOfFileInProcess)
                calculateProgressValues(percent)
                Log.d("TAG", "onProgressChanged numBytes / totalBytes: " + numBytes + "/" + totalBytes + " , percent: " + (percent*100).toInt() + ", speed: " + speed)
            }

            override fun onUIProgressFinish() {
                super.onUIProgressFinish()
                Log.d("TAG", "onProgressFinish ")
            }

        })
        return body
    }

    private fun enableEditMedia(enabled: Boolean) {

        if (mediaList.size == 5) {
            ib_add_media.visibility = View.INVISIBLE
        } else {
            ib_add_media.visibility = View.VISIBLE
        }

        tv_edit.isEnabled = enabled
        tv_edit.isClickable = enabled
        when (enabled) {
            true -> tv_edit.alpha = 1f
            false -> tv_edit.alpha = .4f
        }
    }

    private fun setListeners() {

        ib_add_media.setOnClickListener {
            showAddProfilePicDialog()
        }
        tv_edit.setOnClickListener {
            when (mediaList.get(mediaList.size-1).isEditMode) {
                true -> {
                    tv_edit.text = getString(R.string.edit_post)
                    mediaList.forEachIndexed { index, postMedia ->
                        postMedia.isEditMode = false
                    }
                    postsAdapter.notifyDataSetChanged()
                    enableAddMedia(true)
                    enablePreviewAndSharePost(true)
                    enable_textField_radioBtns_buttons(true)
                }
                else -> {
                    mediaList.forEachIndexed { index, postMedia ->
                        postMedia.isEditMode = true
                    }
//                    mediaList[mediaList.size-1].isEditMode = true
                    postsAdapter.notifyDataSetChanged()
                    tv_edit.text = getString(R.string.cancel).toUpperCase()
                    enableAddMedia(false)
                    enablePreviewAndSharePost(false)
                    enable_textField_radioBtns_buttons(false)
                }
            }

        }
        etComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                postText = s.toString()
                if (mediaList.isEmpty())
                    enablePreviewAndSharePost(false)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        btn_share_post.setOnClickListener {
            enableFieldsButtons(false)
            if (!isEditPost) {
                openUploadingPopup(false)
            }
            progress_bar.visibility = View.VISIBLE
            setMediaToMap()
            mapCreatePost.put("text",
                GeneralUtils.toTextRequestBody(
                    postText
                )
            )
            if (groupId != null) {
                mapCreatePost.put("group",
                    GeneralUtils.toTextRequestBody(
                        groupId.toString()
                    )
                )
            }
            if (isEditPost) {
            } else {
                postViewModel.createPostLiveData(mapCreatePost)
            }
        }

        rg_privacy.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_everyone -> {
                    val value = if (groupId != null) "group" else "everyone"
                    mapCreatePost.put(
                        "share_with",
                        GeneralUtils.toTextRequestBody(
                            value
                        )
                    )
                }
                R.id.rb_followers -> {
                    val value = if (groupId != null) "group_and_home_feed" else "followers"
                    mapCreatePost.put(
                        "share_with",
                        GeneralUtils.toTextRequestBody(
                            value
                        )
                    )
                }
            }
        }
        rb_everyone.isChecked = true
    }

    private fun enableFieldsButtons(enabled: Boolean) {
        tv_edit.isClickable = enabled
        tv_edit.isEnabled = enabled
        etComment.setFocusableInTouchMode(false)
        etComment.setFocusable(false)
        rb_everyone.isClickable = enabled
        rb_followers.isClickable = enabled
        btn_preview.isClickable = enabled
        btn_share_post.isClickable = enabled
        when (enabled){
            true -> {
                btn_preview.alpha = .4f
                btn_share_post.alpha = .4f
                etComment.alpha = .4f
            }
            false -> {
                btn_preview.alpha = 1f // because these are already dimmed
                btn_share_post.alpha = 1f
                etComment.alpha = 1f
            }
        }
    }

    fun enable_textField_radioBtns_buttons(enabled: Boolean)
    {
        if (mediaList.size == 5) {
            ib_add_media.visibility = View.INVISIBLE
        } else {
            ib_add_media.visibility = View.VISIBLE
        }
        etComment.isEnabled = enabled
        rb_everyone.isEnabled = enabled
        rb_followers.isEnabled = enabled
        btn_preview.isClickable = enabled
        btn_preview.isEnabled = enabled
        btn_share_post.isClickable = enabled
        btn_share_post.isEnabled = enabled
        when (enabled) {
            true -> {
                etComment.alpha = 1f
                rg_privacy.alpha = 1f
                tv_privacy.alpha = 1f
                tv_comment.alpha = 1f
                btn_preview.alpha = 1f
                btn_share_post.alpha = 1f
            }
            false -> {
                etComment.alpha = .4f
                rg_privacy.alpha = .4f
                tv_privacy.alpha = .4f
                tv_comment.alpha = .4f
                btn_preview.alpha = .4f
                btn_share_post.alpha = .4f
            }
        }


    }

    private fun enableEditButton(enabled: Boolean) {
        tv_edit.isClickable = enabled
        tv_edit.isEnabled = enabled
        when (enabled){
            true -> {
                tv_edit.alpha = 1f
            }
            false -> {
                tv_edit.alpha = .4f
            }
        }
    }

    private fun enableAddMedia(enabled: Boolean) {
        ib_add_media.isClickable = enabled
        ib_add_media.isEnabled = enabled
        when (enabled) {
            true -> {
                ib_add_media.alpha = 1.0f
            }
            false -> {
                ib_add_media.alpha = 0.4f
            }

        }
    }

    fun enablePreviewAndSharePost(enabled: Boolean) {

        when (enabled) {
            true -> {
                btn_preview.alpha = 1f
                btn_share_post.alpha = 1f
            }
            false -> {
                btn_preview.alpha = .4f
                btn_share_post.alpha = .4f
            }
        }
        btn_preview.isClickable = enabled
        btn_preview.isEnabled = enabled
        btn_share_post.isClickable = enabled
        btn_share_post.isEnabled = enabled
    }

    fun enableShareWithRadioButtons(enabled: Boolean) {

        when (enabled) {
            true -> {
                rb_everyone.alpha = 1f
                rb_followers.alpha = 1f
            }
            false -> {
                rb_everyone.alpha = .4f
                rb_followers.alpha = .4f
            }
        }
        rb_everyone.isClickable = enabled
        rb_everyone.isEnabled = enabled
        rb_followers.isClickable = enabled
        rb_followers.isEnabled = enabled
    }

    fun launchPicker(isCameraEnable: Boolean) {
        isGallerySelected = true
        val galleryVideoFilter = if(groupId != null && groupVerified) {
            GalleryVideoFilter().setMaxVideoDuration(150700)
        } else {
            GalleryVideoFilter().setMaxVideoDuration(60700)
        }
        Matisse.from(this)
            .choose(MimeType.ofAll(), false)
            .countable(false)
            .capture(isCameraEnable)
            .captureStrategy(
                CaptureStrategy(true, BuildConfig.APPLICATION_ID + ".provider")
            )
            .maxSelectable(1)
            .gridExpectedSize(
                resources.getDimensionPixelSize(R.dimen.grid_expected_size)
            )
            .addFilter(galleryVideoFilter)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(GlideEngine())
            .originalEnable(false)
            .maxOriginalSize(20)
            .autoHideToolbarOnSingleTap(true)
            .forResult(REQUEST_CODE_CHOOSE)
    }

    private fun showAddProfilePicDialog() {
        val fm = activity!!.supportFragmentManager
        val dialogFragment =
            ModeSelectDialogFragment()
        dialogFragment.setModeSelectListener(this)
        dialogFragment.show(fm, "picModeSelector")
    }

    override fun onModeSelected(mode: String?) {
        when (mode) {
            ImageUtils.PicModes.CAMERA ->
                Toast.makeText(activity, "Under Development", Toast.LENGTH_SHORT).show()
            ImageUtils.PicModes.GALLERY ->
            {
                rxPermissions.request(*Constants.PERMISSIONS).subscribe { granted ->
                    if (granted) {
                        if (mediaList != null && mediaList.size == 0) {
                            FileUtils.deleteFolderForMedia(File(rootPath))
                            FileUtils.createFolderForMedia()
                        }
                        launchPicker(false)
                    } else {
                    }
                }
            }
        }
    }

    private fun launchCamera() {
        isGallerySelected = false
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context?.packageManager!!) != null) {
            var photoFile: File? = null
            try {
                photoFile = File(storageImagePath + File.separator + "capture_" + System.currentTimeMillis() + ".jpg")
            } catch (ex: IOException) {
                Log.d("TAG", "dispatchTakePIctureIntent Exp:\n"+ex.message)
            }

            if (photoFile != null) {
                val photoURI = Uri.fromFile(photoFile)
                mPhotoFile = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE)
            }
        }
    }

    override fun TrimSuccessFull() {
        Log.d("TAG", "TrimSuccessFull")
    }

    override fun trimCanceled() {
        Log.d("TAG", "trimCanceled")
        clearMedia()
    }

    override fun cropSuccessFull(uri: Uri?, fileType: String) {
        Log.d("TAG", "cropSuccessFull uri: " + uri.toString() + " fileType: " + fileType)
        if (fileType.equals(FileUtils.VIDEO, true)) {
            compressVideo(uri)
        } else {
            enableAddMedia(false)
            enableDisableButtons()
            progress_bar.bringToFront()
            progress_bar.visibility = View.VISIBLE
            ImageCompression(object :
                ImageCompression_Interface {
                override fun imageCompressionSuccessfull(compressedImagePath: String) {
                    Log.d("TAG", "imageCompressionSuccessfull: \n" + compressedImagePath)
                    val file = File(compressedImagePath)
                    mediaList.get(mediaList.size - 1).path = Uri.fromFile(file)
                    postsAdapter.mediaItems = mediaList
                    rv_media.adapter?.notifyDataSetChanged()
                    progress_bar.visibility = View.GONE
                    enableDisableButtons()
                }

                override fun imageCompressionFailed(errorMessage: String?) {
                    enableDisableButtons()
                    Log.d("TAG", "imageCompressionFailed: \n" + errorMessage)
                    progress_bar.visibility = View.GONE
                    clearMedia()
                }

            }).execute(RealPathUtil.realPathFromUriApi19(activity!!, uri!!))
        }
    }

    private fun enableDisableButtons(){
        try {

            if (mediaList != null && mediaList.size == 0) {
                enableEditButton(false)
                enableAddMedia(true)
                enable_textField_radioBtns_buttons(false)
            } else if (mediaList.size > 0 && mediaList.size < 5){
                enableEditButton(true)
                enableAddMedia(true)
                enable_textField_radioBtns_buttons(true)
            } else if(mediaList.size == 5){
                enableEditButton(true)
                enableAddMedia(false)
                enable_textField_radioBtns_buttons(true)
            } else {
                enableEditButton(true)
                enableAddMedia(false)
                enable_textField_radioBtns_buttons(false)
            }

        } catch (e: Exception) {
            Log.d("TAG", "enableDisableButtons Exp:\n"+e.message)
        }
    }

    override fun nativeSuccessFull(uri: Uri?) {
        compressVideo(uri)
    }

    override fun cropCanceled() {
        Log.d("TAG", "cropCanceled")
         clearMedia()
    }

    private fun compressVideo(uri: Uri?){
        enable_textField_radioBtns_buttons(false)
        enableEditButton(false)
        enableAddMedia(false)
        progress_bar.bringToFront()
        progress_bar.visibility = View.VISIBLE
        val uriString = uri.toString()
        val extension = uriString.substring(uriString.lastIndexOf("."))
        val compressedFile = File(compressedVideoFilePath + File.separator + "compressed_" + System.currentTimeMillis() + extension)
        mediaList.get(mediaList.size-1).path = uri

        postsAdapter.mediaItems = mediaList
        rv_media.adapter?.notifyDataSetChanged()

        val widthHeightArray : Array<Int> = getVideoWidthOrHeight(File(uri.toString()))

        val pathVideoFile = RealPathUtil.realPathFromUriApi19(activity!!, uri!!)?:""
        val tempFile = File(pathVideoFile)
        val videoLength = tempFile.getMediaDuration(Uri.parse(pathVideoFile))

        VideoOptions(activity!!).compressVideo(
            pathVideoFile,
            compressedFile.path,
            Uri.fromFile(compressedFile),
            videoLength,
            widthHeightArray[0].toString(),
            widthHeightArray[1].toString(),
            object : OnCompressVideoListener {
                override fun onProgressUpdate(progress: Long) {
                    Log.d("TAG" , " onProgressUpdate $progress")
                    updateProgress(progress.toInt())
                    updateFileUploadProgressText(true, progress.toInt())
                    if (progress.toInt() == 100 && createPostDialog != null && createPostDialog?.isShowing?:false) {
                        createPostDialog?.dismiss()
                    }

                }
                override fun onCompressStarted() {
                    Log.d("TAG", "onCompressStarted")
                    openUploadingPopup(true)
                }

                override fun getResult(uri: Uri) {
                    if (isVisible) {
                        Log.d("TAG", "onResult: " + uri.toString())
                        mediaList.get(mediaList.size-1).path = uri
                        postsAdapter.mediaItems = mediaList
                        rv_media.adapter?.notifyDataSetChanged()
                        progress_bar.visibility = View.GONE
                        enableDisableButtons()
                    }

                }

                override fun onProgress(message: String?) {
                    Log.d("TAG", "onProgress: \n" + message)
                }

                override fun onError(message: String) {
                    if (isVisible) {
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                        Log.d("TAG", "onError: \n" + message)
                        enableDisableButtons()
                        if (createPostDialog != null && createPostDialog?.isShowing?:false) {
                            createPostDialog?.dismiss()
                        }
                    }
                }
            }
        )
    }
    fun File.getMediaDuration(uri: Uri): Long {
        if (!exists()) return 0
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
        retriever.release()

        return duration.toLongOrNull() ?: 0
    }

    fun clearMedia(){
        if (!mediaList.isNullOrEmpty() && mediaList.size > 0) {
            mediaList.removeAt(mediaList.size-1)
            tv_edit.text = getString(R.string.edit_post)
            enableAddMedia(true)

            enablePreviewAndSharePost(!(mediaList.isNullOrEmpty() && postText.isNullOrEmpty()))
            if (mediaList.size == 0) {
                enableEditMedia(false)
                rv_media.visibility = View.GONE
            } else {
                enableEditMedia(true)
                rv_media.visibility = View.VISIBLE
            }
            postsAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FileUtils.deleteFolderForMedia(File(rootPath))
    }

}