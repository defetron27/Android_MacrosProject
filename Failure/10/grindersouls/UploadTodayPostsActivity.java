package com.deffe.macros.grindersouls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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

public class UploadTodayPostsActivity extends AppCompatActivity
{
    private DatabaseReference UploadPostReference,UploadUserReference;
    private FirebaseAuth firebaseAuth;

    private String online_user_id;

    private ArrayList<String> CountUploadedPosts = new ArrayList<>();

    private String NextPostPosition;

    private AllPostsRecyclerViewAdapter allPostsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_today_posts);

        Toolbar uploadTodayPostToolbar = findViewById(R.id.upload_today_posts_toolbar);
        setSupportActionBar(uploadTodayPostToolbar);
        getSupportActionBar().setTitle("Upload today post");

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        UploadPostReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);
        UploadPostReference.keepSynced(true);

        UploadUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        UploadUserReference.keepSynced(true);


        UploadPostReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                CountUploadedPosts.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if (!snapshot.getKey().equals("empty_post"))
                    {
                        CountUploadedPosts.add(snapshot.getKey());
                    }
                }

                for (int i=0; i<CountUploadedPosts.size(); i++)
                {
                    int number = Integer.valueOf(CountUploadedPosts.get(i));

                    if (number != i)
                    {
                        NextPostPosition = String.valueOf(number);

                        break;
                    }
                }

                if (NextPostPosition == null)
                {
                    NextPostPosition = String.valueOf(CountUploadedPosts.size());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        RecyclerView UploadTodayPostRecyclerView = findViewById(R.id.upload_today_post_recycler_view);
        UploadTodayPostRecyclerView.setLayoutManager(new LinearLayoutManager(UploadTodayPostsActivity.this, LinearLayoutManager.VERTICAL,false));
        UploadTodayPostRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(UploadTodayPostsActivity.this).build());
        allPostsRecyclerViewAdapter = new AllPostsRecyclerViewAdapter(NextPostPosition);
    }

    private class AllPostsRecyclerViewAdapter extends RecyclerView.Adapter<AllPostsRecyclerViewAdapter.PostsViewHolder>
    {
        String NextPostPosition;

        AllPostsRecyclerViewAdapter(String nextPostPosition)
        {
            NextPostPosition = nextPostPosition;
        }

        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_today_post_items,parent,false);

            return new PostsViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull final PostsViewHolder holder, int position)
        {
            UploadUserReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String name = dataSnapshot.child("user_name").getValue().toString();
                    String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                    holder.onlineUserName.setText(name);
                    holder.setUser_thumb_img(UploadTodayPostsActivity.this,thumbImage);
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
            return 0;
        }

        class PostsViewHolder extends RecyclerView.ViewHolder
        {
            TextView onlineUserName;

            TextView postUploadTime;

            View v;

            PostsViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                onlineUserName = v.findViewById(R.id.upload_post_online_user_name);

                postUploadTime = v.findViewById(R.id.uploaded_post_online_user_time);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.upload_post_online_user_image);

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
