package com.zhihu.matisse.filter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GalleryVideoFilter extends Filter {

    private long maxVideoDuration = 60700;

    @Override
    protected Set<MimeType> constraintTypes() {
        return Collections.singleton(MimeType.MP4);
    }

    @Override
    public IncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item))
            return null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, item.uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (time != null) {
            long timeInMillis = Long.parseLong(time);
            if (timeInMillis > maxVideoDuration) {
                return new IncapableCause(IncapableCause.DIALOG, getDurationCause(context));
            }
        }
        return null;
    }

    private String getDurationCause(Context context) {
        if(context != null){
            long seconds;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(maxVideoDuration);
            seconds = (minutes > 0)? (TimeUnit.MILLISECONDS.toSeconds(maxVideoDuration) - (minutes * 60)) : 60;
            if(minutes > 0 && seconds > 0){
                return String.format(Locale.ENGLISH, context.getString(R.string.error_verified_video_too_long), minutes, seconds);
            }else {
                return String.format(Locale.ENGLISH, context.getString(R.string.error_video_too_long), 60);
            }
        }
        return "";
    }

    public GalleryVideoFilter setMaxVideoDuration(long maxVideoDuration){
        this.maxVideoDuration = maxVideoDuration;
        return this;
    }
}
