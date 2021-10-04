package com.deffe.macros.status;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class ItemFactory {

    public static BaseVideoItem createItemFromUrl(VideoPlayerManager<MetaData> videoPlayerManager,String url) throws IOException {
        return new UrlVideoItem(videoPlayerManager,url);
    }

    public static BaseVideoItem createItemFromStorageUri(VideoPlayerManager<MetaData> videoPlayerManager, File storageFile, Context context) throws IOException {
        return new StorageVideoItem(videoPlayerManager,storageFile,context);
    }
}
