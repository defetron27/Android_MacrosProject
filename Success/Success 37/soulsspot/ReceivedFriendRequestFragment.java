package com.deffe.macros.soulsspot;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class ReceivedFriendRequestFragment extends Fragment
{
    private View ReceivedFriendRequestView;

    private String online_user_id,online_user_name;

    private DatabaseReference ReceivedFriendRequestUserReference;
    private DatabaseReference FriendsRequestReference;
    private DatabaseReference FriendsReference;
    private DatabaseReference CustomFriendsReference,friendsRequestsReference;

    private ArrayList<String> UserReceivedFriendRequestToFriendKey = new ArrayList<>();

    private AllReceivedFriendRequestsUsersAdapter allReceivedFriendRequestsUsersAdapter;

    private ArrayList<String> FRIENDSCOUNT = new ArrayList<>();

    public ReceivedFriendRequestFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ReceivedFriendRequestView = inflater.inflate(R.layout.fragment_received_friend_request, container, false);

        return ReceivedFriendRequestView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.ReceivedFriendRequestView = view;


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        friendsRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);
        friendsRequestsReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        CustomFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        CustomFriendsReference.keepSynced(true);

        FriendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendsRequestReference.keepSynced(true);

        ReceivedFriendRequestUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ReceivedFriendRequestUserReference.keepSynced(true);


        init();

        loadData();
    }


    private void loadData()
    {

        ReceivedFriendRequestUserReference.child(online_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                online_user_name = (String) dataSnapshot.child("user_name").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        friendsRequestsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserReceivedFriendRequestToFriendKey.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String requestType = (String) snapshot.child("request_type").getValue();

                    if (requestType != null && requestType.equals("received"))
                    {
                        UserReceivedFriendRequestToFriendKey.add(snapshot.getKey());
                    }

                }
                allReceivedFriendRequestsUsersAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

    private void init()
    {
        RecyclerView receivedFriendRequestRecyclerView = ReceivedFriendRequestView.findViewById(R.id.received_friend_request_recycler_view);
        receivedFriendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        receivedFriendRequestRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allReceivedFriendRequestsUsersAdapter = new AllReceivedFriendRequestsUsersAdapter(UserReceivedFriendRequestToFriendKey,getActivity());
        receivedFriendRequestRecyclerView.setAdapter(allReceivedFriendRequestsUsersAdapter);

    }


    private class AllReceivedFriendRequestsUsersAdapter extends RecyclerView.Adapter<AllReceivedFriendRequestsUsersAdapter.ReceivedFriendRequestUsersViewHolder>
    {

        ArrayList<String> UserReceivedFriendRequestToFriendKey = new ArrayList<>();
        Context context;

        AllReceivedFriendRequestsUsersAdapter(ArrayList<String> UserReceivedFriendRequestToFriendKey, Context context)
        {
            this.UserReceivedFriendRequestToFriendKey = UserReceivedFriendRequestToFriendKey;
            this.context = context;
        }

        @NonNull
        @Override
        public ReceivedFriendRequestUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_friend_request_items,parent,false);

            return new ReceivedFriendRequestUsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ReceivedFriendRequestUsersViewHolder holder, int position)
        {
            final String userReceivedFriendRequestUserId = UserReceivedFriendRequestToFriendKey.get(position);

            ReceivedFriendRequestUserReference.child(userReceivedFriendRequestUserId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String userSenderName = (String) dataSnapshot.child("user_name").getValue();
                    final String userSenderImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String sender_user_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.userName.setText(userSenderName);
                    holder.setUser_thumb_img(getContext(),userSenderImage);

                    holder.acceptFriendRequestButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Calendar calFordATE = Calendar.getInstance();
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                            if (sender_user_unique_id != null)
                            {
                                FriendsReference.child(online_user_id).child(sender_user_unique_id).child("date").setValue(saveCurrentDate)
                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                FriendsReference.child(sender_user_unique_id).child(online_user_id).child("date").setValue(saveCurrentDate)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                                        {
                                                            @Override
                                                            public void onSuccess(Void aVoid)
                                                            {
                                                                FriendsRequestReference.child(online_user_id).child(sender_user_unique_id).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    FriendsRequestReference.child(sender_user_unique_id).child(online_user_id).removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        Toast.makeText(getContext(), "Friend Request Accept Successfully", Toast.LENGTH_SHORT).show();

                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                            }

                        }
                    });

                    holder.rejectFriendRequestButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (sender_user_unique_id != null) {
                                FriendsRequestReference.child(online_user_id).child(sender_user_unique_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    FriendsRequestReference.child(sender_user_unique_id).child(online_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful()) {

                                                                        Toast.makeText(getContext(), userSenderName+" friend request has been rejected", Toast.LENGTH_SHORT).show();

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

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return UserReceivedFriendRequestToFriendKey.size();
        }

        class ReceivedFriendRequestUsersViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName;

            Button acceptFriendRequestButton;
            Button rejectFriendRequestButton;

            LinearLayout acceptOrRejectLinearLayout;

            ReceivedFriendRequestUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.received_friend_request_user_name);
                acceptFriendRequestButton = v.findViewById(R.id.request_received_accept_button);
                rejectFriendRequestButton = v.findViewById(R.id.request_received_decline_button);

                acceptOrRejectLinearLayout = v.findViewById(R.id.request_received_accept_or_reject_linear_layout);

            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.received_friend_request_user_image);

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
