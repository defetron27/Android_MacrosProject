package com.deffe.macros.animations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerModelManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;


public class MainActivity extends AppCompatActivity
{

    ScrollerViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ScrollerViewPager) findViewById(R.id.view_pager);
        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        PagerModelManager manager = new PagerModelManager();

        manager.addFragment(getItem(0),getPageTitle(0));
        manager.addFragment(getItem(1),getPageTitle(1));
        manager.addFragment(getItem(2),getPageTitle(2));
        manager.addFragment(getItem(3),getPageTitle(3));
        manager.addFragment(getItem(4),getPageTitle(4));


        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);



        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();
        viewPager.setPageTransformer(true,new CubeOutTransformer());

        // just set viewPager
        springIndicator.setViewPager(viewPager);

    }

    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new StatusFragment();
            case 1:
                return new ChatsFragment();
            case 2:
                return new FriendsFragment();
            case 3:
                return new OnlineFragment();
            case 4:
                return new TabFragment();
            default:
                return null;
        }

    }

    public int getCount()
    {
        return 4;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Status";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            case 3:
                return "Online";
            case 4:
                return "Tab";
            default:
                return null;
        }
    }


}