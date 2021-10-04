package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfileActivity extends AppCompatActivity
{
    private ImageView ViewUserProfileImageBackground;
    private CircleImageView ViewUserProfileImage;
    private TextView ViewUserProfileName;

    private TextView ViewUserProfileFriends,ViewUserProfileFriendsCount,ViewUserProfilePosts,ViewUserProfilePostsCount;

    private Button ViewUserProfileSendFriendRequestButton;
    private TextView ViewUserProfileRequestSentTextView,ViewUserProfileRequestSentUndoTextView;
    private LinearLayout ViewUserProfileAcceptOrDeclineButtonLinearLayout;
    private Button ViewUserProfileAcceptFriendRequestButton,ViewUserProfileDeclineFriendRequestButton;
    private Button ViewUserProfileUnFriendAFriendButton;

    private TextView ViewUserProfileMobileTextView,ViewUserProfileMobileNumberTextView,ViewUserProfileAboutTextView,ViewUserProfileAboutDetailsTextView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ViewUserProfileReference, FriendsRequestReference, FriendsReference, NotificationsReference;

    private String sender_user_id, receiver_user_id;

    private String CURRENT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        receiver_user_id = getIntent().getExtras().getString("visit_user_unique_id");

        ViewUserProfileReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ViewUserProfileReference.keepSynced(true);

        FriendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendsRequestReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();
        sender_user_id = firebaseAuth.getCurrentUser().getUid();

        ViewUserProfileImageBackground = findViewById(R.id.view_user_profile_image_background);
        ViewUserProfileImage = findViewById(R.id.view_user_profile_image);

        ViewUserProfileName = findViewById(R.id.view_user_profile_name);

        ViewUserProfileFriends = findViewById(R.id.view_user_profile_friends);
        ViewUserProfileFriendsCount = findViewById(R.id.view_user_profile_friends_count);
        ViewUserProfilePosts = findViewById(R.id.view_user_profile_posts);
        ViewUserProfilePostsCount = findViewById(R.id.view_user_profile_posts_count);

        ViewUserProfileSendFriendRequestButton = findViewById(R.id.view_user_profile_send_friend_request_button);
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

        CURRENT_STATE = "not_friends";

        ViewUserProfileReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String userName = (String) dataSnapshot.child("user_name").getValue();
                final String userMobileNumberWithPlus = (String) dataSnapshot.child("user_mobile_number_with_plus").getValue();
                final String userViewProfileStatus = (String) dataSnapshot.child("user_invite_profile_status").getValue();
                String img = (String) dataSnapshot.child("user_img").getValue();

                ViewUserProfileName.setText(userName);
                ViewUserProfileMobileNumberTextView.setText(userMobileNumberWithPlus);
                UserViewProfileStatus.setText(userViewProfileStatus);
                getSupportActionBar().setTitle(userName + " Profile");
                Picasso.with(ViewUserProfileActivity.this).load(img).placeholder(R.drawable.vadim).into(UserViewProfileImage);

                FriendsRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {

                                if (dataSnapshot.hasChild(receiver_user_id))
                                {
                                    String req_type = (dataSnapshot.child(receiver_user_id)).child("request_type").getValue().toString();

                                    if (req_type.equals("sent"))
                                    {
                                        CURRENT_STATE = "request_sent";

                                        UserViewProfileAddFriendButton.setVisibility(View.INVISIBLE);

                                        UserViewProfileRequestSentButton.setVisibility(View.VISIBLE);
                                        UserViewProfileUndoRequestSentTextView.setVisibility(View.VISIBLE);


                                    }
                                    else if (req_type.equals("received"))
                                    {
                                        CURRENT_STATE = "request_received";

                                        UserViewProfileAddFriendButton.setVisibility(View.INVISIBLE);


                                        UserViewProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.VISIBLE);

                                    }

                                }
                                else
                                {
                                    FriendsReference.child(sender_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener()
                                            {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                {
                                                    if (dataSnapshot.hasChild(receiver_user_id))
                                                    {
                                                        CURRENT_STATE = "friends";

                                                        UserViewProfileUnFriendButton.setVisibility(View.VISIBLE);

                                                        UserAllFriendsCountTextView.setText(friends);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });


        if (!sender_user_id.equals(receiver_user_id))
        {
            UserViewProfileAddFriendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToAPerson();
                    }
                }
            });

            UserViewProfileUndoRequestSentTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        UndoFriendRequest();


                    }
                }
            });

            UserViewProfileAcceptFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();


                    }
                }
            });

            UserViewProfileDeclineFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        DeclineFriendRequest();

                    }
                }
            });

            UserViewProfileUnFriendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAFriend();
                    }
                }
            });
        }
    }

    private void SendFriendRequestToAPerson()
    {
        FriendsRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) {
                            FriendsRequestReference.child(receiver_user_id).child(sender_user_id).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {

                                                UserViewProfileAddFriendButton.setVisibility(View.INVISIBLE);
                                                CURRENT_STATE = "request_sent";

                                                UserViewProfileRequestSentButton.setVisibility(View.VISIBLE);
                                                UserViewProfileUndoRequestSentTextView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void UndoFriendRequest()
    {
        FriendsRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {

                                                CURRENT_STATE = "not_friends";

                                                UserViewProfileRequestSentButton.setVisibility(View.INVISIBLE);
                                                UserViewProfileUndoRequestSentTextView.setVisibility(View.INVISIBLE);

                                                UserViewProfileAddFriendButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest()
    {

        Calendar calFordATE = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");

        final String saveCurrentDate = currentDate.format(calFordATE.getTime());


        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        FriendsRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            FriendsRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                UserViewProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.INVISIBLE);

                                                                                CURRENT_STATE = "friends";

                                                                                UserViewProfileUnFriendButton.setVisibility(View.VISIBLE);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void DeclineFriendRequest()
    {
        FriendsRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                UserViewProfileAcceptOrDeclineButtonLinearLayout.setVisibility(View.INVISIBLE);

                                                Toast.makeText(ViewUserProfileActivity.this, "Declined", Toast.LENGTH_SHORT).show();

                                                CURRENT_STATE = "not_friends";

                                                UserViewProfileAddFriendButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void UnFriendAFriend()
    {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                UserViewProfileUnFriendButton.setVisibility(View.INVISIBLE);

                                                CURRENT_STATE = "not_friends";

                                                UserViewProfileAddFriendButton.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
