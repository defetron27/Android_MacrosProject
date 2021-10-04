package com.deffe.macros.status;

import android.content.Context;
import android.net.Uri;

import java.io.File;

class StorageVideoItem extends BaseVideoItem
{
    private static final String TAG = UrlVideoItem.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    private File StorageFile;
    private Context context;

    StorageVideoItem(VideoPlayerManager<MetaData> videoPlayerManager, File storageFile,Context context)
    {
        super(videoPlayerManager);
        StorageFile = storageFile;
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
        videoPlayerManager.playNewVideo(currentItemMetaData, player, StorageFile,context);
    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager)
    {
        videoPlayerManager.stopAnyPlayback();
    }
}
