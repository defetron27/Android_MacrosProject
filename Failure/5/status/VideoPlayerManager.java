package com.deffe.macros.status;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

public interface VideoPlayerManager<T extends MetaData> {


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, Uri videoUri, Context context);


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor);

    void stopAnyPlayback();


    void resetMediaPlayer();
}
