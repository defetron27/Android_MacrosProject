package com.deffe.macros.status;

public class UrlVideoItem extends BaseVideoItem
{
    private static final String TAG = UrlVideoItem.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    private final String Urls;

    UrlVideoItem(VideoPlayerManager<MetaData> videoPlayerManager, String urls)
    {
        super(videoPlayerManager);
        Urls = urls;
    }

    @Override
    public void update(int position, VideoViewHolder view, VideoPlayerManager videoPlayerManager)
    {
        if(SHOW_LOGS) Logger.v(TAG, "update, position " + position);
    }

    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager)
    {
        videoPlayerManager.playNewVideo(currentItemMetaData, player, Urls);
    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager)
    {
        videoPlayerManager.stopAnyPlayback();
    }
}
