package com.deffe.macros.themepack;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by Deffe on 3/17/2018.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter
{
    public TabsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new StatusFragment();
            case 1:
                return new ChatsFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Status";
            case 1:
                return "Chats";
            default:
                return null;
        }
    }

}
