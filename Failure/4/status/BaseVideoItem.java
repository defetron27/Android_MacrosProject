package com.deffe.macros.status;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseVideoItem implements VideoItem, ListItem
{

    private final VideoPlayerManager<MetaData> mVideoPlayerManager;

    BaseVideoItem(VideoPlayerManager<MetaData> videoPlayerManager) {
        mVideoPlayerManager = videoPlayerManager;
    }


    public abstract void update(int position, VideoViewHolder view, VideoPlayerManager videoPlayerManager);


    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {
        VideoViewHolder viewHolder = (VideoViewHolder) newActiveView.getTag();
        playNewVideo(new CurrentItemMetaData(newActiveViewPosition, newActiveView), viewHolder.mPlayer, mVideoPlayerManager);
    }


    @Override
    public void deactivate(View currentView, int position) {
        stopPlayback(mVideoPlayerManager);
    }

    public View createView(ViewGroup parent, int screenWidth)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = screenWidth;

        final VideoViewHolder videoViewHolder = new VideoViewHolder(view);
        view.setTag(videoViewHolder);

        videoViewHolder.mPlayer.addMediaPlayerListener(new MediaPlayerWrapper.MainThreadMediaPlayerListener()
        {
            @Override
            public void onVideoSizeChangedMainThread(int width, int height)
            {
            }

            @Override
            public void onVideoPreparedMainThread()
            {
                videoViewHolder.mPlayer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideoCompletionMainThread() {
            }

            @Override
            public void onErrorMainThread(int what, int extra) {
            }

            @Override
            public void onBufferingUpdateMainThread(int percent) {
            }

            @Override
            public void onVideoStoppedMainThread()
            {
                videoViewHolder.mPlayer.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }
}
