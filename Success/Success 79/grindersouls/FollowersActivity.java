package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.deffe.macros.grindersouls.Models.TrendPostsModel;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowersActivity extends AppCompatActivity
{
    private final static String TAG = FollowersActivity.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Toolbar uploadTrendPostToolbar;
        final RecyclerView uploadTrendPostRecyclerView;

        uploadTrendPostToolbar = findViewById(R.id.upload_trend_post_toolbar);
        setSupportActionBar(uploadTrendPostToolbar);
        getSupportActionBar().setTitle("Top Trending");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uploadTrendPostRecyclerView = findViewById(R.id.upload_trend_post_recycler_view);
        uploadTrendPostRecyclerView.setLayoutManager(new LinearLayoutManager(FollowersActivity.this, LinearLayoutManager.VERTICAL, false));

        firebaseAuth = FirebaseAuth.getInstance();

        onlineUserId = firebaseAuth.getCurrentUser().getUid();

        CollectionReference trendPostRef = FirebaseFirestore.getInstance().collection("TrendPosts");

        Query query = trendPostRef.orderBy("stars", Query.Direction.ASCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
                else
                {
                    if (queryDocumentSnapshots != null)
                    {
                        ArrayList<TrendPostsModel> trendPosts = new ArrayList<>();

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                        {
                            TrendPostsModel trendPostsModel = documentSnapshot.toObject(TrendPostsModel.class);
                            trendPosts.add(trendPostsModel);
                        }

                        TrendPostAdapter trendPostAdapter = new TrendPostAdapter(trendPosts);
                        uploadTrendPostRecyclerView.setAdapter(trendPostAdapter);
                    }
                }
            }
        });
    }

    public class TrendPostAdapter extends RecyclerView.Adapter<TrendPostAdapter.TrendPostViewHolder>
    {
        private ArrayList<TrendPostsModel> trendPosts;
        private boolean support = false;

        TrendPostAdapter(ArrayList<TrendPostsModel> trendPosts)
        {
            this.trendPosts = trendPosts;
        }

        @NonNull
        @Override
        public TrendPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followers_layout_items, parent, false);

            return new TrendPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final TrendPostViewHolder holder, int position)
        {
            final TrendPostsModel trendPostsModel = trendPosts.get(position);

            holder.supportHeader.setVisibility(View.VISIBLE);
            holder.supportMain.setVisibility(View.VISIBLE);
            holder.supportFooter.setVisibility(View.VISIBLE);
            holder.supportFooterMain.setVisibility(View.VISIBLE);
            holder.opinionAreaLinearLayout.setVisibility(View.VISIBLE);
            holder.opinionsRecyclerView.setVisibility(View.VISIBLE);

            if (trendPostsModel.getUser_key().equals(onlineUserId))
            {
                holder.follow_button.setVisibility(View.GONE);
                holder.opinionAreaLinearLayout.setVisibility(View.GONE);

                holder.trend_post_remove_button.setVisibility(View.VISIBLE);

                holder.supported_button.setEnabled(false);
                holder.unsupported_button.setEnabled(false);
                holder.support_star.setEnabled(false);
                holder.support_star.setLiked(true);

                holder.supported_button.setText("Supporters");
                holder.unsupported_button.setText("UnSupporters");
            }
            else
            {
                holder.trend_post_remove_button.setVisibility(View.GONE);

                holder.follow_button.setVisibility(View.VISIBLE);

                holder.supported_button.setEnabled(true);
                holder.unsupported_button.setEnabled(true);
                holder.support_star.setEnabled(true);

                if (!trendPostsModel.getUser_key().equals(onlineUserId))
                {
                    FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("Supporters").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                holder.supported_button.setTextColor(Color.GRAY);
                                holder.supported_button.setText("Supporting");
                            }
                            else
                            {
                                holder.supported_button.setTextColor(Color.BLACK);
                                holder.supported_button.setText("Support");
                            }
                        }
                    });

                    FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("UnSupporters").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                holder.unsupported_button.setTextColor(Color.GRAY);
                                holder.unsupported_button.setText("UnSupporting");
                            }
                            else
                            {
                                holder.unsupported_button.setTextColor(Color.BLACK);
                                holder.unsupported_button.setText("UnSupport");
                            }
                        }
                    });
                }

            }

            CollectionReference onlineRef = FirebaseFirestore.getInstance().collection("Users");

            onlineRef.document(trendPostsModel.getUser_key()).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        String userName = documentSnapshot.getString("user_name");
                        String thumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.support_user_name.setText(userName);
                        holder.setPostedUserImage(FollowersActivity.this,thumbImage);
                    }
                }
            });

            onlineRef.document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
            {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                {
                    if (e != null)
                    {
                        Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }

                    if (documentSnapshot != null && documentSnapshot.exists())
                    {
                        String thumbImage = documentSnapshot.getString("user_thumb_img");

                        holder.setSupportedUserImage(FollowersActivity.this,thumbImage);
                    }
                }
            });

            holder.setTrendPostImageView(FollowersActivity.this,trendPostsModel.getUrl());
            holder.supported_image_desc.setText(trendPostsModel.getDesc());

            holder.supported_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!trendPostsModel.getUser_key().equals(onlineUserId))
                    {
                        if (!support)
                        {
                            support = true;

                            Map<String, Object> support = new HashMap<>();
                            support.put("time", FieldValue.serverTimestamp());

                            FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("Supporters").document(onlineUserId).set(support).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(FollowersActivity.this, "Supporting", Toast.LENGTH_SHORT).show();
                                        holder.supported_button.setTextColor(Color.GRAY);
                                        holder.supported_button.setText("Supporting");

                                        FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("UnSupporters").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
                                        {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                                            {
                                                if (e != null)
                                                {
                                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG, e.toString());
                                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                                }

                                                if (documentSnapshot != null && documentSnapshot.exists())
                                                {
                                                    FirebaseFirestore.getInstance().collection("TrendPost").document(trendPostsModel.getPost_key()).collection("UnSupporters").document(onlineUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                holder.unsupported_button.setTextColor(Color.BLACK);
                                                                holder.unsupported_button.setText("UnSupport");
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, e.toString());
                                                            Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                                        }
                                                    });
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
                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, e.toString());
                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                }
                            });
                        }
                        else
                        {
                            FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("Supporters").document(onlineUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        holder.supported_button.setTextColor(Color.BLACK);
                                        holder.supported_button.setText("Support");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, e.toString());
                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                }
                            });
                        }
                    }
                }
            });

            holder.unsupported_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!trendPostsModel.getUser_key().equals(onlineUserId))
                    {
                        if (support)
                        {
                            support = false;

                            Map<String, Object> support = new HashMap<>();
                            support.put("time", FieldValue.serverTimestamp());

                            FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("UnSupporters").document(onlineUserId).set(support).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(FollowersActivity.this, "UnSupporting", Toast.LENGTH_SHORT).show();
                                        holder.unsupported_button.setTextColor(Color.GRAY);
                                        holder.unsupported_button.setText("UnSupporting");

                                        FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("Supporters").document(onlineUserId).addSnapshotListener(new EventListener<DocumentSnapshot>()
                                        {
                                            @Override
                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                                            {
                                                if (e != null)
                                                {
                                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG, e.toString());
                                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                                }

                                                if (documentSnapshot != null && documentSnapshot.exists())
                                                {
                                                    FirebaseFirestore.getInstance().collection("TrendPost").document(trendPostsModel.getPost_key()).collection("Supporters").document(onlineUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                holder.supported_button.setTextColor(Color.BLACK);
                                                                holder.supported_button.setText("Support");
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, e.toString());
                                                            Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                                        }
                                                    });
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
                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, e.toString());
                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                }
                            });
                        }
                        else
                        {
                            FirebaseFirestore.getInstance().collection("TrendPosts").document(trendPostsModel.getPost_key()).collection("UnSupporters").document(onlineUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        holder.unsupported_button.setTextColor(Color.BLACK);
                                        holder.unsupported_button.setText("UnSupport");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, e.toString());
                                    Crashlytics.log(Log.ERROR, TAG, e.getMessage());
                                }
                            });
                        }
                    }
                }
            });

            holder.follow_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!trendPostsModel.getUser_key().equals(onlineUserId))
                    {
                        holder.follow_button.setEnabled(false);

                        final Map<String, Object> follow = new HashMap<>();
                        follow.put("time", FieldValue.serverTimestamp());

                        FirebaseFirestore.getInstance().collection("Users").document(trendPostsModel.getUser_key()).collection("Followers").document(onlineUserId).set(follow).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    holder.follow_button.setVisibility(View.GONE);
                                    holder.unfollow_linear_layout.setVisibility(View.VISIBLE);

                                    FirebaseFirestore.getInstance().collection("Users").document(onlineUserId).collection("Following").document(trendPostsModel.getUser_key()).set(follow).addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(FollowersActivity.this, "Following", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        }
                                    });;
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }
                        });
                    }
                }
            });

            holder.unfollow_linear_layout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!trendPostsModel.getUser_key().equals(onlineUserId))
                    {
                        holder.unfollow_linear_layout.setEnabled(false);

                        FirebaseFirestore.getInstance().collection("Users").document(trendPostsModel.getUser_key()).collection("Followers").document(onlineUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    holder.unfollow_linear_layout.setVisibility(View.GONE);
                                    holder.follow_button.setVisibility(View.VISIBLE);

                                    FirebaseFirestore.getInstance().collection("Users").document(onlineUserId).collection("Following").document(trendPostsModel.getUser_key()).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(FollowersActivity.this, "UnFollow", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                        }
                                    });;
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(FollowersActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }
                        });
                    }
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return trendPosts.size();
        }

        class TrendPostViewHolder extends RecyclerView.ViewHolder
        {
            ConstraintLayout supportHeader,supportMain,supportFooter,supportFooterMain;
            LinearLayout opinionAreaLinearLayout,unfollow_linear_layout;
            RecyclerView opinionsRecyclerView;

            CircleImageView supportUserImage,opinion_user_image;

            TextView support_user_name,support_time,support_start_count,supported_count_text_view,unsupported_count_text_view,followers_count_text_view,followers_text_view,supported_image_desc,say_opinion_text_view;

            ImageButton trend_post_remove_button,supported_image_desc_open_button,supported_image_desc_close_button;

            ImageView supporting_post_image_view;

            Button follow_button,supported_button,unsupported_button;

            LikeButton support_star;

            View desc_view,v;

            TrendPostViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                supportHeader = v.findViewById(R.id.supported_header);
                supportMain = v.findViewById(R.id.supported_main);
                supportFooter = v.findViewById(R.id.supported_footer);
                supportFooterMain = v.findViewById(R.id.supported_footer_main);
                opinionAreaLinearLayout = v.findViewById(R.id.opinion_area_linear_layout);
                opinionsRecyclerView = v.findViewById(R.id.opinions_recycler_view);

                supportUserImage = v.findViewById(R.id.support_user_image);

                unfollow_linear_layout = v.findViewById(R.id.unfollow_linear_layout);
                opinion_user_image = v.findViewById(R.id.opinion_user_image);
                support_user_name = v.findViewById(R.id.support_user_name);
                support_time = v.findViewById(R.id.support_time);
                support_start_count = v.findViewById(R.id.support_start_count);
                supported_count_text_view = v.findViewById(R.id.supported_count_text_view);
                unsupported_count_text_view = v.findViewById(R.id.unsupported_count_text_view);
                followers_count_text_view = v.findViewById(R.id.followers_count_text_view);
                followers_text_view = v.findViewById(R.id.followers_text_view);
                supported_image_desc = v.findViewById(R.id.supported_image_desc);
                say_opinion_text_view = v.findViewById(R.id.say_opinion_text_view);
                trend_post_remove_button = v.findViewById(R.id.trend_post_remove_button);
                supported_image_desc_open_button = v.findViewById(R.id.supported_image_desc_open_button);
                supported_image_desc_close_button = v.findViewById(R.id.supported_image_desc_close_button);
                supporting_post_image_view = v.findViewById(R.id.supporting_post_image_view);
                follow_button = v.findViewById(R.id.follow_button);
                supported_button = v.findViewById(R.id.supported_button);
                unsupported_button = v.findViewById(R.id.unsupported_button);
                support_star = v.findViewById(R.id.support_star);
                desc_view = v.findViewById(R.id.desc_view);
            }

            void setPostedUserImage(final Context context, final String user_thumb_img) {

                final CircleImageView thumb_img = v.findViewById(R.id.support_user_image);

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

            void setSupportedUserImage(final Context context, final String user_thumb_img) {

                final CircleImageView thumb_img = v.findViewById(R.id.opinion_user_image);

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

            void setTrendPostImageView(final Context context, final String user_thumb_img)
            {
                final ImageView thumb_img = v.findViewById(R.id.supporting_post_image_view);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                Uri resultUri = result.getUri();

                Intent trendIntent = new Intent(FollowersActivity.this,TrendPostImageAndDescriptionActivity.class);
                trendIntent.setAction(Intent.ACTION_VIEW);
                trendIntent.putExtra("trend_uri",resultUri.toString());
                startActivity(trendIntent);

            }
            if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Upload cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.upload_trend_post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.upload_trend_post_image)
        {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1 ,1).start(FollowersActivity.this);
        }

        if (!isDeviceSupportCamera())
        {
            Toast.makeText(this, "Your does not support camera", Toast.LENGTH_SHORT).show();

            finish();
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private boolean isDeviceSupportCamera()
    {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
