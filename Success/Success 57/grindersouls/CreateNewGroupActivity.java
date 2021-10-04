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
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

    private String AdminUserId,OnlineUserId;

    private TextView SelectedMembers;

    private EditText CreateNewGroupNameEditText;

    private CircleImageView NewGroupImage;

    private RecyclerView SelectedFriendsNamesAndImagesRecyclerView;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference SelectedFriendsRef,AdminNewGroupRef;

    private SelectedFriendsRecyclerView selectedFriendsRecyclerView;

    private Bitmap thumb_bitmap = null;

    private StorageReference createGroupThumbImgRef,storeGroupProfileImagestorageRef;

    private String downloadUrl,thumb_downloadUrl;

    private String EnteredNewGroupName;

    private FloatingActionButton CreateNewGroupButton;

    private Uri resultUri;

    byte[] thumb_byte;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_new_group);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        SelectedUsers = getIntent().getStringArrayListExtra("selected_friends");

        AdminUserId = getIntent().getExtras().getString("admin_user_id");

        AdminTotalFriends = getIntent().getStringArrayListExtra("friends_count");

        firebaseAuth = FirebaseAuth.getInstance();

        OnlineUserId = firebaseAuth.getCurrentUser().getUid();

        AdminAndTotalFriends.add(OnlineUserId);

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

        SelectedFriendsRef = FirebaseDatabase.getInstance().getReference().child("Users");
        SelectedFriendsRef.keepSynced(true);

        AdminNewGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        AdminNewGroupRef.keepSynced(true);

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
                Calendar calFordATE = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                EnteredNewGroupName = CreateNewGroupNameEditText.getText().toString();

                SelectedUsers.add(OnlineUserId);

                final Map<String,Object> selectedMembers = new HashMap<>();
                final Map<String,Object> selectedMembersWithNames = new HashMap<>();

                for (int i = 0; i < SelectedUsers.size(); i++)
                {
                    final int finalI = i;

                    SelectedFriendsRef.child(SelectedUsers.get(i)).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            final String name = dataSnapshot.child("user_name").getValue().toString();

                            if (SelectedUsers.get(finalI).equals(OnlineUserId))
                            {
                                selectedMembers.put(OnlineUserId , "admin");

                                selectedMembersWithNames.put(OnlineUserId,name);

                            }
                            else
                            {
                                selectedMembers.put( SelectedUsers.get(finalI), "not_admin");

                                selectedMembersWithNames.put(SelectedUsers.get(finalI),name);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }

                DatabaseReference group_unique_key = AdminNewGroupRef.push();

                final String group_key = group_unique_key.getKey();

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

                                            AdminNewGroupRef.child(group_key).child("group_profile_image").setValue(downloadUrl);

                                            AdminNewGroupRef.child(group_key).child("group_profile_thumb_image").setValue(thumb_downloadUrl)
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
                    AdminNewGroupRef.child(group_key).child("group_profile_image").setValue("default_group_profile_image");

                    AdminNewGroupRef.child(group_key).child("group_profile_thumb_image").setValue("default_group_profile_thumb_image")
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(CreateNewGroupActivity.this,"Default", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                AdminNewGroupRef.child(group_key).child("group_name").setValue(EnteredNewGroupName);
                AdminNewGroupRef.child(group_key).child("created_group_date").setValue(saveCurrentDate);
                AdminNewGroupRef.child(group_key).child("group_main_admin").setValue(OnlineUserId)
                        .addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    AdminNewGroupRef.child(group_key).child("group_members").updateChildren(selectedMembers);
                                    AdminNewGroupRef.child(group_key).child("group_members_with_names").updateChildren(selectedMembersWithNames)
                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {


                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(CreateNewGroupActivity.this, "Your new group is Created Successfully.", Toast.LENGTH_SHORT).show();

                                                SelectedFriendsRef.child(OnlineUserId).child("group_keys").child(group_key).setValue("admin").addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {

                                                            Runnable runnable = new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    for (int i=0; i<selectedMembers.size(); i++)
                                                                    {
                                                                        if (!SelectedUsers.get(i).equals(OnlineUserId))
                                                                        {
                                                                            SelectedFriendsRef.child(SelectedUsers.get(i)).child("group_keys").child(group_key).setValue("not_admin");
                                                                        }
                                                                    }
                                                                }
                                                            };

                                                            new Thread(runnable).start();


                                                            Intent mainIntent = new Intent(CreateNewGroupActivity.this,MainActivity.class);
                                                            startActivity(mainIntent);
                                                        }
                                                    }
                                                });


                                            }


                                        }
                                    });



                                }
                            }
                        });

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
