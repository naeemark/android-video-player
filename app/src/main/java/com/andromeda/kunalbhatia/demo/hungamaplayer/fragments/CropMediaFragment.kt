package com.andromeda.kunalbhatia.demo.hungamaplayer.fragments


import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.os.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.cropping.ImageCropView
import com.bumptech.glide.Glide
import com.andromeda.kunalbhatia.demo.hungamaplayer.activities.CropMediaActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.RunOnUiThread
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.OnCropVideoListener
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.BitmapLoadUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.PHOTO
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.VIDEO
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.convertGooglePhotosVideoToLocalFile
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.convertWhatsAppImageToLocalFile
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.convertWhatsAppVideoToLocalFile
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.storageImagePath
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.FileUtils.Companion.storageVideoPath
import com.video.trimmer.utils.RealPathUtil
import kotlinx.android.synthetic.main.fragment_crop_media.*
import kotlinx.android.synthetic.main.view_cropper.view.*
import java.io.*

class CropMediaFragment : BaseFragment(), OnCropVideoListener {
	var width: Int = 0
	private var resetBtnState: Int = 0 // enabled = 1, disabled = 0
	private lateinit var mImageUri: Uri
	val minRatio = 1.0f
	val maxRatio = 1.0f
	var SHARE : Boolean = false

	//this is used to get the input uri from gallery in bundle
	private var mInputUri: Uri ? = null
	//this is used to crop the media and produce output and send back to create post fragment
	private lateinit var mOutputUri: Uri
	lateinit var imageView : ImageCropView

	private var uploadingTextView: AppCompatTextView? = null
	private var tvTitle: AppCompatTextView? = null
	private var postUploadingProgress: ProgressBar? = null
	private var createPostDialog: Dialog? = null
	//endregion


	companion object {
		fun newInstance(): CropMediaFragment =
			CropMediaFragment()
	}

