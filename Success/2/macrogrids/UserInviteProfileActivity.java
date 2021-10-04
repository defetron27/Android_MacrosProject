package com.deffe.macros.macrogrids;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class UserInviteProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_invite_profile);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.invite_app_bar_layout);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        collapsingToolbarLayout.setTitle("Your Invite Profile Settings");

        Context context = this;
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(context,R.color.colorAccent));
    }
}
