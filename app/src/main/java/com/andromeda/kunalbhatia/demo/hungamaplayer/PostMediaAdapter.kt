package com.andromeda.kunalbhatia.demo.hungamaplayer

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.andromeda.kunalbhatia.demo.hungamaplayer.data.MediaTypes
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostMedia
import kotlinx.android.synthetic.main.media_item.view.*


class PostMediaAdapter(
    var mediaItems: ArrayList<PostMedia>,
    val context: Context,
    val deleteListener: (Int) -> Unit
) :
    RecyclerView.Adapter<PostMediaAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return PostMediaAdapter.ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.media_item, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Glide.with(context)
//            .load(mediaItems.get(position).path)
//            .centerCrop()
//            .into(holder.ivThumbnail)
//        when (mediaItems.get(position).mediaType) {
//            MediaTypes.VIDEO.toString() -> {
//                holder.ivMediaType.setImageResource(R.drawable.video_thumb)
//            }
//            MediaTypes.PHOTO.toString() -> {
//                holder.ivMediaType.setImageResource(R.drawable.photo_thumb)
//            }
//        }
        holder.progress.visibility = View.VISIBLE
        holder.ivThumbnail.visibility = View.GONE
        val listener = object: RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                holder.progress.visibility = View.INVISIBLE
                holder.ivThumbnail.visibility = View.VISIBLE
                holder.ivThumbnail.setImageDrawable(resource)
                return true
            }

        }

        Glide.with(context)
            .load(mediaItems.get(position).path)
            .listener(listener)
            .centerCrop()
            .into(holder.ivThumbnail)

        if (mediaItems.get(position).mediaType.equals(MediaTypes.VIDEO.toString(), ignoreCase = true)) {
            holder.ivMediaType.setImageResource(R.drawable.video_thumb)
        } else {
            holder.ivMediaType.setImageResource(R.drawable.photo_thumb)
        }

        if (mediaItems.get(position).isEditMode) {
            val drawableOverlay =
                ColorDrawable(ContextCompat.getColor(context, R.color.edit_overlay_color))
            holder.flThumb.foreground = drawableOverlay
            holder.deleteMedia.visibility = View.VISIBLE
        } else {
            holder.flThumb.foreground = null
            holder.deleteMedia.visibility = View.GONE
        }
        holder.deleteMedia.setOnClickListener{
            mediaItems.removeAt(position)
            notifyDataSetChanged()
            deleteListener(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivThumbnail = view.iv_thumb
        val ivMediaType = view.iv_media_type
        val flThumb = view.fl_thumb
        val deleteMedia = view.iv_delete_media
        val progress = view.progress
    }
}