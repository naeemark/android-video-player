package com.andromeda.kunalbhatia.demo.hungamaplayer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostData
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostGroup
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostResponseData
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostShareBy
import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.GeneralUtils
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class HomePostAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TAG : String = HomePostAdapter::class.java.simpleName
        private const val GENERAL_POST = 1
    }

    private var postDataList: ArrayList<PostResponseData>? = ArrayList()
    private var loadItem = true
    private var glideRequests: RequestManager? = null
    private var thumbnailRequest: RequestBuilder<GifDrawable>? = null
    private val screenWidth = context.resources?.displayMetrics?.widthPixels ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (glideRequests == null) {
            glideRequests = Glide.with(context)
        }
        if (thumbnailRequest == null) {
            thumbnailRequest = Glide.with(context).asGif()
                .load(R.drawable.ic_volume_on_icon).decode(GifDrawable::class.java)
        }
        return GroupInfoHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_general_post_item, parent, false), glideRequests)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder...")
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {

        val postResponse: PostResponseData? = postDataList?.get(holder.adapterPosition)
        val postData: PostData? = postResponse?.postData
        val postGroup: PostGroup? = postResponse?.group
        val postShareByUser: PostShareBy? = postResponse?.shareBy
        if (getItemViewType(position) == GENERAL_POST) {
            if (holder is GroupInfoHolder) {

                postData?.let { it1 ->
                    val admin =  postResponse.admin
                    var imagePath = ""
                    var title = ""
                    if(admin != null && admin){
                        postGroup?.coverImage?.let { imagePath = it }
                        postGroup?.title?.let { title = it }
                    }else{
                        postShareByUser?.imagePath?.let { imagePath = it }
                        postShareByUser?.username?.let { title = it}
                    }
                    holder.tvUserName.text = title

                    Glide.with(context)
                        .load(imagePath)
                        .centerCrop()
                        .placeholder(R.drawable.ic_volume_off_icon)
                        .into(holder.ivUserImage)

                    if (!it1.media.isNullOrEmpty()) {
                        it1.media?.let {
                            if (!it.isNullOrEmpty()) {

                                it[0].media?.let { mediaUrl ->

                                    it[0].type?.let { mediaType ->

                                        val heightVal = it1.media?.get(0)?.height?:0
                                        var widthVal = it1.media?.get(0)?.width?:0
                                        if (mediaType == "image" || mediaUrl.endsWith(".gif") || mediaUrl.endsWith(".png")) {

                                            holder.mediaContainer.setBackgroundColor(Color.WHITE)
                                            holder.mediaContainer.visibility = View.GONE
                                            holder.ivImage.alpha = 0f
                                            holder.ivImage.visibility = View.VISIBLE
                                            holder.ivImageThumbnail.visibility = View.VISIBLE
                                            val constraintParams =  holder.ivImage.getLayoutParams() as (ConstraintLayout.LayoutParams)
                                            if(widthVal > 0 && widthVal > screenWidth){
                                                widthVal = screenWidth
                                            }else {
                                                widthVal = screenWidth
                                            }
                                            constraintParams.width= widthVal
                                            constraintParams.height = widthVal
                                            holder.ivImage.layoutParams = constraintParams
                                            holder.ivImage.invalidate()

                                            if (!mediaUrl.endsWith("gif")) {
                                                glideRequests!!.load(mediaUrl).thumbnail(0.1f).listener(object : RequestListener<Drawable> {
                                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                                        Log.d(TAG, "onLoadFailed...")
                                                        return false
                                                    }
                                                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                        Log.d(TAG, "onResourceReady...")
                                                        holder.ivImageThumbnail.visibility = View.GONE
                                                        holder.ivImage.alpha = 1f
                                                        holder.ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
                                                        return false
                                                    }
                                                }).into(holder.ivImage)

                                            } else {
                                                glideRequests!!.asGif().load(mediaUrl)
                                                    .listener(object : RequestListener<GifDrawable> {
                                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                                                            Log.d(TAG, "onLoadFailed...")
                                                            return false
                                                        }
                                                        override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                            Log.d(TAG, "onResourceReady...")
                                                            holder.ivImageThumbnail.visibility = View.GONE
                                                            holder.ivImage.alpha = 1f
                                                            holder.ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
                                                            return false
                                                        }
                                                    }).into(holder.ivImage)
                                            }

                                        } else if (mediaType == "video") {
                                            // Load Thumbnail
                                            holder.mediaContainer.setBackgroundColor(Color.BLACK)
                                            holder.backView.visibility = View.VISIBLE
                                            
                                            if(heightVal == widthVal){
                                                val frameParams =  holder.mediaThumbnail.getLayoutParams() as (FrameLayout.LayoutParams)
                                                val constraintParams =  holder.mediaContainer.getLayoutParams() as (ConstraintLayout.LayoutParams)
                                                widthVal = screenWidth
                                                frameParams.width= widthVal
                                                frameParams.height = widthVal
                                                constraintParams.width= widthVal
                                                constraintParams.height = widthVal
                                                holder.mediaThumbnail.layoutParams = frameParams
                                                holder.mediaThumbnail.invalidate()
                                                holder.mediaContainer.layoutParams = constraintParams
                                                holder.mediaContainer.invalidate()

                                                // PORTRAIT VIDEO
                                            } else if(heightVal > widthVal){
                                                if(heightVal > 0){
                                                    val frameParams =  holder.mediaThumbnail.getLayoutParams() as (FrameLayout.LayoutParams)
                                                    val constraintParams =  holder.mediaContainer.getLayoutParams() as (ConstraintLayout.LayoutParams)
                                                    frameParams.width= FrameLayout.LayoutParams.WRAP_CONTENT
                                                    frameParams.height = screenWidth
                                                    frameParams.gravity = Gravity.CENTER_HORIZONTAL
                                                    constraintParams.width= ConstraintLayout.LayoutParams.MATCH_PARENT
                                                    constraintParams.height = screenWidth
                                                    holder.mediaContainer.layoutParams = constraintParams
                                                    holder.mediaContainer.invalidate()
                                                    holder.mediaThumbnail.layoutParams = frameParams
                                                    holder.mediaThumbnail.invalidate()
                                                    holder.mediaThumbnail.adjustViewBounds =  true
                                                }

                                            }else{
                                                // LANDSCAPE VIDEO
                                                if(heightVal > 0){
                                                    val frameParams =  holder.mediaThumbnail.getLayoutParams() as (FrameLayout.LayoutParams)
                                                    val constraintParams =  holder.mediaContainer.getLayoutParams() as (ConstraintLayout.LayoutParams)
                                                    val heightProportion : Double = heightVal / (widthVal*1.0)
                                                    val tempHeight = heightProportion * screenWidth
                                                    frameParams.height = tempHeight.toInt()
                                                    constraintParams.width= screenWidth
                                                    constraintParams.height = tempHeight.toInt()
                                                    holder.mediaThumbnail.layoutParams = frameParams
                                                    holder.mediaThumbnail.invalidate()
                                                    holder.mediaContainer.layoutParams = constraintParams
                                                    holder.mediaContainer.invalidate()
                                                }
                                            }

                                            holder.mediaThumbnail.visibility = View.VISIBLE
                                            Glide.with(context)
                                                .load(postData.media?.get(0)?.thumbnail)
                                                .into(holder.mediaThumbnail)
                                            holder.ivImage.visibility = View.GONE
                                            holder.ivImageThumbnail.visibility = View.GONE
                                            holder.mediaContainer.visibility = View.VISIBLE

                                        }else {}
                                    }
                                }
                            }
                        }
                    } else {
                        holder.thumbnail.visibility = View.GONE
                        holder.mediaThumbnail.visibility = View.GONE
                        holder.mediaContainer.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (postDataList.isNullOrEmpty()) {
            return 0
        }
        return postDataList?.size?:0
    }

    fun getAdapterItem(position: Int): PostResponseData? {
        postDataList?.let { if (position < it.size) return it[position] }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        return GENERAL_POST
    }

    fun getItemList(): ArrayList<PostResponseData>? {
        return postDataList
    }

    fun setItemsList(postList: ArrayList<PostResponseData>?) {
        postDataList = postList
        notifyDataSetChanged()
    }

    fun setLoadItem(loadItem: Boolean) {
        this.loadItem = loadItem
        Log.d(TAG, "set Load Item " + this.loadItem)
    }

    fun removeSpecifiedItem(position: Int){
        if(position < postDataList?.size?:0){
            postDataList?.removeAt(position)
        }
    }

    class GroupInfoHolder(view: View, var requestManager: RequestManager?) : RecyclerView.ViewHolder(view) {
        var ivUserImage: CircleImageView = view.findViewById(R.id.cv_user_image)
        var tvUserName: AppCompatTextView = view.findViewById(R.id.tv_username)
        var ivImageThumbnail: ProgressBar = view.findViewById(R.id.iv_image_thumbnail)
        var ivImage: AppCompatImageView = view.findViewById(R.id.iv_image)
        var thumbnail: ProgressBar = view.findViewById(R.id.thumbnail)
        var ivVolumeControl: AppCompatImageView = view.findViewById(R.id.volume_control)
        var backView: View = view.findViewById(R.id.backView)
        var mediaContainer: FrameLayout = view.findViewById(R.id.media_container)
        var mediaThumbnail: AppCompatImageView = view.findViewById(R.id.iv_video_thumbnail)
        var parent: View = view
        init {
            parent.tag = this
        }
    }

}
