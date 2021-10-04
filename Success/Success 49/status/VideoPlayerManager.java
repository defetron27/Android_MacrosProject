package com.deffe.macros.status;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import java.io.File;

public interface VideoPlayerManager<T extends MetaData> {


    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, String videoUrl);

    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor);

    void playNewVideo(T metaData, VideoPlayerView videoPlayerView, File videoFile, Context context);

    void stopAnyPlayback();


    void resetMediaPlayer();
}
