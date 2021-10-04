package com.deffe.macros.apps;

import android.media.MediaPlayer;



public class Stop extends PlayerMessage {
    public Stop(VideoPlayerView videoView, VideoPlayerManagerCallback callback) {
        super(videoView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        currentPlayer.stop();
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.STOPPING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.STOPPED;
    }
}
