package com.deffe.macros.status;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.FileDescriptor;


public class SetAssetsDataSourceMessage extends SetDataSourceMessage{

    private final AssetFileDescriptor mAssetFileDescriptor;

    public SetAssetsDataSourceMessage(VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
        mAssetFileDescriptor = assetFileDescriptor;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.setDataSource(mAssetFileDescriptor);
    }
}
