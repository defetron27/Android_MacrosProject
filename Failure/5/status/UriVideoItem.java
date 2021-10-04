package com.deffe.macros.status;

import android.content.Context;
import android.net.Uri;

public class UriVideoItem extends BaseVideoItem
{
    private static final String TAG = UriVideoItem.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    private final Uri uri;
    private final Context context;

    UriVideoItem(VideoPlayerManager<MetaData> videoPlayerManager, Uri uri,Context context)
    {
        super(videoPlayerManager);
        this.uri = uri;
        this.context = context;
    }

    @Override
    public void update(int position, VideoViewHolder view, VideoPlayerManager videoPlayerManager)
    {
        if(SHOW_LOGS) Logger.v(TAG, "update, position " + position);
    }

    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager)
    {
        videoPlayerManager.playNewVideo(currentItemMetaData, player, uri,context);
    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager)
    {
        videoPlayerManager.stopAnyPlayback();
    }
}
