package com.deffe.macros.status;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class VideoViewHolder extends RecyclerView.ViewHolder{

    public final VideoPlayerView mPlayer;
    private final TextView mTitle;

    VideoViewHolder(View view) {
        super(view);
        mPlayer = view.findViewById(R.id.player);
        mTitle = view.findViewById(R.id.title);
    }
}
