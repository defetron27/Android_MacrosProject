package com.deffe.macros.animations;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.SearchView;

import com.afollestad.appthemeengine.tagprocessors.TagProcessor;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import java.util.HashMap;

/**
 * Created by Deffe on 3/23/2018.
 */

public class TABSBase
{
    protected static final String DEFAULT_PROCESSOR = "[default]";
    private static HashMap<String, ViewProcessor> mViewProcessors;
    private static HashMap<String, TagProcessor> mTagProcessors;
    protected static Class<?> didPreApply = null;

    public TABSBase()
    {
    }




    private static void initViewProcessors()
    {
        mViewProcessors = new HashMap(5);
        mViewProcessors.put("[default]", new DefaultProcessor());
        mViewProcessors.put(SearchView.class.getName(), new SearchViewProcessor());
        mViewProcessors.put(Toolbar.class.getName(), new ToolbarProcessor());
        if(ATEUtil.isInClassPath("android.support.design.widget.NavigationView")) {
            mViewProcessors.put("android.support.design.widget.NavigationView", new NavigationViewProcessor());
        } else {
            Log.d("ATEBase", "NavigationView isn't in the class path. Ignoring.");
        }

        if(ATEUtil.isInClassPath("android.support.v7.widget.SearchView")) {
            mViewProcessors.put("android.support.v7.widget.SearchView", new SearchViewProcessor());
        } else {
            Log.d("ATEBase", "SearchView isn't in the class path. Ignoring.");
        }

    }
}
