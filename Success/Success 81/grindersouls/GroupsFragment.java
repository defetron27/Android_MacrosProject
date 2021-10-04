package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        FloatingActionButton groupsFab = view.findViewById(R.id.groups_fab);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        groupsReference = FirebaseFirestore.getInstance().collection("Groups");

        DocumentReference createdGroupMembersReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id);
        CollectionReference groupKeysReference = FirebaseFirestore.getInstance().collection("Users").document(online_user_id).collection("group_keys");

        final RecyclerView createdGroupsRecyclerView = view.findViewById(R.id.created_groups_recycler_view);
        createdGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));

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
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                    {
                        UserGroupsItemsModel groupsItemsModel = snapshot.toObject(UserGroupsItemsModel.class);
                        createdGroups.add(groupsItemsModel);
                    }
                    createdGroupsAdapter = new CreatedGroupsAdapter(container.getContext(),createdGroups);
                    createdGroupsRecyclerView.setAdapter(createdGroupsAdapter);
                }
            }
        });

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
        private Context context;
        private ArrayList<UserGroupsItemsModel> CreatedGroups;
        private String groupThumbImage,groupName,groupKey;

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
            final UserGroupsItemsModel userGroupsItemsModel = CreatedGroups.get(position);

            final ArrayList<String> GroupMembersKeys = new ArrayList<>();

            groupsReference.document(userGroupsItemsModel.getKey()).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        groupName = documentSnapshot.getString("group_name");
                        groupThumbImage = documentSnapshot.getString("group_profile_thumb_image");
                        groupKey = documentSnapshot.getString("group_key");

                        holder.CreatedGroupName.setText(groupName);
                        holder.setUser_thumb_img(context,groupThumbImage);

                    }
                }
            });

            if (userGroupsItemsModel.getType().equals("admin"))
            {
                holder.Admin.setVisibility(View.VISIBLE);
            }
            else if (userGroupsItemsModel.getType().equals("not_admin"))
            {
                holder.Admin.setVisibility(View.GONE);
            }

            groupsReference.document(userGroupsItemsModel.getKey()).collection("group_members_with_names").addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (queryDocumentSnapshots != null)
                    {
                        MembersName.clear();
                        GroupMembersKeys.clear();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots)
                        {
                            if (!queryDocumentSnapshot.getId().equals(online_user_id))
                            {
                                MembersName.add(queryDocumentSnapshot.getString("name"));
                                GroupMembersKeys.add(queryDocumentSnapshot.getId());
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
                                    viewGroupProfileIntent.putExtra("group_key",userGroupsItemsModel.getKey());
                                    viewGroupProfileIntent.putExtra("admin_or_not",userGroupsItemsModel.getType());
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
