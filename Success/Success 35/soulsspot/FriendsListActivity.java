package com.deffe.macros.soulsspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_SHORT;

public class FriendsListActivity extends AppCompatActivity implements View.OnClickListener
{
    private boolean is_in_action_mode = false;

    private Toolbar FriendsListToolBar;

    private MaterialSearchView friendsListSearchView;

    private LinearLayout SelectedFriendsLinearLayout;

    private RecyclerView FriendsListRecyclerView,SelectedFriendsRecyclerView;

    private TextView onlineUserNameTitle,onlineUserFriendsList;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference FriendsRef,FriendsListRef,UserFriendsListReference,UserFriendsListReferenceInFriends,GroupRef,UserGroupsRef;


    private ArrayList<String> UserFriendsKey = new ArrayList<>();

    private ArrayList<String> FriendsDate = new ArrayList<>();

    private int counter = 0;

    private AllUserFriendsListAdapter allUserFriendsListAdapter;

    private SelectedUserToCreateGroup selectedUserToCreateGroup;

    private String online_user_id;

    private ArrayList<String> friendsCount = new ArrayList<>();

    private FloatingActionButton CreateGroupUsingSelectedMembersButton;

    private ArrayList<String> selectedFriendsToSend;

    private ArrayList<String> countGroups = new ArrayList<>();

