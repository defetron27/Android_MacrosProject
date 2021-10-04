package com.deffe.macros.soulsspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewGroupProfileActivity extends AppCompatActivity
{
    private Toolbar ViewGroupProfileToolbar;

    private CollapsingToolbarLayout ViewGroupProfileCollapsingToolbar;

    private RecyclerView ViewGroupProfileMembersRecyclerView;

    private GroupProfileMembersAdapter groupProfileMembersAdapter;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference GroupMembersReference,GroupRef;

    private String online_user_id;

    private String GroupThumbImage;

    private String GroupName;

    private String AdminKey;

    private String GroupPosition;

    private ArrayList<String> GroupMembers = new ArrayList<>();

    private ImageView GroupProfileImage;

    private String CreatedGroupDate;

    private TextView CreatedDateOfGroup;

    private String AdminName;

    private FloatingActionButton EditGroupNameButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_profile);

        AdminKey = getIntent().getExtras().getString("admin");

        GroupPosition = getIntent().getExtras().getString("group_position");

        GroupName = getIntent().getExtras().getString("group_name");

        GroupThumbImage = getIntent().getExtras().getString("group_thumb_image");

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

        ViewGroupProfileMembersRecyclerView = findViewById(R.id.view_group_profile_members_recycler_view);
        ViewGroupProfileMembersRecyclerView.setLayoutManager(new GridLayoutManager(ViewGroupProfileActivity.this,4, GridLayoutManager.VERTICAL,false));
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

            void setUser_thumb_img(final Context c, final String user_thumb_img) {
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

        return true;
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
    }
}
