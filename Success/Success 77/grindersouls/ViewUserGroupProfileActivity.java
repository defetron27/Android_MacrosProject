package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ViewUserGroupProfileActivity extends AppCompatActivity
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

    private String GroupKey;

    private ArrayList<String> GroupMembers = new ArrayList<>();

    private ArrayList<String> GroupAdmins = new ArrayList<>();

    final ArrayList<String> NextAdminKeys = new ArrayList<>();

    final ArrayList<String> NextAdminNames = new ArrayList<>();

    private String AdminOrNot;

    private String GroupMainAdmin;

    private ImageView GroupProfileImage;

    private TextView CreatedDateOfGroup;

    private FloatingActionButton EditGroupNameButton;

    private Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_group_profile);

        GroupKey = getIntent().getExtras().getString("group_key");

        AdminOrNot = getIntent().getExtras().getString("admin_or_not");

        ViewGroupProfileToolbar = findViewById(R.id.view_group_profile_toolbar);
        setSupportActionBar(ViewGroupProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewGroupProfileToolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        ViewGroupProfileCollapsingToolbar = findViewById(R.id.view_group_profile_collapsing_toolbar);

        ViewGroupProfileCollapsingToolbar.setExpandedTitleColor(Color.WHITE);

        GroupProfileImage = findViewById(R.id.group_profile_image);

        CreatedDateOfGroup = findViewById(R.id.created_group_date);

        EditGroupNameButton = findViewById(R.id.edit_group_name_button);

        GroupMembersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupMembersReference.keepSynced(true);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupRef.keepSynced(true);

        storeGroupProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Images").child(GroupKey + ".jpg");

        createGroupThumbImgRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Thumb_Images").child(GroupKey + ".jpg");

        if (GroupKey != null)
        {

            GroupRef.child(GroupKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {

                    final String GroupImage = (String) dataSnapshot.child("group_profile_image").getValue();
                    GroupName = dataSnapshot.child("group_name").getValue().toString();
                    GroupMainAdmin = dataSnapshot.child("group_main_admin").getValue().toString();
                    final String CreatedGroupDate = dataSnapshot.child("created_group_date").getValue().toString();

                    ViewGroupProfileCollapsingToolbar.setTitle(GroupName);

                    if (GroupMainAdmin.equals(online_user_id))
                    {
                        CreatedDateOfGroup.setText("Created by you, "+CreatedGroupDate);
                    }
                    else
                    {
                        GroupMembersReference.child(GroupMainAdmin).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                final String adminName = dataSnapshot.child("user_name").getValue().toString();

                                CreatedDateOfGroup.setText("Created by "+adminName+", "+CreatedGroupDate);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }


                    if (GroupImage != null && !GroupImage.equals("default_group_profile_image"))
                    {
                        Picasso.with(ViewUserGroupProfileActivity.this).load(GroupImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                .into(GroupProfileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(ViewUserGroupProfileActivity.this).load(GroupImage).placeholder(R.drawable.vadim).into(GroupProfileImage);
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

        if (GroupKey != null)
        {
            GetGroupMembersWithAdmins();

            GroupRef.child(GroupKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("group_other_admins"))
                    {
                        NextAdminKeys.clear();
                        NextAdminNames.clear();

                        for (DataSnapshot snapshot : dataSnapshot.child("group_other_admins").getChildren())
                        {
                            NextAdminKeys.add(snapshot.getKey());
                            NextAdminNames.add(snapshot.getValue().toString());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

        }

        ViewGroupProfileMembersRecyclerView = findViewById(R.id.view_group_profile_members_recycler_view);
        ViewGroupProfileMembersRecyclerView.setLayoutManager(new LinearLayoutManager(ViewUserGroupProfileActivity.this, LinearLayoutManager.VERTICAL,false));
        ViewGroupProfileMembersRecyclerView
                .addItemDecoration(new HorizontalDividerItemDecoration.Builder(ViewUserGroupProfileActivity.this)
                        .color(Color.TRANSPARENT)
                        .size(10)
                        .build());
        groupProfileMembersAdapter = new GroupProfileMembersAdapter(GroupMembers,GroupAdmins,ViewUserGroupProfileActivity.this);
        ViewGroupProfileMembersRecyclerView.setAdapter(groupProfileMembersAdapter);

        EditGroupNameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent editGroupTextIntent = new Intent(ViewUserGroupProfileActivity.this,EditGroupNameActivity.class);
                editGroupTextIntent.putExtra("group_name",GroupName);
                editGroupTextIntent.putExtra("group_key",GroupKey);
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
                        .start(ViewUserGroupProfileActivity.this);
            }
        });

    }

    private void GetGroupMembersWithAdmins()
    {

        GroupRef.child(GroupKey).child("group_members").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                GroupMembers.clear();
                GroupAdmins.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    GroupMembers.add(snapshot.getKey());
                    GroupAdmins.add(snapshot.getValue().toString());
                }
                groupProfileMembersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private class GroupProfileMembersAdapter extends RecyclerView.Adapter<GroupProfileMembersAdapter.GroupProfileMembersViewHolder>
    {
        ArrayList<String> GroupMembers;
        ArrayList<String> GroupAdmins;

        Context context;

        GroupProfileMembersAdapter(ArrayList<String> GroupMembers, ArrayList<String> GroupAdmins, Context context)
        {
            this.GroupMembers = GroupMembers;
            this.GroupAdmins = GroupAdmins;
            this.context = context;
        }

        @NonNull
        @Override
        public GroupProfileMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.created_group_items, parent, false);

            return new GroupProfileMembersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final GroupProfileMembersViewHolder holder, int position)
        {
            final String member = GroupMembers.get(position);
            final String admin = GroupAdmins.get(position);

            GroupMembersReference.child(member).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String memberName = (String) dataSnapshot.child("user_name").getValue();
                    final String memberThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String memberUniqueId = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.MemberName.setText(memberName);
                    holder.setUser_thumb_img(ViewUserGroupProfileActivity.this,memberThumbImage);


                    if (member.equals(online_user_id))
                    {
                        if (AdminOrNot.equals("admin"))
                        {
                            holder.view.setEnabled(false);
                            holder.adminTextView.setVisibility(View.VISIBLE);
                            holder.MakeGroupAdmin.setVisibility(View.GONE);
                            holder.RemoveFromGroup.setVisibility(View.GONE);
                        }
                        else if (AdminOrNot.equals("not_admin"))
                        {
                            holder.view.setEnabled(false);
                            holder.adminTextView.setVisibility(View.GONE);
                            holder.MakeGroupAdmin.setVisibility(View.GONE);
                            holder.RemoveFromGroup.setVisibility(View.GONE);
                        }
                    }
                    else if (member.equals(memberUniqueId))
                    {
                        if (AdminOrNot.equals("admin"))
                        {
                            if (member.equals(memberUniqueId) && admin.equals("admin"))
                            {
                                holder.adminTextView.setVisibility(View.VISIBLE);
                                holder.MakeGroupAdmin.setVisibility(View.GONE);
                                holder.RemoveFromGroup.setVisibility(View.VISIBLE);
                            }
                            else if (member.equals(memberUniqueId) && admin.equals("not_admin"))
                            {
                                holder.adminTextView.setVisibility(View.GONE);
                                holder.MakeGroupAdmin.setVisibility(View.VISIBLE);
                                holder.RemoveFromGroup.setVisibility(View.VISIBLE);
                            }
                        }
                        else if (AdminOrNot.equals("not_admin"))
                        {
                            if (member.equals(memberUniqueId) && admin.equals("admin"))
                            {
                                holder.adminTextView.setVisibility(View.VISIBLE);
                                holder.MakeGroupAdmin.setVisibility(View.GONE);
                                holder.RemoveFromGroup.setVisibility(View.GONE);
                            }
                            else if (member.equals(memberUniqueId) && admin.equals("not_admin"))
                            {
                                holder.adminTextView.setVisibility(View.GONE);
                                holder.MakeGroupAdmin.setVisibility(View.GONE);
                                holder.RemoveFromGroup.setVisibility(View.GONE);
                            }
                        }
                    }

                    holder.view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            CharSequence options[] = new CharSequence[]
                                    {
                                            memberName + "'s Profile",
                                            "Chat with " + memberName,
                                    };

                            AlertDialog.Builder builder = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                            builder.setTitle("Choose Options");

                            builder.setItems(options, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if (which == 0)
                                    {
                                        Intent viewProfileIntent= new Intent(ViewUserGroupProfileActivity.this,ViewUserProfileActivity.class);
                                        viewProfileIntent.putExtra("visit_user_unique_id",memberUniqueId);
                                        startActivity(viewProfileIntent);
                                    }
                                    if (which == 1)
                                    {
                                        Intent chatIntent = new Intent(ViewUserGroupProfileActivity.this,ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id",memberUniqueId);
                                        chatIntent.putExtra("user_name",memberName);
                                        startActivity(chatIntent);
                                    }
                                }
                            });
                            builder.show();

                        }
                    });

                    holder.MakeGroupAdmin.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (AdminOrNot.equals("admin"))
                            {
                                AlertDialog.Builder confirm = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);

                                confirm.setTitle("Confirm");

                                confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if (memberUniqueId != null)
                                        {
                                            GroupRef.child(GroupKey).child("group_members").child(member).setValue("admin");
                                            GroupRef.child(GroupKey).child("group_other_admins").child(member).setValue(memberName)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        GroupMembersReference.child(member).child("group_keys").child(GroupKey).setValue("admin")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        Toast.makeText(ViewUserGroupProfileActivity.this, memberName +" is also one of the group admin", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                                confirm.setNegativeButton("No", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.cancel();
                                    }
                                });

                                confirm.show();
                            }
                        }
                    });

                    holder.RemoveFromGroup.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (AdminOrNot.equals("admin"))
                            {
                                PopupMenu popup = new PopupMenu(ViewUserGroupProfileActivity.this, holder.RemoveFromGroup);

                                popup.inflate(R.menu.member_remove_from_group_menu);

                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item)
                                    {
                                        if (item.getItemId() == R.id.remove_member_from_group)
                                        {
                                            if (memberUniqueId != null && GroupMainAdmin != null)
                                            {
                                                if (memberUniqueId.equals(GroupMainAdmin))
                                                {
                                                    Toast.makeText(context, "No body can remove " + memberName, Toast.LENGTH_SHORT).show();
                                                }
                                                else if (!memberUniqueId.equals(GroupMainAdmin))
                                                {
                                                    AlertDialog.Builder confirm = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);

                                                    confirm.setTitle("Do you want to remove\n" + memberName + " from group");

                                                    confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            GroupRef.child(GroupKey).child("group_members").child(member).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        GroupRef.child(GroupKey).child("group_members_with_names").child(member).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if (task.isSuccessful())
                                                                                        {
                                                                                            GroupMembersReference.child(member).child("group_keys").child(GroupKey).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                    {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                            if (task.isSuccessful())
                                                                                                            {
                                                                                                                Toast.makeText(ViewUserGroupProfileActivity.this, memberName + " removed from group", Toast.LENGTH_SHORT).show();
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
                                                    confirm.setNegativeButton("No", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            dialog.cancel();
                                                        }
                                                    });

                                                    confirm.show();
                                                }
                                            }
                                        }
                                        return true;
                                    }
                                });
                                popup.show();
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

            TextView MakeGroupAdmin;

            ImageView RemoveFromGroup;

            GroupProfileMembersViewHolder(View itemView) {
                super(itemView);

                view = itemView;

                MemberName = itemView.findViewById(R.id.created_user_name);
                adminTextView = itemView.findViewById(R.id.created_admin);

                MakeGroupAdmin = itemView.findViewById(R.id.make_group_admin);

                RemoveFromGroup = itemView.findViewById(R.id.remove_member_from_group_menu_button);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = view.findViewById(R.id.created_user_image);

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

        final MenuItem item = menu.findItem(R.id.add_group_members);

        item.setVisible(false);

        if (AdminOrNot != null && AdminOrNot.equals("admin"))
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
        if (item.getItemId() == R.id.add_group_members)
        {
            Bundle bundle = new Bundle();
            Bundle bundle1 = new Bundle();

            bundle.putStringArrayList("group_members",GroupMembers);
            bundle.putStringArrayList("group_admins",GroupAdmins);

            Intent addNewMembersIntent = new Intent(ViewUserGroupProfileActivity.this,UserFriendsListToCreateGroupActivity.class);
            addNewMembersIntent.putExtras(bundle);
            addNewMembersIntent.putExtras(bundle1);
            addNewMembersIntent.putExtra("activity_name","ViewUserGroupProfileActivity");
            addNewMembersIntent.putExtra("group_key",GroupKey);
            startActivityForResult(addNewMembersIntent,2);
        }
        if (item.getItemId() == R.id.exit_from_group)
        {
            if (AdminOrNot != null && AdminOrNot.equals("admin"))
            {
                if (GroupMainAdmin.equals(online_user_id))
                {
                    if (NextAdminKeys.size() == 0)
                    {
                        Toast.makeText(this, "Please choose other admin to lead the group..!", Toast.LENGTH_SHORT).show();
                    }
                    else if (NextAdminKeys.size() == 1)
                    {
                        final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                        deleteGroup.setTitle("Do you want to leave from " + GroupName);

                        deleteGroup.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (NextAdminKeys.size() > 0)
                                {
                                    final String nextAdmin = NextAdminKeys.get(0);

                                    RemoveAdminFromGroup(nextAdmin);
                                }

                            }
                        });

                        deleteGroup.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        deleteGroup.show();
                    }
                    else
                    {
                        final CharSequence[] FullNames = NextAdminNames.toArray(new String[NextAdminNames.size()]);

                        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                        builder.setTitle("Choose your next main admin to lead the group");
                        builder.setItems(FullNames, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                final String nextAdmin = NextAdminKeys.get(which);

                                RemoveAdminFromGroup(nextAdmin);
                            }
                        });
                        builder.show();
                    }
                }
                else
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                    builder.setTitle("Do you want to leave from the group?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            GroupRef.child(GroupKey).child("group_members").child(online_user_id).removeValue();
                            GroupRef.child(GroupKey).child("group_members_with_names").child(online_user_id).removeValue();
                            GroupRef.child(GroupKey).child("group_other_admins").child(online_user_id).removeValue();
                            GroupMembersReference.child(online_user_id).child("group_keys").child(GroupKey).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(ViewUserGroupProfileActivity.this, "You leave from the group", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ViewUserGroupProfileActivity.this,MainActivity.class));
                                            }
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }

            }
            else
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                builder.setTitle("Do you want to leave from the group?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        GroupRef.child(GroupKey).child("group_members").child(online_user_id).removeValue();
                        GroupRef.child(GroupKey).child("group_members_with_names").child(online_user_id).removeValue();
                        GroupMembersReference.child(online_user_id).child("group_keys").child(GroupKey).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(ViewUserGroupProfileActivity.this, "You leave from the group", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(ViewUserGroupProfileActivity.this,MainActivity.class));
                                        }
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                builder.show();

            }

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
                Toast.makeText(this, "New group members added successfully", Toast.LENGTH_SHORT).show();
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

                StorageReference filePath = storeGroupProfileImagestorageRef;

                final StorageReference thumb_filePath = createGroupThumbImgRef;

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ViewUserGroupProfileActivity.this, "Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().toString();

                                    if (thumb_task.isSuccessful())
                                    {
                                        Map<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("group_profile_image", downloadUrl);
                                        update_user_data.put("group_profile_thumb_image", thumb_downloadUrl);

                                        GroupRef.child(GroupKey).updateChildren(update_user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ViewUserGroupProfileActivity.this, "Image Updated Successfully..", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ViewUserGroupProfileActivity.this, "Error Occurred, While Uploading Your Profile Image", Toast.LENGTH_LONG).show();
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

    private void RemoveAdminFromGroup(final String nextAdmin)
    {
        GroupRef.child(GroupKey).child("group_members").child(online_user_id).removeValue();
        GroupRef.child(GroupKey).child("group_members_with_names").child(online_user_id).removeValue();
        GroupMembersReference.child(online_user_id).child("group_keys").child(GroupKey).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            GroupRef.child(GroupKey).child("group_main_admin").setValue(nextAdmin);
                            GroupRef.child(GroupKey).child("group_other_admins").child(nextAdmin).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(ViewUserGroupProfileActivity.this, MainActivity.class));
                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}
