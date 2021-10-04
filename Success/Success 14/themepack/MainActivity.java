package com.deffe.macros.themepack;

import android.content.Intent;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.appthemeengine.ATE;

public class MainActivity extends BaseThemedActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;


    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.appbar_frame,new TabFragment()).commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);



        final Toolbar toolbar = (Toolbar) findViewById(R.id.appbar_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle= new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_view_example));
//        searchView.setIconifiedByDefault(false);
//        searchItem.expandActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        drawerLayout.closeDrawers();

        if (item.getItemId() == R.id.nav_item_settings)
        {
            Intent cameraIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(cameraIntent);
        }

        return true;
    }
}
