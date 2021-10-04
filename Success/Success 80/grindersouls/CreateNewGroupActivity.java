package com.deffe.macros.grindersouls;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreateNewGroupActivity extends AppCompatActivity
{
    private static final String TAG = CreateNewGroupActivity.class.getSimpleName();

    private Toolbar CreateNewGroupToolbar;

    private ArrayList<String> SelectedUsers = new ArrayList<>();

    private ArrayList<String> AdminTotalFriends = new ArrayList<>();

    private ArrayList<String> AdminAndTotalFriends = new ArrayList<>();

    private String adminUserId,onlineUserId;

    private TextView SelectedMembers;

    private EditText CreateNewGroupNameEditText;

    private CircleImageView NewGroupImage;

    private RecyclerView SelectedFriendsNamesAndImagesRecyclerView;

    private FirebaseAuth firebaseAuth;

    private CollectionReference selectedFriendsRef,adminNewGroupRef;

    private SelectedFriendsRecyclerView selectedFriendsRecyclerView;

    private Bitmap thumb_bitmap = null;

    private StorageReference createGroupThumbImgRef,storeGroupProfileImagestorageRef;

    private String downloadUrl,thumb_downloadUrl;

    private String EnteredNewGroupName;

    private FloatingActionButton CreateNewGroupButton;

    private Uri resultUri;

    byte[] thumb_byte;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_new_group);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        SelectedUsers = getIntent().getStringArrayListExtra("selected_friends");

        adminUserId = getIntent().getExtras().getString("admin_user_id");

        AdminTotalFriends = getIntent().getStringArrayListExtra("friends_count");

        firebaseAuth = FirebaseAuth.getInstance();

        onlineUserId = firebaseAuth.getCurrentUser().getUid();

        AdminAndTotalFriends.add(onlineUserId);

        AdminAndTotalFriends.addAll(SelectedUsers);

        CreateNewGroupToolbar = findViewById(R.id.create_new_group_tool_bar);
        setSupportActionBar(CreateNewGroupToolbar);

        if (SelectedUsers.size() == 1)
        {
            getSupportActionBar().setSubtitle("     You and "+ SelectedUsers.size() + " Friend");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            getSupportActionBar().setSubtitle("     You and "+ SelectedUsers.size() + " Friends");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        CreateNewGroupToolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });


        SelectedMembers = findViewById(R.id.selected_members);
        SelectedMembers.setText("Selected Members: " + SelectedUsers.size() + " of " + AdminTotalFriends.size());

        selectedFriendsRef = FirebaseFirestore.getInstance().collection("Users");

        adminNewGroupRef = FirebaseFirestore.getInstance().collection("Groups");

        storeGroupProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Images");

        createGroupThumbImgRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Thumb_Images");

        NewGroupImage = findViewById(R.id.create_new_group_image);

        CreateNewGroupNameEditText = findViewById(R.id.create_new_group_name_edit_text);

        CreateNewGroupButton = findViewById(R.id.create_new_group_button);

        NewGroupImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1 ,1)
                        .start(CreateNewGroupActivity.this);
            }
        });

        SelectedFriendsNamesAndImagesRecyclerView = findViewById(R.id.selected_members_recycler_view);
        SelectedFriendsNamesAndImagesRecyclerView.setLayoutManager(new GridLayoutManager(CreateNewGroupActivity.this,4, GridLayoutManager.VERTICAL,false));
        SelectedFriendsNamesAndImagesRecyclerView
                .addItemDecoration(new VerticalDividerItemDecoration.Builder(CreateNewGroupActivity.this)
                        .color(Color.TRANSPARENT)
                        .size(10)
                        .build());
        selectedFriendsRecyclerView = new SelectedFriendsRecyclerView(AdminAndTotalFriends,CreateNewGroupActivity.this);
        SelectedFriendsNamesAndImagesRecyclerView.setAdapter(selectedFriendsRecyclerView);

        CreateNewGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EnteredNewGroupName = CreateNewGroupNameEditText.getText().toString();

                if (TextUtils.isEmpty(EnteredNewGroupName))
                {
                    Toast.makeText(CreateNewGroupActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final GrinderLoadingProgressBar grinderLoadingProgressBar = new GrinderLoadingProgressBar();

                    grinderLoadingProgressBar.showLoadingBar(CreateNewGroupActivity.this,"Please wait.. while creating your group");

                    SelectedUsers.add(onlineUserId);

                    final Map<String,Object> selectedMembersWithType = new HashMap<>();
                    final Map<String,Object> selectedMembersWithNames = new HashMap<>();

                    DocumentReference group_unique_key = adminNewGroupRef.document();

                    final String group_key = group_unique_key.getId();

                    final StorageReference filePath = storeGroupProfileImagestorageRef.child(group_key + ".jpg");

                    final StorageReference thumb_filePath = createGroupThumbImgRef.child(group_key + ".jpg");

                    if (resultUri != null && thumb_byte != null)
                    {
                        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(CreateNewGroupActivity.this,"Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                                    downloadUrl = task.getResult().toString();

                                    UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                        {
                                            thumb_downloadUrl = thumb_task.getResult().toString();

                                            if(thumb_task.isSuccessful())
                                            {
                                                Map<String, Object> newGroup = new HashMap<>();
                                                newGroup.put("group_name",EnteredNewGroupName);
                                                newGroup.put("time", FieldValue.serverTimestamp());
                                                newGroup.put("group_profile_image", downloadUrl);
                                                newGroup.put("group_profile_thumb_image", thumb_downloadUrl);
                                                newGroup.put("group_main_admin",onlineUserId);

                                                adminNewGroupRef.document(group_key).set(newGroup).addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (!task.isSuccessful())
                                                        {
                                                            Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                            Log.e(TAG,task.getException().getMessage());
                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }

                                                        if (task.isSuccessful())
                                                        {
                                                            Runnable runnable = new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    for (int i = 0; i < SelectedUsers.size(); i++)
                                                                    {
                                                                        final int finalI = i;

                                                                        selectedFriendsRef.document(SelectedUsers.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>()
                                                                        {
                                                                            @Override
                                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                                                                            {
                                                                                if (e != null)
                                                                                {
                                                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                                    Log.e(TAG,e.getMessage());
                                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                if ( documentSnapshot != null && documentSnapshot.exists())
                                                                                {
                                                                                    String name = documentSnapshot.getString("user_name");

                                                                                    if (SelectedUsers.get(finalI).equals(onlineUserId))
                                                                                    {
                                                                                        selectedMembersWithType.put("type","admin");

                                                                                        selectedMembersWithNames.put("name",name);

                                                                                        adminNewGroupRef.document(group_key).collection("group_members_with_types").document(onlineUserId).set(selectedMembersWithType).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (!task.isSuccessful())
                                                                                                {
                                                                                                    Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                                                                    Log.e(TAG,task.getException().getMessage());
                                                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    adminNewGroupRef.document(group_key).collection("group_members_with_names").document(onlineUserId).set(selectedMembersWithNames).addOnFailureListener(new OnFailureListener()
                                                                                                    {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e)
                                                                                                        {
                                                                                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                                                            Log.e(TAG,e.getMessage());
                                                                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                    else
                                                                                    {
                                                                                        selectedMembersWithType.put( "type" , "not_admin");

                                                                                        selectedMembersWithNames.put( "name" , name);

                                                                                        adminNewGroupRef.document(group_key).collection("group_members_with_types").document(SelectedUsers.get(finalI)).set(selectedMembersWithType).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (!task.isSuccessful())
                                                                                                {
                                                                                                    Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                                                                    Log.e(TAG,task.getException().getMessage());
                                                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    adminNewGroupRef.document(group_key).collection("group_members_with_names").document(SelectedUsers.get(finalI)).set(selectedMembersWithNames).addOnFailureListener(new OnFailureListener()
                                                                                                    {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e)
                                                                                                        {
                                                                                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                                                            Log.e(TAG,e.getMessage());
                                                                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            };

                                                            new Thread(runnable).start();

                                                            Map<String, Object> admin = new HashMap<>();
                                                            admin.put("type","admin");
                                                            admin.put("key",group_key);

                                                            selectedFriendsRef.document(onlineUserId).collection("group_keys").document(group_key).set(admin).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (!task.isSuccessful())
                                                                    {
                                                                        Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                                        Log.e(TAG,task.getException().getMessage());
                                                                        Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }

                                                                    if (task.isSuccessful())
                                                                    {
                                                                        Runnable runnable = new Runnable()
                                                                        {
                                                                            @Override
                                                                            public void run()
                                                                            {
                                                                                for (int i=0; i < SelectedUsers.size(); i++)
                                                                                {
                                                                                    if (!SelectedUsers.get(i).equals(onlineUserId))
                                                                                    {
                                                                                        Map<String, Object> notAdmin = new HashMap<>();
                                                                                        notAdmin.put("type","not_admin");
                                                                                        notAdmin.put("key",group_key);

                                                                                        selectedFriendsRef.document(SelectedUsers.get(i)).collection("group_keys").document(group_key).set(notAdmin);
                                                                                    }
                                                                                }
                                                                            }
                                                                        };

                                                                        new Thread(runnable).start();

                                                                        Toast.makeText(CreateNewGroupActivity.this, "New group created successfully", Toast.LENGTH_SHORT).show();

                                                                        Intent mainIntent = new Intent(CreateNewGroupActivity.this,MainActivity.class);
                                                                        startActivity(mainIntent);

                                                                        grinderLoadingProgressBar.hideLoadingBar();
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener()
                                                            {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e)
                                                                {
                                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                    Log.e(TAG,e.getMessage());
                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(CreateNewGroupActivity.this,"Error Occurred, While Uploading Your Profile Image", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        Map<String, Object> newGroup = new HashMap<>();
                        newGroup.put("group_name",EnteredNewGroupName);
                        newGroup.put("time", FieldValue.serverTimestamp());
                        newGroup.put("group_profile_image","default_group_profile_image");
                        newGroup.put("group_profile_thumb_image","default_group_profile_thumb_image");
                        newGroup.put("group_main_admin",onlineUserId);

                        adminNewGroupRef.document(group_key).set(newGroup).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (!task.isSuccessful())
                                {
                                    Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                    Log.e(TAG,task.getException().getMessage());
                                    Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                if (task.isSuccessful())
                                {
                                    Runnable runnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            for (int i = 0; i < SelectedUsers.size(); i++)
                                            {
                                                final int finalI = i;

                                                selectedFriendsRef.document(SelectedUsers.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>()
                                                {
                                                    @Override
                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                                                    {
                                                        if (e != null)
                                                        {
                                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                            Log.e(TAG,e.getMessage());
                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                        if ( documentSnapshot != null && documentSnapshot.exists())
                                                        {
                                                            String name = documentSnapshot.getString("user_name");

                                                            if (SelectedUsers.get(finalI).equals(onlineUserId))
                                                            {
                                                                selectedMembersWithType.put("type","admin");

                                                                selectedMembersWithNames.put("name",name);

                                                                adminNewGroupRef.document(group_key).collection("group_members_with_types").document(onlineUserId).set(selectedMembersWithType).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (!task.isSuccessful())
                                                                        {
                                                                            Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                                            Log.e(TAG,task.getException().getMessage());
                                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            adminNewGroupRef.document(group_key).collection("group_members_with_names").document(onlineUserId).set(selectedMembersWithNames).addOnFailureListener(new OnFailureListener()
                                                                            {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e)
                                                                                {
                                                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                                    Log.e(TAG,e.getMessage());
                                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                        Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                            else
                                                            {
                                                                selectedMembersWithType.put( "type" , "not_admin");

                                                                selectedMembersWithNames.put( "name" , name);

                                                                adminNewGroupRef.document(group_key).collection("group_members_with_types").document(SelectedUsers.get(finalI)).set(selectedMembersWithType).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (!task.isSuccessful())
                                                                        {
                                                                            Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                                            Log.e(TAG,task.getException().getMessage());
                                                                            Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            adminNewGroupRef.document(group_key).collection("group_members_with_names").document(SelectedUsers.get(finalI)).set(selectedMembersWithNames).addOnFailureListener(new OnFailureListener()
                                                                            {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e)
                                                                                {
                                                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                                    Log.e(TAG,e.getMessage());
                                                                                    Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                        Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    };

                                    new Thread(runnable).start();

                                    Map<String, Object> admin = new HashMap<>();
                                    admin.put("type","admin");
                                    admin.put("key",group_key);

                                    selectedFriendsRef.document(onlineUserId).collection("group_keys").document(group_key).set(admin).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (!task.isSuccessful())
                                            {
                                                Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                Log.e(TAG,task.getException().getMessage());
                                                Toast.makeText(CreateNewGroupActivity.this, "Error while getting documents " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                            if (task.isSuccessful())
                                            {
                                                Runnable runnable = new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        for (int i=0; i < SelectedUsers.size(); i++)
                                                        {
                                                            if (!SelectedUsers.get(i).equals(onlineUserId))
                                                            {
                                                                Map<String, Object> notAdmin = new HashMap<>();
                                                                notAdmin.put("type","not_admin");
                                                                notAdmin.put("key",group_key);

                                                                selectedFriendsRef.document(SelectedUsers.get(i)).collection("group_keys").document(group_key).set(notAdmin);
                                                            }
                                                        }
                                                    }
                                                };

                                                new Thread(runnable).start();

                                                Toast.makeText(CreateNewGroupActivity.this, "New group created successfully", Toast.LENGTH_SHORT).show();

                                                Intent mainIntent = new Intent(CreateNewGroupActivity.this,MainActivity.class);
                                                startActivity(mainIntent);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                            Log.e(TAG,e.getMessage());
                                            Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    public class SelectedFriendsRecyclerView extends RecyclerView.Adapter<SelectedFriendsRecyclerView.SelectedFriendsViewHolder>
    {
        ArrayList<String> incomingUsersKeys;
        Context context;

        SelectedFriendsRecyclerView(ArrayList<String> incomingUsersKeys, Context context)
        {
            this.incomingUsersKeys = incomingUsersKeys;
            this.context = context;
        }

        @NonNull
        @Override
        public SelectedFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_friends_create_group_items,parent,false);

            return new SelectedFriendsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final SelectedFriendsViewHolder holder, int position)
        {
            final String incomingKey = incomingUsersKeys.get(position);

            selectedFriendsRef.document(incomingKey).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(CreateNewGroupActivity.this, "Error while storing documents " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String name = documentSnapshot.getString("user_name");
                        final String thumb_img = documentSnapshot.getString("user_thumb_img");

                        if (incomingKey.equals(adminUserId))
                        {
                            holder.adminTextView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            holder.adminTextView.setVisibility(View.GONE);
                        }

                        holder.incomingUserName.setText(name);
                        holder.setUser_thumb_img(CreateNewGroupActivity.this,thumb_img);
                    }
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return incomingUsersKeys.size();
        }

        class SelectedFriendsViewHolder extends RecyclerView.ViewHolder
        {
            View view;

            TextView incomingUserName;

            TextView adminTextView;

            SelectedFriendsViewHolder(View itemView)
            {
                super(itemView);

                view = itemView;

                incomingUserName = itemView.findViewById(R.id.selected_user_name_to_create_group);
                adminTextView = itemView.findViewById(R.id.admin);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = view.findViewById(R.id.selected_user_image_to_create_group);

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
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
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                resultUri = result.getUri();

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

                thumb_byte = byteArrayOutputStream.toByteArray();

                if (resultUri != null)
                {
                    Picasso.with(CreateNewGroupActivity.this).load(resultUri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                            .into(NewGroupImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(CreateNewGroupActivity.this).load(resultUri).placeholder(R.drawable.vadim).into(NewGroupImage);
                                }
                            });
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
