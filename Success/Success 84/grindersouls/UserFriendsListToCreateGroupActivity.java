package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFriendsListToCreateGroupActivity extends BaseThemedActivity implements View.OnClickListener
{
    private static final String TAG = UserFriendsListToCreateGroupActivity.class.getSimpleName();

    private boolean is_in_action_mode = false;

    private Toolbar FriendsListToolBar;

    private LinearLayout SelectedFriendsLinearLayout;

    private RecyclerView FriendsListRecyclerView,SelectedFriendsRecyclerView;

    private TextView onlineUserNameTitle,onlineUserFriendsList;

    private CircleImageView onlineUserCreateGroupProfileImage;

    private FirebaseAuth firebaseAuth;

    private CollectionReference friendsListRef, userGroupsRef;

    private ArrayList<String> UserFriendsKey = new ArrayList<>();

    private ArrayList<String> FriendsDate = new ArrayList<>();

    private int counter = 0;

    private AllUserFriendsListAdapter allUserFriendsListAdapter;

    private SelectedUserToCreateGroup selectedUserToCreateGroup;

    private String online_user_id;

    private ArrayList<String> friendsCount = new ArrayList<>();

    private ArrayList<String> selectedFriendsToSend;

    private ArrayList<String> GroupMembers = new ArrayList<>();

    private String Incoming;

    private String GroupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friend_list_to_create_group);

        FriendsListToolBar = findViewById(R.id.friends_list_bar_layout);
        setSupportActionBar(FriendsListToolBar);
        FriendsListToolBar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        if (FriendsListToolBar != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        GroupMembers = getIntent().getStringArrayListExtra("group_members");

        Incoming = getIntent().getExtras().getString("activity_name");

        GroupKey = getIntent().getExtras().getString("group_key");

        DocumentReference friendsRef = FirebaseFirestore.getInstance().collection("Users").document(online_user_id);

        friendsListRef = FirebaseFirestore.getInstance().collection("Users");

        userGroupsRef = FirebaseFirestore.getInstance().collection("Groups");

        CollectionReference userFriendsListReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("Friends");

        onlineUserNameTitle = findViewById(R.id.create_group_user_custom_profile_name);

        onlineUserFriendsList = findViewById(R.id.create_group_user_custom_profile_friends);

        onlineUserCreateGroupProfileImage = findViewById(R.id.create_group_user_img);

        FloatingActionButton createGroupUsingSelectedMembersButton = findViewById(R.id.all_friends_activity_fab);

        SelectedFriendsLinearLayout = findViewById(R.id.selected_friends_linear_layout);

        if (Incoming != null && Incoming.equals("ViewUserGroupProfileActivity"))
        {
            onlineUserFriendsList.setText("");
        }
        else
        {
            onlineUserFriendsList.setText("Add Members");
        }

        friendsRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(UserFriendsListToCreateGroupActivity.this, "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    String userName =  userName = documentSnapshot.getString("user_name");
                    String userThumb = userThumb = documentSnapshot.getString("user_thumb_img");

                    if (Incoming != null && Incoming.equals("ViewUserGroupProfileActivity"))
                    {
                        onlineUserNameTitle.setText("Add members");
                    }
                    else
                    {
                        onlineUserNameTitle.setText(userName+" Friends");
                    }

                    final String finalUserThumb = userThumb;
                    Picasso.with(UserFriendsListToCreateGroupActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(onlineUserCreateGroupProfileImage, new Callback()
                            {
                                @Override
                                public void onSuccess()
                                {

                                }
                                @Override
                                public void onError()
                                {
                                    Picasso.with(UserFriendsListToCreateGroupActivity.this).load(finalUserThumb).placeholder(R.drawable.vadim).into(onlineUserCreateGroupProfileImage);
                                }
                            });
                }
            }
        });

        init();

        userFriendsListReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(UserFriendsListToCreateGroupActivity.this, "Error while getting documents" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                UserFriendsKey.clear();
                FriendsDate.clear();
                friendsCount.clear();

                if (queryDocumentSnapshots != null)
                {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges())
                    {
                        UserFriendsKey.add(documentChange.getDocument().getId());

                        friendsCount.add(documentChange.getDocument().getId());

//                        FriendsDate.add(documentChange.getDocument().getTimestamp("time"));
                    }
                    allUserFriendsListAdapter.notifyDataSetChanged();
                }
            }
        });

        createGroupUsingSelectedMembersButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectedFriendsToSend = new ArrayList<>();

                selectedFriendsToSend.addAll(allUserFriendsListAdapter.SelectedGroupMembers);

                if (Incoming.equals("ViewUserGroupProfileActivity"))
                {
                   /* selectedFriendsToSend = new ArrayList<>();

                    selectedFriendsToSend.addAll(allUserFriendsListAdapter.SelectedGroupMembers);

                    selectedFriendsToSend.add(online_user_id);

                    final ArrayList<String> NewMembers = new ArrayList<>(GroupMembers);

                    NewMembers.addAll(selectedFriendsToSend);

                    final ArrayList<String> NewMembersIntersection = new ArrayList<>(GroupMembers);

                    NewMembersIntersection.retainAll(selectedFriendsToSend);

                    NewMembers.removeAll(NewMembersIntersection);

                    final Map<String,Object> NewAddedMembers = new HashMap<>();
                    final Map<String,Object> NewAddedMembersWithNames = new HashMap<>();

                    for (int i = 0; i < selectedFriendsToSend.size(); i++)
                    {
                        final int finalI = i;

                        FriendsListRef.child(selectedFriendsToSend.get(i)).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                final String name = dataSnapshot.child("user_name").getValue().toString();

                                if (dataSnapshot.hasChild("group_keys"))
                                {
                                    if (dataSnapshot.child("group_keys").hasChild(GroupKey))
                                    {
                                        final String admin = dataSnapshot.child("group_keys").child(GroupKey).getValue().toString();

                                        NewAddedMembers.put(selectedFriendsToSend.get(finalI),admin);

                                    }
                                    else
                                    {
                                        NewAddedMembers.put(selectedFriendsToSend.get(finalI),"not_admin");
                                    }
                                }
                                else
                                {
                                    NewAddedMembers.put(selectedFriendsToSend.get(finalI),"not_admin");
                                }

                                NewAddedMembersWithNames.put(selectedFriendsToSend.get(finalI),name);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }

                    UserGroupsRef.child(GroupKey).child("group_members").removeValue();
                    UserGroupsRef.child(GroupKey).child("group_members_with_names").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            UserGroupsRef.child(GroupKey).child("group_members").updateChildren(NewAddedMembers);
                            UserGroupsRef.child(GroupKey).child("group_members_with_names").updateChildren(NewAddedMembersWithNames)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                for (String member : NewMembers)
                                                {
                                                    FriendsListRef.child(member).child("group_keys").child(GroupKey).setValue("not_admin");

                                                    Toast.makeText(UserFriendsListToCreateGroupActivity.this, "New "+member, Toast.LENGTH_SHORT).show();
                                                }

                                                Toast.makeText(UserFriendsListToCreateGroupActivity.this, "NewMembers size "+String.valueOf(NewMembers.size()), Toast.LENGTH_SHORT).show();

                                                Intent viewGroupProfileIntent = new Intent();
                                                setResult(RESULT_OK,viewGroupProfileIntent);
                                                finish();
                                            }
                                        }
                                    });
                        }
                    });*/
                }
                else if (selectedFriendsToSend.size() > 0)
                {
                    final Bundle bundle = new Bundle();
                    final Bundle bundle1 = new Bundle();

                    bundle.putStringArrayList("selected_friends",selectedFriendsToSend);
                    bundle1.putStringArrayList("friends_count",friendsCount);

                    Intent createGroupIntent = new Intent(UserFriendsListToCreateGroupActivity.this,CreateNewGroupActivity.class);
                    createGroupIntent.putExtras(bundle);
                    createGroupIntent.putExtras(bundle1);
                    createGroupIntent.putExtra("admin_user_id",online_user_id);
                    startActivity(createGroupIntent);
                }
                else
                {
                    Toast.makeText(UserFriendsListToCreateGroupActivity.this, "Please select atleast one member", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void init()
    {
        FriendsListRecyclerView = findViewById(R.id.friends_list_recycler_view);
        FriendsListRecyclerView.setLayoutManager(new LinearLayoutManager(UserFriendsListToCreateGroupActivity.this, LinearLayoutManager.VERTICAL,false));
        FriendsListRecyclerView
                .addItemDecoration(new HorizontalDividerItemDecoration.Builder(UserFriendsListToCreateGroupActivity.this)
                        .color(Color.BLACK)
                        .margin(125, 0)
                        .build());
        allUserFriendsListAdapter = new AllUserFriendsListAdapter(UserFriendsKey,FriendsDate,UserFriendsListToCreateGroupActivity.this);
        FriendsListRecyclerView.setAdapter(allUserFriendsListAdapter);

        SelectedFriendsRecyclerView = findViewById(R.id.selected_friends_recycler_view);
        SelectedFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(UserFriendsListToCreateGroupActivity.this, LinearLayoutManager.HORIZONTAL,false));
        SelectedFriendsRecyclerView
                .addItemDecoration(new VerticalDividerItemDecoration.Builder(UserFriendsListToCreateGroupActivity.this)
                        .color(Color.BLACK)
                        .size(10)
                        .build());
        selectedUserToCreateGroup = new SelectedUserToCreateGroup(UserFriendsListToCreateGroupActivity.this,allUserFriendsListAdapter.SelectedGroupMembers);
        SelectedFriendsRecyclerView.setAdapter(selectedUserToCreateGroup);

    }

    private class AllUserFriendsListAdapter extends RecyclerView.Adapter<AllUserFriendsListAdapter.UserFriendsListViewHolder>
    {
        ArrayList<String> UserFriendKey;

        ArrayList<String> FriendsDate;

        ArrayList<String> SelectedGroupMembers = new ArrayList<>();

        Context context;

        UserFriendsListToCreateGroupActivity friendsListActivity;

        AllUserFriendsListAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
        {
            UserFriendKey = UserFriendsKey;
            this.FriendsDate = FriendsDate;
            this.context = context;
            friendsListActivity = (UserFriendsListToCreateGroupActivity) context;
        }

        @NonNull
        @Override
        public UserFriendsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_friends_list_items_layout_for_activity,parent,false);

            return new UserFriendsListViewHolder(view,friendsListActivity);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserFriendsListViewHolder holder, @SuppressLint("RecyclerView") final int position)
        {
            final String userFriendId = UserFriendKey.get(position);
//            final String friendSinceDate = "Friends Since:\n"+ "    " + FriendsDate.get(position);

            friendsListRef.document(userFriendId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(UserFriendsListToCreateGroupActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String friendName = documentSnapshot.getString("user_name");
                        final String friendThumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.userName.setText(friendName);
                        holder.setUser_thumb_img(UserFriendsListToCreateGroupActivity.this,friendThumbImage);
//                        holder.userFriendSinceDate.setText(friendSinceDate);

                        if (!friendsListActivity.is_in_action_mode)
                        {
                            holder.selectUserToAddToGroupCheckBox.setVisibility(View.VISIBLE);
                            holder.selectUserToAddToGroupCheckBox.setChecked(false);
                        }
                        else
                        {
                            holder.selectUserToAddToGroupCheckBox.setVisibility(View.VISIBLE);
                            holder.selectUserToAddToGroupCheckBox.setChecked(false);
                        }

                        if (Incoming.equals("ViewUserGroupProfileActivity"))
                        {
                            SelectedGroupMembers.clear();
                            for (String mem : GroupMembers)
                            {
                                if (mem.equals(userFriendId))
                                {
                                    holder.itemView.setEnabled(false);
                                    holder.selectUserToAddToGroupCheckBox.setChecked(true);
                                    holder.selectUserToAddToGroupCheckBox.setEnabled(false);
                                    holder.userFriendSinceDate.setText("Already added to group");
                                }

                                if (!mem.equals(online_user_id))
                                {
                                    SelectedGroupMembers.add(mem);
                                }
                            }
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (holder.selectUserToAddToGroupCheckBox.isChecked())
                                {
                                    counter = counter - 1;

                                    updateCounter(counter);

                                    SelectedGroupMembers.remove(UserFriendsKey.get(position));

                                    holder.selectUserToAddToGroupCheckBox.setChecked(false);

                                    if (SelectedGroupMembers.size() == 0)
                                    {
                                        SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                        SelectedFriendsRecyclerView.setVisibility(View.GONE);

                                        selectedUserToCreateGroup.selectedUsersKeys.clear();

                                        selectedUserToCreateGroup.notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        if (selectedUserToCreateGroup.selectedUsersKeys.size() == 0)
                                        {
                                            SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                            SelectedFriendsRecyclerView.setVisibility(View.GONE);
                                        }

                                        else
                                        {
                                            SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                                            SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                                            selectedUserToCreateGroup.notifyDataSetChanged();
                                        }

                                    }

                                }
                                else if (!holder.selectUserToAddToGroupCheckBox.isChecked())
                                {
                                    counter = counter + 1;

                                    updateCounter(counter);

                                    SelectedGroupMembers.add(UserFriendsKey.get(position));

                                    holder.selectUserToAddToGroupCheckBox.setChecked(true);

                                    if (SelectedGroupMembers.size() == 0)
                                    {
                                        SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                        SelectedFriendsRecyclerView.setVisibility(View.GONE);

                                        selectedUserToCreateGroup.selectedUsersKeys.clear();
                                    }
                                    else
                                    {

                                        if (selectedUserToCreateGroup.selectedUsersKeys.size() == 0)
                                        {
                                            SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                            SelectedFriendsRecyclerView.setVisibility(View.GONE);
                                        }

                                        else
                                        {
                                            SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                                            SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                                            selectedUserToCreateGroup.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return UserFriendKey.size();
        }

        class UserFriendsListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            View v;

            TextView userName,userFriendSinceDate;

            CheckBox selectUserToAddToGroupCheckBox;

            UserFriendsListToCreateGroupActivity friendsListActivity;

            UserFriendsListViewHolder(View itemView,UserFriendsListToCreateGroupActivity friendsListActivity)
            {
                super(itemView);

                this.friendsListActivity = friendsListActivity;

                v = itemView;

                userName = v.findViewById(R.id.user_friend_list_name);
                userFriendSinceDate = v.findViewById(R.id.user_friend_list_since_date);
                selectUserToAddToGroupCheckBox = v.findViewById(R.id.select_user_to_add_to_group_check_box);

                selectUserToAddToGroupCheckBox.setOnClickListener(this);
            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.user_friend_list_image);

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

            @Override
            public void onClick(View v)
            {
                friendsListActivity.prepareSelection(v,getAdapterPosition());
            }
        }

    }

    @Override
    public void onClick(View v)
    {
        is_in_action_mode = true;

        allUserFriendsListAdapter.notifyDataSetChanged();

        selectedUserToCreateGroup.notifyDataSetChanged();
    }

    public void prepareSelection(View view, int position)
    {
        if (((CheckBox)view).isChecked())
        {
            allUserFriendsListAdapter.SelectedGroupMembers.add(UserFriendsKey.get(position));

            SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

            SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

            counter = counter + 1;

            updateCounter(counter);

            if (allUserFriendsListAdapter.SelectedGroupMembers.size() == 0)
            {
                SelectedFriendsLinearLayout.setVisibility(View.GONE);

                SelectedFriendsRecyclerView.setVisibility(View.GONE);

                selectedUserToCreateGroup.selectedUsersKeys.clear();

            }
            else
            {
                if (selectedUserToCreateGroup.selectedUsersKeys.size() == 0)
                {

                    SelectedFriendsLinearLayout.setVisibility(View.GONE);

                    SelectedFriendsRecyclerView.setVisibility(View.GONE);
                }
                else
                {
                    SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                    SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                    selectedUserToCreateGroup.notifyDataSetChanged();
                }
            }
        }
        else
        {

            allUserFriendsListAdapter.SelectedGroupMembers.remove(UserFriendsKey.get(position));

            selectedUserToCreateGroup.notifyDataSetChanged();

            counter = counter - 1;

            updateCounter(counter);

            if (allUserFriendsListAdapter.SelectedGroupMembers.size() == 0)
            {
                SelectedFriendsLinearLayout.setVisibility(View.GONE);

                SelectedFriendsRecyclerView.setVisibility(View.GONE);

                selectedUserToCreateGroup.notifyDataSetChanged();
            }
            else
            {
                if (selectedUserToCreateGroup.selectedUsersKeys.size() == 0)
                {

                    SelectedFriendsLinearLayout.setVisibility(View.GONE);

                    SelectedFriendsRecyclerView.setVisibility(View.GONE);
                }

                else
                {
                    SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                    SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                    selectedUserToCreateGroup.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateCounter(int counter)
    {
        if (counter == 0)
        {
            if (Incoming != null && Incoming.equals("ViewUserGroupProfileActivity"))
            {
                onlineUserFriendsList.setText("");
            }
            else
            {
                onlineUserFriendsList.setText("Add Members");
            }
        }
        else
        {
            if (Incoming != null && Incoming.equals("ViewUserGroupProfileActivity"))
            {
                onlineUserFriendsList.setText("");
            }
            else
            {
                onlineUserFriendsList.setText(counter+ " of " + friendsCount.size() + " selected");
            }
        }
    }

    public class SelectedUserToCreateGroup extends RecyclerView.Adapter<SelectedUserToCreateGroup.SelectedUserToCreateGroupViewHolder>
    {

        ArrayList<String> selectedUsersKeys;
        Context c;

        SelectedUserToCreateGroup(Context c,  ArrayList<String> selectedUsersKeys)
        {
            this.selectedUsersKeys = selectedUsersKeys;
            this.c = c;
        }

        @NonNull
        @Override
        public SelectedUserToCreateGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_user_to_create_group_items,parent,false);

            return new SelectedUserToCreateGroupViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull final SelectedUserToCreateGroupViewHolder holder, @SuppressLint("RecyclerView") final int position)
        {
            final String selectedUserKey = selectedUsersKeys.get(position);

            friendsListRef.document(selectedUserKey).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(UserFriendsListToCreateGroupActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String userName = documentSnapshot.getString("user_name");
                        final String userThumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.selectedUsername.setText(userName);
                        holder.setUser_thumb_img(UserFriendsListToCreateGroupActivity.this,userThumbImage);
                    }
                }
            });
        }
        @Override
        public int getItemCount()
        {
            return selectedUsersKeys.size();
        }

        class SelectedUserToCreateGroupViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView selectedUsername;

            SelectedUserToCreateGroupViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                selectedUsername = itemView.findViewById(R.id.selected_user_name_to_create_group);
            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.selected_user_image_to_create_group);

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
