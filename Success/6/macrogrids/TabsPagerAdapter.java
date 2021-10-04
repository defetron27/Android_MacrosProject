package com.deffe.macros.macrogrids;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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
                CameraFragment cameraFragment = new CameraFragment();
                return cameraFragment;
            case 1:
                StatusFragment statusFragment = new StatusFragment();
                return statusFragment;
            case 2:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Camera";
            case 1:
                return "Status";
            case 2:
                return "Chats";
            default:
                return null;
        }
    }
}
