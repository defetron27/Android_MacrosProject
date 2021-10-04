package com.deffe.macros.grindersouls;


import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserReceivedFriendRequestsFragment extends Fragment
{
    private final static String TAG = UserSentFriendRequestsFragment.class.getSimpleName();

    private View ReceivedFriendRequestView;

    private String online_user_id;

    private ArrayList<String> userReceivedFriendRequestToFriendKey = new ArrayList<>();
    private ArrayList<String> userReceivedFriendRequestDate = new ArrayList<>();

    private AllReceivedFriendRequestsUsersAdapter allReceivedFriendRequestsUsersAdapter;

    public UserReceivedFriendRequestsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ReceivedFriendRequestView = inflater.inflate(R.layout.fragment_user_received_friend_requests, container, false);

        return ReceivedFriendRequestView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        this.ReceivedFriendRequestView = view;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        CollectionReference friendsRequestsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("Received");

        final RecyclerView receivedFriendRequestRecyclerView = ReceivedFriendRequestView.findViewById(R.id.received_friend_request_recycler_view);
        receivedFriendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        receivedFriendRequestRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());

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
                    userReceivedFriendRequestToFriendKey.clear();
                    userReceivedFriendRequestDate.clear();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                    {
                        userReceivedFriendRequestToFriendKey.add(snapshot.getId());
                        userReceivedFriendRequestDate.add(snapshot.getString("time"));
                    }
                    allReceivedFriendRequestsUsersAdapter = new AllReceivedFriendRequestsUsersAdapter(userReceivedFriendRequestToFriendKey,userReceivedFriendRequestDate,getContext());
                    receivedFriendRequestRecyclerView.setAdapter(allReceivedFriendRequestsUsersAdapter);
                }
            }
        });
    }

    private class AllReceivedFriendRequestsUsersAdapter extends RecyclerView.Adapter<AllReceivedFriendRequestsUsersAdapter.ReceivedFriendRequestUsersViewHolder>
    {

        ArrayList<String> UserReceivedFriendRequestToFriendKey;
        ArrayList<String> UserReceivedFriendRequestDate;
        Context context;

        AllReceivedFriendRequestsUsersAdapter(ArrayList<String> UserReceivedFriendRequestToFriendKey, ArrayList<String> UserReceivedFriendRequestDate,Context context)
        {
            this.UserReceivedFriendRequestToFriendKey = UserReceivedFriendRequestToFriendKey;
            this.UserReceivedFriendRequestDate = UserReceivedFriendRequestDate;
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
            final String userReceivedDate = UserReceivedFriendRequestDate.get(position);

            DocumentReference sendFriendRef = FirebaseFirestore.getInstance().collection("Users").document(userReceivedFriendRequestUserId);
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
                        holder.request_sent_to_friend.setVisibility(View.VISIBLE);
                        holder.received_friend_request_user_image.setVisibility(View.VISIBLE);
                        holder.acceptOrRejectLinearLayout.setVisibility(View.VISIBLE);
                        holder.request_sent_to_friend_date.setVisibility(View.VISIBLE);

                        holder.userName.setText(userRequestReceiverName);
                        holder.setUser_thumb_img(getContext(), userRequestReceiverImage);
                        holder.request_sent_to_friend_date.setText(userReceivedDate);

                        holder.acceptFriendRequestButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (receiver_user_unique_id != null)
                                {
                                    Calendar calFordATE = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                    final String time = currentDate.format(calFordATE.getTime());
                                    final Map<String, Object> timeFriends = new HashMap<>();
                                    timeFriends.put("time", time);

                                    friendRequestRef.document(online_user_id).collection("Received").document(receiver_user_unique_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.document(receiver_user_unique_id).collection("Sent").document(online_user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            friendRequestRef.document(online_user_id).collection("Friends").document(receiver_user_unique_id).set(timeFriends).addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        friendRequestRef.document(receiver_user_unique_id).collection("Friends").document(online_user_id).set(timeFriends).addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show();
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

                        holder.rejectFriendRequestButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (receiver_user_unique_id != null)
                                {
                                    friendRequestRef.document(online_user_id).collection("Received").document(receiver_user_unique_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.document(receiver_user_unique_id).collection("Sent").document(online_user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            Toast.makeText(getContext(), "Friend Request Rejected", Toast.LENGTH_SHORT).show();
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
            return UserReceivedFriendRequestToFriendKey.size();
        }

        class ReceivedFriendRequestUsersViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,request_sent_to_friend,request_sent_to_friend_date;

            Button acceptFriendRequestButton;
            Button rejectFriendRequestButton;

            LinearLayout acceptOrRejectLinearLayout;

            CircleImageView received_friend_request_user_image;

            ReceivedFriendRequestUsersViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.received_friend_request_user_name);
                request_sent_to_friend = v.findViewById(R.id.request_sent_to_friend);
                acceptFriendRequestButton = v.findViewById(R.id.request_received_accept_button);
                rejectFriendRequestButton = v.findViewById(R.id.request_received_decline_button);
                request_sent_to_friend_date = v.findViewById(R.id.request_sent_to_friend_date);

                acceptOrRejectLinearLayout = v.findViewById(R.id.request_received_accept_or_reject_linear_layout);

                received_friend_request_user_image = v.findViewById(R.id.received_friend_request_user_image);

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
