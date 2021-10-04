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

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;

public class TabFragment extends Fragment
{
    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3;

    public TabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tab,null);


        viewPager=(ViewPager)v.findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new TabsPagerAdapter( getChildFragmentManager()));

        tabLayout=(TabLayout)v.findViewById(R.id.main_tabs);

        viewPager.setPageTransformer(true,new CubeOutTransformer());

        tabLayout.setupWithViewPager(viewPager);




        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });


        return v;
    }

}