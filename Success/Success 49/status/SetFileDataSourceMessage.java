package com.deffe.macros.status;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class SetFileDataSourceMessage extends SetDataSourceMessage
{

    private File mVideoFile;
    private Context context;

    SetFileDataSourceMessage(VideoPlayerView videoPlayerView, File videoFile, VideoPlayerManagerCallback callback,Context context)
    {
        super(videoPlayerView, callback);
        mVideoFile = videoFile;
        this.context = context;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer)
    {
        currentPlayer.setDataSource(mVideoFile,context);
    }
}
