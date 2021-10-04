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
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class PostsFragment extends Fragment
{

    private RecyclerView FriendsPostsRecyclerView;

    private DatabaseReference FriendsPostsReference,FriendsReference,UploadUserReference;

    private FirebaseAuth firebaseAuth;

    private String OnlineUserId;

    private ArrayList<String> Friends = new ArrayList<>();

    private ArrayList<MultiRecyclerModel> Posts = new ArrayList<>();

    private AllPostsAdapter allPostsAdapter;

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

        FriendsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Friends.clear();
                Posts.clear();

                for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Friends.add(snapshot.getKey());
                }

                for (final String member : Friends)
                {
                    FriendsPostsReference.child(member).addChildEventListener(new ChildEventListener()
                    {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s)
                        {
                            MultiRecyclerModel model = dataSnapshot.getValue(MultiRecyclerModel.class);

                            Posts.add(model);

                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s)
                        {
                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot)
                        {
                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s)
                        {
                            allPostsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            allPostsAdapter.notifyDataSetChanged();

                            Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FriendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        FriendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        allPostsAdapter = new AllPostsAdapter(getContext(),Posts);
        FriendsPostsRecyclerView.setAdapter(allPostsAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class AllPostsAdapter extends RecyclerView.Adapter
    {
        Context context;

        ArrayList<MultiRecyclerModel> Posts;

        AllPostsAdapter(Context context, ArrayList<MultiRecyclerModel> Posts)
        {
            this.context = context;
            this.Posts = Posts;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view;

            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_image_items, parent, false);

                return new PostImageTypeViewHolder(view);
            } else if (viewType == 1) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_post_video_items, parent, false);

                return new PostVideoTypeViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position)
        {
            final MultiRecyclerModel object = Posts.get(position);

            if (object != null)
            {
                switch (object.getType())
                {
                    case 0:

                        UploadUserReference.child(object.getPost_user_key()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                                ((PostImageTypeViewHolder) holder).ImageOnlineUserName.setText(name);
                                ((PostImageTypeViewHolder) holder).setUser_Image_thumb_img(context, thumbImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                long upload_time = object.getUploaded_time();

                                String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                                final String result = getImageLocalStorageRef(context, object.getPost_key());

                                if (result == null)
                                {
                                    ((PostImageTypeViewHolder) holder).ImageOnlineUserName.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).ImagePostUploadTime.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).uploaded_img.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).thumb_img.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).ImageLine1.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).ImageLine2.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).ThumbsUp.setVisibility(View.GONE);
                                    ((PostImageTypeViewHolder) holder).ThumbsDown.setVisibility(View.GONE);

                                    StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getPost());

                                    final File localFile = new File(Environment.getExternalStorageDirectory()
                                            + "/" + "GrindersSouls/Grinders Posts", "Images");

                                    if (!localFile.exists())
                                    {
                                        localFile.mkdirs();
                                    }

                                    final File downloadFile = new File(localFile, "IMG_" + System.currentTimeMillis() + ".jpg");

                                    downloadRef.getFile(downloadFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                                    {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                        {
                                            Toast.makeText(context, "file downloaded", Toast.LENGTH_SHORT).show();

                                            Uri file = Uri.fromFile(downloadFile);

                                            addImagePostRefInStorage(context, object.getPost_key(), file.toString());

                                            ((PostImageTypeViewHolder) holder).ImageOnlineUserName.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).ImagePostUploadTime.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).uploaded_img.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).thumb_img.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).ImageLine1.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).ImageLine2.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).ThumbsUp.setVisibility(View.VISIBLE);
                                            ((PostImageTypeViewHolder) holder).ThumbsDown.setVisibility(View.VISIBLE);

                                            ((PostImageTypeViewHolder) holder).ThumbsUp.setIcon(IconType.ThumbsUp);
                                            ((PostImageTypeViewHolder) holder).ThumbsDown.setIcon(IconType.ThumbDown);
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else
                                {
                                    final String uri = getImageLocalStorageRef(context, object.getPost_key());

                                    ((PostImageTypeViewHolder) holder).ImageOnlineUserName.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).ImagePostUploadTime.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).uploaded_img.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).thumb_img.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).ImageLine1.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).ImageLine2.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).ThumbsUp.setVisibility(View.VISIBLE);
                                    ((PostImageTypeViewHolder) holder).ThumbsDown.setVisibility(View.VISIBLE);

                                    ((PostImageTypeViewHolder) holder).ThumbsUp.setIcon(IconType.ThumbsUp);
                                    ((PostImageTypeViewHolder) holder).ThumbsDown.setIcon(IconType.ThumbDown);

                                    ((PostImageTypeViewHolder) holder).ImagePostUploadTime.setText(postUploadedTime);

                                    Picasso.with(context).load(uri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                                            .into(((PostImageTypeViewHolder) holder).uploaded_img, new Callback()
                                            {
                                                @Override
                                                public void onSuccess()
                                                {

                                                }

                                                @Override
                                                public void onError()
                                                {
                                                    Picasso.with(context).load(uri).placeholder(R.drawable.vadim).into(((PostImageTypeViewHolder) holder).uploaded_img);
                                                }
                                            });

                                    ((PostImageTypeViewHolder) holder).uploaded_img.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent imageIntent = new Intent(context,PostImageActivity.class);
                                            imageIntent.putExtra("imageUri",uri);
                                            startActivity(imageIntent);
                                        }
                                    });
                                }

                                ((PostImageTypeViewHolder) holder).RemovePostImage.setVisibility(View.GONE);

                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("likes").addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild(OnlineUserId))
                                        {
                                            ((PostImageTypeViewHolder) holder).ThumbsUp.setLiked(true);
                                        }
                                        else
                                        {
                                            ((PostImageTypeViewHolder) holder).ThumbsUp.setLiked(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError)
                                    {

                                    }
                                });
                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("dislikes").addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild(OnlineUserId))
                                        {
                                            ((PostImageTypeViewHolder) holder).ThumbsDown.setLiked(true);
                                        }
                                        else
                                        {
                                            ((PostImageTypeViewHolder) holder).ThumbsDown.setLiked(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError)
                                    {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });



                        ((PostImageTypeViewHolder) holder).ThumbsUp.setOnLikeListener(new OnLikeListener()
                        {
                            @Override
                            public void liked(LikeButton likeButton)
                            {
                                Calendar calFordATE = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
                                final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                                Map<String,Object> likes = new HashMap<>();

                                likes.put(OnlineUserId,saveCurrentDate);

                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("likes").updateChildren(likes)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    if (((PostImageTypeViewHolder) holder).ThumbsDown.isLiked())
                                                    {
                                                        FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("dislikes").child(OnlineUserId).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            ((PostImageTypeViewHolder) holder).ThumbsDown.setLiked(false);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                    }
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void unLiked(LikeButton likeButton)
                            {
                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("likes").child(OnlineUserId).removeValue()
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

                        ((PostImageTypeViewHolder) holder).ThumbsDown.setOnLikeListener(new OnLikeListener()
                        {
                            @Override
                            public void liked(LikeButton likeButton)
                            {
                                Calendar calFordATE = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
                                final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                                Map<String,Object> likes = new HashMap<>();

                                likes.put(OnlineUserId,saveCurrentDate);

                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("dislikes").updateChildren(likes)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    if (((PostImageTypeViewHolder) holder).ThumbsUp.isLiked())
                                                    {
                                                        FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("likes").child(OnlineUserId).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            ((PostImageTypeViewHolder) holder).ThumbsUp.setLiked(false);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void unLiked(LikeButton likeButton)
                            {
                                FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).child("dislikes").child(OnlineUserId).removeValue()
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(context, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

                        break;
                    case 1:

                        UploadUserReference.child(object.getPost_user_key()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String thumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                                ((PostVideoTypeViewHolder) holder).VideoOnlineUserName.setText(name);
                                ((PostVideoTypeViewHolder) holder).setUser_Video_thumb_img(context, thumbImage);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsPostsReference.child(object.getPost_user_key()).child(object.getPost_key()).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                long upload_time = object.getUploaded_time();

                                String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                                GlideApp.with(context).load(object.getRef()).centerCrop().into(((PostVideoTypeViewHolder) holder).videoThumbImage);

                                String downloaded = VideoPlayActivity.getVideoLocalStorageRef(context,object.getPost_key());

                                if (downloaded == null)
                                {
                                    ((PostVideoTypeViewHolder) holder).VideoPlayButton.setVisibility(View.GONE);

                                    ((PostVideoTypeViewHolder) holder).DownloadVideo.setVisibility(View.VISIBLE);

                                    ((PostVideoTypeViewHolder) holder).DownloadVideo.setText(object.getSize());
                                }
                                else
                                {
                                    ((PostVideoTypeViewHolder) holder).VideoPlayButton.setVisibility(View.VISIBLE);

                                    ((PostVideoTypeViewHolder) holder).DownloadVideo.setVisibility(View.GONE);
                                }

                                ((PostVideoTypeViewHolder) holder).videoThumbImage.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent videoIntent = new Intent(context,VideoPlayActivity.class);
                                        videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                        videoIntent.putExtra("purpose","download");
                                        videoIntent.putExtra("key",object.getPost_key());
                                        videoIntent.putExtra("videoUri", object.getPost());
                                        startActivity(videoIntent);
                                    }
                                });

                                ((PostVideoTypeViewHolder) holder).VideoPostUploadTime.setText(postUploadedTime);

                                ((PostVideoTypeViewHolder) holder).RemoveVideoPost.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        break;
                }
            }
        }

        private void updateLike(String post_user_key, String post_key, String addLike)
        {

        }

        private void updateDislike(String post_user_key, String post_key, String addDislike)
        {
            FriendsPostsReference.child(post_user_key).child(post_key).child("dislikes").setValue(addDislike);
        }

        @Override
        public int getItemViewType(int position)
        {
            int i = Posts.get(position).getType();

            switch (i) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                default:
                    return -1;
            }
        }

        @Override
        public int getItemCount()
        {
            return Posts.size();
        }

        class PostImageTypeViewHolder extends RecyclerView.ViewHolder
        {
            ImageView uploaded_img;

            TextView ImageOnlineUserName;

            TextView ImagePostUploadTime;

            ImageButton RemovePostImage;

            CircleImageView thumb_img;

            TextView ImageLine1,ImageLine2;

            TextView PostImageDescription;

            LikeButton ThumbsUp,ThumbsDown,Heart;

            View v;

            PostImageTypeViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                ImageOnlineUserName = v.findViewById(R.id.image_post_user_name);

                ImagePostUploadTime = v.findViewById(R.id.image_post_user_time);

                uploaded_img = v.findViewById(R.id.image_post_image_view);

                RemovePostImage = v.findViewById(R.id.image_post_remove_button);

                ImageLine1 = v.findViewById(R.id.image_line1);

                ImageLine2 = v.findViewById(R.id.image_line2);

                ThumbsUp = v.findViewById(R.id.post_image_thumbs_up);
                ThumbsDown = v.findViewById(R.id.post_image_thumbs_down);
                Heart = v.findViewById(R.id.post_image_heart);

            }

            void setUser_Image_thumb_img(final Context context, final String user_thumb_img) {

                thumb_img = v.findViewById(R.id.image_post_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
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

        class PostVideoTypeViewHolder extends RecyclerView.ViewHolder {
            TextView VideoOnlineUserName;

            TextView VideoPostUploadTime;

            View v;

            ImageView videoThumbImage;

            ImageButton RemoveVideoPost;

            TextView DownloadVideo;

            ImageView VideoPlayButton;

            PostVideoTypeViewHolder(View itemView) {
                super(itemView);

                v = itemView;

                VideoOnlineUserName = v.findViewById(R.id.video_post_user_name);

                VideoPostUploadTime = v.findViewById(R.id.video_post_user_time);

                videoThumbImage = v.findViewById(R.id.video_thumb_view);

                RemoveVideoPost = v.findViewById(R.id.video_post_remove_button);

                DownloadVideo = v.findViewById(R.id.download_video_with_size);

                VideoPlayButton = v.findViewById(R.id.video_play_button);
            }

            void setUser_Video_thumb_img(final Context context, final String user_thumb_img) {
                final CircleImageView thumb_img = v.findViewById(R.id.video_post_user_image);

                Picasso.with(context).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
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
    }

    public static String getImageLocalStorageRef(Context context, String key)
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

}
