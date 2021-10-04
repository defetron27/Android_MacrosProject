package com.deffe.macros.macrogrids;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

public class LoginRegisterActivity extends AppCompatActivity
{

    private Button LoginActivityButton;
    private Button RegisterActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        LoginActivityButton = (Button) findViewById(R.id.login_activity_button);
        RegisterActivityButton = (Button) findViewById(R.id.register_activity_button);

        LoginActivityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent loginIntent = new Intent(LoginRegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        RegisterActivityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent registerIntent = new Intent(LoginRegisterActivity.this,RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
