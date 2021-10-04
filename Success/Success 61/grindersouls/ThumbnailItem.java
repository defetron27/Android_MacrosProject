package com.deffe.macros.grindersouls;

import android.graphics.Bitmap;

import com.deffe.macros.grindersouls.imageprocessors.Filter;

public class ThumbnailItem {
    public Bitmap image;
    public Filter filter;

    ThumbnailItem()
    {
        image = null;
        filter = new Filter();
    }
}
