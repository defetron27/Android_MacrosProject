package com.deffe.macros.imageprocess.imageprocessors.subfilters;

import android.graphics.Bitmap;

import com.deffe.macros.imageprocess.imageprocessors.ImageProcessor;
import com.deffe.macros.imageprocess.imageprocessors.SubFilter;


public class ContrastSubFilter implements SubFilter {

    private static String tag = "";

    // The value is in fraction, value 1 has no effect
    private float contrast = 0;

    /**
     * Initialise contrast subfilter
     *
     * @param contrast The contrast value ranges in fraction, value 1 has no effect
     */
    public ContrastSubFilter(float contrast) {
        this.contrast = contrast;
    }

    @Override
    public Bitmap process(Bitmap inputImage) {
        return ImageProcessor.doContrast(contrast, inputImage);
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(Object tag) {
        ContrastSubFilter.tag = (String) tag;
    }

    /**
     * Sets the contrast value by the value passed in as parameter
     */
    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    /**
     * Changes contrast value by the value passed in as a parameter
     */
    public void changeContrast(float value) {
        this.contrast += value;
    }
}
