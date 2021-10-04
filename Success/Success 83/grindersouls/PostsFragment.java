package com.deffe.macros.grindersouls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.deffe.macros.grindersouls.Models.PostsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.content.Context.MODE_PRIVATE;


public class PostsFragment extends Fragment {

    private static final String TAG = PostsFragment.class.getSimpleName();

    private ArrayList<PostsModel> friendsPosts = new ArrayList<>();

    private AllPostAdapter allPostsAdapter;

    private RecyclerView friendsPostsRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManagerCompat;

    private static final String POST_VIDEO_DOWNLOAD_CHANNEL_ID = "com.deffe.macros.grindersouls.DOWNLOADING_VIDEO";
    private static final int POST_VIDEO_DOWNLOAD_ID = (int) ((new Date().getTime() / 100L) % Integer.MAX_VALUE);

    private boolean isUploading = false;

    private String onlineUserId;

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

        onlineUserId = firebaseAuth.getCurrentUser().getUid();

        CollectionReference postsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(onlineUserId).collection("Friends_Posts");

        friendsPostsRecyclerView = view.findViewById(R.id.friends_post_recycler_view);
        friendsPostsRecyclerView.setHasFixedSize(true);
        friendsPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        postsReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(getContext(), "Error while getting documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
                if (queryDocumentSnapshots != null)
                {
                    friendsPosts.clear();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                    {
                        PostsModel postsModel = snapshot.toObject(PostsModel.class);
                        friendsPosts.add(postsModel);
                    }

                    allPostsAdapter = new AllPostAdapter(getContext(),friendsPosts);
                    friendsPostsRecyclerView.setAdapter(allPostsAdapter);
                }
            }
        });

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
        allPostsAdapter = new AllPostAdapter(getContext(),friendsPosts);
        friendsPostsRecyclerView.setAdapter(allPostsAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    public class AllPostAdapter extends RecyclerView.Adapter<AllPostAdapter.PostViewHolder>
    {
        Context context;

        private ArrayList<PostsModel> Posts;

        private ArrayList<String> likes = new ArrayList<>();
        private ArrayList<String> dislikes = new ArrayList<>();
        private ArrayList<String> hearts = new ArrayList<>();

        AllPostAdapter(Context context, ArrayList<PostsModel> posts)
        {
            this.context = context;
            Posts = posts;
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
            final CollectionReference usersRef = FirebaseFirestore.getInstance().collection("Users");

            final PostsModel postsModel = Posts.get(position);

            holder.PostRemoveButton.setVisibility(View.GONE);

            usersRef.document(postsModel.getPost_user_key()).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        String name = documentSnapshot.getString("user_name");
                        String thumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.PostedUserName.setText(name);
                        holder.setPostedUserImage(context, thumbImage);
                    }
                }
            });

            usersRef.document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        String thumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.setCommentedUserImage(context, thumbImage);
                    }
                }
            });

            long upload_time = postsModel.getUploaded_time();
            String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

            holder.PostedTime.setText(postUploadedTime);

            if (postsModel.getType().equals("0"))
            {
                final String result = getImageLocalStorageRef(context, postsModel.getPost_key());

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
                        holder.PostThumbsUp.setVisibility(View.GONE);
                        holder.PostThumbsDown.setVisibility(View.GONE);
                        holder.PostHeart.setVisibility(View.GONE);
                        holder.PostLikesCount.setVisibility(View.GONE);
                        holder.PostDisLikesCount.setVisibility(View.GONE);
                        holder.PostHeartsCount.setVisibility(View.GONE);
                        holder.postCommentLinearLayout.setVisibility(View.GONE);
                        holder.thumb_img.setVisibility(View.GONE);
                        holder.editText.setVisibility(View.GONE);
                        holder.PostCommentRecyclerView.setVisibility(View.GONE);
                        holder.view.setVisibility(View.GONE);
                        holder.post_video_duration_linear_layout.setVisibility(View.GONE);

                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(postsModel.getPost());

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
                                    final Uri file = Uri.fromFile(downloadFile);

                                    addImagePostRefInStorage(context, postsModel.getPost_key(), file.toString());

                                    holder.PostSize.setVisibility(View.GONE);
                                    holder.PostImagePlayButton.setVisibility(View.GONE);
                                    holder.post_video_duration_linear_layout.setVisibility(View.GONE);

                                    holder.PostSharingButton.setVisibility(View.GONE);

                                    holder.PostedUserName.setVisibility(View.VISIBLE);
                                    holder.PostedTime.setVisibility(View.VISIBLE);
                                    holder.PostLine1.setVisibility(View.VISIBLE);
                                    holder.PostLine2.setVisibility(View.VISIBLE);
                                    holder.PostDescription.setVisibility(View.VISIBLE);
                                    holder.PostImageView.setVisibility(View.VISIBLE);
                                    holder.PostedUserImage.setVisibility(View.VISIBLE);
                                    holder.PostThumbsUp.setVisibility(View.VISIBLE);
                                    holder.PostThumbsDown.setVisibility(View.VISIBLE);
                                    holder.PostHeart.setVisibility(View.VISIBLE);
                                    holder.PostLikesCount.setVisibility(View.VISIBLE);
                                    holder.PostDisLikesCount.setVisibility(View.VISIBLE);
                                    holder.PostHeartsCount.setVisibility(View.VISIBLE);
                                    holder.postCommentLinearLayout.setVisibility(View.VISIBLE);
                                    holder.thumb_img.setVisibility(View.VISIBLE);
                                    holder.editText.setVisibility(View.VISIBLE);
                                    holder.PostCommentRecyclerView.setVisibility(View.VISIBLE);
                                    holder.view.setVisibility(View.VISIBLE);

                                    holder.setImagePostImageView(context,file.toString());

                                    holder.PostImageView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent imageIntent = new Intent(context,PostImageActivity.class);
                                            imageIntent.putExtra("imageUri",file.toString());
                                            imageIntent.putExtra("purpose","view");
                                            context.startActivity(imageIntent);
                                        }
                                    });
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
                    holder.post_video_duration_linear_layout.setVisibility(View.GONE);

                    holder.PostedUserName.setVisibility(View.VISIBLE);
                    holder.PostedTime.setVisibility(View.VISIBLE);
                    holder.PostLine1.setVisibility(View.VISIBLE);
                    holder.PostLine2.setVisibility(View.VISIBLE);
                    holder.PostDescription.setVisibility(View.VISIBLE);
                    holder.PostImageView.setVisibility(View.VISIBLE);
                    holder.PostedUserImage.setVisibility(View.VISIBLE);
                    holder.PostThumbsUp.setVisibility(View.VISIBLE);
                    holder.PostThumbsDown.setVisibility(View.VISIBLE);
                    holder.PostHeart.setVisibility(View.VISIBLE);
                    holder.PostLikesCount.setVisibility(View.VISIBLE);
                    holder.PostDisLikesCount.setVisibility(View.VISIBLE);
                    holder.PostHeartsCount.setVisibility(View.VISIBLE);
                    holder.postCommentLinearLayout.setVisibility(View.VISIBLE);
                    holder.thumb_img.setVisibility(View.VISIBLE);
                    holder.editText.setVisibility(View.VISIBLE);
                    holder.PostCommentRecyclerView.setVisibility(View.VISIBLE);
                    holder.view.setVisibility(View.VISIBLE);

                    holder.setImagePostImageView(context,result);

                    holder.PostImageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent imageIntent = new Intent(context,PostImageActivity.class);
                            imageIntent.putExtra("imageUri",result);
                            imageIntent.putExtra("purpose","view");
                            context.startActivity(imageIntent);
                        }
                    });
                }
            }
            else if (postsModel.getType().equals("1"))
            {
                final String downloaded = getVideoLocalStorageRef(context,postsModel.getPost_key());

                if (downloaded == null)
                {
                    GlideApp.with(context).load(postsModel.getVideo_thumbnail()).centerCrop().into(holder.PostImageView);

                    holder.PostSize.setText(postsModel.getSize());

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
                    holder.post_video_duration_linear_layout.setVisibility(View.VISIBLE);
                    holder.post_video_duration.setVisibility(View.VISIBLE);

                    holder.PostThumbsUp.setVisibility(View.VISIBLE);
                    holder.PostThumbsDown.setVisibility(View.VISIBLE);
                    holder.PostHeart.setVisibility(View.VISIBLE);
                    holder.PostLikesCount.setVisibility(View.VISIBLE);
                    holder.PostDisLikesCount.setVisibility(View.VISIBLE);
                    holder.PostHeartsCount.setVisibility(View.VISIBLE);
                    holder.postCommentLinearLayout.setVisibility(View.VISIBLE);
                    holder.thumb_img.setVisibility(View.VISIBLE);
                    holder.editText.setVisibility(View.VISIBLE);
                    holder.PostCommentRecyclerView.setVisibility(View.VISIBLE);
                    holder.view.setVisibility(View.VISIBLE);
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
                    holder.post_video_duration_linear_layout.setVisibility(View.VISIBLE);
                    holder.post_video_duration.setVisibility(View.VISIBLE);

                    holder.PostThumbsUp.setVisibility(View.VISIBLE);
                    holder.PostThumbsDown.setVisibility(View.VISIBLE);
                    holder.PostHeart.setVisibility(View.VISIBLE);
                    holder.PostLikesCount.setVisibility(View.VISIBLE);
                    holder.PostDisLikesCount.setVisibility(View.VISIBLE);
                    holder.PostHeartsCount.setVisibility(View.VISIBLE);
                    holder.postCommentLinearLayout.setVisibility(View.VISIBLE);
                    holder.thumb_img.setVisibility(View.VISIBLE);
                    holder.editText.setVisibility(View.VISIBLE);
                    holder.PostCommentRecyclerView.setVisibility(View.VISIBLE);
                    holder.view.setVisibility(View.VISIBLE);
                }

                holder.post_video_duration.setText(postsModel.getDuration());

                holder.PostSize.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isConnected(context))
                        {
                            if (!isUploading)
                            {
                                Toast.makeText(context, "Video Downloading Started Please wait...", Toast.LENGTH_LONG).show();

                                holder.postVideoDownloadingTextView.setVisibility(View.VISIBLE);
                                holder.PostSize.setVisibility(View.GONE);

                                isUploading = true;

                                notificationManagerCompat = NotificationManagerCompat.from(context);
                                builder = new NotificationCompat.Builder(context,POST_VIDEO_DOWNLOAD_CHANNEL_ID);
                                builder.setContentTitle("Post Video Download")
                                        .setSmallIcon(R.drawable.ic_file_download)
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setOngoing(false)
                                        .setAutoCancel(true);

                                StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(postsModel.getPost());

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
                                        builder.setContentText("Download Completed").setProgress(0, 0, false);
                                        notificationManagerCompat.notify(POST_VIDEO_DOWNLOAD_ID, builder.build());

                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(context, "Video Downloading Completed", Toast.LENGTH_SHORT).show();

                                            Uri file = Uri.fromFile(downloadFile);

                                            addVideoPostRefInStorage(context,postsModel.getPost_key(),file.toString());

                                            notificationManagerCompat.cancel(POST_VIDEO_DOWNLOAD_ID);
                                            builder = null;

                                            isUploading = false;

                                            holder.postVideoDownloadingTextView.setVisibility(View.GONE);
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
                                                        final String down = getVideoLocalStorageRef(context,postsModel.getPost_key());

                                                        Intent videoIntent = new Intent(context,VideoPlayActivity.class);
                                                        videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                                        videoIntent.putExtra("purpose","play");
                                                        videoIntent.putExtra("videoUri", down);
                                                        context.startActivity(videoIntent);
                                                    }
                                                }
                                            });
                                        }

                                        if (task.isCanceled())
                                        {
                                            Toast.makeText(context, "Downloading Cancelled", Toast.LENGTH_SHORT).show();

                                            isUploading = false;
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        builder.setContentTitle("Download failed").setOngoing(false);
                                        notificationManagerCompat.notify(POST_VIDEO_DOWNLOAD_ID, builder.build());

                                        isUploading = false;

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
            }

            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key());

            Query query = documentReference.collection("Comments").orderBy("time",Query.Direction.DESCENDING);

            query.addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    else
                    {
                        if (queryDocumentSnapshots != null)
                        {
                            ArrayList<Comments> comments = new ArrayList<>();

                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                            {
                                Comments comments1 = documentSnapshot.toObject(Comments.class);
                                comments.add(comments1);
                            }

                            CommentsCategory commentsCategory = new CommentsCategory(comments,"Comments",String.valueOf(comments.size()));

                            final ArrayList<CommentsCategory> commentsCategories = new ArrayList<>();

                            commentsCategories.add(commentsCategory);

                            holder.mAdapter = new CommentsCategoryAdapter(context, commentsCategories);
                            holder.PostCommentRecyclerView.setAdapter(holder.mAdapter);
                        }
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    else
                    {
                        if (queryDocumentSnapshots != null)
                        {
                            likes.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots)
                            {
                                likes.add(queryDocumentSnapshot.getId());
                            }

                            Integer like = likes.size();

                            final String addLike;

                            addLike = String.valueOf(like);

                            holder.PostLikesCount.setText(addLike);
                        }
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("DisLikes").addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    else
                    {
                        if (queryDocumentSnapshots != null)
                        {
                            dislikes.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots)
                            {
                                dislikes.add(queryDocumentSnapshot.getId());
                            }

                            Integer dislike = dislikes.size();

                            final String addDisLike;

                            addDisLike = String.valueOf(dislike);

                            holder.PostDisLikesCount.setText(addDisLike);
                        }
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Hearts").addSnapshotListener(new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                    else
                    {
                        if (queryDocumentSnapshots != null)
                        {
                            hearts.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots)
                            {
                                hearts.add(queryDocumentSnapshot.getId());
                            }

                            Integer dislike = hearts.size();

                            final String addHearts;

                            addHearts = String.valueOf(dislike);

                            holder.PostHeartsCount.setText(addHearts);
                        }
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Likes").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        holder.PostThumbsUp.setLiked(true);
                    }
                    else
                    {
                        holder.PostThumbsUp.setLiked(false);
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("DisLikes").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        holder.PostThumbsDown.setLiked(true);
                    }
                    else
                    {
                        holder.PostThumbsDown.setLiked(false);
                    }
                }
            });

            FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Hearts").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        holder.PostHeart.setLiked(true);
                    }
                    else
                    {
                        holder.PostHeart.setLiked(false);
                    }
                }
            });

            holder.PostThumbsUp.setOnLikeListener(new OnLikeListener()
            {
                @Override
                public void liked(LikeButton likeButton)
                {
                    Map<String, Object> like = new HashMap<>();
                    like.put("time", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Likes").document(onlineUserId).set(like).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                if (holder.PostThumbsDown.isLiked())
                                {
                                    holder.PostThumbsDown.setLiked(false);
                                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("DisLikes").document(onlineUserId).delete().addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(getContext(), "Error while change disliked", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton)
                {
                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Likes").document(onlineUserId).delete().addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }
            });

            holder.PostThumbsDown.setOnLikeListener(new OnLikeListener()
            {
                @Override
                public void liked(LikeButton likeButton)
                {
                    Map<String, Object> dislikes = new HashMap<>();
                    dislikes.put("time", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("DisLikes").document(onlineUserId).set(dislikes).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                if (holder.PostThumbsUp.isLiked())
                                {
                                    holder.PostThumbsUp.setLiked(false);
                                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Likes").document(onlineUserId).delete().addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(getContext(), "Error while change disliked", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton)
                {
                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("DisLikes").document(onlineUserId).delete().addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }
            });

            holder.PostHeart.setOnLikeListener(new OnLikeListener()
            {
                @Override
                public void liked(LikeButton likeButton)
                {
                    Map<String, Object> heart = new HashMap<>();
                    heart.put("time", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Hearts").document(onlineUserId).set(heart).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton)
                {
                    FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Hearts").document(onlineUserId).delete().addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getContext(), "Error while liked", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }
            });

            holder.editText.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    View promptsView = layoutInflater.inflate(R.layout.popup_edittext, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

                    alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            if (TextUtils.isEmpty(userInput.getText().toString()))
                            {
                                Toast.makeText(context, "Please enter any comment", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Map<String, Object> comment = new HashMap<>();
                                comment.put("comment_key",onlineUserId);
                                comment.put("time",FieldValue.serverTimestamp());
                                comment.put("comment",userInput.getText().toString());

                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Comments").document();

                                String newCommentId = documentReference.getId();

                                FirebaseFirestore.getInstance().collection("Posts_Likes_And_Dislikes").document(postsModel.getPost_key()).collection("Comments").document(newCommentId).set(comment).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(context, "Error while storing your comment " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                    }
                                });
                            }
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    alertDialog.show();
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
            TextView PostedUserName,PostedTime,PostLine1,PostLine2,PostSize,PostDescription,postVideoDownloadingTextView,post_video_duration;
            ImageButton PostRemoveButton,PostSharingButton;
            ImageView PostImageView,PostImagePlayButton;
            CircleImageView PostedUserImage;

            LinearLayout post_video_duration_linear_layout;

            LikeButton PostThumbsUp,PostThumbsDown,PostHeart;
            TextView PostLikesCount,PostDisLikesCount,PostHeartsCount;
            CommentsCategoryAdapter mAdapter;
            RecyclerView PostCommentRecyclerView;
            TextView editText;
            CircleImageView thumb_img;
            LinearLayout postCommentLinearLayout;

            View v,view;

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

                PostThumbsUp = v.findViewById(R.id.post_thumbs_up);
                PostThumbsUp.setIcon(IconType.ThumbsUp);

                PostThumbsDown = v.findViewById(R.id.post_thumbs_down);
                PostThumbsDown.setIcon(IconType.ThumbsDown);

                PostHeart = v.findViewById(R.id.post_heart);
                PostHeart.setIcon(IconType.Heart);

                PostLikesCount = v.findViewById(R.id.post_likes_count);

                PostDisLikesCount = v.findViewById(R.id.post_dislikes_count);

                PostHeartsCount = v.findViewById(R.id.post_hearts_count);

                PostCommentRecyclerView = v.findViewById(R.id.post_comments_recycler_view);
                PostCommentRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                thumb_img = v.findViewById(R.id.comment_user_image);

                editText = v.findViewById(R.id.post_comment_edit_text);

                postCommentLinearLayout = v.findViewById(R.id.post_likes_comments_area);

                view = v.findViewById(R.id.add_comment_underline);

                postVideoDownloadingTextView = v.findViewById(R.id.post_video_downloading_text_view);

                post_video_duration_linear_layout = v.findViewById(R.id.post_video_duration_linear_layout);

                post_video_duration = v.findViewById(R.id.post_video_duration);
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

            void setCommentedUserImage(final Context context, final String user_thumb_img) {

                final CircleImageView thumb_img = v.findViewById(R.id.comment_user_image);

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
