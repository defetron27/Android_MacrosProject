 package com.deffe.macros.readcontacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

 public class MainActivity extends AppCompatActivity
{
    private ViewPager viewPager;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new TabsPageAdapter(fragmentManager));
    }







    public class TabsPageAdapter extends FragmentStatePagerAdapter
    {

        public TabsPageAdapter(FragmentManager fm)
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
                case 2:
                    return new FriendsFragment();
                case 3:
                    return new OnlineFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount()
        {
            return 4;
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
                case 2:
                    return "Friends";
                case 3:
                    return  "Online";
                default:
                    return null;
            }
        }
    }


}
