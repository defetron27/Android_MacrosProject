package com.deffe.macros.soulsspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreateNewGroupActivity extends AppCompatActivity
{
    private Toolbar CreateNewGroupToolbar;

    private ArrayList<String> SelectedUsers = new ArrayList<>();

    private ArrayList<String> AdminTotalFriends = new ArrayList<>();

    private ArrayList<String> AdminAndTotalFriends = new ArrayList<>();

    private String AdminUserId,UniqueAdminUserId;

    private TextView SelectedMembers;

    private EditText CreateNewGroupNameEditText;

    private CircleImageView NewGroupImage;

    private RecyclerView SelectedFriendsNamesAndImagesRecyclerView;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference SelectedFriendsRef,AdminNewGroupRef,CreateAdminUsersRef,GroupsRef;

    private SelectedFriendsRecyclerView selectedFriendsRecyclerView;

    private Bitmap thumb_bitmap = null;

    private StorageReference createGroupThumbImgRef,storeGroupProfileImagestorageRef;

    private String downloadUrl,thumb_downloadUrl;

    private String EnteredNewGroupName;

    private ArrayList<String> CountedGroups = new ArrayList<>();

    private FloatingActionButton CreateNewGroupButton;

    private ArrayList<String> countGroups = new ArrayList<>();

    private String groupsCount;

    private  String counted;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
        }


        setContentView(R.layout.activity_create_new_group);

        SelectedUsers = getIntent().getStringArrayListExtra("selected_friends");

        AdminUserId = getIntent().getExtras().getString("admin_user_id");

        AdminTotalFriends = getIntent().getStringArrayListExtra("friends_count");

        CountedGroups = getIntent().getStringArrayListExtra("groups_count");

        firebaseAuth = FirebaseAuth.getInstance();

        UniqueAdminUserId = firebaseAuth.getCurrentUser().getUid();

        AdminAndTotalFriends.add(UniqueAdminUserId);

        AdminAndTotalFriends.addAll(SelectedUsers);

        CreateNewGroupToolbar = (Toolbar) findViewById(R.id.create_new_group_tool_bar);
        setSupportActionBar(CreateNewGroupToolbar);
        getSupportActionBar().setSubtitle("     You and "+ SelectedUsers.size() + " Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CreateNewGroupToolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        SelectedMembers = (TextView) findViewById(R.id.selected_members);
        SelectedMembers.setText("Selected Members: " + SelectedUsers.size() + " of " + AdminTotalFriends.size());

        SelectedFriendsRef = FirebaseDatabase.getInstance().getReference().child("Users");

        CreateAdminUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        AdminNewGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(UniqueAdminUserId);

        storeGroupProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Images").child(UniqueAdminUserId);

        createGroupThumbImgRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Thumb_Images").child(UniqueAdminUserId);



        NewGroupImage = (CircleImageView) findViewById(R.id.create_new_group_image);

        counted = String.valueOf(CountedGroups.size());

        if (counted.equals("1"))
        {
            AdminNewGroupRef.child("1").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String groupProfileThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();

                    if (groupProfileThumbImage != null && !groupProfileThumbImage.equals("default_group_profile_thumb_image"))
                    {


                        Picasso.with(CreateNewGroupActivity.this).load(groupProfileThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                .into(NewGroupImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(CreateNewGroupActivity.this).load(groupProfileThumbImage).placeholder(R.drawable.vadim).into(NewGroupImage);
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
            AdminNewGroupRef.child(counted).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String groupProfileThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();

                    if (groupProfileThumbImage != null && !groupProfileThumbImage.equals("default_group_profile_thumb_image"))
                    {


                        Picasso.with(CreateNewGroupActivity.this).load(groupProfileThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                .into(NewGroupImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(CreateNewGroupActivity.this).load(groupProfileThumbImage).placeholder(R.drawable.vadim).into(NewGroupImage);
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



        CreateNewGroupNameEditText = (EditText) findViewById(R.id.create_new_group_name_edit_text);

        CreateNewGroupButton = (FloatingActionButton) findViewById(R.id.create_new_group_button);

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

        SelectedFriendsNamesAndImagesRecyclerView = (RecyclerView) findViewById(R.id.selected_members_recycler_view);
        SelectedFriendsNamesAndImagesRecyclerView.setLayoutManager(new LinearLayoutManager(CreateNewGroupActivity.this, LinearLayoutManager.HORIZONTAL,false));
        selectedFriendsRecyclerView = new SelectedFriendsRecyclerView(AdminAndTotalFriends,CreateNewGroupActivity.this);
        SelectedFriendsNamesAndImagesRecyclerView.setAdapter(selectedFriendsRecyclerView);


        AdminNewGroupRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                countGroups.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if (snapshot.getKey().equals(UniqueAdminUserId))
                    {
                        countGroups.add(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        CreateNewGroupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                EnteredNewGroupName = CreateNewGroupNameEditText.getText().toString();

                final Map<String,Object> selectedMembers = new HashMap<>();

                for (int i = 0; i < SelectedUsers.size(); i++)
                {
                    selectedMembers.put(String.valueOf(i), SelectedUsers.get(i));
                }

                if (counted.equals("1"))
                {
                    AdminNewGroupRef.child("1").child("new_group_name").setValue(EnteredNewGroupName);
                    AdminNewGroupRef.child("1").child("admin_key").setValue(UniqueAdminUserId)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        CreateAdminUsersRef.child(UniqueAdminUserId).child("user_have_groups").setValue("1");

                                        AdminNewGroupRef.child("1").child("admin_have_groups").setValue("1");

                                        AdminNewGroupRef.child("1").child("no_of_new_group_members").updateChildren(selectedMembers).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                Toast.makeText(CreateNewGroupActivity.this, "Your new group is Created Successfully.", Toast.LENGTH_SHORT).show();

                                                Intent mainIntent = new Intent(CreateNewGroupActivity.this,MainActivity.class);
                                                startActivity(mainIntent);
                                            }
                                        });

                                    }
                                }
                            });


                }
                else
                {
                    AdminNewGroupRef.child(counted).child("new_group_name").setValue(EnteredNewGroupName);
                    AdminNewGroupRef.child(counted).child("admin_key").setValue(UniqueAdminUserId)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        CreateAdminUsersRef.child(UniqueAdminUserId).child("user_have_groups").setValue(counted);

                                        AdminNewGroupRef.child(counted).child("admin_have_groups").setValue(counted);

                                        AdminNewGroupRef.child(counted).child("no_of_new_group_members").updateChildren(selectedMembers).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                Toast.makeText(CreateNewGroupActivity.this, "Your new group is Created Successfully.", Toast.LENGTH_SHORT).show();

                                                Intent mainIntent = new Intent(CreateNewGroupActivity.this,MainActivity.class);
                                                startActivity(mainIntent);
                                            }
                                        });

                                    }
                                }
                            });
                }
            }
        });
    }

    public class SelectedFriendsRecyclerView extends RecyclerView.Adapter<SelectedFriendsRecyclerView.SelectedFriendsViewHolder>
    {
        ArrayList<String> incomingUsersKeys = new ArrayList<>();
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

            SelectedFriendsRef.child(incomingKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String name = (String) dataSnapshot.child("user_name").getValue();
                    final String thumb_img = (String) dataSnapshot.child("user_thumb_img").getValue();

                    if (incomingKey.equals(AdminUserId))
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

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

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

                incomingUserName = (TextView) itemView.findViewById(R.id.selected_user_name_to_create_group);
                adminTextView = (TextView) itemView.findViewById(R.id.admin);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = (CircleImageView) view.findViewById(R.id.selected_user_image_to_create_group);

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


                if (counted.equals("1"))
                {
                    StorageReference filePath = storeGroupProfileImagestorageRef.child("1" + ".jpg");


                    final StorageReference thumb_filePath = createGroupThumbImgRef.child("1" + ".jpg");


                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CreateNewGroupActivity.this,"Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                                downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                    {
                                        thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful())
                                        {

                                            AdminNewGroupRef.child("1").child("group_profile_image").setValue(downloadUrl);

                                            AdminNewGroupRef.child("1").child("group_profile_thumb_image").setValue(thumb_downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                Toast.makeText(CreateNewGroupActivity.this,"Image Updated Successfully..", Toast.LENGTH_SHORT).show();
                                                            }

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

                    StorageReference filePath = storeGroupProfileImagestorageRef.child(counted + ".jpg");


                    final StorageReference thumb_filePath = createGroupThumbImgRef.child(counted + ".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CreateNewGroupActivity.this,"Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                                downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                    {
                                        thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful())
                                        {

                                            AdminNewGroupRef.child(counted).child("group_profile_image").setValue(downloadUrl);

                                            AdminNewGroupRef.child(counted).child("group_profile_thumb_image").setValue(thumb_downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                Toast.makeText(CreateNewGroupActivity.this,"Image Updated Successfully..", Toast.LENGTH_SHORT).show();
                                                            }

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
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        if (counted.equals("1"))
        {

            AdminNewGroupRef.child("1").child("group_profile_image").removeValue();

            AdminNewGroupRef.child("1").child("group_profile_thumb_image").removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(CreateNewGroupActivity.this,"Image Deleted Successfully..", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    Toast.makeText(CreateNewGroupActivity.this, "Storage Image Deleted", Toast.LENGTH_SHORT).show();
                }
            });

            StorageReference storageReference1 = FirebaseStorage.getInstance().getReferenceFromUrl(thumb_downloadUrl);

            storageReference1.delete().addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    Toast.makeText(CreateNewGroupActivity.this, "Storage Thumb Deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            AdminNewGroupRef.child(counted).child("group_profile_image").removeValue();

            AdminNewGroupRef.child(counted).child("group_profile_thumb_image").removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(CreateNewGroupActivity.this,"Thumb Image Deleted Successfully..", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    Toast.makeText(CreateNewGroupActivity.this, "Storage Image Deleted", Toast.LENGTH_SHORT).show();
                }
            });

            StorageReference storageReference1 = FirebaseStorage.getInstance().getReferenceFromUrl(thumb_downloadUrl);

            storageReference1.delete().addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    Toast.makeText(CreateNewGroupActivity.this, "Storage Thumb Deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        onBackPressed();
    }
}
