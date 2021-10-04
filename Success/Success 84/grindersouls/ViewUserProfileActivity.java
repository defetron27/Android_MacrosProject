package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
import static com.google.firebase.firestore.DocumentChange.Type.REMOVED;

public class ViewUserProfileActivity extends BaseThemedActivity
{
    private static final String TAG = ViewUserGroupProfileActivity.class.getSimpleName();

    private ImageView ViewUserProfileImageBackground;
    private CircleImageView ViewUserProfileImage;
    private TextView ViewUserProfileName;

    private TextView ViewUserProfileFriends,ViewUserProfileFriendsCount,ViewUserProfilePosts,ViewUserProfilePostsCount;

    private Button ViewUserProfileSendFriendRequestButton;
    private LinearLayout viewUserProfileRequestSentLinearLayout;
    private TextView ViewUserProfileRequestSentTextView,ViewUserProfileRequestSentUndoTextView;
    private LinearLayout ViewUserProfileAcceptOrDeclineButtonLinearLayout;
    private Button ViewUserProfileAcceptFriendRequestButton,ViewUserProfileDeclineFriendRequestButton;
    private Button ViewUserProfileUnFriendAFriendButton;

    private TextView ViewUserProfileMobileTextView,ViewUserProfileMobileNumberTextView,ViewUserProfileAboutTextView,ViewUserProfileAboutDetailsTextView;

    private FirebaseAuth firebaseAuth;
    private CollectionReference friendsRequestReference;
    private CollectionReference notificationsReference;

    private String sender_user_id, receiver_user_id;

    private String CURRENT_STATE = "not_friends";;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        receiver_user_id = getIntent().getExtras().getString("visit_user_unique_id");

        firebaseAuth = FirebaseAuth.getInstance();
        sender_user_id = firebaseAuth.getCurrentUser().getUid();

        CollectionReference viewUserProfileReference = FirebaseFirestore.getInstance().collection("Users");

        friendsRequestReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts");

        notificationsReference = FirebaseFirestore.getInstance().collection("Notifications");

        ViewUserProfileImageBackground = findViewById(R.id.view_user_profile_image_background);
        ViewUserProfileImage = findViewById(R.id.view_user_profile_image);

        ViewUserProfileName = findViewById(R.id.view_user_profile_name);

        ViewUserProfileFriends = findViewById(R.id.view_user_profile_friends);
        ViewUserProfileFriendsCount = findViewById(R.id.view_user_profile_friends_count);
        ViewUserProfilePosts = findViewById(R.id.view_user_profile_posts);
        ViewUserProfilePostsCount = findViewById(R.id.view_user_profile_posts_count);

        ViewUserProfileSendFriendRequestButton = findViewById(R.id.view_user_profile_send_friend_request_button);
        viewUserProfileRequestSentLinearLayout = findViewById(R.id.view_user_profile_request_send_linear_layout);
        ViewUserProfileRequestSentTextView = findViewById(R.id.view_user_profile_request_sent_text_view);
        ViewUserProfileRequestSentUndoTextView = findViewById(R.id.view_user_profile_request_sent_undo_text_view);
        ViewUserProfileAcceptOrDeclineButtonLinearLayout = findViewById(R.id.view_user_profile_accept_or_decline_linear_layout);
        ViewUserProfileAcceptFriendRequestButton = findViewById(R.id.view_user_profile_accept_friend_request_button);
        ViewUserProfileDeclineFriendRequestButton = findViewById(R.id.view_user_profile_decline_friend_request_button);
        ViewUserProfileUnFriendAFriendButton = findViewById(R.id.view_user_profile_unfriend_a_friend_button);

        ViewUserProfileMobileTextView = findViewById(R.id.view_user_profile_mobile_text_view);
        ViewUserProfileMobileNumberTextView = findViewById(R.id.view_user_profile_mobile_number_text_view);
        ViewUserProfileAboutTextView = findViewById(R.id.view_user_profile_about_text_view);
        ViewUserProfileAboutDetailsTextView = findViewById(R.id.view_user_profile_about_details_text_view);