	override fun setArguments(args: Bundle?) {
		super.setArguments(args)
		args.let {
			if (it!!.containsKey("fileUri") && it!!.containsKey("SHARE")) {
				it.getParcelable<Uri>("fileUri").let {
					mInputUri = it
					Log.d("TAG", "fileUri: " + mInputUri.toString())
				}

				it.getBoolean("SHARE").let {
					SHARE = it
				}
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_crop_media, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		Log.d("LifeCycle", "onViewCreated")
		imageView = view.findViewById<ImageCropView>(R.id.imageCropper)
		ClickListeners()
		createChildViewsAccordingToMimeType()
	}

	private fun createChildViewsAccordingToMimeType() {
		val displayMetrics = DisplayMetrics()
		activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

		 width = displayMetrics.widthPixels

		fl_media.layoutParams.height = width

		activity?.intent?.extras?.let {
			if (it.getString("TASK", "").equals("PHOTO_CROPPING", ignoreCase = true)) {
				setUpimageView()
			}else {
				setUpVideoCropper()
			}
		}
	}

	private fun setUpimageView(){
		imageView.visibility = View.VISIBLE
		imageView.invalidate()
		if (SHARE) {
			if (mInputUri!!.toString().contains("whatsapp", true) || mInputUri!!.toString().contains("skype", true)) {
				val path = convertWhatsAppImageToLocalFile(activity!!, mInputUri!!)
				mInputUri = Uri.fromFile(File(path))
			}
			loadAsync(mInputUri!!)
		} else {
			loadAsync(Uri.parse(RealPathUtil.realPathFromUriApi19(activity!!, mInputUri!!)))
		}

		Glide.with(activity!!).load(mInputUri).into(ivThumbnail)
		showHideCropOptions(true)
		enableDisableButtons(true)

		val uriString = if (SHARE) {
			RealPathUtil.realPathFromUriApi19(activity!!,mInputUri!!)
		} else {
			RealPathUtil.realPathFromUriApi19(activity!!,mInputUri!!)
		}
        val extension: String? = uriString?.substring(uriString.lastIndexOf("."));
        val croppedFile = File(storageImagePath + File.separator + "cropped_" + System.currentTimeMillis() + extension)
		mOutputUri = Uri.fromFile(croppedFile)

		enableResetButton(false)

		imageView.setOnTouchListener(object: View.OnTouchListener{
			override fun onTouch(v: View?, event: MotionEvent?): Boolean {
				enableResetButton(true)
				return false
			}
		})
	}



	fun enableResetButton(enabled: Boolean){
		btn_reset.isEnabled = enabled
		btn_reset.isClickable = enabled
		when(enabled){
			true -> {
				btn_reset.alpha = 1f
			}
			false -> {
				btn_reset.alpha = .4f
			}
		}
	}


	fun calculateScreenWidth(): Int{
		val displayMetrics = DisplayMetrics()
		activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
		val width = displayMetrics.widthPixels
		return width
	}

	private fun setUpVideoCropper(){
		if (
			mInputUri!!.toString().contains("whatsapp", ignoreCase = true) ||
			mInputUri!!.toString().contains("skype", ignoreCase = true)) {
			val path = convertWhatsAppVideoToLocalFile(activity!!, mInputUri!!)
			mInputUri = Uri.fromFile(File(path))
		} else if (mInputUri!!.toString().contains("content://com.google.android.apps.photos.contentprovider", ignoreCase = true)) {
			val path = convertGooglePhotosVideoToLocalFile(activity!!, mInputUri!!, FileUtils.storageVideoPath + System.currentTimeMillis() + ".mp4")
			mInputUri = Uri.fromFile(File(path))
		}

		showHideCropOptions(false)
		enableDisableButtons(true)
		videoCropper.visibility = View.VISIBLE
		videoCropper.setVideoURI(mInputUri!!, false, calculateScreenWidth())
			.setOnCropVideoListener(this)
			.setMinMaxRatios(minRatio, maxRatio)
			.setDestinationPath(storageVideoPath)
		videoCropper.getThumbnailUri().let {
			Glide.with(activity!!).load(it).into(ivThumbnail)
		}



		enableResetButton(false)

		videoCropper.cropFrame.setOnTouchListener(object: View.OnTouchListener{

			override fun onTouch(v: View?, event: MotionEvent?): Boolean {
				enableResetButton(true)
				resetBtnState = 1
				return false
			}
		})

	}

	private fun showHideCropOptions(isPicture: Boolean){
		if (isPicture) {
			rg_crop_options.visibility = View.GONE
			tvCropOptionsDetails.text = getString(R.string.drag_and_or_resize_your_photo)
		} else {
			rg_crop_options.visibility = View.VISIBLE
			tvCropOptionsDetails.text = getString(R.string.drag_and_or_resize_your_video)
		}
	}

	private fun ClickListeners() {
		btn_continue.setOnClickListener {
			enableRadiosAndButtons(false)

			when(rg_crop_options.checkedRadioButtonId){
				R.id.rb_square -> {
					activity?.intent?.extras?.let {
						if (it.getString("TASK", "").equals("PHOTO_CROPPING", ignoreCase = true)) {
							imageView.croppedImage?.let {
								val file = FileUtils.bitmapConvertToFile(activity!!, it)
								if (file != null) {
									(context as CropMediaActivity).cropMediaSuccessful(Uri.fromFile(file), PHOTO)
								}
								Log.d("TAG", file?.absolutePath ?: "")
							}
						} else {
							videoCropper.onSaveClicked()
						}
					}
				}
				R.id.rb_native -> {
					//send the media without cropping using mInputUri
					(activity!! as CropMediaActivity).nativeSuccessful(Uri.fromFile(File(RealPathUtil.realPathFromUriApi19(activity!!, mInputUri!!))))
				}
			}
		}

		btn_reset.setOnClickListener {
			enableDisableProgress(true)
			ivThumbnail.visibility = View.GONE


			activity?.intent?.extras?.let {
				if (it.getString("TASK", "").equals("PHOTO_CROPPING", ignoreCase = true)) {
					imageView.visibility = View.VISIBLE
					imageView.resetDisplay()
				} else {
					rg_crop_options.check(R.id.rb_square)
					videoCropper.visibility = View.VISIBLE
					videoCropper.reset()
				}
			}

			enableDisableProgress(false)
			enableResetButton(false)
			resetBtnState = 0
		}

		rg_crop_options.setOnCheckedChangeListener { group, checkedId ->
			when (checkedId) {
				R.id.rb_square -> {
					handleViewAccordingToRadioButtons(true)
					tvCropOptionsDetails.visibility = View.VISIBLE
					if (resetBtnState == 1) {
						enableResetButton(true)
					}
					else if (resetBtnState == 0){
						enableResetButton(false)
					}
				}
				R.id.rb_native -> {
					handleViewAccordingToRadioButtons(false)
					tvCropOptionsDetails.visibility = View.GONE
					enableResetButton(false)
				}
			}
		}
	}

	private fun enableRadiosAndButtons(enabled: Boolean) {
		rb_square.isClickable = enabled
		rb_native.isClickable = enabled
        btn_continue.isEnabled = enabled
        btn_continue.isClickable = enabled
        btn_reset.isEnabled = enabled
        btn_reset.isClickable = enabled
	}

	private fun enableDisableProgress(enable: Boolean){
		if (enable) {
			progress_bar.bringToFront()
			progress_bar.visibility = View.VISIBLE
		}else{
			progress_bar.visibility = View.GONE
		}
	}

	private fun handleViewAccordingToRadioButtons(isSquare: Boolean){
		if (isSquare) {
			activity?.intent?.extras?.let {
				if (it.getString("TASK", "").equals("PHOTO_CROPPING", ignoreCase = true)) {
					imageView.visibility = View.VISIBLE
				} else {
					videoCropper.visibility = View.VISIBLE
				}
				ivThumbnail.visibility = View.GONE
			}
		}
		else
		{
			ivThumbnail.visibility = View.VISIBLE
			imageView.visibility = View.GONE
			videoCropper.visibility = View.GONE
		}
	}

	private fun enableDisableButtons(enabled: Boolean) {
		when (enabled) {
			true -> {
				btn_continue.alpha = 1f
				btn_reset.alpha = 1f
			}
			false -> {
				btn_continue.alpha = .4f
				btn_reset.alpha = .4f
			}
		}
		btn_continue.isClickable = enabled
		btn_continue.isEnabled = enabled
		btn_reset.isClickable = enabled
		btn_reset.isEnabled = enabled
	}

	override fun onCropStarted() {
		enableDisableProgress(true)
		openTrimmingPopup()
	}

	override fun getResult(uri: Uri) {
		Log.d("TAG", "GET RESULT URI: " + uri.toString())
		if (createPostDialog != null && createPostDialog?.isShowing?:false) {
			createPostDialog?.dismiss()
		}
		RunOnUiThread(activity).safely {
			enableDisableProgress(false)
			activity?.let {
				(it as CropMediaActivity).cropMediaSuccessful(uri, VIDEO)
			}
		}
	}

	override fun cancelAction() {
		RunOnUiThread(activity).safely {
			activity?.finish()
		}
	}

	override fun onError(message: String) {
		Log.e("ERROR", message)
		if (createPostDialog != null && createPostDialog?.isShowing?:false) {
			createPostDialog?.dismiss()
		}
		Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
	}

	override fun onProgress(progress: Float) {
		trimmingProgressText(progress.toInt())
		Log.e("PLAYING", "PROGRESS "+ progress)
	}


	private fun loadAsync(uri: Uri) {
		Log.i("TAG", "loadAsync: $uri")
		if (SHARE) {
			SaveExternalFileFromUri().execute(uri)
		}
		else {
			val task = DownloadAsync()
			task.execute(uri)
		}
	}

	internal inner class SaveExternalFileFromUri: AsyncTask<Uri, Void, String>(){
		var bitmap: Bitmap?=null

		override fun doInBackground(vararg params: Uri?): String? {
			val destinationFilename = FileUtils.savefile(activity!!, params[0] as Uri)
			return destinationFilename
		}

		override fun onPostExecute(result: String?) {
			super.onPostExecute(result)
			Log.d("TAG", "destinationFilename: " + result)
			result.let {
				val task = DownloadAsync()
				task.execute(Uri.parse(it))
			}
		}

	}

	internal inner class DownloadAsync : AsyncTask<Uri, Void, Bitmap>(), DialogInterface.OnCancelListener {

		private var mUri: Uri? = null

		override fun doInBackground(vararg params: Uri): Bitmap? {
			Log.d("TAG", "doInBackground")
			mUri = params[0]
			var bitmap: Bitmap? = null
			mUri?.let {
				bitmap = BitmapLoadUtils.decode(
					it.toString(),
					1000,
					1000
				)
			}
			return bitmap
		}

		override fun onPostExecute(result: Bitmap?) {
			Log.d("TAG", "onPostExecute")
			super.onPostExecute(result)
			setImageURI(mUri!!, result)
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

	private fun setImageURI(uri: Uri, bitmap: Bitmap?) {
			Log.d("TAG", "image size: " + bitmap?.width + "x" + bitmap?.height)
			imageView.visibility = View.VISIBLE
			imageView.layoutParams = RelativeLayout.LayoutParams(width, width)
			imageView.setImageBitmap(bitmap)
			imageView.setBackgroundDrawable(null)
			mImageUri = uri
	}

	private fun openTrimmingPopup() {
		val createPostDialogBuilder = AlertDialog.Builder(context!!)
		val inflater = activity?.getLayoutInflater()
		val dialogView = inflater?.inflate(R.layout.compress_popup_layout, null)
		createPostDialogBuilder.setView(dialogView)
		postUploadingProgress = dialogView?.findViewById<ProgressBar>(R.id.progressPostUploading)
		uploadingTextView = dialogView?.findViewById<AppCompatTextView>(R.id.uploadingText)
		tvTitle = dialogView?.findViewById<AppCompatTextView>(R.id.tvTitle)
		tvTitle?.text = getString(R.string.trimming)
		createPostDialog = createPostDialogBuilder.create()
		createPostDialog?.setCancelable(false)
		createPostDialog?.show()
	}
	private fun trimmingProgressText(progress: Int){
		if(progress < 101){
            uploadingTextView?.text = ("$progress %")
			updateProgress(progress)
		}
	}

	private fun updateProgress(progress: Int){
        postUploadingProgress?.progress = progress
	}

}
