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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.deffe.macros.grindersouls.Models.UserGroupsItemsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsFragment extends Fragment
{
    private static final String TAG = GroupsFragment.class.getSimpleName();

    private CreatedGroupsAdapter createdGroupsAdapter;

    private CollectionReference groupsReference;

    private ArrayList<UserGroupsItemsModel> createdGroups = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        FloatingActionButton groupsFab = view.findViewById(R.id.groups_fab);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        groupsReference = FirebaseFirestore.getInstance().collection("Groups");

        DocumentReference createdGroupMembersReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id);
        CollectionReference groupKeysReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id).collection("group_keys");

        createdGroupMembersReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(), "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    online_user_name = documentSnapshot.getString("user_name");
                }
            }
        });

        groupKeysReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(), "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if (queryDocumentSnapshots != null)
                {
                    createdGroups.clear();
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges())
                    {
                        if (documentChange.getType() == DocumentChange.Type.ADDED)
                        {
                            UserGroupsItemsModel groupsItemsModel = documentChange.getDocument().toObject(UserGroupsItemsModel.class);
                            createdGroups.add(groupsItemsModel);
                            createdGroupsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        RecyclerView createdGroupsRecyclerView = view.findViewById(R.id.created_groups_recycler_view);
        createdGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        createdGroupsAdapter = new CreatedGroupsAdapter(container.getContext(),createdGroups);
        createdGroupsRecyclerView.setAdapter(createdGroupsAdapter);

        groupsFab.setOnClickListener(new View.OnClickListener()
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
        ArrayList<UserGroupsItemsModel> CreatedGroups;

        CreatedGroupsAdapter(Context context, ArrayList<UserGroupsItemsModel> createdGroups)
        {
            this.context = context;
            CreatedGroups = createdGroups;
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
            UserGroupsItemsModel userGroupsItemsModel = CreatedGroups.get(position);

            final ArrayList<String> GroupMembersKeys = new ArrayList<>();

            groupsReference.document(userGroupsItemsModel.getKey()).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {

                }
            });

           /* GroupsReference.child(groupKey).addValueEventListener(new ValueEventListener()
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
            });*/
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
