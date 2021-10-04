package com.deffe.macros.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ToxicBakery.viewpager.transforms.ABaseTransformer;
import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.ToxicBakery.viewpager.transforms.CubeInTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.ToxicBakery.viewpager.transforms.FlipHorizontalTransformer;
import com.ToxicBakery.viewpager.transforms.FlipVerticalTransformer;
import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer;
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer;
import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutSlideTransformer;

public class TabsLayoutActivity extends AppCompatActivity
{
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_layout);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        viewPager = (ViewPager) findViewById(R.id.viewPager);


        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new TabsAdapter(fragmentManager));

        refreshViewPager();
    }


    private void refreshViewPager()
    {
        String transformers = SettingsActivity.getTransformers(this);

        switch (transformers)
        {
            case "DefaultTransformer":
                viewPager.setPageTransformer(true,new DefaultTransformer());
                break;
            case "AccordionTransformer":
                viewPager.setPageTransformer(true,new AccordionTransformer());
                break;
            case "BackgroundToForegroundTransformer":
                viewPager.setPageTransformer(true,new BackgroundToForegroundTransformer());
                break;
            case "CubeInTransformer":
                viewPager.setPageTransformer(true,new CubeInTransformer());
                break;
            case "CubeOutTransformer":
                viewPager.setPageTransformer(true,new CubeOutTransformer());
                break;
            case "DepthPageTransformer":
                viewPager.setPageTransformer(true,new DepthPageTransformer());
                break;
            case "DrawerTransformer":
                viewPager.setPageTransformer(true, new ABaseTransformer()
                {
                    @Override
                    protected void onTransform(View view, float position)
                    {
                        if (position <= 0)
                        {
                            view.setTranslationX(0);
                        }
                        else if (position > 0 && position <= 1)
                        {
                            view.setTranslationX(-view.getWidth() / 2 * position);
                        }
                    }
                });
                break;
            case "FlipHorizontalTransformer":
                viewPager.setPageTransformer(true,new FlipHorizontalTransformer());
                break;
            case "FlipVerticalTransformer":
                viewPager.setPageTransformer(true,new FlipVerticalTransformer());
                break;
            case "ForegroundToBackgroundTransformer":
                viewPager.setPageTransformer(true,new ForegroundToBackgroundTransformer());
                break;
            case "RotateDownTransformer":
                viewPager.setPageTransformer(true,new RotateDownTransformer());
                break;
            case "RotateUpTransformer":
                viewPager.setPageTransformer(true,new RotateUpTransformer());
                break;
            case "ScaleInOutTransformer":
                viewPager.setPageTransformer(true,new ScaleInOutTransformer());
                break;
            case "StackTransformer":
                viewPager.setPageTransformer(true,new StackTransformer());
                break;
            case "TabletTransformer":
                viewPager.setPageTransformer(true,new TabletTransformer());
                break;
            case "ZoomInTransformer":
                viewPager.setPageTransformer(true,new ZoomInTransformer());
                break;
            case "ZoomOutSlideTransformer":
                viewPager.setPageTransformer(true,new ZoomOutSlideTransformer());
                break;
            case "ZoomOutTransformer":
                viewPager.setPageTransformer(true,new ZoomOutSlideTransformer());
                break;

        }
    }



    public class TabsAdapter extends FragmentStatePagerAdapter
    {

        public TabsAdapter(FragmentManager fm) {
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
                    return new GroupsFragment();
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
                    return "Status";
                case 1:
                    return "Chats";
                case 2:
                    return "Groups";
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_tabs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_tabs_settings)
        {

            startActivity(new Intent(TabsLayoutActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        refreshViewPager();
    }
}
