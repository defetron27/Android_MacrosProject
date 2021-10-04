package com.deffe.macros.status;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ItemFactory {

    public static BaseVideoItem createItemFromUri(VideoPlayerManager<MetaData> videoPlayerManager, Uri uri,Context context) throws IOException {
        return new UriVideoItem(videoPlayerManager,uri,context);
    }
}
