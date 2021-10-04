package com.deffe.macros.status;

import android.content.res.AssetFileDescriptor;

import java.util.Arrays;


public class SingleVideoPlayerManager implements VideoPlayerManager<MetaData>, VideoPlayerManagerCallback, MediaPlayerWrapper.MainThreadMediaPlayerListener {

    private static final String TAG = SingleVideoPlayerManager.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;


    private final MessagesHandlerThread mPlayerHandler = new MessagesHandlerThread();

    private final PlayerItemChangeListener mPlayerItemChangeListener;

    private VideoPlayerView mCurrentPlayer = null;
    private PlayerMessageState mCurrentPlayerState = PlayerMessageState.IDLE;

    SingleVideoPlayerManager(PlayerItemChangeListener playerItemChangeListener) {
        mPlayerItemChangeListener = playerItemChangeListener;
    }


    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, String videoUrl) {
        if(SHOW_LOGS) Logger.v(TAG, ">> playNewVideo, videoPlayer " + videoPlayerView + ", mCurrentPlayer " + mCurrentPlayer + ", videoPlayerView " + videoPlayerView);

        /** 1. */
        mPlayerHandler.pauseQueueProcessing(TAG);

        boolean currentPlayerIsActive = mCurrentPlayer == videoPlayerView;
        boolean isAlreadyPlayingTheFile =
                mCurrentPlayer != null &&
                        videoUrl.equals(mCurrentPlayer.getVideoUrlDataSource());

        if (SHOW_LOGS) Logger.v(TAG, "playNewVideo, isAlreadyPlayingTheFile " + isAlreadyPlayingTheFile);
        if (SHOW_LOGS) Logger.v(TAG, "playNewVideo, currentPlayerIsActive " + currentPlayerIsActive);
        /** 2. */
        if(currentPlayerIsActive){
            if(isInPlaybackState() && isAlreadyPlayingTheFile){
                if(SHOW_LOGS) Logger.v(TAG, "playNewVideo, videoPlayer " + videoPlayerView + " is already in state " + mCurrentPlayerState);
                /** 3. */
            } else {
                /** 4. */
                startNewPlayback(currentItemMetaData, videoPlayerView, videoUrl);
            }
        } else {
            /** 4. */
            startNewPlayback(currentItemMetaData, videoPlayerView, videoUrl);
        }

        /** 5. */
        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< playNewVideo, videoPlayer " + videoPlayerView + ", videoUrl " + videoUrl);
    }


    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor) {
        if(SHOW_LOGS) Logger.v(TAG, ">> playNewVideo, videoPlayer " + videoPlayerView + ", mCurrentPlayer " + mCurrentPlayer + ", assetFileDescriptor " + assetFileDescriptor);
        if(SHOW_LOGS) Logger.v(TAG, "playNewVideo, currentItemMetaData " + currentItemMetaData);

        /** 1. */
        mPlayerHandler.pauseQueueProcessing(TAG);

        boolean currentPlayerIsActive = mCurrentPlayer == videoPlayerView;
        boolean isAlreadyPlayingTheFile =
                mCurrentPlayer != null &&
                mCurrentPlayer.getAssetFileDescriptorDataSource() == assetFileDescriptor;

        if (SHOW_LOGS) Logger.v(TAG, "playNewVideo, isAlreadyPlayingTheFile " + isAlreadyPlayingTheFile);
        if (SHOW_LOGS) Logger.v(TAG, "playNewVideo, currentPlayerIsActive " + currentPlayerIsActive);

        if(currentPlayerIsActive)
        {
            if(isInPlaybackState() && isAlreadyPlayingTheFile)
            {
                if(SHOW_LOGS) Logger.v(TAG, "playNewVideo, videoPlayer " + videoPlayerView + " is already in state " + mCurrentPlayerState);
            }
            else
                {
                    startNewPlayback(currentItemMetaData, videoPlayerView, assetFileDescriptor);
                }
        }
        else
            {
                startNewPlayback(currentItemMetaData, videoPlayerView, assetFileDescriptor);
            }

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< playNewVideo, videoPlayer " + videoPlayerView + ", assetFileDescriptor " + assetFileDescriptor);
    }

    private boolean isInPlaybackState() {
        boolean isPlaying = mCurrentPlayerState == PlayerMessageState.STARTED || mCurrentPlayerState == PlayerMessageState.STARTING;
        if(SHOW_LOGS) Logger.v(TAG, "isInPlaybackState, " + isPlaying);
        return isPlaying;
    }


    private void startNewPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor) {

        videoPlayerView.addMediaPlayerListener(this);
        if (SHOW_LOGS) Logger.v(TAG, "startNewPlayback, mCurrentPlayerState " + mCurrentPlayerState);


        mPlayerHandler.clearAllPendingMessages(TAG);

        stopResetReleaseClearCurrentPlayer();

        setNewViewForPlayback(currentItemMetaData, videoPlayerView);

        startPlayback(videoPlayerView, assetFileDescriptor);
    }


    private void startNewPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, String videoUrl)
    {

        videoPlayerView.addMediaPlayerListener(this);
        if (SHOW_LOGS) Logger.v(TAG, "startNewPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.clearAllPendingMessages(TAG);

        stopResetReleaseClearCurrentPlayer();
        setNewViewForPlayback(currentItemMetaData, videoPlayerView);
        startPlayback(videoPlayerView, videoUrl);
    }


    @Override
    public void stopAnyPlayback() {
        if(SHOW_LOGS) Logger.v(TAG, ">> stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.pauseQueueProcessing(TAG);
        if (SHOW_LOGS) Logger.v(TAG, "stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);

        mPlayerHandler.clearAllPendingMessages(TAG);
        stopResetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< stopAnyPlayback, mCurrentPlayerState " + mCurrentPlayerState);
    }


    @Override
    public void resetMediaPlayer() {
        if(SHOW_LOGS) Logger.v(TAG, ">> resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);


        mPlayerHandler.pauseQueueProcessing(TAG);
        if (SHOW_LOGS) Logger.v(TAG, "resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
        mPlayerHandler.clearAllPendingMessages(TAG);
        resetReleaseClearCurrentPlayer();

        mPlayerHandler.resumeQueueProcessing(TAG);

        if(SHOW_LOGS) Logger.v(TAG, "<< resetMediaPlayer, mCurrentPlayerState " + mCurrentPlayerState);
    }


    private void startPlayback(VideoPlayerView videoPlayerView, String videoUrl) {
        if(SHOW_LOGS) Logger.v(TAG, "startPlayback");

        mPlayerHandler.addMessages(Arrays.asList(
                new CreateNewPlayerInstance(videoPlayerView, this),
                new SetUrlDataSourceMessage(videoPlayerView, videoUrl, this),
                new Prepare(videoPlayerView, this),
                new Start(videoPlayerView, this)
        ));
    }

    private void startPlayback(VideoPlayerView videoPlayerView, AssetFileDescriptor assetFileDescriptor) {
        if(SHOW_LOGS) Logger.v(TAG, "startPlayback");

        mPlayerHandler.addMessages(Arrays.asList(
                new CreateNewPlayerInstance(videoPlayerView, this),
                new SetAssetsDataSourceMessage(videoPlayerView, assetFileDescriptor, this),
                new Prepare(videoPlayerView, this),
                new Start(videoPlayerView, this)
        ));
    }


    private void setNewViewForPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView) {
        if(SHOW_LOGS) Logger.v(TAG, "setNewViewForPlayback, currentItemMetaData " + currentItemMetaData + ", videoPlayer " + videoPlayerView);
        mPlayerHandler.addMessage(new SetNewViewForPlayback(currentItemMetaData, videoPlayerView, this));
    }


    private void stopResetReleaseClearCurrentPlayer() {
        if(SHOW_LOGS) Logger.v(TAG, "stopResetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState +", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState){
            case SETTING_NEW_PLAYER:
            case IDLE:

            case CREATING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CREATED:

            case CLEARING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CLEARED:
                // in these states player is stopped
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
                mPlayerHandler.addMessage(new Stop(mCurrentPlayer, this));
                //FALL-THROUGH

            case SETTING_DATA_SOURCE:
            case DATA_SOURCE_SET:
            case STOPPING:
            case STOPPED:
            case ERROR: // reset if error
            case PLAYBACK_COMPLETED:
                mPlayerHandler.addMessage(new Reset(mCurrentPlayer, this));
                //FALL-THROUGH
            case RESETTING:
            case RESET:
                mPlayerHandler.addMessage(new Release(mCurrentPlayer, this));
                //FALL-THROUGH
            case RELEASING:
            case RELEASED:
                mPlayerHandler.addMessage(new ClearPlayerInstance(mCurrentPlayer, this));

                break;
            case END:
                throw new RuntimeException("unhandled " + mCurrentPlayerState);
        }
    }

    private void resetReleaseClearCurrentPlayer() {
        if(SHOW_LOGS) Logger.v(TAG, "resetReleaseClearCurrentPlayer, mCurrentPlayerState " + mCurrentPlayerState +", mCurrentPlayer " + mCurrentPlayer);

        switch (mCurrentPlayerState){
            case SETTING_NEW_PLAYER:
            case IDLE:

            case CREATING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CREATED:

            case SETTING_DATA_SOURCE:
            case DATA_SOURCE_SET:

            case CLEARING_PLAYER_INSTANCE:
            case PLAYER_INSTANCE_CLEARED:
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
            case STOPPING:
            case STOPPED:
            case ERROR: // reset if error
            case PLAYBACK_COMPLETED:
                mPlayerHandler.addMessage(new Reset(mCurrentPlayer, this));
                //FALL-THROUGH
            case RESETTING:
            case RESET:
                mPlayerHandler.addMessage(new Release(mCurrentPlayer, this));
                //FALL-THROUGH
            case RELEASING:
            case RELEASED:
                mPlayerHandler.addMessage(new ClearPlayerInstance(mCurrentPlayer, this));

                break;
            case END:
                throw new RuntimeException("unhandled " + mCurrentPlayerState);
        }
    }


    @Override
    public void setCurrentItem(MetaData currentItemMetaData, VideoPlayerView videoPlayerView) {
        if(SHOW_LOGS) Logger.v(TAG, ">> onPlayerItemChanged");

        mCurrentPlayer = videoPlayerView;
        mPlayerItemChangeListener.onPlayerItemChanged(currentItemMetaData);

        if(SHOW_LOGS) Logger.v(TAG, "<< onPlayerItemChanged");
    }


    @Override
    public void setVideoPlayerState(VideoPlayerView videoPlayerView, PlayerMessageState playerMessageState) {
        if(SHOW_LOGS) Logger.v(TAG, ">> setVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);

        mCurrentPlayerState = playerMessageState;

        if(SHOW_LOGS) Logger.v(TAG, "<< setVideoPlayerState, playerMessageState " + playerMessageState + ", videoPlayer " + videoPlayerView);
    }

    @Override
    public PlayerMessageState getCurrentPlayerState() {
        if(SHOW_LOGS) Logger.v(TAG, "getCurrentPlayerState, mCurrentPlayerState " + mCurrentPlayerState);
        return mCurrentPlayerState;
    }

    @Override
    public void onVideoSizeChangedMainThread(int width, int height) {
    }

    @Override
    public void onVideoPreparedMainThread() {
    }

    @Override
    public void onVideoCompletionMainThread() {
        mCurrentPlayerState = PlayerMessageState.PLAYBACK_COMPLETED;
    }

    @Override
    public void onErrorMainThread(int what, int extra) {
        if(SHOW_LOGS) Logger.v(TAG, "onErrorMainThread, what " + what + ", extra " + extra);


        mCurrentPlayerState = PlayerMessageState.ERROR;
    }

    @Override
    public void onBufferingUpdateMainThread(int percent) {
    }

    @Override
    public void onVideoStoppedMainThread() {

    }
}
