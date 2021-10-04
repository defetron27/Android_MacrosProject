package com.deffe.macros.grindersouls;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class AllPostAdapter extends RecyclerView.Adapter<AllPostAdapter.PostViewHolder>
{

    Context context;

    ArrayList<String> Posts;
    ArrayList<String> FriendsWithPosts;

    ArrayList<String> Likes = new ArrayList<>();
    ArrayList<String> Dislikes = new ArrayList<>();
    ArrayList<String> Hearts = new ArrayList<>();
    ArrayList<String> Shares = new ArrayList<>();

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

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        final String OnlineUserId = firebaseAuth.getCurrentUser().getUid();

        FriendsPostsReference.child(friend).child(key).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Toast.makeText(context, "Posts key " + key, Toast.LENGTH_SHORT).show();

                Toast.makeText(context, "Posts  " + Posts.toString(), Toast.LENGTH_SHORT).show();

                holder.PostRemoveButton.setVisibility(View.GONE);

                UploadUserReference.child(friend).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot snapshot)
                    {
                        String name = snapshot.child("user_name").getValue().toString();
                        String thumbImage = snapshot.child("user_thumb_img").getValue().toString();

                        holder.PostedUserName.setText(name);
                        holder.setPostedUserImage(context, thumbImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                long upload_time = (long) dataSnapshot.child("uploaded_time").getValue();
                String postUploadedTime = PostUplodedTime.getTimeAgo(upload_time, context);

                holder.PostedTime.setText(postUploadedTime);

                final String result = getImageLocalStorageRef(context, key);

                String type = dataSnapshot.child("type").getValue().toString();

                if (type.equals("0"))
                {
                    if (result == null)
                    {
                        holder.PostedUserName.setVisibility(View.GONE);
                        holder.PostedTime.setVisibility(View.GONE);
                        holder.PostLine1.setVisibility(View.GONE);
                        holder.PostLine2.setVisibility(View.GONE);
                        holder.PostSize.setVisibility(View.GONE);
                        holder.PostDescription.setVisibility(View.GONE);
                        holder.PostDownloading.setVisibility(View.GONE);
                        holder.PostImageView.setVisibility(View.GONE);
                        holder.PostImagePlayButton.setVisibility(View.GONE);
                        holder.PostThumbsUp.setVisibility(View.GONE);
                        holder.PostThumbsDown.setVisibility(View.GONE);
                        holder.PostHeart.setVisibility(View.GONE);
                        holder.PostSharingButton.setVisibility(View.GONE);
                        holder.PostLikesCount.setVisibility(View.GONE);
                        holder.PostDislikesCount.setVisibility(View.GONE);
                        holder.PostHeartsCount.setVisibility(View.GONE);
                        holder.PostShareContainer.setVisibility(View.GONE);
                        holder.PostLikesCommentsArea.setVisibility(View.GONE);
                        holder.PostedUserImage.setVisibility(View.GONE);
                        holder.CommentUserImage.setVisibility(View.GONE);
                        holder.PostCommentEditText.setVisibility(View.GONE);
                        holder.PostCommentsRecyclerView.setVisibility(View.GONE);

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
                                    holder.PostDownloading.setVisibility(View.GONE);
                                    holder.PostImagePlayButton.setVisibility(View.GONE);

                                    holder.PostedUserName.setVisibility(View.VISIBLE);
                                    holder.PostedTime.setVisibility(View.VISIBLE);
                                    holder.PostLine1.setVisibility(View.VISIBLE);
                                    holder.PostLine2.setVisibility(View.VISIBLE);
                                    holder.PostDescription.setVisibility(View.VISIBLE);
                                    holder.PostImageView.setVisibility(View.VISIBLE);
                                    holder.PostThumbsUp.setVisibility(View.VISIBLE);
                                    holder.PostThumbsDown.setVisibility(View.VISIBLE);
                                    holder.PostHeart.setVisibility(View.VISIBLE);
                                    holder.PostSharingButton.setVisibility(View.VISIBLE);
                                    holder.PostLikesCount.setVisibility(View.VISIBLE);
                                    holder.PostDislikesCount.setVisibility(View.VISIBLE);
                                    holder.PostHeartsCount.setVisibility(View.VISIBLE);
                                    holder.PostShareContainer.setVisibility(View.VISIBLE);
                                    holder.PostLikesCommentsArea.setVisibility(View.VISIBLE);
                                    holder.PostedUserImage.setVisibility(View.VISIBLE);
                                    holder.CommentUserImage.setVisibility(View.VISIBLE);
                                    holder.PostCommentEditText.setVisibility(View.VISIBLE);
                                    holder.PostCommentsRecyclerView.setVisibility(View.VISIBLE);

                                    holder.setImagePostImageView(context,file.toString());

                                    UploadUserReference.child(OnlineUserId).addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            holder.setCommentUserImage(context,dataSnapshot.child("user_thumb_img").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });
                                }
                            }
                        });
                    }
                    else
                    {
                        holder.PostSize.setVisibility(View.GONE);
                        holder.PostDownloading.setVisibility(View.GONE);
                        holder.PostImagePlayButton.setVisibility(View.GONE);

                        holder.PostedUserName.setVisibility(View.VISIBLE);
                        holder.PostedTime.setVisibility(View.VISIBLE);
                        holder.PostLine1.setVisibility(View.VISIBLE);
                        holder.PostLine2.setVisibility(View.VISIBLE);
                        holder.PostDescription.setVisibility(View.VISIBLE);
                        holder.PostImageView.setVisibility(View.VISIBLE);
                        holder.PostThumbsUp.setVisibility(View.VISIBLE);
                        holder.PostThumbsDown.setVisibility(View.VISIBLE);
                        holder.PostHeart.setVisibility(View.VISIBLE);
                        holder.PostSharingButton.setVisibility(View.VISIBLE);
                        holder.PostLikesCount.setVisibility(View.VISIBLE);
                        holder.PostDislikesCount.setVisibility(View.VISIBLE);
                        holder.PostHeartsCount.setVisibility(View.VISIBLE);
                        holder.PostShareContainer.setVisibility(View.VISIBLE);
                        holder.PostLikesCommentsArea.setVisibility(View.VISIBLE);
                        holder.PostedUserImage.setVisibility(View.VISIBLE);
                        holder.CommentUserImage.setVisibility(View.VISIBLE);
                        holder.PostCommentEditText.setVisibility(View.VISIBLE);
                        holder.PostCommentsRecyclerView.setVisibility(View.VISIBLE);

                        holder.setImagePostImageView(context,result);

                        UploadUserReference.child(OnlineUserId).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                holder.setCommentUserImage(context,dataSnapshot.child("user_thumb_img").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }
                }


                if (dataSnapshot.hasChild("likes"))
                {
                    FriendsPostsReference.child(friend).child(key).child("likes").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(OnlineUserId))
                            {
                                holder.PostThumbsUp.setLiked(true);
                            }
                            else
                            {
                                holder.PostThumbsUp.setLiked(false);
                            }

                            Likes.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Likes.add(snapshot.getKey());
                            }

                            holder.PostLikesCount.setText(String.valueOf(Likes.size()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    holder.PostLikesCount.setText("0");
                }

                if (dataSnapshot.hasChild("dislikes"))
                {
                    FriendsPostsReference.child(friend).child(key).child("dislikes").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(OnlineUserId))
                            {
                                holder.PostThumbsDown.setLiked(true);
                            }
                            else
                            {
                                holder.PostThumbsDown.setLiked(false);
                            }

                            Dislikes.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Dislikes.add(snapshot.getKey());
                            }

                            holder.PostDislikesCount.setText(String.valueOf(Dislikes.size()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    holder.PostDislikesCount.setText("0");
                }

                if (dataSnapshot.hasChild("hearts"))
                {
                    FriendsPostsReference.child(friend).child(key).child("hearts").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(OnlineUserId))
                            {
                                holder.PostHeart.setLiked(true);
                            }
                            else
                            {
                                holder.PostHeart.setLiked(false);
                            }

                            Hearts.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Hearts.add(snapshot.getKey());
                            }

                            holder.PostHeartsCount.setText(String.valueOf(Hearts.size()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    holder.PostHeartsCount.setText("0");
                }

                if (dataSnapshot.hasChild("shares"))
                {
                    FriendsPostsReference.child(friend).child(key).child("shares").addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Shares.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                Shares.add(snapshot.getKey());
                            }

                            holder.PostSharedCount.setText(String.valueOf(Shares.size()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                else
                {
                    holder.PostSharedCount.setText("0");
                }

                holder.PostThumbsUp.setOnLikeListener(new OnLikeListener()
                {
                    @Override
                    public void liked(LikeButton likeButton)
                    {
                        Calendar calFordATE = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
                        final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                        Map<String,Object> likes = new HashMap<>();

                        likes.put(OnlineUserId,saveCurrentDate);

                        FriendsPostsReference.child(friend).child(key).child("likes").updateChildren(likes)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            if (holder.PostThumbsDown.isLiked())
                                            {
                                                FriendsPostsReference.child(friend).child(key).child("dislikes").child(OnlineUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    holder.PostThumbsDown.setLiked(false);
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
                        FriendsPostsReference.child(friend).child(key).child("likes").child(OnlineUserId).removeValue()
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

                holder.PostThumbsDown.setOnLikeListener(new OnLikeListener()
                {
                    @Override
                    public void liked(LikeButton likeButton)
                    {
                        Calendar calFordATE = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
                        final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                        Map<String,Object> likes = new HashMap<>();

                        likes.put(OnlineUserId,saveCurrentDate);

                        FriendsPostsReference.child(friend).child(key).child("dislikes").updateChildren(likes)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            if (holder.PostThumbsUp.isLiked())
                                            {
                                                FriendsPostsReference.child(friend).child(key).child("likes").child(OnlineUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    holder.PostThumbsUp.setLiked(false);
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
                        FriendsPostsReference.child(friend).child(key).child("dislikes").child(OnlineUserId).removeValue()
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

                holder.PostHeart.setOnLikeListener(new OnLikeListener()
                {
                    @Override
                    public void liked(LikeButton likeButton)
                    {
                        Calendar calFordATE = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
                        final String saveCurrentDate = currentDate.format(calFordATE.getTime());

                        Map<String,Object> likes = new HashMap<>();

                        likes.put(OnlineUserId,saveCurrentDate);

                        FriendsPostsReference.child(friend).child(key).child("hearts").updateChildren(likes)
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
                        FriendsPostsReference.child(friend).child(key).child("hearts").child(OnlineUserId).removeValue()
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

    class PostViewHolder extends RecyclerView.ViewHolder
    {
        TextView PostedUserName,PostedTime,PostLine1,PostLine2,PostSize,PostDescription,PostDownloading;
        ImageButton PostRemoveButton,PostSharingButton;
        ImageView PostImageView,PostImagePlayButton;
        LikeButton PostThumbsUp,PostThumbsDown,PostHeart;
        TextView PostLikesCount,PostDislikesCount,PostHeartsCount,PostSharedCount;
        LinearLayout PostLikesCommentsArea,PostShareContainer;
        CircleImageView PostedUserImage,CommentUserImage;
        EditText PostCommentEditText;
        RecyclerView PostCommentsRecyclerView;

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
            PostDownloading = v.findViewById(R.id.post_downloading);
            PostRemoveButton = v.findViewById(R.id.post_remove_button);
            PostSharingButton = v.findViewById(R.id.post_sharing_button);
            PostImageView = v.findViewById(R.id.post_image_view);
            PostImagePlayButton = v.findViewById(R.id.post_image_play_button);

            PostThumbsUp = v.findViewById(R.id.post_thumbs_up);
            PostLikesCount = v.findViewById(R.id.post_likes_count);
            PostThumbsUp.setIcon(IconType.ThumbsUp);

            PostThumbsDown = v.findViewById(R.id.post_thumbs_down);
            PostDislikesCount = v.findViewById(R.id.post_dislikes_count);
            PostThumbsDown.setIcon(IconType.ThumbsDown);

            PostHeart = v.findViewById(R.id.post_heart);
            PostHeartsCount = v.findViewById(R.id.post_hearts_count);
            PostHeart.setIcon(IconType.Heart);

            PostShareContainer = v.findViewById(R.id.post_share_container);
            PostSharedCount = v.findViewById(R.id.post_shared_count);

            PostLikesCommentsArea = v.findViewById(R.id.post_likes_comments_area);

            PostedUserImage = v.findViewById(R.id.posted_user_image);
            CommentUserImage = v.findViewById(R.id.comment_user_image);

            PostCommentEditText =  v.findViewById(R.id.post_comment_edit_text);

            PostCommentsRecyclerView = v.findViewById(R.id.post_comments_recycler_view);
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

        void setCommentUserImage(final Context context, final String user_thumb_img) {

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

        void setImagePostImageView(final Context context, final String user_thumb_img) {

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

