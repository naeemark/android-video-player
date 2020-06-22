package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.andromeda.kunalbhatia.demo.hungamaplayer.HomePostAdapter;
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostData;
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostMedium;
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostResponseData;
import com.andromeda.kunalbhatia.demo.hungamaplayer.rest.ApiClient;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.*;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = "VideoPlayerRecyclerView";

    public enum VolumeState {ON, OFF};

    // ui
    private ProgressBar thumbnail;
    private AppCompatImageView videoThumbnail;
    private static ImageView volumeControl;
    private String myUrl = null;
    private Integer width = null;
    private Integer height = null;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    private View backView;
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;

    // vars
    private List<PostResponseData> mediaObjects = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private static RequestManager requestManager;
    private long timeMilli = 0;
    private Player.EventListener playerEventListener;
    DataSource.Factory mDataSourceFactory = null;

    // controlling playback state
    private static VolumeState volumeState = VolumeState.ON;

    public VideoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        timeMilli = System.currentTimeMillis();
        Log.d(TAG, "STEP 1  ");
        this.context = context.getApplicationContext();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        mDataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "RecyclerViewVideoPlayer"));
        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        videoSurfaceView.setBackgroundColor(Color.BLACK);
        videoSurfaceView.setShutterBackgroundColor(Color.BLACK);
        mDataSourceFactory = new CacheDataSourceFactory(
                BallogyApplication.getInstance().getSimpleCache(context),
                mDataSourceFactory,
                new FileDataSource.Factory(),
                new CacheDataSinkFactory(BallogyApplication.getInstance().getSimpleCache(context), 2 * 1024 * 1024),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, new CacheDataSource.EventListener() {
            @Override
            public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {

            }

            @Override
            public void onCacheIgnored(int reason) {

            }
        });
        // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
        // LoadControl is injected when the player is created.
        // 2. Create the player
        videoPlayer = new SimpleExoPlayer.Builder(
                context,
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(context),
                BallogyApplication.getInstance().getLoadControl(),
                DefaultBandwidthMeter.getSingletonInstance(context),
                Util.getLooper(),
                new AnalyticsCollector(
                        Clock.DEFAULT
                ),
                true,
                Clock.DEFAULT
        ).build();

        // Bind the player to the view.
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        videoPlayer.setPlayWhenReady(true);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                Log.d(TAG, "STEP 5  " + (System.currentTimeMillis() - timeMilli));
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");
                    if(thumbnail != null && myUrl != null){
                        thumbnail.setVisibility(VISIBLE);
                        videoThumbnail.setVisibility(VISIBLE);
                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if(!recyclerView.canScrollVertically(1)){
                        playVideo(true);
                    }
                    else{
                        playVideo(false);
                    }
                }else{
                    Integer tPos = getPositionToStop();
                    Log.d(TAG, "getPositionToStop " + tPos);
                    if(tPos != null && tPos != playPosition && videoPlayer != null){
                        Log.d(TAG, "Stop Player " + tPos);
                        videoPlayer.setPlayWhenReady(false);
                        videoPlayer.getPlaybackState();
                    }
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                Log.d(TAG, "STEP 4  " + (System.currentTimeMillis() - timeMilli));
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    Log.d(TAG, "onChildViewDetachedFromWindow: ");
                    resetVideoView();
                }

            }
        });

        playerEventListener = new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                Log.d(TAG, "onTimelineChanged: 1 called");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.d(TAG, "onTracksChanged: 1 called");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onLoadingChanged: 1 called");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "STEP 3  " + (System.currentTimeMillis() - timeMilli));
                Log.d(TAG, "onPlayerStateChanged Before: 1 called" + " PlaybackState = " + playbackState);
                if(isShown()){
                    Log.d(TAG, "onPlayerStateChanged After: 1 called");
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                            Log.e(TAG, "onPlayerStateChanged: STATE_BUFFERING.");
                            if(thumbnail != null && myUrl != null){
                                thumbnail.setAlpha(1f);
                                videoThumbnail.setAlpha(1f);
                            }

                            break;
                        case Player.STATE_ENDED:
                            Log.d(TAG, "onPlayerStateChanged: STATE_ENDED.");
                            videoPlayer.seekTo(0);
                            break;
                        case Player.STATE_IDLE:
                            Log.d(TAG, "onPlayerStateChanged: STATE_IDLE.");
                            break;
                        case Player.STATE_READY:
                            Log.e(TAG, "onPlayerStateChanged: STATE_READY.");
                            if(thumbnail != null){
                                thumbnail.setAlpha(0f);
                                videoThumbnail.setAlpha(0f);
                            }

                            if(!isVideoViewAdded){
                                addVideoView();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.e(TAG, ": onRepeatModeChanged.");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.e(TAG, ": onShuffleModeEnabledChanged.");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, ": onPlayerError. " + error.getMessage() + "  Details are : " + error.getLocalizedMessage());
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.e(TAG, ": onPositionDiscontinuity. Reason is : " + reason);
                if(reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION){
                    videoPlayer.seekTo(0);
                }
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.e(TAG, ": onPlaybackParametersChanged.");

            }

            @Override
            public void onSeekProcessed() {
                Log.e(TAG, ": onSeekProcessed.");
            }

        };
        setVolumeControl(ApiClient.Companion.getVolumeValue());

        Log.d(TAG, "STEP 2  " + (System.currentTimeMillis() - timeMilli));
    }

    public Integer getPositionToStop(){

        Integer tPos;

        if(getLayoutManager() != null){
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return null;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition, "start");
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition, " end");

                tPos = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            }
            else {
                tPos = startPosition;
            }
        }
        else{
            tPos = mediaObjects.size() - 1;
        }

        return tPos;
    }

    public void setPlayPosition(Integer playPosition) {
        this.playPosition = playPosition;
    }

    public void playVideo(boolean isEndOfList) {

        Log.d(TAG, "STEP 6  " + (System.currentTimeMillis() - timeMilli));
        int targetPosition;

        if(!isEndOfList && getLayoutManager() != null){
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition, "start");
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition, " end");

                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            }
            else {
                targetPosition = startPosition;
            }
        }
        else{
            targetPosition = mediaObjects.size() - 1;
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition);

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }
        pausePlayer();

        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstVisibleItemPosition();
        String[] mediaUrl = getPositionUrl(targetPosition);
        if (mediaUrl!=null) {
            myUrl = mediaUrl[0];
            if(mediaUrl.length == 4 && mediaUrl[2] != null && mediaUrl[3] != null &&
            !mediaUrl[2].contains("null") && !mediaUrl[3].contains("null")){
                width = Integer.parseInt(mediaUrl[2]);
                height = Integer.parseInt(mediaUrl[3]);
            }
        }


        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }
        HomePostAdapter.GroupInfoHolder holder;
        if (child.getTag() instanceof HomePostAdapter.GroupInfoHolder){
            holder = (HomePostAdapter.GroupInfoHolder) child.getTag();
        }
        else
            return;

        if (holder == null) {
            playPosition = -1;
            return;
        }
        thumbnail = holder.getThumbnail();
        videoThumbnail = holder.getMediaThumbnail();
        frameLayout = holder.getMediaContainer();
        backView = holder.getBackView();
        requestManager = holder.getRequestManager();

        Log.d(TAG, "STEP 7  " + (System.currentTimeMillis() - timeMilli));
        if (mediaUrl != null) {
            if (mediaUrl[0] == null) {
                pausePlayer();
                frameLayout.setVisibility(View.GONE);
                thumbnail.setVisibility(View.GONE);
                videoThumbnail.setVisibility(View.GONE);
                return;
            } else {
                frameLayout.setVisibility(View.VISIBLE);
                thumbnail.setVisibility(View.VISIBLE);
                thumbnail.setAlpha(1f);
                videoThumbnail.setAlpha(1f);
            }
        }

        if(!isShown()){
            pausePlayer();
            return;
        }
        volumeControl = holder.getIvVolumeControl();
        videoSurfaceView.setPlayer(videoPlayer);
        viewHolderParent = holder.itemView;
        viewHolderParent.setOnClickListener(videoViewClickListener);

        if (width != null && height != null && mediaUrl != null) {
            if (width.equals(height)) {
                //square
                videoThumbnail.invalidate();
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

            } else if (width < height) {
                Log.d("VideoOrientation", " Portrait work ");
                //portrait
                videoThumbnail.invalidate();
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } else {
                //landscape
                Log.d("VideoOrientation", " Landscape work ");
                videoThumbnail.invalidate();
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            MediaSource videoSource = buildMediaSource(Uri.parse(mediaUrl[0]));
            Log.d(TAG, " VideoPlayer CHECK");
            if(videoSource != null && videoPlayer != null){
                Log.d(TAG, " VideoPlayer is not NULL");
                LoopingMediaSource loopingMediaSource = new LoopingMediaSource(videoSource);
                videoPlayer.setPlayWhenReady(true);
                videoPlayer.prepare(loopingMediaSource);
            }
        }

        Log.d(TAG, "STEP 8" + (System.currentTimeMillis() - timeMilli));
    }


    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER :
                return new ExtractorMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
        }
        return null;
    }

    private String[] getPositionUrl(int position) {
        if(mediaObjects.size() > position && position>= 0 && mediaObjects.get(position) != null &&  mediaObjects.get(position).getPostData() != null) {
            PostData postData = mediaObjects.get(position).getPostData();
            if(postData != null && postData.getMedia() != null){
                List<PostMedium> postMediaList = postData.getMedia();
                if (postMediaList != null && postMediaList.size() > 0){
                    PostMedium postMedium = postMediaList.get(0);
                    if(postMedium != null){
                        String type = postMedium.getType();
                        if(type != null && type.equalsIgnoreCase("video") && postMedium.getMedia() != null){

                            String arr [] = new String[4];
                            arr[0] = postMedium.getMedia();
                            arr[1] = postMedium.getThumbnail();
                            arr[2] = postMedium.getWidth()+"";
                            arr[3] = postMedium.getHeight() + "";

                            return arr;
                        }
                    }
                }
            }

        }
        return null;
    }

    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleVolume();
        }
    };

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private int getVisibleVideoSurfaceHeight(int playPosition, String da) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: " + at);

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {

            Rect r = new Rect();
            child.getGlobalVisibleRect(r);
            Log.d(TAG, "Data is " + r.top + " " + r.bottom + " "+ da +" height = " + (r.bottom - r.top));
            return (r.bottom - r.top);
        }
    }

    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        Log.d(TAG, " : removeVideoView");
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            if(viewHolderParent != null){
                viewHolderParent.setOnClickListener(null);
            }
        }

    }

    public void pausePlayer(){
        if(videoPlayer != null){
            Log.d(TAG, " VideoPlayer is pausePlayer : ");
            videoPlayer.setPlayWhenReady(false);
            videoPlayer.getPlaybackState();
        }
    }

    public void startPlayer(){
        if(videoPlayer != null && myUrl != null){
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.getPlaybackState();
        }
    }
    public void resetValues(){
        playPosition =-1;
    }

    private void addVideoView(){
        Log.d(TAG, " : addVideoView");
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
    }

    public void resetVideoView(){

        Log.d(TAG, " : resetVideoView");
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            if(myUrl != null){
                thumbnail.setAlpha(1f);
                videoThumbnail.setAlpha(1f);
            }
        }
    }

    public void removeEventListener() {
        Log.d(TAG, "RemoveEventListener");
        if(videoPlayer != null){
            videoPlayer.removeListener(playerEventListener);
        }
        pausePlayer();
    }

    public void addEventListener() {
        Log.d(TAG, "AddEventListener");
        if(videoPlayer != null){
            videoPlayer.addListener(playerEventListener);
        }
    }


    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
            Log.d(TAG, " VideoPlayer = NULL");
        }

        viewHolderParent = null;
    }

    public void toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);

            } else if(volumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);

            }
        }
    }

    private void setVolumeControl(VolumeState state){
        volumeState = state;
        ApiClient.Companion.setVolumeValue(volumeState);
        if(state == VolumeState.OFF){
            videoPlayer.setVolume(0f);
            animateVolumeControl();
        }
        else if(state == VolumeState.ON){
            videoPlayer.setVolume(1f);
            animateVolumeControl();
        }
    }

    private static void animateVolumeControl(){
        if(volumeControl != null){
            volumeControl.bringToFront();
            if(volumeState == VolumeState.OFF){
                requestManager.load(R.drawable.ic_volume_off_icon)
                        .into(volumeControl);
            }
            else if(volumeState == VolumeState.ON){
                requestManager.load(R.drawable.ic_volume_on_icon)
                        .into(volumeControl);
            }
            volumeControl.animate().cancel();

            volumeControl.setAlpha(1f);

            volumeControl.animate()
                    .alpha(0f)
                    .setDuration(600).setStartDelay(1000);
        }
    }

    public void setMediaObjects(List<PostResponseData> mediaObjects){
        this.mediaObjects = mediaObjects;
    }

}



























