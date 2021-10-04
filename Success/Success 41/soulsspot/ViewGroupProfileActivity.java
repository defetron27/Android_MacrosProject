package com.deffe.macros.soulsspot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ViewGroupProfileActivity extends AppCompatActivity
{
    private Toolbar ViewGroupProfileToolbar;

    private CollapsingToolbarLayout ViewGroupProfileCollapsingToolbar;

    private RecyclerView ViewGroupProfileMembersRecyclerView;

    private GroupProfileMembersAdapter groupProfileMembersAdapter;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference GroupMembersReference,GroupRef;

    private StorageReference createGroupThumbImgRef,storeGroupProfileImagestorageRef;

    private String online_user_id;

    private String GroupName;

    private String AdminKey;

    private String GroupPosition;

    private ArrayList<String> GroupMembers = new ArrayList<>();

    private ImageView GroupProfileImage;

    private String CreatedGroupDate;

    private TextView CreatedDateOfGroup;

    private String AdminName;

    private FloatingActionButton EditGroupNameButton;

    Bitmap thumb_bitmap = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_profile);

        AdminKey = getIntent().getExtras().getString("admin");

        GroupPosition = getIntent().getExtras().getString("group_position");

        GroupName = getIntent().getExtras().getString("group_name");

        AdminName = getIntent().getExtras().getString("admin_name");

        CreatedGroupDate = getIntent().getExtras().getString("date");

        ViewGroupProfileToolbar = findViewById(R.id.view_group_profile_toolbar);
        setSupportActionBar(ViewGroupProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        ViewGroupProfileCollapsingToolbar = findViewById(R.id.view_group_profile_collapsing_toolbar);
        ViewGroupProfileCollapsingToolbar.setTitle(GroupName);
        ViewGroupProfileCollapsingToolbar.setExpandedTitleColor(Color.WHITE);

        GroupProfileImage = findViewById(R.id.group_profile_image);

        CreatedDateOfGroup = findViewById(R.id.created_group_date);

        EditGroupNameButton = findViewById(R.id.edit_group_name_button);

        GroupMembersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");


        storeGroupProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Images").child(AdminKey);

        createGroupThumbImgRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Thumb_Images").child(AdminKey);

        ViewGroupProfileMembersRecyclerView = findViewById(R.id.view_group_profile_members_recycler_view);
        ViewGroupProfileMembersRecyclerView.setLayoutManager(new GridLayoutManager(ViewGroupProfileActivity.this,4, GridLayoutManager.VERTICAL,false));
        ViewGroupProfileMembersRecyclerView
                .addItemDecoration(new VerticalDividerItemDecoration.Builder(ViewGroupProfileActivity.this)
                        .color(Color.TRANSPARENT)
                        .size(10)
                        .build());
        groupProfileMembersAdapter = new GroupProfileMembersAdapter(GroupMembers,ViewGroupProfileActivity.this);
        ViewGroupProfileMembersRecyclerView.setAdapter(groupProfileMembersAdapter);

        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                GroupMembers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {

                    if (snapshot.getValue().toString().equals("admin"))
                    {
                        GroupMembers.add(AdminKey);
                    }
                    else
                    {
                        GroupMembers.add(snapshot.getValue().toString());
                    }
                }
                groupProfileMembersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        GroupRef.child(AdminKey).child(GroupPosition).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                final String GroupThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();

                if (GroupThumbImage != null && !GroupThumbImage.equals("default_group_profile_thumb_image"))
                {
                    Picasso.with(ViewGroupProfileActivity.this).load(GroupThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                            .into(GroupProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ViewGroupProfileActivity.this).load(GroupThumbImage).placeholder(R.drawable.vadim).into(GroupProfileImage);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });



        if (AdminKey.equals(online_user_id))
        {
            CreatedDateOfGroup.setText("Created by you, "+CreatedGroupDate);
        }
        else
        {
            CreatedDateOfGroup.setText("Created by "+AdminName+", "+CreatedGroupDate);
        }

        EditGroupNameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent editGroupTextIntent = new Intent(ViewGroupProfileActivity.this,EditGroupNameActivity.class);
                editGroupTextIntent.putExtra("group_name",GroupName);
                editGroupTextIntent.putExtra("admin_key",AdminKey);
                editGroupTextIntent.putExtra("admin_group_key",GroupPosition);
                startActivityForResult(editGroupTextIntent,1);
            }
        });

        GroupProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1 ,1)
                        .start(ViewGroupProfileActivity.this);
            }
        });

    }
    private class GroupProfileMembersAdapter extends RecyclerView.Adapter<GroupProfileMembersAdapter.GroupProfileMembersViewHolder>
    {
        ArrayList<String> GroupMembers = new ArrayList<>();

        Context c;

        GroupProfileMembersAdapter(ArrayList<String> GroupMembers, Context c)
        {
            this.GroupMembers = GroupMembers;
            this.c = c;
        }

        @NonNull
        @Override
        public GroupProfileMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_friends_create_group_items, parent, false);

            return new GroupProfileMembersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final GroupProfileMembersViewHolder holder, int position)
        {
            final String members = GroupMembers.get(position);

            GroupMembersReference.child(members).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String memberName = (String) dataSnapshot.child("user_name").getValue();
                    final String memberThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String memberUniqueId = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.MemberName.setText(memberName);
                    holder.setUser_thumb_img(ViewGroupProfileActivity.this,memberThumbImage);

                    if (members.equals(AdminKey))
                    {
                        holder.adminTextView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        holder.adminTextView.setVisibility(View.INVISIBLE);
                    }
                    if (members.equals(online_user_id))
                    {
                        holder.view.setEnabled(false);
                    }

                    holder.view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (AdminKey.equals(online_user_id))
                            {

                            }
                            else
                            {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                memberName + "'s Profile",
                                                "Chat with " + memberName,
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewGroupProfileActivity.this);
                                builder.setTitle("Choose Options");

                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if (which == 0)
                                        {
                                            Intent viewProfileIntent= new Intent(ViewGroupProfileActivity.this,ViewProfileActivity.class);
                                            viewProfileIntent.putExtra("visit_user_unique_id",memberUniqueId);
                                            startActivity(viewProfileIntent);
                                        }
                                        if (which == 1)
                                        {
                                            Intent chatIntent = new Intent(ViewGroupProfileActivity.this,ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",memberUniqueId);
                                            chatIntent.putExtra("user_name",memberName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();
                            }

                        }
                    });
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
            return GroupMembers.size();
        }

        class GroupProfileMembersViewHolder extends RecyclerView.ViewHolder {

            View view;

            TextView MemberName;

            TextView adminTextView;

            GroupProfileMembersViewHolder(View itemView) {
                super(itemView);

                view = itemView;

                MemberName = itemView.findViewById(R.id.selected_user_name_to_create_group);
                adminTextView = itemView.findViewById(R.id.admin);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = view.findViewById(R.id.selected_user_image_to_create_group);

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(c).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.view_group_profile_menu, menu);

        MenuItem item = menu.findItem(R.id.add_group_members);

        if (AdminKey.equals(online_user_id))
        {
            item.setVisible(true);
        }
        else
        {
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.add_group_members:


                for (int i = GroupMembers.size()-1; i>=0; i--)
                {
                    if (GroupMembers.get(i).equals(online_user_id))
                    {
                        GroupMembers.remove(i);
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("group_members",GroupMembers);

                Intent addNewMembersIntent = new Intent(ViewGroupProfileActivity.this,FriendsListActivity.class);
                addNewMembersIntent.putExtras(bundle);
                addNewMembersIntent.putExtra("activity_name","ViewGroupProfileActivity");
                addNewMembersIntent.putExtra("admin_key",AdminKey);
                addNewMembersIntent.putExtra("admin_group_key",GroupPosition);
                startActivityForResult(addNewMembersIntent,2);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  1)
        {
            if (resultCode == RESULT_OK)
            {
                GroupName = data.getStringExtra("group_name");
                ViewGroupProfileCollapsingToolbar.setTitle(GroupName);
            }
        }
        if (requestCode == 2)
        {
            if (resultCode == RESULT_OK)
            {
                GroupMembers = data.getStringArrayListExtra("group_members");

                final Map<String,Object> NewAddedMembers = new HashMap<>();

                for (int i = 0; i < GroupMembers.size(); i++)
                {
                    final int finalI = i;

                    GroupMembersReference.child(GroupMembers.get(i)).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            final String name = dataSnapshot.child("user_name").getValue().toString();

                            if (GroupMembers.get(finalI).equals(online_user_id))
                            {
                                NewAddedMembers.put(name, "admin");
                            }
                            else
                            {
                                NewAddedMembers.put(name, GroupMembers.get(finalI));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }

                GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").updateChildren(NewAddedMembers).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {

                                GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        groupProfileMembersAdapter.GroupMembers.clear();

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                        {

                                            if (snapshot.getValue().toString().equals("admin"))
                                            {
                                                groupProfileMembersAdapter.GroupMembers.add(AdminKey);
                                            }
                                            else
                                            {
                                                groupProfileMembersAdapter.GroupMembers.add(snapshot.getValue().toString());
                                            }
                                        }
                                        groupProfileMembersAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }
                });

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                final Uri resultUri = result.getUri();

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
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                StorageReference filePath = storeGroupProfileImagestorageRef.child(GroupPosition + ".jpg");

                final StorageReference thumb_filePath = createGroupThumbImgRef.child(GroupPosition + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ViewGroupProfileActivity.this, "Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful())
                                    {
                                        Map<String, Object> update_user_data = new HashMap<String, Object>();
                                        update_user_data.put("group_profile_image", downloadUrl);
                                        update_user_data.put("group_profile_thumb_image", thumb_downloadUrl);

                                        GroupRef.child(AdminKey).child(GroupPosition).updateChildren(update_user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ViewGroupProfileActivity.this, "Image Updated Successfully..", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                }
                            });

                            Picasso.with(ViewGroupProfileActivity.this).load(resultUri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                    .into(GroupProfileImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(ViewGroupProfileActivity.this).load(resultUri).placeholder(R.drawable.vadim).into(GroupProfileImage);
                                        }
                                    });

                        }
                        else
                        {
                            Toast.makeText(ViewGroupProfileActivity.this, "Error Occurred, While Uploading Your Profile Image", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }
}