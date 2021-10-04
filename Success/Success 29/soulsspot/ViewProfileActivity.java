package com.deffe.macros.soulsspot;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {
    private Toolbar UserViewProfileToolbar;

    private Button UserViewProfileAddFriendButton;
    private Button UserViewProfileRequestSentButton;
    private Button UserViewProfileUnFriendButton;
    private Button UserViewProfileAcceptFriendRequestButton;
    private Button UserViewProfileDeclineFriendRequestButton;

    private LinearLayout UserViewProfileAcceptOrDeclineButtonLinearLayout;

    private TextView UserViewProfileUndoRequestSentTextView;

    private TextView ShowFriendsTextView;
    private TextView UserAllFriendsCountTextView;

    private TextView UserViewProfileName;
    private TextView UserViewProfileStatus;
    private TextView UserViewProfileMobileNumber;

    private CircleImageView UserViewProfileImage;
    private ImageView UserViewProfileImageBackground;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ViewUserProfileReference, FriendsRequestReference, FriendsReference, NotificationsReference;

    private String sender_user_id, receiver_user_id;

    private String CURRENT_STATE;

    private ArrayList<String> FRIENDSCOUNT = new ArrayList<>();

    private String COUNT,GlobalFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        receiver_user_id = (String) getIntent().getExtras().getString("visit_user_unique_id");

        COUNT = (String) getIntent().getExtras().getString("friends");

        ViewUserProfileReference = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendsRequestReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();
        sender_user_id = firebaseAuth.getCurrentUser().getUid();

        UserViewProfileToolbar = (Toolbar) findViewById(R.id.user_view_profile_toolbar);
        setSupportActionBar(UserViewProfileToolbar);
        getSupportActionBar().setTitle("Username Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        UserViewProfileAddFriendButton = (Button) findViewById(R.id.user_view_profile_add_friend_button);
        UserViewProfileRequestSentButton = (Button) findViewById(R.id.user_view_profile_request_sent_button);
        UserViewProfileUnFriendButton = (Button) findViewById(R.id.user_view_profile_unfriend_button);
        UserViewProfileAcceptFriendRequestButton = (Button) findViewById(R.id.user_view_profile_accept_friend_request_button);
        UserViewProfileDeclineFriendRequestButton = (Button) findViewById(R.id.user_view_profile_decline_friend_request_button);

        UserViewProfileAcceptOrDeclineButtonLinearLayout = (LinearLayout) findViewById(R.id.user_view_profile_accept_or_decline_button_linear_layout);

        UserViewProfileUndoRequestSentTextView = (TextView) findViewById(R.id.user_view_profile_undo_request_sent_text_view);
        ShowFriendsTextView = (TextView) findViewById(R.id.show_friends_text_view);
        UserAllFriendsCountTextView = (TextView) findViewById(R.id.user_all_friends_count_text_view);

        UserAllFriendsCountTextView.setText(COUNT);

        UserViewProfileName = (TextView) findViewById(R.id.user_view_profile_name);
        UserViewProfileStatus = (TextView) findViewById(R.id.user_view_profile_status);
        UserViewProfileMobileNumber = (TextView) findViewById(R.id.user_view_profile_mobile_number);

        UserViewProfileImage = (CircleImageView) findViewById(R.id.user_view_profile_image);
        UserViewProfileImageBackground = (ImageView) findViewById(R.id.user_view_profile_image_background);

        CURRENT_STATE = "not_friends";

        ViewUserProfileReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String userName = (String) dataSnapshot.child("user_name").getValue();
                final String userMobileNumber = (String) dataSnapshot.child("user_mobile_number").getValue();
                final String userViewProfileStatus = (String) dataSnapshot.child("user_invite_profile_status").getValue();
                String img = (String) dataSnapshot.child("user_img").getValue();
                final String friends = (String) dataSnapshot.child("friends").getValue();

                GlobalFriends = friends;

                UserViewProfileName.setText(userName);
                UserViewProfileMobileNumber.setText(userMobileNumber);
                UserViewProfileStatus.setText(userViewProfileStatus);
                getSupportActionBar().setTitle(userName + " Profile");
                Picasso.with(ViewProfileActivity.this).load(img).placeholder(R.drawable.vadim).into(UserViewProfileImage);

                FriendsRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
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
                                                public void onDataChange(DataSnapshot dataSnapshot)
                                                {
                                                    if (dataSnapshot.hasChild(receiver_user_id))
                                                    {
                                                        CURRENT_STATE = "friends";

                                                        UserViewProfileUnFriendButton.setVisibility(View.VISIBLE);

                                                        UserAllFriendsCountTextView.setText(friends);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
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

                        FriendsReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(receiver_user_id).child("friends").setValue(friendscount);

                                UserAllFriendsCountTextView.setText(GlobalFriends);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(sender_user_id).child("friends").setValue(friendscount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
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

                        FriendsReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(receiver_user_id).child("friends").setValue(friendscount);

                                UserAllFriendsCountTextView.setText(GlobalFriends);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(sender_user_id).child("friends").setValue(friendscount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
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

                        FriendsReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(receiver_user_id).child("friends").setValue(friendscount);

                                UserAllFriendsCountTextView.setText(GlobalFriends);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(sender_user_id).child("friends").setValue(friendscount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
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

                        FriendsReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(receiver_user_id).child("friends").setValue(friendscount);

                                UserAllFriendsCountTextView.setText(GlobalFriends);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(sender_user_id).child("friends").setValue(friendscount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
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

                        FriendsReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(receiver_user_id).child("friends").setValue(friendscount);

                                UserAllFriendsCountTextView.setText(GlobalFriends);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                        FriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                FRIENDSCOUNT.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    FRIENDSCOUNT.add(snapshot.getKey());
                                }

                                String friendscount = String.valueOf(FRIENDSCOUNT.size());

                                ViewUserProfileReference.child(sender_user_id).child("friends").setValue(friendscount);
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

                                                Toast.makeText(ViewProfileActivity.this, "Declined", Toast.LENGTH_SHORT).show();

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