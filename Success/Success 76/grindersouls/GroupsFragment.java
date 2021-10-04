package com.deffe.macros.grindersouls;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class GroupsFragment extends Fragment
{

    private View view;

    private FloatingActionButton GroupsFab;

    private RecyclerView CreatedGroupsRecyclerView;

    private CreatedGroupsAdapter createdGroupsAdapter;

    private DatabaseReference CreatedGroupMembersReference,GroupsReference;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> AdminOrNot = new ArrayList<>();

    private ArrayList<String> CreatedGroups = new ArrayList<>();

    private  String online_user_id;

    private ArrayList<String> MembersName = new ArrayList<>();

    private String online_user_name;


    public GroupsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupsFab = view.findViewById(R.id.groups_fab);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        GroupsReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupsReference.keepSynced(true);

        CreatedGroupMembersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        CreatedGroupMembersReference.keepSynced(true);

        CreatedGroupMembersReference.child(online_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                online_user_name = dataSnapshot.child("user_name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        CreatedGroupMembersReference.child(online_user_id).child("group_keys").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                CreatedGroups.clear();
                AdminOrNot.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    CreatedGroups.add(snapshot.getKey());
                    AdminOrNot.add(snapshot.getValue().toString());
                }

                createdGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        CreatedGroupsRecyclerView = view.findViewById(R.id.created_groups_recycler_view);
        CreatedGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        createdGroupsAdapter = new CreatedGroupsAdapter(container.getContext(),CreatedGroups,AdminOrNot);
        CreatedGroupsRecyclerView.setAdapter(createdGroupsAdapter);

        GroupsFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent createGroupIntent = new Intent(getContext(),UserFriendsListToCreateGroupActivity.class);
                createGroupIntent.putExtra("activity_name","GroupsFragment");
                startActivity(createGroupIntent);
            }
        });
        return view;
    }

    private class CreatedGroupsAdapter extends RecyclerView.Adapter<CreatedGroupsAdapter.CreatedGroupsViewHolder>
    {

        Context context;
        ArrayList<String> CreatedGroups;
        ArrayList<String> AdminOrNot;

        CreatedGroupsAdapter(Context context, ArrayList<String> createdGroups, ArrayList<String> adminOrNot)
        {
            this.context = context;
            CreatedGroups = createdGroups;
            AdminOrNot = adminOrNot;
        }


        @NonNull
        @Override
        public CreatedGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.created_groups_layout_items,parent,false);

            return new CreatedGroupsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CreatedGroupsViewHolder holder, int position)
        {
            final String groupKey = CreatedGroups.get(position);
            final String admin = AdminOrNot.get(position);

            final ArrayList<String> GroupMembersKeys = new ArrayList<>();

            GroupsReference.child(groupKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    final String groupName = (String) dataSnapshot.child("group_name").getValue();
                    final String groupThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();

                    holder.CreatedGroupName.setText(groupName);
                    holder.setUser_thumb_img(context,groupThumbImage);

                    if (admin.equals("admin"))
                    {
                        holder.Admin.setVisibility(View.VISIBLE);
                    }
                    else if (admin.equals("not_admin"))
                    {
                        holder.Admin.setVisibility(View.GONE);
                    }

                    GroupsReference.child(groupKey).child("group_members_with_names").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            MembersName.clear();
                            GroupMembersKeys.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                final String members = snapshot.getValue().toString();

                                if (!members.equals(online_user_name))
                                {
                                    MembersName.add(members);
                                    GroupMembersKeys.add(snapshot.getKey());
                                }
                            }

                            String s = android.text.TextUtils.join(",",MembersName);

                            if (s.equals(""))
                            {
                                holder.CreatedGroupMembersName.setText(getResources().getString(R.string.you_only));
                            }
                            else
                            {
                                holder.CreatedGroupMembersName.setText("You and\n"+s);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });


                    holder.MenuButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            PopupMenu popup = new PopupMenu(context, holder.MenuButton);

                            popup.inflate(R.menu.group_details_menu);

                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                            {
                                @Override
                                public boolean onMenuItemClick(MenuItem item)
                                {
                                    switch (item.getItemId())
                                    {
                                        case R.id.action_group_details:

                                            Intent viewGroupProfileIntent = new Intent(context,ViewUserGroupProfileActivity.class);
                                            viewGroupProfileIntent.putExtra("group_key",groupKey);
                                            viewGroupProfileIntent.putExtra("admin_or_not",admin);
                                            startActivity(viewGroupProfileIntent);

                                            break;
                                    }
                                    return true;
                                }
                            });
                            popup.show();
                        }
                    });

                    holder.view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Bundle bundle = new Bundle();
                            Bundle bundle1 = new Bundle();

                            bundle.putStringArrayList("ids",GroupMembersKeys);
                            bundle1.putStringArrayList("members_names",MembersName);

                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                            chatIntent.setAction(Intent.ACTION_VIEW);
                            chatIntent.setType("group");
                            chatIntent.putExtras(bundle);
                            chatIntent.putExtras(bundle1);
                            chatIntent.putExtra("group_key",groupKey);
                            chatIntent.putExtra("group_thumb_img",groupThumbImage);
                            chatIntent.putExtra("group_name",groupName);
                            startActivity(chatIntent);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    Toast.makeText(context, "Error When Database Group", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return CreatedGroups.size();
        }

        class CreatedGroupsViewHolder extends RecyclerView.ViewHolder
        {
            TextView CreatedGroupName;
            TextView CreatedGroupMembersName;
            TextView Admin;
            ImageButton MenuButton;

            View view;

            CreatedGroupsViewHolder(View itemView)
            {
                super(itemView);

                view = itemView;

                CreatedGroupName = view.findViewById(R.id.created_group_name);

                CreatedGroupMembersName = view.findViewById(R.id.created_group_members_name);

                Admin = view.findViewById(R.id.group_admin);

                MenuButton = view.findViewById(R.id.group_menu_button);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = view.findViewById(R.id.created_group_image);

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
}
