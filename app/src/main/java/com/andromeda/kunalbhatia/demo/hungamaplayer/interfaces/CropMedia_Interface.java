package com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface CropMedia_Interface {

    void cropSuccessFull(@Nullable Uri uri, @NonNull String fileType);

    void TrimSuccessFull();

    void cropCanceled();

    void trimCanceled();

    void nativeSuccessFull(@Nullable Uri uri);

}
