package com.deffe.macros.macrogrids;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserInviteProfileActivity extends AppCompatActivity
{

    private EditText UserInviteProfileName;
    private EditText UserInviteProfileStatus;

    private Button UserSaveInviteProfileSettingButton;
    private Button UserAfterSaveInviteProfileNextButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_invite_profile);

        UserInviteProfileName = (EditText) findViewById(R.id.user_invite_profile_name);
        UserInviteProfileStatus = (EditText) findViewById(R.id.user_invite_profile_status);


        UserSaveInviteProfileSettingButton = (Button) findViewById(R.id.user_save_invite_profile_settings_buttton);
        UserAfterSaveInviteProfileNextButton = (Button) findViewById(R.id.user_after_save_invite_profile_settings_next_button);

        UserSaveInviteProfileSettingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UserAfterSaveInviteProfileNextButton.setVisibility(View.VISIBLE);
                UserSaveInviteProfileSettingButton.setVisibility(View.INVISIBLE);
            }
        });

        UserAfterSaveInviteProfileNextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent mainIntent = new Intent(UserInviteProfileActivity.this,MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }
        });

    }
}
