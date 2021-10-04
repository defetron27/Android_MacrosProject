package com.deffe.macros.soulsspot;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment
{
    private View ChatUserFriendView;

    private RecyclerView OnlineFriendRecyclerView,OfflineFriendRecyclerView;

    private String online_user_id;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference UserFriendReference,FriendsReference,UserFriendReferenceInFriends;

    private ArrayList<String> UserFriendsKey = new ArrayList<>();

    private ArrayList<String> FriendsDate = new ArrayList<>();

    private ArrayList<String> OnlineUsersKey = new ArrayList<>();

    private ArrayList<String> OfflineUsersKey = new ArrayList<>();

    private AllUserFriendsOnlineAdapter allUserFriendsOnlineAdapter;

    private AllUserFriendsOfflineAdapter allUserFriendsOfflineAdapter;

    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ChatUserFriendView = inflater.inflate(R.layout.fragment_chats, container, false);

        return ChatUserFriendView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.ChatUserFriendView = view;


        online();

        offline();

        loadData();
    }

    private void online()
    {
        OnlineFriendRecyclerView = (RecyclerView) ChatUserFriendView.findViewById(R.id.online_friends);
        OnlineFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        allUserFriendsOnlineAdapter = new AllUserFriendsOnlineAdapter(UserFriendsKey,FriendsDate,getActivity());
        OnlineFriendRecyclerView.setAdapter(allUserFriendsOnlineAdapter);
    }

    private void offline()
    {
        OfflineFriendRecyclerView = (RecyclerView) ChatUserFriendView.findViewById(R.id.offline_friends);
        OfflineFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        OfflineFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allUserFriendsOfflineAdapter = new AllUserFriendsOfflineAdapter(UserFriendsKey,FriendsDate,getActivity());
        OfflineFriendRecyclerView.setAdapter(allUserFriendsOfflineAdapter);
    }

    private void loadData()
    {
        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UserFriendReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UserFriendReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsReference.keepSynced(true);

        UserFriendReferenceInFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        UserFriendReferenceInFriends.keepSynced(true);

        FriendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserFriendsKey.clear();
                FriendsDate.clear();

                for (DataSnapshot single: dataSnapshot.getChildren())
                {

                    UserFriendsKey.add(single.getKey());

                    FriendsDate.add(single.child("date").getValue().toString());
                }
                allUserFriendsOnlineAdapter.notifyDataSetChanged();
                allUserFriendsOfflineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });
    }

    private class AllUserFriendsOnlineAdapter extends RecyclerView.Adapter<AllUserFriendsOnlineAdapter.UserFriendOnlineViewHolder>
    {
        ArrayList<String> UserFriendKey = new ArrayList<>();
        ArrayList<String> FriendsDate;
        Context context;

        AllUserFriendsOnlineAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
        {
            UserFriendKey = UserFriendsKey;
            this.FriendsDate = FriendsDate;
            this.context = context;
        }

        @NonNull
        @Override
        public UserFriendOnlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.online_chats_users_recycler_view_items,parent,false);

            return new UserFriendOnlineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserFriendOnlineViewHolder holder, int position)
        {

            final String userFriendId = UserFriendKey.get(position);
            final String friendSinceDate = "Friends Since\n"+ "    " + FriendsDate.get(position);

            UserFriendReference.child(userFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String onlineName = (String) dataSnapshot.child("user_name").getValue();
                    final String onlineThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String online_user_friend_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    if(dataSnapshot.hasChild("online"))
                    {
                        String online_status = dataSnapshot.child("online").getValue().toString();

                        if (online_status.equals("true"))
                        {
                            holder.onlineUserName.setVisibility(View.VISIBLE);

                            holder.setUserOnline(online_status,getContext(),onlineThumbImage);

                            holder.online_Status.setVisibility(View.VISIBLE);

                            holder.onlineUserName.setText(onlineName);

                        }
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
            return UserFriendKey.size();
        }

        class UserFriendOnlineViewHolder extends RecyclerView.ViewHolder
        {

            View v;

            TextView onlineUserName;

            CircleImageView online_Status;

            UserFriendOnlineViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                onlineUserName = (TextView) itemView.findViewById(R.id.online_chat_user_name);


                online_Status = (CircleImageView) itemView.findViewById(R.id.online_user_chat_online_status);
            }

            public void setUserOnline(String online_status,final Context c, final String user_thumb_img)
            {

                if(online_status.equals("true"))
                {
                    final CircleImageView thumb_img = (CircleImageView) v.findViewById(R.id.online_user_chat_friend_image);

                    thumb_img.setVisibility(View.VISIBLE);

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

    private class AllUserFriendsOfflineAdapter extends RecyclerView.Adapter<AllUserFriendsOfflineAdapter.UserFriendOfflineViewHolder>
    {
        ArrayList<String> UserFriendKey = new ArrayList<>();
        ArrayList<String> FriendsDate;
        Context context;

        AllUserFriendsOfflineAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
        {
            UserFriendKey = UserFriendsKey;
            this.FriendsDate = FriendsDate;
            this.context = context;
        }

        @NonNull
        @Override
        public UserFriendOfflineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_chats_users_recycler_view_items,parent,false);

            return new UserFriendOfflineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserFriendOfflineViewHolder holder, int position)
        {

            final String userFriendId = UserFriendKey.get(position);

            UserFriendReference.child(userFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String offlineName = (String) dataSnapshot.child("user_name").getValue();
                    final String offlineThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String online_user_friend_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    if(dataSnapshot.hasChild("online"))
                    {
                        String online_status = dataSnapshot.child("online").getValue().toString();

                        if (!online_status.equals("true"))
                        {
                            holder.offlineUserName.setVisibility(View.VISIBLE);
                            holder.lastSeenTime.setVisibility(View.VISIBLE);

                            LastSeenTime getTime = new LastSeenTime();

                            long last_seen = Long.parseLong(online_status);

                            String lastSeenDisplayTime = getTime.getTimeAgo(last_seen,getContext()).toString();


                            holder.offlineUserName.setText(offlineName);
                            holder.lastSeenTime.setText(lastSeenDisplayTime);
                            holder.setUserOffline(online_status,getContext(),offlineThumbImage);
                        }
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
            return UserFriendKey.size();
        }

        class UserFriendOfflineViewHolder extends RecyclerView.ViewHolder
        {

            View v;

            TextView offlineUserName,lastSeenTime;

            UserFriendOfflineViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                offlineUserName = (TextView) itemView.findViewById(R.id.chat_user_offline_name);
                lastSeenTime = (TextView) itemView.findViewById(R.id.chat_user_offline_last_seen_time);

            }

            public void setUserOffline(String online_status,final Context c, final String user_thumb_img)
            {

                if(!online_status.equals("true"))
                {
                    final CircleImageView thumb_img = (CircleImageView) v.findViewById(R.id.chat_user_offline_image);

                    thumb_img.setVisibility(View.VISIBLE);

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


}
