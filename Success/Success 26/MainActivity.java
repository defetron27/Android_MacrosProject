package com.deffe.macros.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mainToolbar;

    private String temporary;
    public String userid;

    private DatabaseReference UserDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private DatabaseReference getUserDataReference;
    private StorageReference storeProfileImagestorageRef,thumbImgRef;

    ImageButton addCamera;

    ImageView UserProfileImg;

    Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();


        String online_user_id = firebaseAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        storeProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        thumbImgRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");


        getSupportActionBar().setTitle("Username");

        UserProfileImg = (ImageView) findViewById(R.id.invite_user_img);

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


        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {


            UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }



        getUserDataReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String img = (String) dataSnapshot.child("user_img").getValue();
                String thumb_img = (String) dataSnapshot.child("user_thumb_img").getValue();


                if (img != null && !img.equals("default_profile")) {


                    Picasso.with(MainActivity.this).load(img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.oleksii)
                            .into(UserProfileImg, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
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
        else if(currentUser != null)
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


}
