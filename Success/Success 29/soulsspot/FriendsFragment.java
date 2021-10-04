package com.deffe.macros.soulsspot;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class FriendsFragment extends Fragment
{

    private View UserFriendView;

    private RecyclerView UserFriendRecyclerView;

    private String online_user_id;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference UserFriendReference,FriendsReference,UserFriendReferenceInFriends;

    private ArrayList<String> UserFriendsKey = new ArrayList<>();

    private ArrayList<String> FriendsDate = new ArrayList<>();

    private AllUserFriendsAdapter allUserFriendsAdapter;

    public FriendsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        UserFriendView = inflater.inflate(R.layout.fragment_friends, container, false);

        return UserFriendView;
    }

    @Override
    public void onViewCreated(@NonNull View mainView, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(mainView, savedInstanceState);

        this.UserFriendView = mainView;

        init();

        loadData();
    }

    private void init()
    {
        UserFriendRecyclerView = (RecyclerView) UserFriendView.findViewById(R.id.user_friend_recycler_view);
        UserFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        UserFriendRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        allUserFriendsAdapter = new AllUserFriendsAdapter(UserFriendsKey,FriendsDate,getActivity());
        UserFriendRecyclerView.setAdapter(allUserFriendsAdapter);
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
                allUserFriendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private class AllUserFriendsAdapter extends RecyclerView.Adapter<AllUserFriendsAdapter.UserFriendsViewHolder>
    {
        ArrayList<String> UserFriendKey = new ArrayList<>();
        ArrayList<String> FriendsDate;
        Context context;

        public AllUserFriendsAdapter(ArrayList<String> UserFriendsKey, ArrayList<String> FriendsDate, Context context)
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

            UserFriendReference.child(userFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String friendName = (String) dataSnapshot.child("user_name").getValue();
                    final String friendThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();
                    final String user_friend_unique_id = (String) dataSnapshot.child("user_unique_id").getValue();

                    if(dataSnapshot.hasChild("online"))
                    {
                        String online_status = dataSnapshot.child("online").getValue().toString();

                        holder.setUserOnline(online_status);
                    }



                    holder.userName.setText(friendName);
                    holder.setUser_thumb_img(getContext(),friendThumbImage);
                    holder.userFriendSinceDate.setText(friendSinceDate);

                    holder.itemView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Toast.makeText(context, "Onclick", Toast.LENGTH_SHORT).show();

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
                                        Intent viewProfileIntent= new Intent(getContext(),ViewProfileActivity.class);
                                        viewProfileIntent.putExtra("visit_user_unique_id",user_friend_unique_id);
                                        startActivity(viewProfileIntent);
                                    }
                                    if (which == 1)
                                    {
                                        Toast.makeText(context, "Chat", Toast.LENGTH_SHORT).show();
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

                                            UserFriendReferenceInFriends.child(online_user_id).child(user_friend_unique_id).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                UserFriendReferenceInFriends.child(user_friend_unique_id).child(online_user_id).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(), "UnFriend a person Successfully", Toast.LENGTH_SHORT).show();
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

        public class UserFriendsViewHolder extends RecyclerView.ViewHolder
        {
            View v;

            TextView userName,userFriendSinceDate;

            Button unFriendButton;

            public UserFriendsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                userName = (TextView) v.findViewById(R.id.user_friend_name);
                userFriendSinceDate = (TextView) v.findViewById(R.id.user_friend_since_date);
                unFriendButton = (Button) v.findViewById(R.id.user_friend_unfriend_button);

            }
            public void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = (CircleImageView) v.findViewById(R.id.user_friend_image);

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

            public void setUserOnline(String online_status)
            {
                ImageView onlineStatusView = (ImageView) v.findViewById(R.id.user_online_status);

                if(online_status.equals("true"))
                {
                    onlineStatusView.setVisibility(View.VISIBLE);
                }
                else
                {
                    onlineStatusView.setVisibility(View.INVISIBLE);
                }
            }

        }
    }
}
