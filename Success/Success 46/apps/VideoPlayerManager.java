package com.deffe.macros.apps;

import android.content.res.AssetFileDescriptor;
import android.view.View;

public interface VideoPlayerManager<T extends MetaData> {


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, String videoUrl);


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor);

    void stopAnyPlayback();


    void resetMediaPlayer();
}