        viewUserProfileReference.document(receiver_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable final FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(ViewUserProfileActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    final String userName = documentSnapshot.getString("user_name");
                    final String userMobileNumberWithPlus = documentSnapshot.getString("user_mobile_number_with_plus");
                    final String aboutUser = documentSnapshot.getString("about_user");
                    String thumbImage = documentSnapshot.getString("user_thumb_img");

                    ViewUserProfileName.setText(userName);
                    ViewUserProfileMobileNumberTextView.setText(userMobileNumberWithPlus);
                    ViewUserProfileAboutDetailsTextView.setText(aboutUser);
                    Picasso.with(ViewUserProfileActivity.this).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim).into(ViewUserProfileImage);

                    friendsRequestReference.document(sender_user_id).collection("Sent").document(receiver_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot)
                        {
                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                CURRENT_STATE = "request_sent";

                                ViewUserProfileSendFriendRequestButton.setVisibility(View.GONE);
                                ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);

                                viewUserProfileRequestSentLinearLayout.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                friendsRequestReference.document(sender_user_id).collection("Received").document(receiver_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot)
                                    {
                                        if (documentSnapshot != null && documentSnapshot.exists())
                                        {
                                            CURRENT_STATE = "request_received";

                                            ViewUserProfileSendFriendRequestButton.setVisibility(View.GONE);
                                            ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                            viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                            ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            friendsRequestReference.document(sender_user_id).collection("Friends").document(receiver_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                            {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot)
                                                {
                                                    if (documentSnapshot != null && documentSnapshot.exists())
                                                    {
                                                        CURRENT_STATE = "friends";

                                                        ViewUserProfileSendFriendRequestButton.setVisibility(View.GONE);
                                                        viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);
                                                        ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);

                                                        ViewUserProfileUnFriendAFriendButton.setVisibility(View.VISIBLE);
                                                    }
                                                    else
                                                    {
                                                        CURRENT_STATE = "not_friends";

                                                        ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                                        ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                        viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                                        ViewUserProfileSendFriendRequestButton.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener()
                                            {
                                                @Override
                                                public void onFailure(@NonNull Exception e)
                                                {
                                                    Toast.makeText(ViewUserProfileActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG,e.toString());
                                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(ViewUserProfileActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(ViewUserProfileActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }
                    });
                }
            }
        });

        if (!sender_user_id.equals(receiver_user_id))
        {
            ViewUserProfileSendFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (NetworkStatus.isConnected(ViewUserProfileActivity.this))
                    {
                        if (NetworkStatus.isConnectedFast(ViewUserProfileActivity.this))
                        {
                            if (CURRENT_STATE.equals("not_friends"))
                            {
                                ViewUserProfileSendFriendRequestButton.setEnabled(false);
                                SendFriendRequestToAPerson();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(R.id.view_user_profile_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.view_user_profile_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            viewUserProfileRequestSentLinearLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (NetworkStatus.isConnected(ViewUserProfileActivity.this))
                    {
                        if (NetworkStatus.isConnectedFast(ViewUserProfileActivity.this))
                        {
                            if (CURRENT_STATE.equals("request_sent"))
                            {
                                viewUserProfileRequestSentLinearLayout.setEnabled(false);
                                UndoFriendRequest();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(R.id.view_user_profile_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.view_user_profile_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            ViewUserProfileAcceptFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (NetworkStatus.isConnected(ViewUserProfileActivity.this))
                    {
                        if (NetworkStatus.isConnectedFast(ViewUserProfileActivity.this))
                        {
                            if (CURRENT_STATE.equals("request_received"))
                            {
                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setEnabled(false);
                                AcceptFriendRequest();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(R.id.view_user_profile_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.view_user_profile_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            ViewUserProfileDeclineFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (NetworkStatus.isConnected(ViewUserProfileActivity.this))
                    {
                        if (NetworkStatus.isConnectedFast(ViewUserProfileActivity.this))
                        {
                            if (CURRENT_STATE.equals("request_received"))
                            {
                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setEnabled(false);
                                DeclineFriendRequest();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(R.id.view_user_profile_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.view_user_profile_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            ViewUserProfileUnFriendAFriendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (NetworkStatus.isConnected(ViewUserProfileActivity.this))
                    {
                        if (NetworkStatus.isConnectedFast(ViewUserProfileActivity.this))
                        {
                            if (CURRENT_STATE.equals("friends"))
                            {
                                ViewUserProfileUnFriendAFriendButton.setEnabled(false);
                                UnFriendAFriend();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(R.id.view_user_profile_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.view_user_profile_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void SendFriendRequestToAPerson()
    {
        Calendar calFordATE = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        final String time = currentDate.format(calFordATE.getTime());
        final Map<String, Object> timeFriends = new HashMap<>();
        timeFriends.put("time", time);

        friendsRequestReference.document(sender_user_id).collection("Sent").document(receiver_user_id).set(timeFriends)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendsRequestReference.document(receiver_user_id).collection("Received").document(sender_user_id).set(timeFriends)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Map<String,Object> request = new HashMap<>();

                                                request.put("from",sender_user_id);
                                                request.put("type","request");

                                                notificationsReference.document(receiver_user_id).collection("Received").document().set(request).addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            CURRENT_STATE = "request_sent";

                                                            ViewUserProfileSendFriendRequestButton.setVisibility(View.GONE);
                                                            ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                            ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);

                                                            viewUserProfileRequestSentLinearLayout.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener()
                                                {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {
                                                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG,e.toString());
                                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                    }
                                                });


                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,e.toString());
                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
    }

    private void UndoFriendRequest()
    {
        friendsRequestReference.document(sender_user_id).collection("Sent").document(receiver_user_id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendsRequestReference.document(receiver_user_id).collection("Received").document(sender_user_id).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                CURRENT_STATE = "not_friends";

                                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                                viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                                ViewUserProfileSendFriendRequestButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,e.toString());
                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        Calendar calFordATE = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        final String time = currentDate.format(calFordATE.getTime());
        final Map<String, Object> timeFriends = new HashMap<>();
        timeFriends.put("time", time);

        friendsRequestReference.document(sender_user_id).collection("Friends").document(receiver_user_id).set(timeFriends)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendsRequestReference.document(receiver_user_id).collection("Friends").document(sender_user_id).set(timeFriends)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendsRequestReference.document(sender_user_id).collection("Received").document(receiver_user_id).delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                friendsRequestReference.document(receiver_user_id).collection("Sent").document(sender_user_id).delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                CURRENT_STATE = "friends";

                                                                                ViewUserProfileSendFriendRequestButton.setVisibility(View.GONE);
                                                                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                                                viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                                                                ViewUserProfileUnFriendAFriendButton.setVisibility(View.VISIBLE);
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                                                        Log.e(TAG,e.toString());
                                                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                                Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                                                Log.e(TAG,e.toString());
                                                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                            }
                                                        });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,e.toString());
                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
    }

    private void DeclineFriendRequest()
    {
        friendsRequestReference.document(sender_user_id).collection("Received").document(receiver_user_id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendsRequestReference.document(receiver_user_id).collection("Sent").document(sender_user_id).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                CURRENT_STATE = "not_friends";

                                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                                viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                                ViewUserProfileSendFriendRequestButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,e.toString());
                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
    }

    private void UnFriendAFriend()
    {
        friendsRequestReference.document(sender_user_id).collection("Friends").document(receiver_user_id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendsRequestReference.document(receiver_user_id).collection("Friends").document(sender_user_id).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                CURRENT_STATE = "not_friends";

                                                ViewUserProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.GONE);
                                                ViewUserProfileUnFriendAFriendButton.setVisibility(View.GONE);
                                                viewUserProfileRequestSentLinearLayout.setVisibility(View.GONE);

                                                ViewUserProfileSendFriendRequestButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,e.toString());
                                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ViewUserProfileActivity.this, "Error while sent request", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            FirebaseFirestore.getInstance().collection("Users").document(sender_user_id).update("online","true");
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            Calendar calFordATE = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            final String time = currentDate.format(calFordATE.getTime());

            try
            {
                Date date = currentDate.parse(time);
                long millis = date.getTime();

                FirebaseFirestore.getInstance().collection("Users").document(sender_user_id).update("online", millis);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
    }
}
