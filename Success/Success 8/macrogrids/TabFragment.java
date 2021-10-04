package com.deffe.macros.macrogrids;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.List;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerModelManager;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class TabFragment extends Fragment
{
    public static TabLayout tabLayout;
    public static ScrollerViewPager viewPager;
    public static int int_items = 3;

    public TabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tab,null);
        tabLayout=(TabLayout)v.findViewById(R.id.tabs);
        viewPager= (ScrollerViewPager) v.findViewById(R.id.viewpager);



        PagerModelManager manager = new PagerModelManager();
        manager.addCommonFragment(StatusFragment.class, getBgRes(), getTitles());
        viewPager.setAdapter(new TabsPagerAdapter( getChildFragmentManager()));
        viewPager.fixScrollSpeed();

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });


        return v;
    }


    private List<String> getTitles(){
        return Lists.newArrayList("1", "2", "3", "4");
    }

    private List<Integer> getBgRes(){
        return Lists.newArrayList(R.drawable.bg1, R.drawable.bg2, R.drawable.bg3, R.drawable.bg4);
    }

}