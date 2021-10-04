package com.deffe.macros.status;

import android.content.res.AssetFileDescriptor;

public interface VideoPlayerManager<T extends MetaData> {


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, String videoUrl);


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor);

    void stopAnyPlayback();


    void resetMediaPlayer();
}
