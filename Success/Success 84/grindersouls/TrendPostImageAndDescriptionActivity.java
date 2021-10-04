package com.deffe.macros.grindersouls;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class TrendPostImageAndDescriptionActivity extends AppCompatActivity
{
    private final static String TAG = TrendPostImageAndDescriptionActivity.class.getSimpleName();

    private FirebaseAuth firebaseAuth;

    private String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_post_image_and_description);

        Intent trend = getIntent();
        String action = trend.getAction();

        ImageView trendPostImageView = findViewById(R.id.trend_post_image);
        final EditText imageDesc = findViewById(R.id.trend_post_image_description);
        final FloatingActionButton uploadTrendPost = findViewById(R.id.upload_trend_post);

        firebaseAuth = FirebaseAuth.getInstance();

        onlineUserId = firebaseAuth.getCurrentUser().getUid();

        Uri imageUri = null;

        if (action != null && action.equals(Intent.ACTION_VIEW))
        {
            imageUri = Uri.parse(trend.getExtras().getString("trend_uri"));

            if (imageUri != null)
            {
                trendPostImageView.setImageURI(imageUri);
            }
        }

        if (imageUri != null)
        {
            final Uri finalImageUri = imageUri;

            uploadTrendPost.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String desc = imageDesc.getText().toString();

                    uploadPost(desc,finalImageUri);
                }
            });
        }
    }

    private void uploadPost(final String desc, Uri finalImageUri)
    {
        if (TextUtils.isEmpty(desc))
        {
            Toast.makeText(this, "Please enter description of post", Toast.LENGTH_SHORT).show();
        }
        else
        {
            final CollectionReference uploadPost = FirebaseFirestore.getInstance().collection("TrendPosts");

            DocumentReference documentReference = uploadPost.document();

            final String documentId = documentReference.getId();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("TrendPosts").child(documentId + ".jpg");

            UploadTask uploadTask = storageReference.putFile(finalImageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        Log.e(TAG,task.getException().toString());
                        Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                        Toast.makeText(TrendPostImageAndDescriptionActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        final String downloadUrl = task.getResult().toString();

                        Map<String,Object> trendPost = new HashMap<>();

                        trendPost.put("url",downloadUrl);
                        trendPost.put("user_key",onlineUserId);
                        trendPost.put("post_key",documentId);
                        trendPost.put("desc",desc);
                        trendPost.put("time", FieldValue.serverTimestamp());
                        trendPost.put("stars",0);

                        uploadPost.document(documentId).set(trendPost).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (!task.isSuccessful())
                                {
                                    Log.e(TAG,task.getException().toString());
                                    Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                                    Toast.makeText(TrendPostImageAndDescriptionActivity.this, "Error while storing trend post details " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(TrendPostImageAndDescriptionActivity.this, "Trend Post upload successfully", Toast.LENGTH_SHORT).show();

                                    Intent followIntent = new Intent(TrendPostImageAndDescriptionActivity.this,FollowersActivity.class);
                                    startActivity(followIntent);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                Toast.makeText(TrendPostImageAndDescriptionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Toast.makeText(TrendPostImageAndDescriptionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
