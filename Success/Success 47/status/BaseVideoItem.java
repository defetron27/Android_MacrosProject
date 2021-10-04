package com.deffe.macros.status;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseVideoItem implements VideoItem, ListItem{

    private static final boolean SHOW_LOGS = false;
    private static final String TAG = BaseVideoItem.class.getSimpleName();



    private final Rect mCurrentViewRect = new Rect();
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

        videoViewHolder.mPlayer.addMediaPlayerListener(new MediaPlayerWrapper.MainThreadMediaPlayerListener() {
            @Override
            public void onVideoSizeChangedMainThread(int width, int height) {
            }

            @Override
            public void onVideoPreparedMainThread() {
                videoViewHolder.mCover.setVisibility(View.INVISIBLE);
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
            public void onVideoStoppedMainThread() {
                videoViewHolder.mCover.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }


    @Override
    public int getVisibilityPercents(View currentView) {
        if(SHOW_LOGS) Logger.v(TAG, ">> getVisibilityPercents currentView " + currentView);

        int percents = 100;

        currentView.getLocalVisibleRect(mCurrentViewRect);
        if(SHOW_LOGS) Logger.v(TAG, "getVisibilityPercents mCurrentViewRect top " + mCurrentViewRect.top + ", left " + mCurrentViewRect.left + ", bottom " + mCurrentViewRect.bottom + ", right " + mCurrentViewRect.right);

        int height = currentView.getHeight();
        if(SHOW_LOGS) Logger.v(TAG, "getVisibilityPercents height " + height);

        if(viewIsPartiallyHiddenTop()){
            // view is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if(viewIsPartiallyHiddenBottom(height)){
            percents = mCurrentViewRect.bottom * 100 / height;
        }

        setVisibilityPercentsText(currentView, percents);
        if(SHOW_LOGS) Logger.v(TAG, "<< getVisibilityPercents, percents " + percents);

        return percents;
    }

    private void setVisibilityPercentsText(View currentView, int percents) {
        if(SHOW_LOGS) Logger.v(TAG, "setVisibilityPercentsText percents " + percents);
        VideoViewHolder videoViewHolder = (VideoViewHolder) currentView.getTag();
        String percentsText = "Visibility percents: " + String.valueOf(percents);

        videoViewHolder.mVisibilityPercents.setText(percentsText);
    }

    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }
}
