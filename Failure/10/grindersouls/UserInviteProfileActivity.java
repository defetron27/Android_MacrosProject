package com.deffe.macros.grindersouls;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class UserInviteProfileActivity extends AppCompatActivity
{

    private FirebaseAuth firebaseAuth;

    private DatabaseReference getUserDataReference,userInviteProfile,PostRef;

    private StorageReference storeProfileImagestorageRef,thumbImgRef;

    ImageButton addCamera;

    CircleImageView UserProfileImg;

    Bitmap thumb_bitmap = null;

    private EditText userInviteProfileName,userInviteProfileStatus;

    String online_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_invite_profile);

        firebaseAuth = FirebaseAuth.getInstance();


        online_user_id = firebaseAuth.getCurrentUser().getUid();



        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        userInviteProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        userInviteProfile.keepSynced(true);

        PostRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        PostRef.keepSynced(true);

        storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        thumbImgRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");


        Button doneUserInviteProfile = findViewById(R.id.user_invite_profile_done);

        userInviteProfileName = findViewById(R.id.user_invite_profile_name);

        userInviteProfileStatus = findViewById(R.id.user_invite_profile_status);

        UserProfileImg = findViewById(R.id.user_invite_pic);


        getUserDataReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String thumb_img = (String) dataSnapshot.child("user_thumb_img").getValue();

                if (thumb_img != null && !thumb_img.equals("default_image"))
                {

                    Picasso.with(UserInviteProfileActivity.this).load(thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.register_user)
                            .into(UserProfileImg, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(UserInviteProfileActivity.this).load(thumb_img).placeholder(R.drawable.register_user).into(UserProfileImg);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        doneUserInviteProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userName = userInviteProfileName.getText().toString();
                String userProfileStatus = userInviteProfileStatus.getText().toString();

                saveUserNameandInviteStatus(userName,userProfileStatus);
            }
        });


        addCamera = findViewById(R.id.add_user_invite_profile_image_btn);

        addCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1 ,1)
                        .start(UserInviteProfileActivity.this);
            }
        });
    }

    private void saveUserNameandInviteStatus(final String userName, String userProfileStatus)
    {
        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(UserInviteProfileActivity.this,"Please Enter Your Name", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(userProfileStatus))
        {
            Toast.makeText(UserInviteProfileActivity.this,"Please Enter Your InviteStatus", Toast.LENGTH_LONG).show();
        }
        else
        {
            userInviteProfile.child("user_name").setValue(userName);
            userInviteProfile.child("user_invite_profile_status").setValue(userProfileStatus)
                   .addOnCompleteListener(new OnCompleteListener<Void>()
                   {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           if (task.isSuccessful())
                           {
                               PostRef.child(online_user_id).child("empty_post").child("posted_user_name").setValue(userName);
                               PostRef.child(online_user_id).child("empty_post").child("posted_time").setValue("default_time");
                               PostRef.child(online_user_id).child("empty_post").child("posted_image").setValue("default_img")
                                       .addOnCompleteListener(new OnCompleteListener<Void>()
                                       {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task)
                                           {
                                               if (task.isSuccessful())
                                               {
                                                   Toast.makeText(UserInviteProfileActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

                                                   Intent userInviteProfileIntent = new Intent(UserInviteProfileActivity.this,MainActivity.class);
                                                   userInviteProfileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                   startActivity(userInviteProfileIntent);
                                                   finish();
                                               }
                                           }
                                       });
                           }
                       }
                   });
        }

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
                            Toast.makeText(UserInviteProfileActivity.this,"Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                {
                                    final String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

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
                                                if (task.isSuccessful())
                                                {
                                                    PostRef.child(online_user_id).child("empty_post").child("posted_user_thumb_img").setValue(thumb_downloadUrl)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    Toast.makeText(UserInviteProfileActivity.this,"Image Updated Successfully..", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(UserInviteProfileActivity.this,"Error Occurred, While Uploading Your Profile Image", Toast.LENGTH_LONG).show();

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
}
