package com.deffe.macros.grindersouls.imageprocessors;

import android.graphics.Bitmap;


public interface SubFilter {
    Bitmap process(Bitmap inputImage);

    Object getTag();

    void setTag(Object tag);
}
