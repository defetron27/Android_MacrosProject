package com.deffe.macros.soulsspot;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class ViewGroupProfileActivity extends AppCompatActivity
{
    private Toolbar ViewGroupProfileToolbar;

    private CollapsingToolbarLayout ViewGroupProfileCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_profile);

        ViewGroupProfileToolbar = findViewById(R.id.view_group_profile_toolbar);
        setSupportActionBar(ViewGroupProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewGroupProfileCollapsingToolbar = findViewById(R.id.view_group_profile_collapsing_toolbar);
        ViewGroupProfileCollapsingToolbar.setTitle("Group Name");

    }
}
