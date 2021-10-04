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


public class PostsFragment extends Fragment
{

    private RecyclerView FriendsPostsRecyclerView;

    private DatabaseReference FriendsPostsReference,FriendsReference,UploadUserReference;

    private FirebaseAuth firebaseAuth;

    private String OnlineUserId;

    private ArrayList<String> Friends = new ArrayList<>();

    private ArrayList<String> Posts = new ArrayList<>();

    private ArrayList<String> FriendsKey = new ArrayList<>();

//    private AllPostsAdapter allPostsAdapter;


    public PostsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        OnlineUserId = firebaseAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(OnlineUserId);
        FriendsReference.keepSynced(true);

        FriendsPostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        FriendsPostsReference.keepSynced(true);

        UploadUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UploadUserReference.keepSynced(true);

      /*  FriendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Friends.clear();
                Posts.clear();
                FriendsKey.clear();

                for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Friends.add(snapshot.getKey());
                }


                for (final String member : Friends)
                {
                    FriendsPostsReference.child(member).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot snapshot1 : dataSnapshot.getChildren())
                            {
                                Posts.add(snapshot1.getKey());
                                FriendsKey.add(member);
                            }

                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/

        /*FriendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        FriendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        allPostsAdapter = new AllPostsAdapter(getContext(),Posts,FriendsKey);
        FriendsPostsRecyclerView.setAdapter(allPostsAdapter);
*/
        return view;
    }

/*
    private class AllPostsAdapter extends RecyclerView.Adapter<AllPostsAdapter.AllPostsViewHolder>
    {
        Context context;

        ArrayList<String> Posts;

        ArrayList<String> FriendsKey;

        AllPostsAdapter(Context context, ArrayList<String> Posts, ArrayList<String> FriendsKey)
        {
            this.context = context;
            this.Posts = Posts;
            this.FriendsKey = FriendsKey;
        }

        @NonNull
        @Override
        public AllPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_image_items,parent,false);

            return new AllPostsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllPostsViewHolder holder, int position)
        {
            final String key = FriendsKey.get(position);
            final String postKey = Posts.get(position);
            UploadUserReference.child(key).addValueEventListener(new ValueEventListener()
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
            FriendsPostsReference.child(key).child(postKey).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String img = dataSnapshot.child("posted_image").getValue().toString();
                    String time = dataSnapshot.child("uploaded_time").getValue().toString();

                    long upload_time = Long.parseLong(time);

                    String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                    holder.setUser_Uploaded_img(context,img);

                    holder.postUploadTime.setText(postUploadedTime);
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
            return FriendsKey.size();
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
    }*/

}