    private int adapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);


        FriendsListToolBar = findViewById(R.id.friends_list_bar_layout);
        setSupportActionBar(FriendsListToolBar);
        if (FriendsListToolBar != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        friendsListSearchView = findViewById(R.id.friends_list_search_view);
        friendsListSearchView.setVoiceSearch(true);
        friendsListSearchView.setCursorDrawable(R.drawable.color_cursor_white);




        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference();

        FriendsListRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsListRef.keepSynced(true);

        UserFriendsListReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserFriendsListReference.keepSynced(true);

        UserFriendsListReferenceInFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        UserFriendsListReferenceInFriends.keepSynced(true);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(online_user_id);

        UserGroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        onlineUserNameTitle = findViewById(R.id.create_group_user_custom_profile_name);

        onlineUserFriendsList = findViewById(R.id.create_group_user_custom_profile_friends);

        CreateGroupUsingSelectedMembersButton = findViewById(R.id.all_friends_activity_fab);

        SelectedFriendsLinearLayout = findViewById(R.id.selected_friends_linear_layout);

        FriendsRef.child("Users").child(online_user_id).addValueEventListener(new ValueEventListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String userName = dataSnapshot.child("user_name").getValue().toString();

                onlineUserNameTitle.setText(userName+" Friends");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });

        init();

        GroupRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                countGroups.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    countGroups.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        UserFriendsListReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserFriendsKey.clear();
                FriendsDate.clear();
                friendsCount.clear();

                for (DataSnapshot single: dataSnapshot.getChildren())
                {

                    UserFriendsKey.add(single.getKey());

                    friendsCount.add(single.getKey());

                    FriendsDate.add(single.child("date").getValue().toString());
                }
                allUserFriendsListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });



        friendsListSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                SearchForPeopleAndFriends(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                SearchForPeopleAndFriends(newText);

                return true;
            }
        });

        friendsListSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener()
        {
            @Override
            public void onSearchViewShown()
            {
                UserFriendsListReference.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        UserFriendsKey.clear();
                        FriendsDate.clear();
                        friendsCount.clear();

                        for (DataSnapshot single: dataSnapshot.getChildren())
                        {

                            UserFriendsKey.add(single.getKey());

                            friendsCount.add(single.getKey());

                            FriendsDate.add(single.child("date").getValue().toString());
                        }
                        allUserFriendsListAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                });
            }

            @Override
            public void onSearchViewClosed()
            {

                UserFriendsListReference.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        UserFriendsKey.clear();
                        FriendsDate.clear();
                        friendsCount.clear();

                        for (DataSnapshot single: dataSnapshot.getChildren())
                        {

                            UserFriendsKey.add(single.getKey());

                            friendsCount.add(single.getKey());

                            FriendsDate.add(single.child("date").getValue().toString());
                        }
                        allUserFriendsListAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                    }
                });
            }
        });





        CreateGroupUsingSelectedMembersButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectedFriendsToSend = new ArrayList<>();

                selectedFriendsToSend.addAll(allUserFriendsListAdapter.selectedKeys);

                if (selectedFriendsToSend.size() > 0)
                {
                    final Bundle bundle = new Bundle();
                    final Bundle bundle1 = new Bundle();
                    final Bundle bundle2 = new Bundle();

                    bundle.putStringArrayList("selected_friends",selectedFriendsToSend);
                    bundle1.putStringArrayList("friends_count",friendsCount);
                    bundle2.putStringArrayList("groups_count",countGroups);

                    Intent createGroupIntent = new Intent(FriendsListActivity.this,CreateNewGroupActivity.class);
                    createGroupIntent.putExtras(bundle);
                    createGroupIntent.putExtras(bundle1);
                    createGroupIntent.putExtras(bundle2);
                    createGroupIntent.putExtra("admin_user_id",online_user_id);
                    startActivity(createGroupIntent);


                }
                else
                {
                    Toast.makeText(FriendsListActivity.this, "Please select atleast one member", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void init()
    {
        FriendsListRecyclerView = findViewById(R.id.friends_list_recycler_view);
        FriendsListRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsListActivity.this, LinearLayoutManager.VERTICAL,false));
        FriendsListRecyclerView
                .addItemDecoration(new HorizontalDividerItemDecoration.Builder(FriendsListActivity.this)
                        .color(Color.BLACK)
                        .margin(125, 0)
                        .build());
        allUserFriendsListAdapter = new AllUserFriendsListAdapter(UserFriendsKey,FriendsDate,FriendsListActivity.this);
        FriendsListRecyclerView.setAdapter(allUserFriendsListAdapter);


        SelectedFriendsRecyclerView = findViewById(R.id.selected_friends_recycler_view);
        SelectedFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsListActivity.this, LinearLayoutManager.HORIZONTAL,false));
        selectedUserToCreateGroup = new SelectedUserToCreateGroup(FriendsListActivity.this,allUserFriendsListAdapter.SelectedUserKeyWithPostion);
        SelectedFriendsRecyclerView.setAdapter(selectedUserToCreateGroup);


    }

    private void SearchForPeopleAndFriends(String searchUserName)
    {
        if (allUserFriendsListAdapter.selectedKeys.size() == 0)
        {
            Toast.makeText(FriendsListActivity.this,"Searching..", LENGTH_SHORT).show();

            Query searchPeopleAndFriendsToLowerCase = UserFriendsListReference.orderByChild("receiver_name")

                    .startAt(searchUserName).endAt(searchUserName + "\uf8ff");

            searchPeopleAndFriendsToLowerCase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    UserFriendsKey.clear();
                    FriendsDate.clear();
                    friendsCount.clear();

                    for (DataSnapshot single: dataSnapshot.getChildren())
                    {

                        UserFriendsKey.add(single.getKey());

                        friendsCount.add(single.getKey());

                        FriendsDate.add(single.child("date").getValue().toString());
                    }
                    allUserFriendsListAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                }
            });
        }


    }

    private class AllUserFriendsListAdapter extends RecyclerView.Adapter<AllUserFriendsListAdapter.UserFriendsListViewHolder>
    {
        ArrayList<String> UserFriendKey = new ArrayList<>();
        ArrayList<String> selectedKeys  = new ArrayList<>();
        ArrayList<String> FriendsDate = new ArrayList<>();

        HashMap<Integer,String> SelectedUserKeyWithPostion = new HashMap<>();

        Context context;

        FriendsListActivity friendsListActivity;

        AllUserFriendsListAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
        {
            UserFriendKey = UserFriendsKey;
            this.FriendsDate = FriendsDate;
            this.context = context;
            friendsListActivity = (FriendsListActivity) context;
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
            final String friendSinceDate = "Friends Since:\n"+ "    " + FriendsDate.get(position);



            FriendsListRef.child(userFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String friendName = (String) dataSnapshot.child("user_name").getValue();
                    final String friendThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String user_friend_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();



                    holder.userName.setText(friendName);
                    holder.setUser_thumb_img(FriendsListActivity.this,friendThumbImage);
                    holder.userFriendSinceDate.setText(friendSinceDate);

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


                    holder.itemView.setOnClickListener(new View.OnClickListener()
                    {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(View v)
                        {
                            Toast.makeText(context, Integer.toString(position), Toast.LENGTH_SHORT).show();

                            if (holder.selectUserToAddToGroupCheckBox.isChecked())
                            {
                                counter = counter - 1;

                                updateCounter(counter);

                                selectedKeys.remove(UserFriendsKey.get(position));


                                SelectedUserKeyWithPostion.remove(position,UserFriendsKey.get(position));


                                holder.selectUserToAddToGroupCheckBox.setChecked(false);

                                if (selectedKeys.size() == 0)
                                {
                                    SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                    SelectedFriendsRecyclerView.setVisibility(View.GONE);

                                    selectedUserToCreateGroup.selectedUsersKeys.clear();

                                    SelectedUserKeyWithPostion.clear();

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

                                selectedKeys.add(UserFriendsKey.get(position));

                                SelectedUserKeyWithPostion.put(position,UserFriendsKey.get(position));

                                holder.selectUserToAddToGroupCheckBox.setChecked(true);

                                if (selectedKeys.size() == 0)
                                {
                                    SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                    SelectedFriendsRecyclerView.setVisibility(View.GONE);

                                    selectedUserToCreateGroup.selectedUsersKeys.clear();

                                    SelectedUserKeyWithPostion.clear();
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

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

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

            FriendsListActivity friendsListActivity;

            UserFriendsListViewHolder(View itemView,FriendsListActivity friendsListActivity)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    friendsListActivity.prepareSelection(v,getAdapterPosition());
                }

                getPos(getAdapterPosition());
            }
        }

    }

    private void getPos(int adapterPosition)
    {
        this.adapterPosition = adapterPosition;
    }


    @Override
    public void onClick(View v)
    {
        is_in_action_mode = true;

        allUserFriendsListAdapter.notifyDataSetChanged();

        selectedUserToCreateGroup.notifyDataSetChanged();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void prepareSelection(View view, int position)
    {
        if (((CheckBox)view).isChecked())
        {
            allUserFriendsListAdapter.selectedKeys.add(UserFriendsKey.get(position));

            allUserFriendsListAdapter.SelectedUserKeyWithPostion.put(position,UserFriendsKey.get(position));

            SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

            SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

            counter = counter + 1;

            updateCounter(counter);

            if (allUserFriendsListAdapter.selectedKeys.size() == 0)
            {
                SelectedFriendsLinearLayout.setVisibility(View.GONE);

                SelectedFriendsRecyclerView.setVisibility(View.GONE);

                selectedUserToCreateGroup.selectedUsersKeys.clear();

                allUserFriendsListAdapter.SelectedUserKeyWithPostion.clear();

                selectedUserToCreateGroup.notifyDataSetChanged();
            }
            else
            {
                SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                selectedUserToCreateGroup.notifyDataSetChanged();
            }

        }
        else
        {
            allUserFriendsListAdapter.selectedKeys.remove(UserFriendsKey.get(position));

            allUserFriendsListAdapter.SelectedUserKeyWithPostion.remove(position,UserFriendsKey.get(position));

            selectedUserToCreateGroup.notifyDataSetChanged();

            counter = counter - 1;

            updateCounter(counter);

            if (allUserFriendsListAdapter.selectedKeys.size() == 0)
            {
                SelectedFriendsLinearLayout.setVisibility(View.GONE);

                SelectedFriendsRecyclerView.setVisibility(View.GONE);

                selectedUserToCreateGroup.selectedUsersKeys.clear();

                allUserFriendsListAdapter.SelectedUserKeyWithPostion.clear();

                selectedUserToCreateGroup.notifyDataSetChanged();
            }
            else
            {
                SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                selectedUserToCreateGroup.notifyDataSetChanged();
            }

        }

    }

    @SuppressLint("SetTextI18n")
    public void updateCounter(int counter)
    {
        if (counter == 0)
        {
            onlineUserFriendsList.setText("Add Members");
        }
        else
        {
            onlineUserFriendsList.setText(counter+ " of " + friendsCount.size() + " selected");
        }
    }



    public class SelectedUserToCreateGroup extends RecyclerView.Adapter<SelectedUserToCreateGroup.SelectedUserToCreateGroupViewHolder>
    {

        @SuppressLint("UseSparseArrays")
        HashMap<Integer,String> selectedUsersKeys = new HashMap<>();
        Context c;

        SelectedUserToCreateGroup(Context c,  HashMap<Integer,String> selectedUsersKeys)
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
            final String selectedUserkey = selectedUsersKeys.get(position);

            FriendsListRef.child(selectedUserkey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String userName = (String) dataSnapshot.child("user_name").getValue();
                    final String userThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();

                    holder.selectedUsername.setText(userName);
                    holder.setUser_thumb_img(FriendsListActivity.this,userThumbImage);

                   /* holder.v.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            allUserFriendsListAdapter.selectedKeys.remove(UserFriendsKey.get(adapterPosition));

                            Toast.makeText(c, Integer.toString(position), Toast.LENGTH_SHORT).show();

                            selectedUserToCreateGroup.notifyDataSetChanged();

                            counter = counter - 1;

                            updateCounter(counter);

                            if (allUserFriendsListAdapter.selectedKeys.size() == 0)
                            {
                                SelectedFriendsLinearLayout.setVisibility(View.GONE);

                                SelectedFriendsRecyclerView.setVisibility(View.GONE);

                                selectedUserToCreateGroup.notifyDataSetChanged();
                            }
                            else
                            {
                                SelectedFriendsLinearLayout.setVisibility(View.VISIBLE);

                                SelectedFriendsRecyclerView.setVisibility(View.VISIBLE);

                                selectedUserToCreateGroup.notifyDataSetChanged();
                            }
                        }
                    });*/

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
            return selectedUsersKeys.size();
        }

        class SelectedUserToCreateGroupViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView selectedUsername;

            ImageView cancelSelectedButton;

            SelectedUserToCreateGroupViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                selectedUsername = itemView.findViewById(R.id.selected_user_name_to_create_group);
                cancelSelectedButton = itemView.findViewById(R.id.cancel_selected_user_button);
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

    @Override
    public void onBackPressed()
    {
        if (friendsListSearchView.isSearchOpen())
        {
            friendsListSearchView.closeSearch();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK)
        {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0)
            {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd))
                {
                    friendsListSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.friends_list_menu, menu);

        MenuItem item = menu.findItem(R.id.action_friends_list_search);

        friendsListSearchView.setMenuItem(item);

        return true;
    }


}