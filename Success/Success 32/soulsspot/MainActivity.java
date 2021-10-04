package com.deffe.macros.soulsspot;

import android.app.SearchManager;
import android.content.Context;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;

import github.chenupt.springindicator.SpringIndicator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,TabLayout.OnTabSelectedListener,SearchView.OnQueryTextListener,IFragmentListener
{

    private ViewPager viewPager;
    private DrawerLayout drawerLayout;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    ArrayList<ISearch> iSearch = new ArrayList<>();
    private MenuItem searchMenuItem;
    private String newText;

    TabsPageAdapter tabsPageAdapter;

    private TabLayout tabLayout;

    ArrayList<String> listData = null;

    IDataCallback iDataCallback = null;

    public void setiDataCallback(IDataCallback iDataCallback)
    {
        this.iDataCallback = iDataCallback;
        iDataCallback.onFragmentCreated(listData);
    }

    @Override
    public void addiSearch(ISearch iSearch)
    {
        this.iSearch.add(iSearch);
    }

    @Override
    public void removeISearch(ISearch iSearch)
    {
        this.iSearch.remove(iSearch);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        listData = new ArrayList<>();

        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        tabLayout = (TabLayout) findViewById(R.id.main_tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Status"));
        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Groups"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close));

        final NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);

        tabsPageAdapter = new TabsPageAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),newText);

        viewPager.setAdapter(tabsPageAdapter);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(this);
    }


    public class TabsPageAdapter extends FragmentStatePagerAdapter
    {
        private String mSearchTerm;
        //integer to count number of tabs
        int tabCount;

        TabsPageAdapter(FragmentManager fm, int tabCount, String searchTerm)
        {
            super(fm);
            //Initializing tab count
            this.tabCount= tabCount;
            this.mSearchTerm= searchTerm;
        }


        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    return StatusFragment.newInstance(mSearchTerm);
                case 1:
                    return ChatsFragment.newInstance(mSearchTerm);
                case 2:
                    return GroupsFragment.newInstance(mSearchTerm);
                default:
                    return null;
            }
        }

        @Override
        public int getCount()
        {
            return tabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
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

        void setTextQueryChanged(String newText)
        {
            mSearchTerm = newText;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    public void getDataFromFragment_one(ArrayList<String> listData)
    {
        this.listData = listData;
        Log.e("-->", "" + listData.toString());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        this.newText = newText;

        tabsPageAdapter.setTextQueryChanged(newText);

        for (ISearch iSearchLocal : this.iSearch)
        {
            iSearchLocal.onTextQuery(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


}
