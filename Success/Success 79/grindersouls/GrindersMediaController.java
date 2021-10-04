package com.deffe.macros.grindersouls;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

public class GrindersMediaController extends MediaController
{
    private FrameLayout anchorView;

    public GrindersMediaController(Context context, FrameLayout anchorView)
    {
        super(context);
        this.anchorView = anchorView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) anchorView.getLayoutParams();

        layoutParams.setMargins(0,0,0,h);

        anchorView.setLayoutParams(layoutParams);
        anchorView.requestLayout();
    }
}
