package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alexfu.countdownview.CountDownView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rilixtech.CountryCodePicker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DocumentReference storeLoginUserDetailsToDatabaseReference;
    private StorageReference storeProfileImageStorageRef,storeProfileThumbImageStorageRef;

    private CountryCodePicker ccp;
    private AppCompatEditText LoginUserMobileNumber;
    private EditText LoginUserReceivedOTPCode;
    private Button LoginUserSendOTPCodeButton, LoginUserReceivedOTPCodeVerifyButton, LoginUserResendOTPCodeButton;
    private CountDownView LoginUserMobileCodeCountDown;
    private CardView loginUserVerificationCardView;

    private String mVerificationId, userMobileNumberWithOutSpaces, UserCountryFullName, UserCountryNameCode, UserCountryPhoneCode;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private GrinderLoadingProgressBar grinderLoadingProgressBar = new GrinderLoadingProgressBar();

    private CircleImageView loginUserProfileImageCircularImageView;
    private ImageView loginUserProfileImageBackgroundImageView;
    private ImageButton loginUserTakeProfileImageCameraButton;

    private LinearLayout loginUserProfileDetailsInputLinearLayout;
    private EditText loginUserProfileName,loginUserAbout;
    private Button loginUserProfileDetailsDoneButton;

    private Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        storeProfileImageStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        storeProfileThumbImageStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Thumb_Images");

        ccp = findViewById(R.id.ccp);
        loginUserVerificationCardView = findViewById(R.id.login_user_verification_card_view);
        LoginUserMobileNumber = findViewById(R.id.login_user_mobile_number);
        LoginUserSendOTPCodeButton = findViewById(R.id.login_user_send_otp_code_button);
        LoginUserReceivedOTPCode = findViewById(R.id.login_user_received_otp_code);
        LoginUserReceivedOTPCodeVerifyButton = findViewById(R.id.login_user_received_otp_code_verify_button);
        LoginUserResendOTPCodeButton = findViewById(R.id.login_user_resend_otp_code_button);
        LoginUserMobileCodeCountDown = findViewById(R.id.login_user_mobile_code_count_down);

        loginUserProfileImageCircularImageView = findViewById(R.id.login_user_profile_image_circular_image_view);
        loginUserProfileImageBackgroundImageView = findViewById(R.id.login_user_profile_image_background_image_view);
        loginUserTakeProfileImageCameraButton = findViewById(R.id.login_user_take_profile_image_button);
        loginUserProfileDetailsInputLinearLayout = findViewById(R.id.login_user_profile_details_input_linear_layout);
        loginUserProfileName = findViewById(R.id.login_user_profile_name);
        loginUserAbout = findViewById(R.id.login_user_about);
        loginUserProfileDetailsDoneButton = findViewById(R.id.login_user_profile_details_done_button);

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
                LoginUserReceivedOTPCode.setEnabled(false);
                LoginUserReceivedOTPCodeVerifyButton.setEnabled(false);
                LoginUserResendOTPCodeButton.setEnabled(false);

                LoginUserReceivedOTPCode.setVisibility(View.GONE);
                LoginUserReceivedOTPCodeVerifyButton.setVisibility(View.GONE);
                LoginUserResendOTPCodeButton.setVisibility(View.GONE);

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

                grinderLoadingProgressBar.showLoadingBar(LoginActivity.this,"Please wait while processing your request");

                mVerificationId = s;

                mResendToken = forceResendingToken;

                LoginUserMobileNumber.setEnabled(false);
                LoginUserSendOTPCodeButton.setEnabled(false);
                LoginUserMobileNumber.setVisibility(View.GONE);
                LoginUserSendOTPCodeButton.setVisibility(View.GONE);

                LoginUserReceivedOTPCode.setVisibility(View.VISIBLE);
                LoginUserReceivedOTPCodeVerifyButton.setVisibility(View.VISIBLE);
                LoginUserResendOTPCodeButton.setVisibility(View.VISIBLE);
                LoginUserMobileCodeCountDown.setVisibility(View.VISIBLE);
                LoginUserMobileCodeCountDown.start();
            }
        };

        FirebaseUser id = firebaseAuth.getCurrentUser();

        if (id != null)
        {
            String userId = id.getUid();

            storeLoginUserDetailsToDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(userId);

            storeLoginUserDetailsToDatabaseReference.addSnapshotListener(this,new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(LoginActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String thumb_img = documentSnapshot.getString("user_thumb_img");

                        if (thumb_img != null && !thumb_img.equals("default_thumb_image"))
                        {

                            Picasso.with(LoginActivity.this).load(thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.register_user)
                                    .into(loginUserProfileImageCircularImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(LoginActivity.this).load(thumb_img).placeholder(R.drawable.register_user).into(loginUserProfileImageCircularImageView);
                                        }
                                    });
                        }
                    }
                }
            });
        }

        LoginUserSendOTPCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (NetworkStatus.isConnected(LoginActivity.this) && NetworkStatus.isConnectedFast(LoginActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(LoginActivity.this))
                    {
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
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });
        LoginUserReceivedOTPCodeVerifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (NetworkStatus.isConnected(LoginActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(LoginActivity.this))
                    {
                        String code = LoginUserReceivedOTPCode.getText().toString();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

                        signInWithPhoneAuthCredential(credential);
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });
        LoginUserResendOTPCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (NetworkStatus.isConnected(LoginActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(LoginActivity.this))
                    {
                        String mobileNumber = LoginUserMobileNumber.getText().toString();

                        LoginUserMobileCodeCountDown.reset();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber
                                (
                                        UserCountryPhoneCode + mobileNumber,60, TimeUnit.SECONDS, LoginActivity.this, mCallbacks, mResendToken
                                );
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        loginUserTakeProfileImageCameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(LoginActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(LoginActivity.this))
                    {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1 ,1)
                                .start(LoginActivity.this);
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }

            }
        });

        loginUserProfileImageBackgroundImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(LoginActivity.this, "not allowed", Toast.LENGTH_SHORT).show();
            }
        });

        loginUserProfileDetailsDoneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (NetworkStatus.isConnected(LoginActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(LoginActivity.this))
                    {
                        loginUserProfileDetailsDoneButton.setEnabled(false);
                        String userName = loginUserProfileName.getText().toString();
                        String aboutUser = loginUserAbout.getText().toString();

                        saveUserNameAndInviteStatus(userName,aboutUser);
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void verificationCompleted()
    {
        LoginUserSendOTPCodeButton.setVisibility(View.GONE);
        LoginUserMobileNumber.setVisibility(View.GONE);
        ccp.setVisibility(View.GONE);
        LoginUserReceivedOTPCode.setVisibility(View.GONE);
        LoginUserReceivedOTPCodeVerifyButton.setVisibility(View.GONE);
        LoginUserResendOTPCodeButton.setVisibility(View.GONE);
        loginUserVerificationCardView.setVisibility(View.GONE);
        LoginUserMobileCodeCountDown.setVisibility(View.GONE);

        loginUserProfileImageCircularImageView.setVisibility(View.VISIBLE);
        loginUserProfileImageBackgroundImageView.setVisibility(View.VISIBLE);
        loginUserTakeProfileImageCameraButton.setVisibility(View.VISIBLE);
        loginUserProfileDetailsInputLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            String onlineUserId = currentUser.getUid();
            boolean verificationStatus = false;

            SharedPreferences preferences = LoginActivity.this.getSharedPreferences(onlineUserId,MODE_PRIVATE);

            if (preferences != null)
            {
                verificationStatus = preferences.getBoolean("verification_status",false);
            }

            if (verificationStatus)
            {
                SignInUser();
            }
            else
            {
                verificationCompleted();
            }
        }
        else
        {
            ccp.setVisibility(View.VISIBLE);
            LoginUserMobileNumber.setVisibility(View.VISIBLE);
            LoginUserSendOTPCodeButton.setVisibility(View.VISIBLE);
            loginUserVerificationCardView.setVisibility(View.VISIBLE);
        }
    }

    private void SignInUser()
    {
        Intent startPageIntent = new Intent(LoginActivity.this,MainActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
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
                    userDetails.put("date", FieldValue.serverTimestamp());

                    storeLoginUserDetailsToDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id);

                    storeLoginUserDetailsToDatabaseReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            if (documentSnapshot.exists())
                            {
                                storeLoginUserDetailsToDatabaseReference.update(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(online_user_id,MODE_PRIVATE);

                                        if (sharedPreferences != null)
                                        {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("verification_status",false);
                                            editor.apply();
                                            grinderLoadingProgressBar.hideLoadingBar();
                                            verificationCompleted();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        Toast.makeText(LoginActivity.this, "Error storing data in database " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        grinderLoadingProgressBar.hideLoadingBar();
                                    }
                                });
                            }
                            else
                            {
                                storeLoginUserDetailsToDatabaseReference.set(userDetails).addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(online_user_id,MODE_PRIVATE);

                                        if (sharedPreferences != null)
                                        {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("verification_status",false);
                                            editor.apply();
                                            grinderLoadingProgressBar.hideLoadingBar();
                                            verificationCompleted();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        Toast.makeText(LoginActivity.this, "Error while storing data " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        grinderLoadingProgressBar.hideLoadingBar();
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
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            grinderLoadingProgressBar.hideLoadingBar();
                        }
                    });
                }
                else
                {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        Toast.makeText(LoginActivity.this, "Invalid Verification", Toast.LENGTH_SHORT).show();

                        Crashlytics.log(Log.ERROR,TAG,task.getException().toString());

                        Log.e(TAG,task.getException().toString());
                    }
                }
            }
        });
    }

    private void saveUserNameAndInviteStatus(final String userName, String aboutUser)
    {
        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(LoginActivity.this,"Please Enter Your Name", Toast.LENGTH_LONG).show();
        }
        else
        {
            currentUser = firebaseAuth.getCurrentUser();

            if (currentUser != null)
            {
                final String userId = firebaseAuth.getCurrentUser().getUid();

                grinderLoadingProgressBar = new GrinderLoadingProgressBar();
                grinderLoadingProgressBar.showLoadingBar(LoginActivity.this,"Please wait...");

                Map<String, Object> userStatus = new HashMap<>();
                userStatus.put("user_name",userName);
                userStatus.put("about_user",aboutUser);

                storeLoginUserDetailsToDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(userId);

                storeLoginUserDetailsToDatabaseReference.update(userStatus).addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(LoginActivity.this, "Your account is created successfully", Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences(userId,MODE_PRIVATE);

                        if (sharedPreferences != null)
                        {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("verification_status",true);
                            editor.apply();
                            grinderLoadingProgressBar.hideLoadingBar();
                            verificationCompleted();
                        }

                        Intent userInviteProfileIntent = new Intent(LoginActivity.this,MainActivity.class);
                        userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        grinderLoadingProgressBar.hideLoadingBar();
                        startActivity(userInviteProfileIntent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        grinderLoadingProgressBar.hideLoadingBar();
                        Toast.makeText(LoginActivity.this, "Error while storing data in database", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            grinderLoadingProgressBar = new GrinderLoadingProgressBar();
            grinderLoadingProgressBar.showLoadingBar(LoginActivity.this,"Please wait...while processing your request");

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                loginUserProfileImageCircularImageView.setImageURI(resultUri);

                File thumb_filePathUri = new File(resultUri.getPath());

                try
                {
                    thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(50).compressToBitmap(thumb_filePathUri);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null)
                {
                    final String userId = firebaseAuth.getCurrentUser().getUid();

                    final StorageReference imageFilePath = storeProfileImageStorageRef.child(userId + ".jpg");

                    final StorageReference thumbImageFilePath = storeProfileThumbImageStorageRef.child(userId + ".jpg");

                    UploadTask uploadTaskImage = imageFilePath.putFile(resultUri);
                    final UploadTask uploadTaskThumb = thumbImageFilePath.putBytes(thumb_byte);

                    uploadTaskImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                Log.e(TAG,task.getException().toString());
                                Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                                Toast.makeText(LoginActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                grinderLoadingProgressBar.hideLoadingBar();
                            }
                            return imageFilePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                final String downloadUrl = task.getResult().toString();

                                uploadTaskThumb.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                                {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                                    {
                                        if (!task.isSuccessful())
                                        {
                                            Log.e(TAG,task.getException().toString());
                                            Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                                            Toast.makeText(LoginActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            grinderLoadingProgressBar.hideLoadingBar();
                                        }
                                        return thumbImageFilePath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            final String downloadThumbUrl = task.getResult().toString();

                                            Map<String, Object> update_user_data = new HashMap<String, Object>();
                                            update_user_data.put("user_img", downloadUrl);
                                            update_user_data.put("user_thumb_img",downloadThumbUrl);

                                            storeLoginUserDetailsToDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(userId);

                                            storeLoginUserDetailsToDatabaseReference.update(update_user_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Toast.makeText(LoginActivity.this,"Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                    grinderLoadingProgressBar.hideLoadingBar();
                                                }
                                            }).addOnFailureListener(new OnFailureListener()
                                            {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                    Log.e(TAG,e.toString());
                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                    Toast.makeText(LoginActivity.this, "Error while storing image " + e.toString(), Toast.LENGTH_SHORT).show();
                                                    grinderLoadingProgressBar.hideLoadingBar();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        Log.e(TAG,e.getMessage());
                                        Toast.makeText(LoginActivity.this, "Error occurred while uploading image in storage " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        grinderLoadingProgressBar.hideLoadingBar();
                                    }
                                });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            Log.e(TAG,e.getMessage());
                            Toast.makeText(LoginActivity.this, "Error while uploading image in storage " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            grinderLoadingProgressBar.hideLoadingBar();
                        }
                    });
                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                grinderLoadingProgressBar.hideLoadingBar();
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(this, "Storage uri not found" + error.getMessage(), Toast.LENGTH_SHORT).show();
                grinderLoadingProgressBar = new GrinderLoadingProgressBar();
                Log.e(TAG,error.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}