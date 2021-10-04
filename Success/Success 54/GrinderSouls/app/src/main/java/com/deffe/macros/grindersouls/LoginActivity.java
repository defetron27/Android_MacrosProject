package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alexfu.countdownview.CountDownView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rilixtech.Country;
import com.rilixtech.CountryCodePicker;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity
{
    private CountryCodePicker ccp;

    private AppCompatEditText LoginUserMobileNumber;

    private EditText LoginUserReceivedOTPCode;

    private Button LoginUserSendOTPCodeButton;

    private Button LoginUserReceivedOTPCodeVerifyButton;
    private Button LoginUserResendOTPCodeButton;

    private CountDownView LoginUserMobileCodeCountDown;

    private String mVerificationId;

    private String UserCountryFullName,UserCountryNameCode,UserCountryPhoneCode;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference storeUserDefaultDataReference;
    private DatabaseReference CheckDataReference;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String userMobileNumberWithOutSpaces;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storeUserDefaultDataReference.keepSynced(true);

        ccp = findViewById(R.id.ccp);

        LoginUserMobileNumber = findViewById(R.id.login_user_mobile_number);

        LoginUserReceivedOTPCode = findViewById(R.id.login_user_received_otp_code);

        LoginUserSendOTPCodeButton = findViewById(R.id.login_user_send_otp_code_button);

        LoginUserReceivedOTPCodeVerifyButton = findViewById(R.id.login_user_received_otp_code_verify_button);
        LoginUserResendOTPCodeButton = findViewById(R.id.login_user_resend_otp_code_button);

        LoginUserMobileCodeCountDown = findViewById(R.id.login_user_mobile_code_count_down);

        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        final String simCountry = tm != null ? tm.getSimCountryIso() : null;

        Toast.makeText(this, simCountry.toUpperCase(), Toast.LENGTH_SHORT).show();


        ccp.setDefaultCountryUsingNameCode(simCountry);

        ccp.setCountryPreference(simCountry);

        ccp.setCountryForNameCode(simCountry);

        ccp.registerPhoneNumberTextView(LoginUserMobileNumber);

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
                Toast.makeText(LoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(LoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if (e instanceof FirebaseTooManyRequestsException)
                {

                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                Toast.makeText(LoginActivity.this, "Code has been Sent", Toast.LENGTH_SHORT).show();

                mVerificationId = s;

                mResendToken = forceResendingToken;

                LoginUserMobileNumber.setVisibility(View.GONE);

                LoginUserSendOTPCodeButton.setVisibility(View.GONE);


                LoginUserReceivedOTPCode.setVisibility(View.VISIBLE);
                LoginUserReceivedOTPCodeVerifyButton.setVisibility(View.VISIBLE);
                LoginUserResendOTPCodeButton.setVisibility(View.VISIBLE);
                LoginUserMobileCodeCountDown.setVisibility(View.VISIBLE);
                LoginUserMobileCodeCountDown.start();
            }
        };

        LoginUserSendOTPCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String userMobileNumber = LoginUserMobileNumber.getText().toString();

                userMobileNumberWithOutSpaces = userMobileNumber.replaceAll("\\s+","");

                Toast.makeText(LoginActivity.this, userMobileNumberWithOutSpaces, Toast.LENGTH_SHORT).show();

                UserCountryFullName = ccp.getSelectedCountryName();

                UserCountryPhoneCode = ccp.getSelectedCountryCodeWithPlus();

                UserCountryNameCode = ccp.getSelectedCountryNameCode();

                ccp.setDefaultCountryUsingNameCode(UserCountryNameCode);

                ccp.setCountryPreference(UserCountryNameCode);

                ccp.setCountryForNameCode(UserCountryNameCode);

                ccp.registerPhoneNumberTextView(LoginUserMobileNumber);


                ccp.resetToDefaultCountry();


                Toast.makeText(LoginActivity.this, UserCountryPhoneCode, Toast.LENGTH_SHORT).show();

                Toast.makeText(LoginActivity.this, UserCountryFullName, Toast.LENGTH_SHORT).show();

                RegisterUserAccount(userMobileNumber);

                PhoneAuthProvider.getInstance().verifyPhoneNumber
                        (
                                UserCountryPhoneCode + userMobileNumber, 60, TimeUnit.SECONDS, LoginActivity.this, mCallbacks
                        );



            }
        });
        LoginUserReceivedOTPCodeVerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String code = LoginUserReceivedOTPCode.getText().toString();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

                signInWithPhoneAuthCredential(credential);
            }
        });
        LoginUserResendOTPCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String mobileNumber = LoginUserMobileNumber.getText().toString();

                LoginUserMobileCodeCountDown.reset();

                PhoneAuthProvider.getInstance().verifyPhoneNumber
                        (
                                UserCountryPhoneCode + mobileNumber,60, TimeUnit.SECONDS, LoginActivity.this, mCallbacks, mResendToken
                        );
            }
        });

    }

    private void RegisterUserAccount(String userMobileNumber)
    {
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
                    Toast.makeText(LoginActivity.this, "Verification Completed", Toast.LENGTH_SHORT).show();

                    final String online_user_id = firebaseAuth.getCurrentUser().getUid();
                    final String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                    CheckDataReference = FirebaseDatabase.getInstance().getReference();

                    CheckDataReference.child("Users").child(online_user_id).addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.getValue() != null)
                            {
                                storeUserDefaultDataReference.child(online_user_id).child("device_token").setValue(DeviceToken)
                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                Intent userInviteProfileIntent = new Intent(LoginActivity.this,UserInviteProfileActivity.class);
                                                userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(userInviteProfileIntent);
                                                finish();
                                            }
                                        });
                            }
                            else
                            {
                                storeUserDefaultDataReference.child(online_user_id).child("user_mobile_number_with_plus").setValue(UserCountryPhoneCode+userMobileNumberWithOutSpaces);
                                storeUserDefaultDataReference.child(online_user_id).child("user_mobile_number_with_out_plus").setValue(userMobileNumberWithOutSpaces);
                                storeUserDefaultDataReference.child(online_user_id).child("user_img").setValue("default_profile");
                                storeUserDefaultDataReference.child(online_user_id).child("user_thumb_img").setValue("default_image");
                                storeUserDefaultDataReference.child(online_user_id).child("user_unique_id").setValue(online_user_id);
                                storeUserDefaultDataReference.child(online_user_id).child("user_country_full_name").setValue(UserCountryFullName);
                                storeUserDefaultDataReference.child(online_user_id).child("user_country_phone_code").setValue(UserCountryPhoneCode);
                                storeUserDefaultDataReference.child(online_user_id).child("device_token").setValue(DeviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    Intent userInviteProfileIntent = new Intent(LoginActivity.this, UserInviteProfileActivity.class);
                                                    userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(userInviteProfileIntent);
                                                    finish();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        Toast.makeText(LoginActivity.this, "Invalid Verification", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
