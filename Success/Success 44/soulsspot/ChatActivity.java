package com.deffe.macros.soulsspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar ChatToolBar;

    private TextView userNameTitle,userLastSeen;
    private CircleImageView userChatProfileImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private String messageReceiverId;
    private String messageReceiverName;


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId = (String) getIntent().getExtras().get("visit_user_id");
        messageReceiverName = (String) getIntent().getExtras().get("user_name");

        ChatToolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        firebaseAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionBar != null)
        {
            actionBar.setDisplayShowCustomEnabled(true);
        }

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = null;
        if (layoutInflater != null)
        {
            action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        }

        if (actionBar != null)
        {
            actionBar.setCustomView(action_bar_view);
        }


        userNameTitle = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = findViewById(R.id.custom_profile_img);

        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_thumb_img").getValue().toString();


                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(userChatProfileImage, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.vadim).into(userChatProfileImage);
                            }
                        });
                if (online.equals("true"))
                {
                    userLastSeen.setText("Online");
                }
                else
                {

                    long last_seen = Long.parseLong(online);

                    String lastSeenDisplayTime = LastSeenTime.getTimeAgo(last_seen, getApplicationContext());

                    userLastSeen.setText(lastSeenDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });

    }
}
