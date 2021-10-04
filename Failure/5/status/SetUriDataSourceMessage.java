package com.deffe.macros.status;

import android.content.Context;
import android.net.Uri;

public class SetUriDataSourceMessage extends SetDataSourceMessage{

    private final Uri mVideoUri;
    private final Context context;

    SetUriDataSourceMessage(VideoPlayerView videoPlayerView, Uri videoUri, Context context, VideoPlayerManagerCallback callback)
    {
        super(videoPlayerView, callback);
        mVideoUri = videoUri;
        this.context = context;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.setDataSource(context,mVideoUri);
    }
}
