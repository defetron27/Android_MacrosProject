package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.opengles.GL;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class PostsFragment extends Fragment {

    private ArrayList<String> Friends = new ArrayList<>();

    private ArrayList<String> Posts = new ArrayList<>();

    private ArrayList<String> FriendsWithPosts = new ArrayList<>();

    private AllPostAdapter allPostsAdapter;

    private RecyclerView friendsPostsRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManagerCompat;

    private static final String POST_VIDEO_DOWNLOAD_CHANNEL_ID = "com.deffe.macros.grindersouls";
    private static final int POST_VIDEO_DOWNLOAD_ID = (int) ((new Date().getTime() / 100L) % Integer.MAX_VALUE);

    private boolean isUploading = false;

    public PostsFragment()
    {
        // Required empty public constructor
    }

    private boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;

        if (connectivityManager != null)
        {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_post_fragment);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String onlineUserId = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        friendsReference.keepSynced(true);

        final DatabaseReference friendsPostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        friendsPostsReference.keepSynced(true);

        friendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
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
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Posts.add(snapshot.getKey());
                                FriendsWithPosts.add(member);
                            }
                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }

                allPostsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        friendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        friendsPostsRecyclerView.setHasFixedSize(true);
        friendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        allPostsAdapter = new AllPostAdapter(getContext(),Posts,FriendsWithPosts);
        friendsPostsRecyclerView.setAdapter(allPostsAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshPage();
                    }
                },2000);
            }
        });

        return view;
    }

    private void refreshPage()
    {
        allPostsAdapter = new AllPostAdapter(getContext(),Posts,FriendsWithPosts);
        friendsPostsRecyclerView.setAdapter(allPostsAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    public class AllPostAdapter extends RecyclerView.Adapter<AllPostAdapter.PostViewHolder>
    {

        Context context;

        private ArrayList<String> Posts;
        private ArrayList<String> FriendsWithPosts;

        AllPostAdapter(Context context, ArrayList<String> posts, ArrayList<String> friendsWithPosts)
        {
            this.context = context;
            Posts = posts;
            FriendsWithPosts = friendsWithPosts;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_items, parent, false);

            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PostViewHolder holder, int position)
        {
            final String key = Posts.get(position);
            final String friend = FriendsWithPosts.get(position);

            final DatabaseReference FriendsPostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
            FriendsPostsReference.keepSynced(true);

            final DatabaseReference UploadUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
            UploadUserReference.keepSynced(true);

            FriendsPostsReference.child(friend).child(key).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                {
                    holder.PostRemoveButton.setVisibility(View.GONE);

                    UploadUserReference.child(friend).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            String name = snapshot.child("user_name").getValue().toString();
                            String thumbImage = snapshot.child("user_thumb_img").getValue().toString();

                            holder.PostedUserName.setText(name);
                            holder.setPostedUserImage(context, thumbImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });

                    long upload_time = (long) dataSnapshot.child("uploaded_time").getValue();
                    String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                    holder.PostedTime.setText(postUploadedTime);

                    String type = dataSnapshot.child("type").getValue().toString();

                    if (type.equals("0"))
                    {
                        final String result = getImageLocalStorageRef(context, key);

                        if (result == null)
                        {
                            if (isConnected(context))
                            {
                                holder.PostedUserName.setVisibility(View.GONE);
                                holder.PostedTime.setVisibility(View.GONE);
                                holder.PostLine1.setVisibility(View.GONE);
                                holder.PostLine2.setVisibility(View.GONE);
                                holder.PostSize.setVisibility(View.GONE);
                                holder.PostDescription.setVisibility(View.GONE);
                                holder.PostImageView.setVisibility(View.GONE);
                                holder.PostImagePlayButton.setVisibility(View.GONE);
                                holder.PostSharingButton.setVisibility(View.GONE);
                                holder.PostedUserImage.setVisibility(View.GONE);

                                StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("post").getValue().toString());

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/" + "GrindersSouls/Grinders Posts", "Images");

                                if (!localFile.exists())
                                {
                                    localFile.mkdirs();
                                }

                                final File downloadFile = new File(localFile, "IMG_" + System.currentTimeMillis() + ".jpg");

                                downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            Uri file = Uri.fromFile(downloadFile);

                                            addImagePostRefInStorage(context, key, file.toString());

                                            holder.PostSize.setVisibility(View.GONE);
                                            holder.PostImagePlayButton.setVisibility(View.GONE);

                                            holder.PostSharingButton.setVisibility(View.GONE);

                                            holder.PostedUserName.setVisibility(View.VISIBLE);
                                            holder.PostedTime.setVisibility(View.VISIBLE);
                                            holder.PostLine1.setVisibility(View.VISIBLE);
                                            holder.PostLine2.setVisibility(View.VISIBLE);
                                            holder.PostDescription.setVisibility(View.VISIBLE);
                                            holder.PostImageView.setVisibility(View.VISIBLE);
                                            holder.PostedUserImage.setVisibility(View.VISIBLE);

                                            holder.setImagePostImageView(context,file.toString());
                                        }
                                    }
                                });

                            }
                        }
                        else
                        {
                            holder.PostSize.setVisibility(View.GONE);
                            holder.PostImagePlayButton.setVisibility(View.GONE);

                            holder.PostSharingButton.setVisibility(View.GONE);

                            holder.PostedUserName.setVisibility(View.VISIBLE);
                            holder.PostedTime.setVisibility(View.VISIBLE);
                            holder.PostLine1.setVisibility(View.VISIBLE);
                            holder.PostLine2.setVisibility(View.VISIBLE);
                            holder.PostDescription.setVisibility(View.VISIBLE);
                            holder.PostImageView.setVisibility(View.VISIBLE);
                            holder.PostedUserImage.setVisibility(View.VISIBLE);

                            holder.setImagePostImageView(context,result);
                        }

                        holder.PostImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent imageIntent = new Intent(context,PostImageActivity.class);
                                imageIntent.putExtra("imageUri",dataSnapshot.child("ref").getValue().toString());
                                imageIntent.putExtra("purpose","view");
                                context.startActivity(imageIntent);
                            }
                        });

                  /*  holder.PostSharingButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent sharingIntent = new Intent(context,SharingActivity.class);
                            sharingIntent.setAction(Intent.ACTION_VIEW);
                            sharingIntent.setType("image");
                            sharingIntent.putExtra("uri",dataSnapshot.child("ref").getValue().toString());
                            context.startActivity(sharingIntent);
                        }
                    });*/
                    }
                    else if (type.equals("1"))
                    {
                        final String downloaded = getVideoLocalStorageRef(context,key);

                        if (downloaded == null)
                        {
                            GlideApp.with(context).load(dataSnapshot.child("post").getValue().toString()).centerCrop().into(holder.PostImageView);

                            holder.PostSize.setText(dataSnapshot.child("size").getValue().toString());

                            holder.PostImagePlayButton.setVisibility(View.GONE);

                            holder.PostSharingButton.setVisibility(View.GONE);

                            holder.PostedUserName.setVisibility(View.VISIBLE);
                            holder.PostedTime.setVisibility(View.VISIBLE);
                            holder.PostLine1.setVisibility(View.VISIBLE);
                            holder.PostLine2.setVisibility(View.VISIBLE);
                            holder.PostDescription.setVisibility(View.VISIBLE);
                            holder.PostImageView.setVisibility(View.VISIBLE);
                            holder.PostedUserImage.setVisibility(View.VISIBLE);
                            holder.PostSize.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            GlideApp.with(context).load(downloaded).centerCrop().into(holder.PostImageView);

                            holder.PostSize.setVisibility(View.GONE);

                            holder.PostSharingButton.setVisibility(View.GONE);

                            holder.PostImagePlayButton.setVisibility(View.VISIBLE);
                            holder.PostedUserName.setVisibility(View.VISIBLE);
                            holder.PostedTime.setVisibility(View.VISIBLE);
                            holder.PostLine1.setVisibility(View.VISIBLE);
                            holder.PostLine2.setVisibility(View.VISIBLE);
                            holder.PostDescription.setVisibility(View.VISIBLE);
                            holder.PostImageView.setVisibility(View.VISIBLE);
                            holder.PostedUserImage.setVisibility(View.VISIBLE);
                        }

                        holder.PostSize.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (isConnected(context))
                                {
                                    if (!isUploading)
                                    {
                                        isUploading = true;

                                        notificationManagerCompat = NotificationManagerCompat.from(context);
                                        builder = new NotificationCompat.Builder(context,POST_VIDEO_DOWNLOAD_CHANNEL_ID);
                                        builder.setContentTitle("Post Video Download")
                                                .setSmallIcon(R.drawable.notifcation_upload)
                                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                                .setOngoing(false)
                                                .setAutoCancel(true);

                                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("post").getValue().toString());

                                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                                + "/" + "GrindersSouls/Grinders Posts","Videos");

                                        if (!localFile.exists())
                                        {
                                            localFile.mkdirs();
                                        }

                                        final File downloadFile = new File(localFile,"VID_" + System.currentTimeMillis()  + ".mp4");

                                        downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                            {
                                                builder.setContentText("Upload Completed").setProgress(0, 0, false);
                                                notificationManagerCompat.notify(POST_VIDEO_DOWNLOAD_ID, builder.build());

                                                if (task.isSuccessful())
                                                {
                                                    Toast.makeText(context, "file downloaded", Toast.LENGTH_SHORT).show();

                                                    Uri file = Uri.fromFile(downloadFile);

                                                    addVideoPostRefInStorage(context,key,file.toString());

                                                    notificationManagerCompat.cancel(POST_VIDEO_DOWNLOAD_ID);
                                                    builder = null;

                                                    isUploading = false;

                                                    holder.PostSize.setVisibility(View.GONE);
                                                    holder.PostImagePlayButton.setVisibility(View.VISIBLE);

                                                    holder.PostImagePlayButton.setOnClickListener(new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                            if (downloaded != null)
                                                            {
                                                                Intent videoIntent = new Intent(context,VideoPlayActivity.class);
                                                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                                                videoIntent.putExtra("purpose","play");
                                                                videoIntent.putExtra("videoUri", downloaded);
                                                                context.startActivity(videoIntent);
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(context, "Please refresh one time", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                builder.setContentTitle("Uploading failed").setOngoing(false);
                                                notificationManagerCompat.notify(POST_VIDEO_DOWNLOAD_ID, builder.build());

                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot)
                                            {
                                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                                                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                                                notificationManagerCompat.notify(POST_VIDEO_DOWNLOAD_ID, builder.build());
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Please wait another downloading is process..!", Toast.LENGTH_LONG).show();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();

                                    if (isUploading)
                                    {
                                        notificationManagerCompat.cancel(POST_VIDEO_DOWNLOAD_ID);
                                        builder = null;

                                        isUploading = false;
                                    }
                                }
                            }
                        });

                        holder.PostImagePlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (downloaded != null)
                                {
                                    Intent videoIntent = new Intent(context,VideoPlayActivity.class);
                                    videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                    videoIntent.putExtra("purpose","play");
                                    videoIntent.putExtra("videoUri", downloaded);
                                    context.startActivity(videoIntent);
                                }
                            }
                        });

                 /*   holder.PostSharingButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent sharingIntent = new Intent(context,SharingActivity.class);
                            sharingIntent.setAction(Intent.ACTION_VIEW);
                            sharingIntent.setType("video");
                            sharingIntent.putExtra("uri",dataSnapshot.child("ref").getValue().toString());
                            context.startActivity(sharingIntent);
                        }
                    });*/
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return Posts.size();
        }

        class PostViewHolder extends RecyclerView.ViewHolder
        {
            TextView PostedUserName,PostedTime,PostLine1,PostLine2,PostSize,PostDescription;
            ImageButton PostRemoveButton,PostSharingButton;
            ImageView PostImageView,PostImagePlayButton;
            CircleImageView PostedUserImage;

            View v;

            PostViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                PostedUserName = v.findViewById(R.id.posted_user_name);
                PostedTime = v.findViewById(R.id.posted_time);
                PostLine1 = v.findViewById(R.id.post_line1);
                PostLine2 = v.findViewById(R.id.post_line2);
                PostSize = v.findViewById(R.id.post_size);
                PostDescription = v.findViewById(R.id.post_description);
                PostRemoveButton = v.findViewById(R.id.post_remove_button);
                PostSharingButton = v.findViewById(R.id.post_sharing_button);
                PostImageView = v.findViewById(R.id.post_image_view);
                PostImagePlayButton = v.findViewById(R.id.post_image_play_button);

                PostedUserImage = v.findViewById(R.id.posted_user_image);
            }

            void setPostedUserImage(final Context context, final String user_thumb_img) {

                final CircleImageView thumb_img = v.findViewById(R.id.posted_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }

            void setImagePostImageView(final Context context, final String user_thumb_img)
            {

                final ImageView thumb_img = v.findViewById(R.id.post_image_view);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }
        }

        private String getStringSizeFromFile(long size)
        {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            float sizeKb = 1024.0f;
            float sizeMb = sizeKb * sizeKb;
            float sizeGb = sizeMb * sizeMb;
            float sizeTera = sizeGb * sizeGb;

            if (size < sizeMb)
            {
                return decimalFormat.format(size / sizeKb) + "Kb";
            }
            else if (size < sizeGb)
            {
                return decimalFormat.format(size / sizeMb) + "Mb";
            }
            else if (size < sizeTera)
            {
                return decimalFormat.format(size / sizeGb) + "Gb";
            }
            return "";
        }

        private String getImageLocalStorageRef(Context context, String key)
        {
            String get = null;
            String result;

            SharedPreferences postPreference = context.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

            if (postPreference != null)
            {
                get = postPreference.getString(key,null);
            }

            if (get == null)
            {
                result = null;
            }
            else
            {
                result = get;
            }

            return result;
        }

        private void addImagePostRefInStorage(Context context,String key, String path)
        {
            SharedPreferences preferences = context.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

            if (preferences != null)
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(key,path);
                editor.apply();
            }
        }

        private String getVideoLocalStorageRef(Context context, String key)
        {
            String get = null;
            String result;

            SharedPreferences postPreference = context.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

            if (postPreference != null)
            {
                get = postPreference.getString(key,null);
            }

            if (get == null)
            {
                result = null;
            }
            else
            {
                result = get;
            }

            return result;
        }

        private void addVideoPostRefInStorage(Context context,String key, String path)
        {
            SharedPreferences preferences = context.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

            if (preferences != null)
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(key,path);
                editor.apply();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isUploading)
        {
            notificationManagerCompat.cancel(POST_VIDEO_DOWNLOAD_ID);
            builder = null;

            isUploading = false;
        }
    }
}
