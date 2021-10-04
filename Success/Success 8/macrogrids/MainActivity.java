package com.deffe.macros.macrogrids;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ToxicBakery.viewpager.transforms.CubeInTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mainToolbar;

    private String temporary;
    public String userid;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;

    private TabsPagerAdapter tabsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabsPagerAdapter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myViewPager.setPageTransformer(true, new CubeInTransformer());
        myTabLayout.setupWithViewPager(myViewPager);




        mainToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Username");
    }


}
