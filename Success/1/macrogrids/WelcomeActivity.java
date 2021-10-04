package com.deffe.macros.macrogrids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomeActivity extends AppCompatActivity
{

    private CardView AcceptAndContinueView;
    private Button AcceptAndContinueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        AcceptAndContinueView = (CardView) findViewById(R.id.accept_and_continue_view);
        AcceptAndContinueButton = (Button) findViewById(R.id.accept_and_continue_button);

        AcceptAndContinueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent LoginRegisterIntent = new Intent(WelcomeActivity.this,LoginRegisterActivity.class);
                LoginRegisterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LoginRegisterIntent);
                finish();
            }
        });
    }

}
