package com.deffe.macros.status;

import android.app.Activity;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ItemFactory {

    public static BaseVideoItem createItemFromUrl(VideoPlayerManager<MetaData> videoPlayerManager,String url) throws IOException {
        return new UrlVideoItem(videoPlayerManager,url);
    }
}
