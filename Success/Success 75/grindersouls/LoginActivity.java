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
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rilixtech.Country;
import com.rilixtech.CountryCodePicker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity
{

    private Toolbar LoginToolbar;

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

    private FirebaseFirestore checkDatabaseReference;
    private DocumentReference storeUserToDatabaseReference;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String userMobileNumberWithOutSpaces;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        ccp = findViewById(R.id.ccp);

        LoginUserMobileNumber = findViewById(R.id.login_user_mobile_number);

        LoginUserReceivedOTPCode = findViewById(R.id.login_user_received_otp_code);

        LoginUserSendOTPCodeButton = findViewById(R.id.login_user_send_otp_code_button);

        LoginUserReceivedOTPCodeVerifyButton = findViewById(R.id.login_user_received_otp_code_verify_button);
        LoginUserResendOTPCodeButton = findViewById(R.id.login_user_resend_otp_code_button);

        LoginUserMobileCodeCountDown = findViewById(R.id.login_user_mobile_code_count_down);

        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        final String simCountry = tm != null ? tm.getSimCountryIso() : null;

        if (simCountry != null)
        {
            Toast.makeText(this, simCountry.toUpperCase(), Toast.LENGTH_SHORT).show();
        }

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

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(LoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if (e instanceof FirebaseTooManyRequestsException)
                {
                    Toast.makeText(LoginActivity.this, "Quota exceeded", Toast.LENGTH_SHORT).show();
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
                if ()
                String userMobileNumber = LoginUserMobileNumber.getText().toString();

                userMobileNumberWithOutSpaces = userMobileNumber.replaceAll("\\s+","");

                UserCountryFullName = ccp.getSelectedCountryName();

                UserCountryPhoneCode = ccp.getSelectedCountryCodeWithPlus();

                UserCountryNameCode = ccp.getSelectedCountryNameCode();

                ccp.setDefaultCountryUsingNameCode(UserCountryNameCode);

                ccp.setCountryPreference(UserCountryNameCode);

                ccp.setCountryForNameCode(UserCountryNameCode);

                ccp.registerPhoneNumberTextView(LoginUserMobileNumber);

                ccp.resetToDefaultCountry();

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

                    FirebaseUser user = task.getResult().getUser();

                    final String online_user_id = user.getUid();

                    final Map<String, Object> deviceToken = new HashMap<>();

                    deviceToken.put("device_token",FirebaseInstanceId.getInstance().getToken());

                    final Map<String, Object> userDetails = new HashMap<>();

                    userDetails.put("user_mobile_number_with_plus",UserCountryPhoneCode+userMobileNumberWithOutSpaces);
                    userDetails.put("user_mobile_number_with_out_plus",userMobileNumberWithOutSpaces);
                    userDetails.put("user_img","default_image");
                    userDetails.put("user_thumb_img","default_thumb_image");
                    userDetails.put("user_unique_id",online_user_id);
                    userDetails.put("user_country_full_name",UserCountryFullName);
                    userDetails.put("user_country_phone_code",UserCountryPhoneCode);
                    userDetails.put("device_token",FirebaseInstanceId.getInstance().getToken());

                    final String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                    checkDatabaseReference = FirebaseFirestore.getInstance();
                    storeUserToDatabaseReference = checkDatabaseReference.collection("Users").document(online_user_id);

                    storeUserToDatabaseReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            if (documentSnapshot.exists())
                            {
                                storeUserToDatabaseReference.update(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        Intent userInviteProfileIntent = new Intent(LoginActivity.this,UserInviteProfileActivity.class);
                                        userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(userInviteProfileIntent);
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(LoginActivity.this, "Error storing data in database " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                storeUserToDatabaseReference.set(userDetails).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        Intent userInviteProfileIntent = new Intent(LoginActivity.this, UserInviteProfileActivity.class);
                                        userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(userInviteProfileIntent);
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(LoginActivity.this, "Error while storing data " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(LoginActivity.this, "Error while storing data " + e.getMessage(), Toast.LENGTH_SHORT).show();
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