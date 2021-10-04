package com.deffe.macros.imageprocess.imageprocessors.subfilters;

import android.graphics.Bitmap;

import com.deffe.macros.imageprocess.imageprocessors.ImageProcessor;
import com.deffe.macros.imageprocess.imageprocessors.SubFilter;

public class SaturationSubfilter implements SubFilter {
    private static String tag = "";

    // The Level value is float, where level = 1 has no effect on the image
    private float level;

    public SaturationSubfilter(float level) {
        this.level = level;
    }

    @Override
    public Bitmap process(Bitmap inputImage) {
        return ImageProcessor.doSaturation(inputImage, level);
    }

    @Override
    public Object getTag() {
        return tag;
    }

    @Override
    public void setTag(Object tag) {
        SaturationSubfilter.tag = (String) tag;
    }

    public void setLevel(float level) {
        this.level = level;
    }
}
