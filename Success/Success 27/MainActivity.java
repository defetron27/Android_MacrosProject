package com.deffe.macros.profile;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mainToolbar;

    private String temporary;
    public String userid;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private DatabaseReference getUserDataReference,userInviteProfile,storeProfileNameAndStatus;
    private StorageReference storeProfileImagestorageRef,thumbImgRef;

    ImageButton addCamera;

    CircleImageView UserProfileImg;

    Bitmap thumb_bitmap = null;

    private ImageView UserImageBgColor;

    private int currentUserProfileImgBgColor;

    private EditText userInviteProfileName,userInviteProfileStatus;

    private Button DoneUserInviteProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("");

        DoneUserInviteProfile = (Button) findViewById(R.id.user_invite_profile_done);

        userInviteProfileName = (EditText) findViewById(R.id.user_invite_profile_name);

        userInviteProfileStatus = (EditText) findViewById(R.id.user_invite_profile_status);



        currentUserProfileImgBgColor = ContextCompat.getColor(this,R.color.colorUserProfileImgBg);



        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        storeProfileNameAndStatus = FirebaseDatabase.getInstance().getReference().child("Users");


        if(currentUser != null)
        {
            String online_user_id = firebaseAuth.getCurrentUser().getUid();

            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

            getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
            getUserDataReference.keepSynced(true);

            userInviteProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

            storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

            thumbImgRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");


            getUserDataReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String img = (String) dataSnapshot.child("user_img").getValue();
                    String thumb_img = (String) dataSnapshot.child("user_thumb_img").getValue();

                    if (!img.equals("default_profile"))
                    {


                        Picasso.with(MainActivity.this).load(img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.oleksii)
                                .into(UserProfileImg, new Callback()
                                {
                                    @Override
                                    public void onSuccess()
                                    {
                                    }

                                    @Override
                                    public void onError()
                                    {
                                        Picasso.with(MainActivity.this).load(img).placeholder(R.drawable.oleksii).into(UserProfileImg);
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });



            DoneUserInviteProfile.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    storeProfileNameAndStatus.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String userName = userInviteProfileName.getText().toString();
                            String userProfileStatus = userInviteProfileStatus.getText().toString();

                            userInviteProfile.child("user_name").setValue(userName);
                            userInviteProfile.child("user_invite_profile_status").setValue(userProfileStatus);

                            Intent userInviteProfileIntent = new Intent(MainActivity.this,TabsLayoutActivity.class);
                            userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(userInviteProfileIntent);
                            finish();

                            Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            Toast.makeText(MainActivity.this, "ErrorOccurred!..", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });



        }

        UserProfileImg = (CircleImageView) findViewById(R.id.user_invite_pic);

        UserImageBgColor = (ImageView) findViewById(R.id.user_profile_img_bg_color);


        addCamera = (ImageButton) findViewById(R.id.add_user_invite_profile_image_btn);

        addCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1 ,1)
                        .start(MainActivity.this);
            }
        });




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                File thumb_filePathUri = new File(resultUri.getPath());

                firebaseAuth = FirebaseAuth.getInstance();

                String user_id = firebaseAuth.getCurrentUser().getUid();

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

                StorageReference filePath = storeProfileImagestorageRef.child(user_id + ".jpg");

                final StorageReference thumb_filePath = thumbImgRef.child(user_id + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this,"Saving Your Profile Image to Firebase Storage",Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        Map<String, Object> update_user_data = new HashMap<String, Object>();
                                        update_user_data.put("user_img", downloadUrl);
                                        update_user_data.put("user_thumb_img",thumb_downloadUrl);

                                        getUserDataReference.updateChildren(update_user_data).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                Toast.makeText(MainActivity.this,"Image Updated Successfully..",Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,"Error Occurred, While Uploading Your Profile Image",Toast.LENGTH_LONG).show();

                        }
                    }
                });


            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null)
        {
            LogOutUser();
        }
        else
        {
            UserDatabaseReference.child("online").setValue("true");
        }
    }



    @Override
    protected void onStop()
    {
        super.onStop();

        if(currentUser != null)
        {
            UserDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void LogOutUser()
    {
        Intent startPageIntent = new Intent(MainActivity.this,LoginActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.invite_profile_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (item.getItemId() == R.id.change_user_invite_profile_bg)
        {

            ColorPickerDialogBuilder
                    .with(MainActivity.this)
                    .setTitle("Choose color")
                    .initialColor(currentUserProfileImgBgColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor)
                        {
                            Toast.makeText(MainActivity.this, "onColorSelected: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setPositiveButton("ok", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int selectedColor, Integer[] allColors)
                        {


                            storeProfileNameAndStatus.addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {

                                    String color = Integer.toHexString(selectedColor);

                                    userInviteProfile.child("user_invite_profile_img_bg_color").setValue(color);

                                    Toast.makeText(MainActivity.this, "Color Saved!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {
                                    Toast.makeText(MainActivity.this, "ErrorOccurred!..", Toast.LENGTH_SHORT).show();
                                }
                            });


                            UserImageBgColor.setBackgroundColor(selectedColor);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .build()
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

}
