package com.deffe.macros.grindersouls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar ChatToolBar;

    private TextView userNameTitle,userLastSeen;
    private CircleImageView userChatProfileImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef,UsersDatabaseReference;

    private String messageReceiverId;
    private String messageReceiverName;

    private ImageButton SendMessageButton,SelectImageButton;
    private EditText InputMessageText;

    private RecyclerView userMessagesListRecyclerView;

    private ArrayList<Messages> messageList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;

    private StorageReference MessageImageStorageRef;

    private String online_user_id;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId = (String) getIntent().getExtras().get("visit_user_id");
        messageReceiverName = (String) getIntent().getExtras().get("user_name");

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        ChatToolBar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);

        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");


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

        SendMessageButton = findViewById(R.id.send_message_btn);
        SelectImageButton = findViewById(R.id.select_img);
        InputMessageText = findViewById(R.id.input_message);

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


        rootRef.child("Messages").child(online_user_id).child(messageReceiverId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messageList.add(messages);

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                messageAdapter.notifyDataSetChanged();
            }
        });


        userMessagesListRecyclerView = findViewById(R.id.messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesListRecyclerView.setHasFixedSize(true);
        userMessagesListRecyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(messageList,ChatActivity.this);
        userMessagesListRecyclerView.setAdapter(messageAdapter);



        SendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendMessage();
            }
        });
        SelectImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1 ,1)
                        .start(ChatActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data!= null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            final Uri ImageUri = result.getUri();

            final String message_sender_ref = "Messages/" + online_user_id + "/" + messageReceiverId;
            final String message_receiver_ref = "Messages/" + messageReceiverId + "/" + online_user_id;

            DatabaseReference user_message_key = rootRef.child("Messages").child(online_user_id).child(messageReceiverId).push();

            final String message_push_id = user_message_key.getKey();

            StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

            if (ImageUri != null)
            {
                filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();


                            Map<String,Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message",downloadUrl);
                            messageTextBody.put("seen",false);
                            messageTextBody.put("type","image");
                            messageTextBody.put("time", ServerValue.TIMESTAMP);
                            messageTextBody.put("from",online_user_id);

                            Toast.makeText(ChatActivity.this, "Uri " + ImageUri, Toast.LENGTH_SHORT).show();

                            Map<String,Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
                            {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                {
                                    if (databaseError != null)
                                    {
                                        Log.d("Chat_Log", databaseError.getMessage());

                                        InputMessageText.setText("");

                                    }
                                }
                            });


                            Toast.makeText(ChatActivity.this,"Picture sent Successfully",Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this,"Picture not sent.Try Again",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }
    }


    private void SendMessage()
    {
        String messageText = InputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Please write your message",Toast.LENGTH_SHORT).show();
        }
        else
        {

            String message_sender_ref = "Messages/" + online_user_id + "/" + messageReceiverId;

            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + online_user_id;

            DatabaseReference user_message_key = rootRef.child("Messages").child(online_user_id).child(messageReceiverId).push();

            String message_push_id = user_message_key.getKey();

            Map<String,Object> messageTextBody = new HashMap<>();

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",online_user_id);

            Map<String,Object> messageBodyDetails = new HashMap<>();

            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
            {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if(databaseError != null)
                    {
                        Log.d("Chat_Log", databaseError.getMessage());
                    }

                    InputMessageText.setText("");
                }
            });
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
    {
        private ArrayList<Messages> userMessagesList;
        private Context context;

        MessageAdapter(ArrayList<Messages> userMessagesList,Context context)
        {
            this.userMessagesList = userMessagesList;
            this.context = context;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_user, parent, false);

            firebaseAuth = FirebaseAuth.getInstance();

            return new MessageViewHolder(V);
        }

        @Override
        public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
        {
            final Messages messages = userMessagesList.get(position);

            final String fromUserId = messages.getFrom();
            String fromMessageType = messages.getType();

            UsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
            UsersDatabaseReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    String userThumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                    if (fromUserId.equals(online_user_id))
                    {
                        holder.userProfileImg.setVisibility(View.GONE);
                    }
                    else
                    {
                        holder.userProfileImg.setVisibility(View.VISIBLE);

                        Picasso.with(holder.userProfileImg.getContext()).load(userThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim).into(holder.userProfileImg);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            if (fromMessageType.equals("text"))
            {
                if (fromUserId.equals(online_user_id))
                {
                    holder.messageSenderText.setVisibility(View.GONE);

                    holder.messageSenderImageView.setVisibility(View.GONE);

                    holder.messageReceiverImageview.setVisibility(View.GONE);

                    holder.messageReceiverText.setVisibility(View.VISIBLE);

                    holder.messageReceiverText.setTextColor(Color.BLACK);

                    holder.messageReceiverText.setText(messages.getMessage());
                }
                else
                {
                    holder.messageReceiverText.setVisibility(View.GONE);

                    holder.messageReceiverImageview.setVisibility(View.GONE);

                    holder.messageSenderImageView.setVisibility(View.GONE);

                    holder.messageSenderText.setVisibility(View.VISIBLE);

                    holder.messageSenderText.setTextColor(Color.WHITE);

                    holder.messageSenderText.setText(messages.getMessage());
                }
            }
            else
            {
                if (fromUserId.equals(online_user_id))
                {
                    holder.messageSenderText.setVisibility(View.GONE);

                    holder.messageSenderImageView.setVisibility(View.GONE);

                    holder.messageReceiverText.setVisibility(View.GONE);

                    holder.messageReceiverImageview.setVisibility(View.VISIBLE);

                    holder.setReceiverImage(context, messages.getMessage());

                    holder.messageReceiverImageview.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v,messages.getMessage() , null);

                        }
                    });

                }
                else
                {
                    holder.messageReceiverText.setVisibility(View.GONE);

                    holder.messageReceiverImageview.setVisibility(View.GONE);

                    holder.messageSenderText.setVisibility(View.GONE);

                    holder.messageSenderImageView.setVisibility(View.VISIBLE);

                    holder.setSenderImage(context, messages.getMessage());

                    holder.messageSenderImageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v,messages.getMessage() , null);

                        }
                    });

                }

            }
        }

        @Override
        public int getItemCount() {
            return userMessagesList.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageSenderText, messageReceiverText;

            CircleImageView userProfileImg;

            View v;

            ImageView messageSenderImageView, messageReceiverImageview;

            MessageViewHolder(View view)
            {
                super(view);

                v = view;

                messageSenderText = view.findViewById(R.id.message_sender_text);

                messageReceiverText = view.findViewById(R.id.message_receiver_text);

                userProfileImg = view.findViewById(R.id.message_sender_profile_img);

                messageSenderImageView = view.findViewById(R.id.message_sender_image_view);

                messageReceiverImageview = view.findViewById(R.id.message_receiver_image_view);
            }

            void setReceiverImage(final Context c, final String receiver_sended_img)
            {
                final ImageView receiver_img = v.findViewById(R.id.message_receiver_image_view);

                Picasso.with(c).load(receiver_sended_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(receiver_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(receiver_sended_img).placeholder(R.drawable.vadim).into(receiver_img);
                            }
                        });


            }

            void setSenderImage(final Context c, final String sender_sended_img)
            {
                final ImageView sender_img = v.findViewById(R.id.message_sender_image_view);

                Picasso.with(c).load(sender_sended_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(sender_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(sender_sended_img).placeholder(R.drawable.vadim).into(sender_img);
                            }
                        });


            }
        }
    }
}
