package com.deffe.macros.soulsspot;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SendFriendRequestFragment extends Fragment
{
    private View SendFriendRequestView;

    private RecyclerView SendFriendRequestRecyclerView;

    private String online_user_id;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference FriendsRequestsReference,SendFriendRequestUserReference,FriendsRequestReference;

    private  ArrayList<String> UserSendFriendRequestToFriendKey = new ArrayList<>();

    private AllSendFriendRequestsUsersAdapter allSendFriendRequestsUsersAdapter;

    public SendFriendRequestFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        SendFriendRequestView = inflater.inflate(R.layout.fragment_send_friend_request, container, false);

        return SendFriendRequestView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.SendFriendRequestView = view;

        init();

        loadData();
    }

    private void loadData()
    {
        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        FriendsRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);
        FriendsRequestsReference.keepSynced(true);

        FriendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendsRequestReference.keepSynced(true);

        SendFriendRequestUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        SendFriendRequestUserReference.keepSynced(true);

        FriendsRequestsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                UserSendFriendRequestToFriendKey.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String requestType = snapshot.child("request_type").getValue().toString();

                    if (requestType.equals("sent"))
                    {
                        UserSendFriendRequestToFriendKey.add(snapshot.getKey());
                    }

                }
                allSendFriendRequestsUsersAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

    private void init()
    {
        SendFriendRequestRecyclerView = (RecyclerView) SendFriendRequestView.findViewById(R.id.send_friend_request_recycler_view);
        SendFriendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        SendFriendRequestRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allSendFriendRequestsUsersAdapter = new AllSendFriendRequestsUsersAdapter(UserSendFriendRequestToFriendKey,getActivity());
        SendFriendRequestRecyclerView.setAdapter(allSendFriendRequestsUsersAdapter);

    }


    private class AllSendFriendRequestsUsersAdapter extends RecyclerView.Adapter<AllSendFriendRequestsUsersAdapter.SendFriendRequestUsersViewHolder>
    {

        ArrayList<String> UserSendFriendRequestToFriendKey = new ArrayList<>();
        Context context;

        public AllSendFriendRequestsUsersAdapter(ArrayList<String> UserSendFriendRequestToFriendKey, Context context)
        {
            this.UserSendFriendRequestToFriendKey = UserSendFriendRequestToFriendKey;
            this.context = context;
        }

        @NonNull
        @Override
        public SendFriendRequestUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_friend_request_items,parent,false);

            return new SendFriendRequestUsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final SendFriendRequestUsersViewHolder holder, int position)
        {
            final String userSendFriendRequestUserId = UserSendFriendRequestToFriendKey.get(position);

            SendFriendRequestUserReference.child(userSendFriendRequestUserId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String userRequestReceiverName = (String) dataSnapshot.child("user_name").getValue();
                    final String userRequestReceiverImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String receiver_user_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    holder.userName.setText(userRequestReceiverName);
                    holder.setUser_thumb_img(getContext(), userRequestReceiverImage);

                    holder.undoFriendRequest.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            FriendsRequestReference.child(online_user_id).child(receiver_user_unique_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                FriendsRequestReference.child(receiver_user_unique_id).child(online_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    Toast.makeText(getContext(), "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
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
            return UserSendFriendRequestToFriendKey.size();
        }

        public class SendFriendRequestUsersViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,undoFriendRequest;

            public SendFriendRequestUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = (TextView) v.findViewById(R.id.send_friend_request_user_name);
                undoFriendRequest = (TextView) v.findViewById(R.id.send_friend_request_undo_text_view);
            }
            public void setUser_thumb_img(final Context c,final String user_thumb_img)
            {
                final CircleImageView thumb_img = (CircleImageView) v.findViewById(R.id.send_friend_request_user_image);

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