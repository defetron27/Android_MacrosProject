package com.deffe.macros.animations;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.afollestad.appthemeengine.ATE;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerModelManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;


public class MainActivity extends BaseThemedActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private ScrollerViewPager viewPager;
    private DrawerLayout drawerLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Default config
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .commit();
        }





        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appbar_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_menu);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);


        viewPager = (ScrollerViewPager) findViewById(R.id.view_pager);
        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close));

        final NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_view_example));

        //        searchView.setIconifiedByDefault(false);
//        searchItem.expandActionView();

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        drawerLayout.closeDrawers();


        final int mItemId = item.getItemId();

        drawerLayout.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                switch (mItemId)
                {
                    case R.id.nav_item_settings:
                        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                        break;
                }
            }
        },75);

        return true;
    }
}