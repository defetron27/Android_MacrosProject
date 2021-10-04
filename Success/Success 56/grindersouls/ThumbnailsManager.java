package com.deffe.macros.grindersouls;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;


public final class ThumbnailsManager {
    private static List<ThumbnailItem> filterThumbs = new ArrayList<>(10);
    private static List<ThumbnailItem> processedThumbs = new ArrayList<>(10);

    private ThumbnailsManager() {
    }

    public static void addThumb(ThumbnailItem thumbnailItem) {
        filterThumbs.add(thumbnailItem);
    }

    public static List<ThumbnailItem> processThumbs(Context context) {
        for (ThumbnailItem thumb : filterThumbs) {
            // scaling down the image
            thumb.image = Bitmap.createScaledBitmap(thumb.image, 80,  80, false);
            thumb.image = thumb.filter.processFilter(thumb.image);
            //cropping circle
            thumb.image = GeneralUtils.generateCircularBitmap(thumb.image);
            processedThumbs.add(thumb);
        }
        return processedThumbs;
    }

    public static void clearThumbs()
    {
        filterThumbs = new ArrayList<>();
        processedThumbs = new ArrayList<>();
    }
}
