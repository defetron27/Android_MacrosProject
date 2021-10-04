package com.deffe.macros.grindersouls;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class UserFriendsFragment extends Fragment
{
    private final static String TAG = UserFriendsFragment.class.getSimpleName();

    private View UserFriendView;

    private RecyclerView UserFriendRecyclerView;

    private String online_user_id;

    private CollectionReference UserFriendReference,friendsReference;

    private ArrayList<String> UserFriendsKey = new ArrayList<>();

    private ArrayList<String> FriendsDate = new ArrayList<>();

    private AllUserFriendsAdapter allUserFriendsAdapter;

    private TextView YouHaveNoFriends;

    private FloatingActionButton FriendsFab;

    public UserFriendsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        UserFriendView = inflater.inflate(R.layout.fragment_user_friends, container, false);

        return UserFriendView;
    }

    @Override
    public void onViewCreated(@NonNull View mainView, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(mainView, savedInstanceState);

        this.UserFriendView = mainView;

        YouHaveNoFriends = mainView.findViewById(R.id.you_have_no_friends);

        FriendsFab = mainView.findViewById(R.id.friends_fab);

        init();

        loadData();
    }

    private void init()
    {
        UserFriendRecyclerView = UserFriendView.findViewById(R.id.user_friend_recycler_view);
        UserFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        UserFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
    }

    private void loadData()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UserFriendReference = FirebaseFirestore.getInstance().collection("Users");

        friendsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts");

        friendsReference.document(online_user_id).collection("Friends").addSnapshotListener(new EventListener<QuerySnapshot>()
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
                if (queryDocumentSnapshots != null )
                {
                    UserFriendsKey.clear();
                    FriendsDate.clear();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                    {
                        UserFriendsKey.add(snapshot.getId());
                        FriendsDate.add(snapshot.getString("time"));
                    }

                    allUserFriendsAdapter = new AllUserFriendsAdapter(UserFriendsKey,FriendsDate,getActivity());
                    UserFriendRecyclerView.setAdapter(allUserFriendsAdapter);

                    if (UserFriendsKey.size() == 0)
                    {
                        YouHaveNoFriends.setVisibility(View.VISIBLE);

                        UserFriendRecyclerView.setVisibility(View.GONE);
                    }
                }
            }
        });

        FriendsFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent allFriendsIntent = new Intent(getContext(),AllUsersActivity.class);
                startActivity(allFriendsIntent);
            }
        });
    }

    private class AllUserFriendsAdapter extends RecyclerView.Adapter<AllUserFriendsAdapter.UserFriendsViewHolder>
    {
        ArrayList<String> UserFriendKey;
        ArrayList<String> FriendsDate;
        Context context;

        AllUserFriendsAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
        {
            UserFriendKey = UserFriendsKey;
            this.FriendsDate = FriendsDate;
            this.context = context;
        }

        @NonNull
        @Override
        public UserFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_friends_items_layout,parent,false);

            return new UserFriendsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserFriendsViewHolder holder, int position)
        {
            final String userFriendId = UserFriendKey.get(position);
            final String friendSinceDate = "Friends Since\n"+ "    " + FriendsDate.get(position);

            YouHaveNoFriends.setVisibility(View.INVISIBLE);

            UserFriendRecyclerView.setVisibility(View.VISIBLE);

            UserFriendReference.document(userFriendId).addSnapshotListener(new EventListener<DocumentSnapshot>()
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
                        final String friendName = documentSnapshot.getString("user_name");
                        final String friendThumbImage = documentSnapshot.getString("user_thumb_img");
                        final String user_friend_unique_id = documentSnapshot.getString("user_unique_id");

                        holder.userName.setVisibility(View.VISIBLE);
                        holder.unFriendButton.setVisibility(View.VISIBLE);
                        holder.user_friend_image.setVisibility(View.VISIBLE);
                        holder.userFriendSinceDate.setVisibility(View.VISIBLE);

                        if(documentSnapshot.contains("online"))
                        {
                            String online_status = documentSnapshot.get("online").toString();

                            holder.setUserOnline(online_status);
                        }

                        holder.userName.setText(friendName);
                        holder.setUser_thumb_img(context,friendThumbImage);
                        holder.userFriendSinceDate.setText(friendSinceDate);

                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                friendName + "'s Profile",
                                                "Chat with " + friendName,
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Choose Options");

                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if (which == 0)
                                        {
                                            Intent viewProfileIntent= new Intent(getContext(),ViewUserProfileActivity.class);
                                            viewProfileIntent.putExtra("visit_user_unique_id",user_friend_unique_id);
                                            startActivity(viewProfileIntent);
                                        }
                                        if (which == 1)
                                        {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",user_friend_unique_id);
                                            chatIntent.putExtra("user_name",friendName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();
                            }
                        });

                        holder.unFriendButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {

                                if (user_friend_unique_id != null)
                                {
                                    AlertDialog.Builder unFriendDialog = new AlertDialog.Builder(getContext());
                                    unFriendDialog.setTitle("Do you want to unfriend "+ friendName);

                                    unFriendDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            friendsReference.document(online_user_id).collection("Friends").document(user_friend_unique_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        friendsReference.document(user_friend_unique_id).collection("Friends").document(online_user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(getContext(), "UnFriend a person", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    unFriendDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    unFriendDialog.show();

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

        class UserFriendsViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,userFriendSinceDate;

            Button unFriendButton;

            CircleImageView user_friend_image;

            UserFriendsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = v.findViewById(R.id.user_friend_name);
                userFriendSinceDate = v.findViewById(R.id.user_friend_since_date);
                unFriendButton = v.findViewById(R.id.user_friend_unfriend_button);
                user_friend_image = v.findViewById(R.id.user_friend_image);

            }
            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.user_friend_image);

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

            void setUserOnline(String online_status)
            {
                ImageView onlineStatusView = v.findViewById(R.id.user_online_status);

                if(online_status.equals("true"))
                {
                    onlineStatusView.setVisibility(View.VISIBLE);
                }
                else
                {
                    onlineStatusView.setVisibility(View.GONE);
                }
            }

        }
    }
}
