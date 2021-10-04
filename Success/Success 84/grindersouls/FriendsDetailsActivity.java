package com.deffe.macros.grindersouls;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import github.chenupt.springindicator.SpringIndicator;

public class FriendsDetailsActivity extends BaseThemedActivity
{

    private ViewPager viewPager;
    private DocumentReference userDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

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
        setContentView(R.layout.activity_friends_details);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        final Toolbar toolbar = findViewById(R.id.friends_details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.friends_details_viewPager);

        SpringIndicator springIndicator = findViewById(R.id.friends_details_indicator);

        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new FriendsTabsAdapter(fragmentManager));

        springIndicator.setViewPager(viewPager);

        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            userDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id);
        }

        refreshViewPager();
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
                    return new UserFriendsFragment();
                case 1:
                    return new UserReceivedFriendRequestsFragment();
                case 2:
                    return new UserSentFriendRequestsFragment();
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


    private void refreshViewPager()
    {
        String transformers = ViewPagerSettingsActivity.getTransformers(this);

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

    @Override
    protected void onStart()
    {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            userDatabaseReference.update("online","true");
        }
        else
        {
            Intent userInviteProfileIntent = new Intent(FriendsDetailsActivity.this, LoginActivity.class);
            userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(userInviteProfileIntent);
            finish();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(currentUser != null)
        {
            Calendar calFordATE = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            final String time = currentDate.format(calFordATE.getTime());

            try
            {
                Date date = currentDate.parse(time);
                long millis = date.getTime();

                userDatabaseReference.update("online",millis);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
    }

}
