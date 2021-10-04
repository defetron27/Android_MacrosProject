package com.deffe.macros.animations;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;

/**
 * Created by Deffe on 3/23/2018.
 */

public class TABSPreferenceCategory extends PreferenceCategory
{
    private String mTABSKey;



    @TargetApi(21)
    public TABSPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTABSKey = context.getTheme().obtainStyledAttributes(attrs, Res.tabsStyleable.TABSPreferenceCategory, 0, 0).getString(Res.tabsStyleable.TABSPreferenceCategory_tabsKey_prefCategory_textColor);

    }

    public TABSPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.mTABSKey = context.getTheme().obtainStyledAttributes(attrs, Res.tabsStyleable.TABSPreferenceCategory, 0, 0).getString(Res.tabsStyleable.TABSPreferenceCategory_tabsKey_prefCategory_textColor);

    }

    public TABSPreferenceCategory(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mTABSKey = context.getTheme().obtainStyledAttributes(attrs, Res.tabsStyleable.TABSPreferenceCategory, 0, 0).getString(Res.tabsStyleable.TABSPreferenceCategory_tabsKey_prefCategory_textColor);

    }

    public TABSPreferenceCategory(Context context, String tabsKey)
    {
        super(context);

        this.mTABSKey =  tabsKey;
    }


    protected void onBindView(View view) {
        super.onBindView(view);
        @SuppressLint("ResourceType") TextView mTitle = (TextView)view.findViewById(16908310);
        mTitle.setTag(String.format("%s|body,%s|accent_color", new Object[]{"text_size", "text_color"}));
        ATE.themeView(mTitle, this.mTABSKey);
    }
}
