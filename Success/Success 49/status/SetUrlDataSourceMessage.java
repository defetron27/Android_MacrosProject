package com.deffe.macros.status;

import android.net.Uri;

public class SetUrlDataSourceMessage extends SetDataSourceMessage
{

    private String mVideoUrl;




    SetUrlDataSourceMessage(VideoPlayerView videoPlayerView, String videoUrl, VideoPlayerManagerCallback callback)
    {
        super(videoPlayerView, callback);
        mVideoUrl = videoUrl;
    }



    @Override
    protected void performAction(VideoPlayerView currentPlayer)
    {
        currentPlayer.setDataSource(mVideoUrl);
    }
}
