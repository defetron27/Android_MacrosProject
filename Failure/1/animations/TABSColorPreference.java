package com.deffe.macros.animations;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Deffe on 3/23/2018.
 */

public class TABSColorPreference extends Preference
{
    private View mView;
    private int color;
    private int border;
    private String mKey;


    public TABSColorPreference(Context context)
    {
        this(context, (AttributeSet)null, 0);
        this.init(context, (AttributeSet)null);
    }

    public TABSColorPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
        this.init(context, attrs);
    }


    public TABSColorPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        this.setLayoutResource(Res.tabsLayout.tabs_preference_custom);
        this.setWidgetLayoutResource(Res.tabsLayout.ate_preference_color);
        this.setPersistent(false);

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, Res.tabsStyleable.TABSColorPreference, 0, 0);

            try {
                this.mKey = a.getString(Res.tabsStyleable.TABSPreferenceCategory_tabsKey_prefCategory_textColor);
            } finally {
                a.recycle();
            }
        }
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mView = view;
        ATE.themeView(view, this.mKey);
        this.invalidateColor();
    }


}
