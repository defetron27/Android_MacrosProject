package com.deffe.macros.soulsspot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import github.chenupt.springindicator.SpringIndicator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private ViewPager viewPager;
    private DrawerLayout drawerLayout;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        viewPager = findViewById(R.id.view_pager);

        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();


        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
            UserDatabaseReference.keepSynced(true);
        }



        drawerLayout = findViewById(R.id.drawerLayout);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close));

        final NavigationView navView = findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager.setAdapter(new TabsPageAdapter(fragmentManager));

        springIndicator.setViewPager(viewPager);

    }


    public class TabsPageAdapter extends FragmentStatePagerAdapter
    {

        TabsPageAdapter(FragmentManager fm)
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
                    case R.id.nav_item_friends:
                        startActivity(new Intent(MainActivity.this,FriendsActivity.class));
                        break;
                    case R.id.nav_item_all_friends:
                        startActivity(new Intent(MainActivity.this,AllFriendsActivity.class));
                        break;
                }
            }
        },75);

        return true;
    }
}
