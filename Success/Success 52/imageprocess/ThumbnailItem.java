package com.deffe.macros.imageprocess;

import android.graphics.Bitmap;

import com.deffe.macros.imageprocess.imageprocessors.Filter;


public class ThumbnailItem {
    public Bitmap image;
    public Filter filter;

    public ThumbnailItem() {
        image = null;
        filter = new Filter();
    }
}
