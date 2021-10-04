package com.deffe.macros.soulsspot;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class FriendsActivity extends AppCompatActivity
{

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.friends_viewPager);



        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FriendsTabsAdapter(fragmentManager));
    }


    public class FriendsTabsAdapter extends FragmentStatePagerAdapter
    {

        FriendsTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    return new FriendsFragment();
                case 1:
                    return new ReceivedFriendRequestFragment();
                case 2:
                    return new SendFriendRequestFragment();
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
                    return "Friends";
                case 1:
                    return "Received";
                case 2:
                    return "Sent";
                default:
                    return null;
            }
        }
    }

}
