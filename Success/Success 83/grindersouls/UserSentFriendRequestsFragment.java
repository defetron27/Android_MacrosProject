package com.deffe.macros.grindersouls;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserSentFriendRequestsFragment extends Fragment
{
    private final static String TAG = UserSentFriendRequestsFragment.class.getSimpleName();

    private View SendFriendRequestView;

    private String online_user_id;

    private ArrayList<String> userSendFriendRequestToFriendKey = new ArrayList<>();

    private ArrayList<String> userSendFriendRequestDate = new ArrayList<>();

    public UserSentFriendRequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        SendFriendRequestView = inflater.inflate(R.layout.fragment_user_sent_friend_requests, container, false);

        return SendFriendRequestView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.SendFriendRequestView = view;

        loadData();
    }

    private void loadData()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        CollectionReference friendsRequestsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("Sent");

        final RecyclerView sendFriendRequestRecyclerView = SendFriendRequestView.findViewById(R.id.send_friend_request_recycler_view);
        sendFriendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        sendFriendRequestRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());

        friendsRequestsReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }

                if (queryDocumentSnapshots != null)
                {
                    userSendFriendRequestToFriendKey.clear();
                    userSendFriendRequestDate.clear();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                    {
                        userSendFriendRequestToFriendKey.add(snapshot.getId());
                        userSendFriendRequestDate.add(snapshot.getString("time"));
                    }
                    AllSendFriendRequestsUsersAdapter allSendFriendRequestsUsersAdapter = new AllSendFriendRequestsUsersAdapter(userSendFriendRequestToFriendKey,userSendFriendRequestDate, getContext());
                    sendFriendRequestRecyclerView.setAdapter(allSendFriendRequestsUsersAdapter);
                }
            }
        });
    }

    private class AllSendFriendRequestsUsersAdapter extends RecyclerView.Adapter<AllSendFriendRequestsUsersAdapter.SendFriendRequestUsersViewHolder>
    {
        ArrayList<String> UserSendFriendRequestToFriendKey;
        ArrayList<String> UserSendFriendRequestDate;
        Context context;

        AllSendFriendRequestsUsersAdapter(ArrayList<String> UserSendFriendRequestToFriendKey,ArrayList<String> UserSendFriendRequestDate,Context context)
        {
            this.UserSendFriendRequestToFriendKey = UserSendFriendRequestToFriendKey;
            this.UserSendFriendRequestDate = UserSendFriendRequestDate;
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
            final String userSendDate = "On " + UserSendFriendRequestDate.get(position);

            DocumentReference sendFriendRef = FirebaseFirestore.getInstance().collection("Users").document(userSendFriendRequestUserId);
            final CollectionReference friendRequestRef =  FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts");

            sendFriendRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        final String userRequestReceiverName =  documentSnapshot.getString("user_name");
                        final String userRequestReceiverImage =  documentSnapshot.getString("user_thumb_img");
                        final String receiver_user_unique_id =  documentSnapshot.getString("user_unique_id");

                        holder.userName.setVisibility(View.VISIBLE);
                        holder.request_sent_to_friend_date.setVisibility(View.VISIBLE);
                        holder.undoFriendRequest.setVisibility(View.VISIBLE);
                        holder.send_friend_request_user_image.setVisibility(View.VISIBLE);

                        holder.userName.setText(userRequestReceiverName);
                        holder.setUser_thumb_img(getContext(), userRequestReceiverImage);
                        holder.request_sent_to_friend_date.setText(userSendDate);

                        holder.undoFriendRequest.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (receiver_user_unique_id != null)
                                {
                                    friendRequestRef.document(online_user_id).collection("Sent").document(receiver_user_unique_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.document(receiver_user_unique_id).collection("Received").document(online_user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            Toast.makeText(getContext(), "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener()
                                                {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {
                                                        Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG,e.toString());
                                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(getContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
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
        public int getItemCount()
        {
            return UserSendFriendRequestToFriendKey.size();
        }

        class SendFriendRequestUsersViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,undoFriendRequest,request_sent_to_friend_date;

            CircleImageView send_friend_request_user_image;

            SendFriendRequestUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.send_friend_request_user_name);
                undoFriendRequest = v.findViewById(R.id.send_friend_request_undo_text_view);
                request_sent_to_friend_date = v.findViewById(R.id.request_sent_to_friend_date);
                send_friend_request_user_image = v.findViewById(R.id.send_friend_request_user_image);
            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.send_friend_request_user_image);

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
