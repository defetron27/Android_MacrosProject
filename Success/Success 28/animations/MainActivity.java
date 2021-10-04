package com.deffe.macros.animations;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.springindicator.SpringIndicator;


public class MainActivity extends BaseThemedActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private ViewPager viewPager;
    private DrawerLayout drawerLayout;

    private String temporary;
    public String userid;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;



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

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_menu);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);


        viewPager = (ViewPager) findViewById(R.id.view_pager);

        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close));

        final NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);


        FragmentManager fragmentManager = getSupportFragmentManager();



        viewPager.setPageTransformer(true,new CubeOutTransformer());

        viewPager.setAdapter(new TabsAdapter(fragmentManager));


        // just set viewPager

        springIndicator.setViewPager(viewPager);

        refreshViewPager();


        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null)
        {
            LogOutUser();
        }
        else
        {
            UserDatabaseReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(currentUser != null)
        {
            UserDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void LogOutUser()
    {
        Intent startPageIntent = new Intent(MainActivity.this,LoginActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }



    private void refreshViewPager()
    {
        String transformers = ViewPagerSettings.getTransformers(this);

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
                viewPager.setPageTransformer(true, new ABaseTransformer()
                {
                    @Override
                    protected void onTransform(View view, float position) {
                        final float scale = 1f + Math.abs(position);
                        view.setScaleX(scale);
                        view.setScaleY(scale);
                        view.setPivotX(view.getWidth() * 0.5f);
                        view.setPivotY(view.getHeight() * 0.5f);
                        view.setAlpha(position < -1f || position > 1f ? 0f : 1f - (scale - 1f));
                        if(position == -1){
                            view.setTranslationX(view.getWidth() * -1);
                        }
                    }
                });
                break;

        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        refreshViewPager();
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
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.settings)
        {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        }

        if (id == R.id.viewPagerTransformer)
        {
            startActivity(new Intent(MainActivity.this,ViewPagerSettings.class));
        }

        return super.onOptionsItemSelected(item);
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
                    case R.id.nav_item_friends:
                        startActivity(new Intent(MainActivity.this,FriendsActivity.class));
                }
            }
        },75);

        return true;
    }
}