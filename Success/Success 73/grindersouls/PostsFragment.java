package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.opengles.GL;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class PostsFragment extends Fragment
{

    private ArrayList<String> Friends = new ArrayList<>();

    private ArrayList<String> Posts = new ArrayList<>();

    private ArrayList<String> FriendsWithPosts = new ArrayList<>();

    private AllPostAdapter allPostsAdapter;

    public PostsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String onlineUserId = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        friendsReference.keepSynced(true);

        final DatabaseReference friendsPostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        friendsPostsReference.keepSynced(true);

        friendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Friends.clear();

                if (Friends.size() == 0)
                {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Friends.add(snapshot.getKey());
                    }
                }
                else
                {
                    Friends.clear();

                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Friends.add(snapshot.getKey());
                    }
                }

                Posts.clear();
                FriendsWithPosts.clear();

                for (final String member : Friends)
                {
                    friendsPostsReference.child(member).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Posts.add(snapshot.getKey());
                                FriendsWithPosts.add(member);
                            }
                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }

                allPostsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        RecyclerView friendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        friendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        allPostsAdapter = new AllPostAdapter(getContext(),Posts,FriendsWithPosts);
        friendsPostsRecyclerView.setAdapter(allPostsAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }
}
