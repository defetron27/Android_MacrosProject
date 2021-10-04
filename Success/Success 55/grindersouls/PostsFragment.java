package com.deffe.macros.grindersouls;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostsFragment extends Fragment
{

    private RecyclerView FriendsPostsRecyclerView;

    private DatabaseReference FriendsPostsReference,FriendsReference,UploadUserReference;

    private FirebaseAuth firebaseAuth;

    private String OnlineUserId;

    private ArrayList<String> Posts = new ArrayList<>();

    private AllPostsAdapter allPostsAdapter;


    public PostsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        OnlineUserId = firebaseAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(OnlineUserId);
        FriendsReference.keepSynced(true);

        FriendsPostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        FriendsPostsReference.keepSynced(true);

        UploadUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UploadUserReference.keepSynced(true);

        FriendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Posts.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Posts.add(snapshot.getKey());
                }
                allPostsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        FriendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        FriendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(),LinearLayoutManager.VERTICAL,false));
        allPostsAdapter = new AllPostsAdapter(container.getContext());
        FriendsPostsRecyclerView.setAdapter(allPostsAdapter);

        return view;
    }

    private class AllPostsAdapter extends RecyclerView.Adapter<AllPostsAdapter.AllPostsViewHolder>
    {
        Context context;
        ArrayList<String> UploadedPosts = new ArrayList<>();

        AllPostsAdapter(Context context)
        {
            this.context = context;
        }

        @NonNull
        @Override
        public AllPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_today_post_items,parent,false);

            return new AllPostsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllPostsViewHolder holder, int position)
        {
            final String post = Posts.get(position);

            FriendsPostsReference.child(post).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    UploadedPosts.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        UploadedPosts.add(snapshot.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            for (int i=0; i<UploadedPosts.size(); i++)
            {
                
            }

            FriendsPostsReference.child(post).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    UploadedPosts.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        UploadedPosts.add(snapshot.getKey());

                        Toast.makeText(context, "Post " + snapshot.getKey(), Toast.LENGTH_SHORT).show();

                        UploadUserReference.child(post).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                                holder.onlineUserName.setText(name);
                                holder.setUser_thumb_img(context, thumbImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });


                        FriendsPostsReference.child(post).child(snapshot.getKey()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String img = dataSnapshot.child("posted_image").getValue().toString();

                                holder.setUser_Uploaded_img(context,img);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
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
            return Posts.size();
        }

        class AllPostsViewHolder extends RecyclerView.ViewHolder
        {
            TextView onlineUserName;

            TextView postUploadTime;

            View v;


            AllPostsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                onlineUserName = v.findViewById(R.id.upload_post_online_user_name);

                postUploadTime = v.findViewById(R.id.uploaded_post_online_user_time);

            }

            void setUser_thumb_img(final Context context, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.upload_post_online_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }

            void setUser_Uploaded_img(final Context context, final String user_thumb_img)
            {
                final ImageView uploaded_img = v.findViewById(R.id.upload_post_image_view);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(uploaded_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(uploaded_img);
                            }
                        });
            }
        }
    }

}
