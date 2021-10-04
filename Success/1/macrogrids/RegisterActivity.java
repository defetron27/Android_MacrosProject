package com.deffe.macros.macrogrids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity
{

    private Toolbar RegisterToolbar;

    private EditText RegisterUserName;
    private EditText RegisterUserMobileNumber;

    private EditText RegisterUserReceivedOTPCode;

    private Button RegisterUserSendOTPCodeButton;

    private Button RegisterUserReceivedOTPCodeVerifyButton;
    private Button RegisterUserResendButton;

    private String mVerificationId;
    private static String userID;

    private boolean check = true;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference storeUserDefaultDataReference;
    private DatabaseReference CheckDataReference;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        RegisterToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(RegisterToolbar);
        getSupportActionBar().setTitle("New User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RegisterUserName = (EditText) findViewById(R.id.register_user_name);
        RegisterUserMobileNumber = (EditText)findViewById(R.id.register_user_mobile_number);

        RegisterUserReceivedOTPCode = (EditText) findViewById(R.id.register_user_received_otp_code);

        RegisterUserSendOTPCodeButton = (Button) findViewById(R.id.register_user_send_otp_code_button);

        RegisterUserReceivedOTPCodeVerifyButton = (Button) findViewById(R.id.register_user_received_otp_verify_button);
        RegisterUserResendButton = (Button) findViewById(R.id.register_user_resend_button);


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Toast.makeText(RegisterActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(RegisterActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if (e instanceof FirebaseTooManyRequestsException)
                {

                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                Toast.makeText(RegisterActivity.this, "Code has been Sent", Toast.LENGTH_SHORT).show();

                mVerificationId = s;

                mResendToken = forceResendingToken;

                RegisterUserName.setVisibility(View.GONE);
                RegisterUserMobileNumber.setVisibility(View.GONE);
                RegisterUserSendOTPCodeButton.setVisibility(View.GONE);

                RegisterUserReceivedOTPCode.setVisibility(View.VISIBLE);
                RegisterUserReceivedOTPCodeVerifyButton.setVisibility(View.VISIBLE);
                RegisterUserResendButton.setVisibility(View.VISIBLE);
            }
        };

        RegisterUserSendOTPCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String userName = RegisterUserName.getText().toString();
                final String userMobileNumber = RegisterUserMobileNumber.getText().toString();

                RegisterUserAccount(userName,userMobileNumber);

                CheckDataReference = FirebaseDatabase.getInstance().getReference();

                CheckDataReference.child("Users").child(userMobileNumber).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.getKey() != null)
                        {
                            Toast.makeText(RegisterActivity.this, "This Mobile Number already registered, Try Login", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            PhoneAuthProvider.getInstance().verifyPhoneNumber
                                    (
                                            "+91" + userMobileNumber, 60, TimeUnit.SECONDS, RegisterActivity.this, mCallbacks
                                    );
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });







            }
        });
        RegisterUserReceivedOTPCodeVerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String code = RegisterUserReceivedOTPCode.getText().toString();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

                signInWithPhoneAuthCredential(credential);
            }
        });
        RegisterUserResendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String mobileNumber = RegisterUserMobileNumber.getText().toString();

                PhoneAuthProvider.getInstance().verifyPhoneNumber
                        (
                                "+91" + mobileNumber,60, TimeUnit.SECONDS, RegisterActivity.this, mCallbacks, mResendToken
                        );
            }
        });

    }

    private void RegisterUserAccount(String userName, String userMobileNumber)
    {
        if (TextUtils.isEmpty(userName))
        {
            Toast.makeText(this, "Please Enter Your Name Properly", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(userMobileNumber))
        {
            Toast.makeText(this, "Please Enter Your Mobile Number Properly", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            final String current_user_Id = firebaseAuth.getCurrentUser().getUid();

                            Toast.makeText(RegisterActivity.this, "Verification Completed", Toast.LENGTH_SHORT).show();

                            String DeviceToken = FirebaseInstanceId.getInstance().getToken();
                            String userName = RegisterUserName.getText().toString();
                            String userMobileNumber = RegisterUserMobileNumber.getText().toString();

                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userMobileNumber);

                            storeUserDefaultDataReference.setValue(current_user_Id);
                            storeUserDefaultDataReference.child("user_name").setValue(userName);
                            storeUserDefaultDataReference.child("user_mobile_number").setValue(userMobileNumber);
                            storeUserDefaultDataReference.child("user_welcome_status").setValue("Hey Hi Guys,I'm using Macro-Grids");
                            storeUserDefaultDataReference.child("user_img").setValue("default_profile");
                            storeUserDefaultDataReference.child("user_thumb_img").setValue("default_image");
                            storeUserDefaultDataReference.child("user_unique_id").setValue(current_user_Id);
                            storeUserDefaultDataReference.child("device_token").setValue(DeviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {

                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                Toast.makeText(RegisterActivity.this, "Invalid Verification", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
        });
    }
}
