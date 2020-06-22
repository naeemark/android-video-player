package com.andromeda.kunalbhatia.demo.hungamaplayer;

import android.app.Application;
import android.content.Context;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class BallogyApplication extends Application {

    private static BallogyApplication sInstance;

    DefaultLoadControl mLoadControl = null;
    private SimpleCache simpleCache = null;
    private DatabaseProvider databaseProvider;

    public static BallogyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initVideoCache();
    }

    private void initVideoCache() {
        getLoadControl();
        getSimpleCache(this);
    }

    public DefaultLoadControl getLoadControl(){
        if (mLoadControl == null){
            // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
            // LoadControl is injected when the player is created.
            DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
            builder.setAllocator(new DefaultAllocator(true, 2 * 1024 * 1024));
            builder.setBufferDurationsMs(2000, 3000, 2000, 2000);
            builder.setPrioritizeTimeOverSizeThresholds(true);
            mLoadControl = builder.createDefaultLoadControl();
        }
        return mLoadControl;
    }

    public SimpleCache getSimpleCache(Context context){
        if (simpleCache == null){
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024);
            File file = new File(context.getCacheDir(), "media");
            simpleCache = new SimpleCache(file, evictor, getDatabaseProvider(context));
        }
        return simpleCache;
    }

    private DatabaseProvider getDatabaseProvider(Context context) {
        if (databaseProvider == null) {
            databaseProvider = new ExoDatabaseProvider(context);
        }
        return databaseProvider;
    }

}
