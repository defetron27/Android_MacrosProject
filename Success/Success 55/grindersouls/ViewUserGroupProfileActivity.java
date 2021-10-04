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

    private String AdminKey;

    private String GroupPosition;

    private ArrayList<String> GroupMembers = new ArrayList<>();

    private ImageView GroupProfileImage;

    private String CreatedGroupDate;

    private TextView CreatedDateOfGroup;

    private FloatingActionButton EditGroupNameButton;

    private Bitmap thumb_bitmap = null;

    private ArrayList<String> OtherAdmins = new ArrayList<>();

    private ArrayList<String> OtherAdminsWithOnlineUser = new ArrayList<>();

    private ArrayList<String> Names = new ArrayList<>();

    final ArrayList<String> Admin1 = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_group_profile);


        AdminKey = getIntent().getExtras().getString("admin");

        GroupPosition = getIntent().getExtras().getString("group_position");

        GroupName = getIntent().getExtras().getString("group_name");

        CreatedGroupDate = getIntent().getExtras().getString("date");

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
        ViewGroupProfileCollapsingToolbar.setTitle(GroupName);
        ViewGroupProfileCollapsingToolbar.setExpandedTitleColor(Color.WHITE);

        GroupProfileImage = findViewById(R.id.group_profile_image);

        CreatedDateOfGroup = findViewById(R.id.created_group_date);

        EditGroupNameButton = findViewById(R.id.edit_group_name_button);

        GroupMembersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupMembersReference.keepSynced(true);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupRef.keepSynced(true);

        storeGroupProfileImagestorageRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Images").child(AdminKey);

        createGroupThumbImgRef = FirebaseStorage.getInstance().getReference().child("Group_Profile_Thumb_Images").child(AdminKey);

        ViewGroupProfileMembersRecyclerView = findViewById(R.id.view_group_profile_members_recycler_view);
        ViewGroupProfileMembersRecyclerView.setLayoutManager(new LinearLayoutManager(ViewUserGroupProfileActivity.this, LinearLayoutManager.VERTICAL,false));
        ViewGroupProfileMembersRecyclerView
                .addItemDecoration(new HorizontalDividerItemDecoration.Builder(ViewUserGroupProfileActivity.this)
                        .color(Color.TRANSPARENT)
                        .size(10)
                        .build());
        groupProfileMembersAdapter = new GroupProfileMembersAdapter(GroupMembers,ViewUserGroupProfileActivity.this);
        ViewGroupProfileMembersRecyclerView.setAdapter(groupProfileMembersAdapter);

        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                GroupMembers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    GroupMembers.add(snapshot.getValue().toString());
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

                final String GroupThumbImage = (String) dataSnapshot.child("group_profile_image").getValue();

                if (GroupThumbImage != null && !GroupThumbImage.equals("default_group_profile_image"))
                {
                    Picasso.with(ViewUserGroupProfileActivity.this).load(GroupThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                            .into(GroupProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ViewUserGroupProfileActivity.this).load(GroupThumbImage).placeholder(R.drawable.vadim).into(GroupProfileImage);
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
            GroupMembersReference.child(AdminKey).addValueEventListener(new ValueEventListener()
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


        GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                OtherAdmins.clear();
                Names.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    final String otherKey = dataSnapshot1.getValue().toString();

                    if (!otherKey.equals(online_user_id))
                    {
                        OtherAdmins.add(otherKey);
                        Names.add(dataSnapshot1.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                OtherAdminsWithOnlineUser.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    final String otherKey = dataSnapshot1.getValue().toString();

                    OtherAdminsWithOnlineUser.add(otherKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Admin1.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            if (!snapshot.getValue().toString().equals(AdminKey))
                            {
                                Admin1.add(snapshot.getValue().toString());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });



        EditGroupNameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent editGroupTextIntent = new Intent(ViewUserGroupProfileActivity.this,EditGroupNameActivity.class);
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
                        .start(ViewUserGroupProfileActivity.this);
            }
        });

    }

    private class GroupProfileMembersAdapter extends RecyclerView.Adapter<GroupProfileMembersAdapter.GroupProfileMembersViewHolder>
    {
        ArrayList<String> GroupMembers = new ArrayList<>();

        Context c;

        GroupProfileMembersAdapter(ArrayList<String> GroupMembers , Context c)
        {
            this.GroupMembers = GroupMembers;
            this.c = c;
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
                    holder.setUser_thumb_img(ViewUserGroupProfileActivity.this,memberThumbImage);

                    if (members.equals(online_user_id))
                    {
                        holder.view.setEnabled(false);
                    }
                    if (OtherAdminsWithOnlineUser.size() > 0)
                    {
                        if (!members.equals(online_user_id))
                        {
                            for (int i = 0; i < OtherAdminsWithOnlineUser.size(); i++)
                            {
                                if (OtherAdminsWithOnlineUser.get(i).equals(online_user_id))
                                {
                                    holder.RemoveFromGroup.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                    if (!AdminKey.equals(online_user_id))
                    {
                        if (OtherAdmins.size() != 0)
                        {
                            for (int i=0; i<OtherAdmins.size(); i++)
                            {
                                if (!OtherAdmins.get(i).equals(online_user_id))
                                {
                                    holder.MakeGroupAdmin.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                    GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                            {
                                final String otherKey = dataSnapshot1.getValue().toString();

                                if (otherKey.equals(members))
                                {
                                    holder.adminTextView.setVisibility(View.VISIBLE);
                                    holder.MakeGroupAdmin.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
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
                    holder.RemoveFromGroup.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
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
                                        if (memberUniqueId != null)
                                        {
                                            if (memberUniqueId.equals(AdminKey))
                                            {
                                                Toast.makeText(c, "No body can remove", Toast.LENGTH_SHORT).show();
                                            }
                                            else if (!memberUniqueId.equals(AdminKey))
                                            {
                                                GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members")
                                                        .addValueEventListener(new ValueEventListener()
                                                        {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot)
                                                            {
                                                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                                {
                                                                    if (snapshot.getValue().toString().equals(memberUniqueId))
                                                                    {
                                                                        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").child(snapshot.getKey()).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        Toast.makeText(ViewUserGroupProfileActivity.this, "You exist from the group bye bye...", Toast.LENGTH_SHORT).show();
                                                                                        Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                                                                                        startActivity(mainIntent);
                                                                                        finish();
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError)
                                                            {

                                                            }
                                                        });
                                                GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                                                {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot)
                                                    {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                        {
                                                            if (snapshot.getValue().toString().equals(memberUniqueId))
                                                            {

                                                                GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").child(snapshot.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        Toast.makeText(ViewUserGroupProfileActivity.this, "You exist from the group bye bye...", Toast.LENGTH_SHORT).show();
                                                                        Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                                                                        startActivity(mainIntent);
                                                                        finish();                                                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError)
                                                    {

                                                    }
                                                });
                                            }
                                        }

                                    }

                                    return true;
                                }
                            });
                            popup.show();
                        }
                    });
                    holder.MakeGroupAdmin.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            AlertDialog.Builder confirm = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);

                            confirm.setTitle("Confirm");

                            confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if (memberName != null)
                                    {
                                        GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").child(memberName).setValue(memberUniqueId);

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

        if (AdminKey.equals(online_user_id))
        {
            item.setVisible(true);
        }
        else
        {
            GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        final String otherKey = dataSnapshot1.getValue().toString();

                        if (otherKey.equals(online_user_id))
                        {
                            item.setVisible(true);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.add_group_members)
        {
            for (int i = GroupMembers.size()-1; i>=0; i--)
            {
                if (GroupMembers.get(i).equals(online_user_id))
                {
                    GroupMembers.remove(i);
                }
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList("group_members",GroupMembers);

            Intent addNewMembersIntent = new Intent(ViewUserGroupProfileActivity.this,ViewUserGroupProfileActivity.class);
            addNewMembersIntent.putExtras(bundle);
            addNewMembersIntent.putExtra("activity_name","ViewUserGroupProfileActivity");
            addNewMembersIntent.putExtra("admin_key",AdminKey);
            addNewMembersIntent.putExtra("admin_group_key",GroupPosition);
            addNewMembersIntent.putExtra("group_name",GroupName);
            addNewMembersIntent.putExtra("date",CreatedGroupDate);
            startActivity(addNewMembersIntent);
        }
        if (item.getItemId() == R.id.exit_from_group)
        {
            if (!AdminKey.equals(online_user_id))
            {
                final ArrayList<String> Admin = new ArrayList<>();

                GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                            final String otherKey = dataSnapshot1.getValue().toString();

                            Admin.add(otherKey);

                            if (otherKey.equals(online_user_id))
                            {
                                final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                                deleteGroup.setTitle("Do you leave");

                                deleteGroup.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot)
                                            {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                {
                                                    if (snapshot.getValue().toString().equals(online_user_id))
                                                    {
                                                        GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").child(snapshot.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                                                                {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot)
                                                                    {
                                                                        for (DataSnapshot snapshot1 : dataSnapshot.getChildren())
                                                                        {
                                                                            if (snapshot1.getValue().toString().equals(online_user_id))
                                                                            {
                                                                                GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").child(snapshot1.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        Toast.makeText(ViewUserGroupProfileActivity.this, "You exist from the group bye bye...", Toast.LENGTH_SHORT).show();
                                                                                        Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                                                                                        startActivity(mainIntent);
                                                                                        finish();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError)
                                                                    {

                                                                    }
                                                                });

                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError)
                                            {

                                            }
                                        });
                                    }
                                });
                                deleteGroup.show();
                            }
                        }

                        if (Admin.size() > 0)
                        {
                            for (int i=0; i<Admin.size(); i++)
                            {
                                if (!Admin.get(i).equals(online_user_id))
                                {

                                    final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                                    deleteGroup.setTitle("Do you leave from the group?");

                                    deleteGroup.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                                            {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot)
                                                {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                    {
                                                        if (snapshot.getValue().toString().equals(online_user_id))
                                                        {
                                                            GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").child(snapshot.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    Toast.makeText(ViewUserGroupProfileActivity.this, "You exist from the group bye bye...", Toast.LENGTH_SHORT).show();
                                                                    Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();
                                                                }
                                                            });
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError)
                                                {

                                                }
                                            });
                                        }
                                    });
                                    deleteGroup.show();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
            else if (AdminKey.equals(online_user_id))
            {
                if (OtherAdmins.size() == 0)
                {
                    Toast.makeText(this, "Admin1 line 892 "+Admin1.size(), Toast.LENGTH_SHORT).show();


                    if (Admin1.size() > 0 )
                    {
                        final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                        deleteGroup.setTitle("Please choose other admin to lead the group");

                        deleteGroup.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                        deleteGroup.show();
                    }
                    if (Admin1.size() == 0)
                    {
                        final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                        deleteGroup.setTitle("Do you want to delete the group");

                        deleteGroup.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                GroupRef.child(AdminKey).child(GroupPosition).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                });
                            }
                        });
                        deleteGroup.show();
                    }
                }
                else if (OtherAdmins.size() == 1)
                {

                    final AlertDialog.Builder deleteGroup = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                    deleteGroup.setTitle("Do you want left from the group?");

                    deleteGroup.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            Calendar calFordATE = Calendar.getInstance();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                            final Map<String,Object> NewMembers = new HashMap<>();

                            final Map<String,Object> NewAdmins = new HashMap<>();

                            final ArrayList<String> NextAdminGroupsCount = new ArrayList<>();

                            final String[] NextGroupPosition = new String[1];

                            GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        String admin = snapshot.getValue().toString();

                                        if (!admin.equals(AdminKey))
                                        {
                                            NewMembers.put(snapshot.getKey(),admin);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });

                            GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        String admin = snapshot.getValue().toString();

                                        if (!admin.equals(AdminKey))
                                        {
                                            NewAdmins.put(snapshot.getKey(),admin);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });

                            GroupRef.child(OtherAdmins.get(0)).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    NextAdminGroupsCount.clear();
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                                    {
                                        NextAdminGroupsCount.add(dataSnapshot1.getKey());
                                    }
                                    for (int i=0; i<NextAdminGroupsCount.size(); i++)
                                    {
                                        int number = Integer.valueOf(NextAdminGroupsCount.get(i));

                                        if (!(number == i))
                                        {
                                            NextGroupPosition[0] = String.valueOf(i);

                                            break;
                                        }

                                    }
                                    if (NextGroupPosition[0] == null)
                                    {
                                        NextGroupPosition[0] = String.valueOf(NextAdminGroupsCount.size());
                                    }

                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("date").setValue(saveCurrentDate);
                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("new_group_name").setValue(GroupName);
                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("group_admin").updateChildren(NewAdmins);
                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("no_of_new_group_members").updateChildren(NewMembers);
                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("group_profile_image").setValue("default_group_profile_image");
                                    GroupRef.child(OtherAdmins.get(0)).child(NextGroupPosition[0]).child("group_profile_thumb_image").setValue("default_group_profile_thumb_image").addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            GroupRef.child(AdminKey).child(GroupPosition).child("usage").setValue("waste");
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });
                            Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                    deleteGroup.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
                    deleteGroup.show();
                }
                else if (OtherAdmins.size() > 1)
                {

                    final CharSequence[] NamesAndKeys = Names.toArray(new String[Names.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewUserGroupProfileActivity.this);
                    builder.setTitle("Choose your next main admin to lead the group");
                    builder.setItems(NamesAndKeys, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final String key = OtherAdmins.get(which);

                            Calendar calFordATE = Calendar.getInstance();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                            final Map<String,Object> NewMembers = new HashMap<>();

                            final Map<String,Object> NewAdmins = new HashMap<>();

                            final ArrayList<String> NextAdminGroupsCount = new ArrayList<>();

                            final String[] NextGroupPosition = new String[1];

                            GroupRef.child(AdminKey).child(GroupPosition).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        String admin = snapshot.getValue().toString();

                                        if (!admin.equals(AdminKey))
                                        {
                                            NewMembers.put(snapshot.getKey(),admin);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });

                            GroupRef.child(AdminKey).child(GroupPosition).child("group_admin").addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        String admin = snapshot.getValue().toString();

                                        if (!admin.equals(AdminKey))
                                        {
                                            NewAdmins.put(snapshot.getKey(),admin);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });

                            GroupRef.child(key).addValueEventListener(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    NextAdminGroupsCount.clear();
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                                    {
                                        NextAdminGroupsCount.add(dataSnapshot1.getKey());
                                    }
                                    for (int i=0; i<NextAdminGroupsCount.size(); i++)
                                    {
                                        int number = Integer.valueOf(NextAdminGroupsCount.get(i));

                                        if (!(number == i))
                                        {
                                            NextGroupPosition[0] = String.valueOf(i);

                                            break;
                                        }

                                    }
                                    if (NextGroupPosition[0] == null)
                                    {
                                        NextGroupPosition[0] = String.valueOf(NextAdminGroupsCount.size());
                                    }

                                    GroupRef.child(key).child(NextGroupPosition[0]).child("date").setValue(saveCurrentDate);
                                    GroupRef.child(key).child(NextGroupPosition[0]).child("new_group_name").setValue(GroupName);
                                    GroupRef.child(key).child(NextGroupPosition[0]).child("group_admin").updateChildren(NewAdmins);
                                    GroupRef.child(key).child(NextGroupPosition[0]).child("no_of_new_group_members").updateChildren(NewMembers);
                                    GroupRef.child(key).child(NextGroupPosition[0]).child("group_profile_image").setValue("default_group_profile_image");
                                    GroupRef.child(key).child(NextGroupPosition[0]).child("group_profile_thumb_image").setValue("default_group_profile_thumb_image").addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            GroupRef.child(AdminKey).child(GroupPosition).child("usage").setValue("waste");
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            });
                            Intent mainIntent = new Intent(ViewUserGroupProfileActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
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
                            Toast.makeText(ViewUserGroupProfileActivity.this, "Saving Your Profile Image to Firebase Storage", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful())
                                    {
                                        Map<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("group_profile_image", downloadUrl);
                                        update_user_data.put("group_profile_thumb_image", thumb_downloadUrl);

                                        GroupRef.child(AdminKey).child(GroupPosition).updateChildren(update_user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        startActivity(new Intent(ViewUserGroupProfileActivity.this,MainActivity.class));
    }
}
