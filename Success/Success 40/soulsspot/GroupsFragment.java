package com.deffe.macros.soulsspot;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Member;
import java.security.acl.Group;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsFragment extends Fragment
{
    private View view;

    private FloatingActionButton GroupsFab;

    private RecyclerView CreatedGroupsRecyclerView;

    private CreatedGroupsAdapter createdGroupsAdapter;

    private DatabaseReference CreatedGroupsReference,CreatedGroupMembersReference,GroupsReference;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> NotAdminPositionKey = new ArrayList<>();

    private ArrayList<String> NotAdminAdminKey = new ArrayList<>();

    private  String online_user_id;

    private ArrayList<String> CreatedGroups = new ArrayList<>();

    private ArrayList<String> MembersName = new ArrayList<>();

    private String online_user_name;

    private ArrayList<String> GroupMembers = new ArrayList<>();

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

        CreatedGroupsReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(online_user_id);

        GroupsReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        CreatedGroupMembersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        CreatedGroupsRecyclerView = view.findViewById(R.id.created_groups_recycler_view);
        CreatedGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        createdGroupsAdapter = new CreatedGroupsAdapter(CreatedGroups,NotAdminAdminKey,NotAdminPositionKey,getActivity());
        CreatedGroupsRecyclerView.setAdapter(createdGroupsAdapter);

        CreatedGroupMembersReference.child(online_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                online_user_name = dataSnapshot.child("user_name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        CreatedGroupsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                CreatedGroups.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    CreatedGroups.add(dataSnapshot1.getKey());

                    NotAdminAdminKey.add("admin");

                    NotAdminPositionKey.add(online_user_id);
                }
                createdGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        GroupsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                        GroupsReference.child(snapshot.getKey()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                for (final DataSnapshot snapshot1 : dataSnapshot.getChildren())
                                {
                                    GroupsReference.child(snapshot.getKey()).child(snapshot1.getKey()).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                                            {
                                                String onlineUserKey = dataSnapshot1.getValue().toString();
                                                if (onlineUserKey.equals(online_user_id))
                                                {
                                                    CreatedGroups.add(snapshot1.getKey());

                                                    NotAdminPositionKey.add(snapshot.getKey());

                                                    NotAdminAdminKey.add("not_admin");
                                                }
                                            }
                                            createdGroupsAdapter.notifyDataSetChanged();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });


                                }
                                createdGroupsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                }
                createdGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        GroupsFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent createGroupIntent = new Intent(getContext(),FriendsListActivity.class);
                startActivity(createGroupIntent);
            }
        });
        return view;
    }


    private class CreatedGroupsAdapter extends RecyclerView.Adapter<CreatedGroupsAdapter.CreatedGroupsViewHolder>
    {

        ArrayList<String> CreatedGroups = new ArrayList<>();
        ArrayList<String> NotAdminAdminKey = new ArrayList<>();
        ArrayList<String> NotAdminPositionKey = new ArrayList<>();
        Context context;


        CreatedGroupsAdapter(ArrayList<String> createdGroups,ArrayList<String> NotAdminAdminKey,ArrayList<String> NotAdminPositionKey, Context context)
        {
            CreatedGroups = createdGroups;
            this.NotAdminAdminKey = NotAdminAdminKey;
            this.NotAdminPositionKey = NotAdminPositionKey;
            this.context = context;

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
            final String groups = CreatedGroups.get(position);
            final String admin = NotAdminAdminKey.get(position);
            final String notAdmin = NotAdminPositionKey.get(position);

            if (admin.equals("admin"))
            {
                CreatedGroupsReference.child(groups).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        final String groupName = (String) dataSnapshot.child("new_group_name").getValue();
                        final String groupThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();
                        final String adminName = (String) dataSnapshot.child("admin_name").getValue();
                        final String date = (String) dataSnapshot.child("date").getValue();


                        holder.CreatedGroupName.setText(groupName);
                        holder.setUser_thumb_img(context,groupThumbImage);

                        holder.Admin.setVisibility(View.VISIBLE);


                        CreatedGroupsReference.child(groups).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                MembersName.clear();


                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    final String members = snapshot.getKey();

                                    if (members.equals(online_user_name))
                                    {

                                    }
                                    else
                                    {
                                        MembersName.add(members);
                                    }
                                }

                                String s = android.text.TextUtils.join(",",MembersName);

                                holder.CreatedGroupMembersName.setText("You and\n"+s);

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


                                                Intent viewGroupProfileIntent = new Intent(getActivity(),ViewGroupProfileActivity.class);
                                                viewGroupProfileIntent.putExtra("admin",online_user_id);
                                                viewGroupProfileIntent.putExtra("group_position",groups);
                                                viewGroupProfileIntent.putExtra("group_name",groupName);
                                                viewGroupProfileIntent.putExtra("group_thumb_image",groupThumbImage);
                                                viewGroupProfileIntent.putExtra("admin_name",adminName);
                                                viewGroupProfileIntent.putExtra("date",date);
                                                startActivity(viewGroupProfileIntent);

                                                break;
                                            case R.id.action_exit_group:
                                                break;
                                        }
                                        return false;
                                    }
                                });
                                popup.show();
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
            else if (admin.equals("not_admin"))
            {
                GroupsReference.child(notAdmin).child(groups).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {


                        final String groupName = (String) dataSnapshot.child("new_group_name").getValue();
                        final String groupThumbImage = (String) dataSnapshot.child("group_profile_thumb_image").getValue();
                        final String adminName = (String) dataSnapshot.child("admin_name").getValue();
                        final String date = (String) dataSnapshot.child("date").getValue();

                        holder.CreatedGroupName.setText(groupName);
                        holder.setUser_thumb_img(context,groupThumbImage);

                        holder.Admin.setVisibility(View.GONE);

                        GroupsReference.child(notAdmin).child(groups).child("no_of_new_group_members").addValueEventListener(new ValueEventListener()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                MembersName.clear();

                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    final String members = snapshot.getKey();

                                    if (members.equals(online_user_name))
                                    {

                                    }
                                    else
                                    {
                                        MembersName.add(members);

                                    }
                                }


                                String s = android.text.TextUtils.join(",",MembersName);

                                holder.CreatedGroupMembersName.setText("You and\n"+s);

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



                                                Intent viewGroupProfileIntent = new Intent(getActivity(),ViewGroupProfileActivity.class);
                                                viewGroupProfileIntent.putExtra("admin",notAdmin);
                                                viewGroupProfileIntent.putExtra("group_position",groups);
                                                viewGroupProfileIntent.putExtra("group_name",groupName);
                                                viewGroupProfileIntent.putExtra("group_thumb_image",groupThumbImage);
                                                viewGroupProfileIntent.putExtra("admin_name",adminName);
                                                viewGroupProfileIntent.putExtra("date",date);
                                                startActivity(viewGroupProfileIntent);


                                                break;
                                            case R.id.action_exit_group:
                                                break;
                                        }
                                        return false;
                                    }
                                });
                                popup.show();
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

        }

        @Override
        public int getItemCount() {
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



