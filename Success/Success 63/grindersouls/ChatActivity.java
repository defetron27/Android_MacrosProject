package com.deffe.macros.grindersouls;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar ChatToolBar;

    private TextView userNameTitle,userLastSeen;
    private CircleImageView userChatProfileImage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private ArrayList<String> MembersNames = new ArrayList<>();

    private String GroupName;

    private String GroupThumbImage;

    private String GroupKey;

    private String messageReceiverName;

    private ImageButton SendMessageButton,SelectImageButton;
    private EditText InputMessageText;

    private RecyclerView userMessagesListRecyclerView;

    private ArrayList<MessagesModel> messageList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageWithDateAdapter messageAdapter;

    private StorageReference MessageImageStorageRef,MessageVideoStorageRef;

    private String online_user_id;

    private String Type;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ReceivedIds = getIntent().getStringArrayListExtra("ids");

        MembersNames = getIntent().getStringArrayListExtra("members_names");

        GroupKey = getIntent().getExtras().getString("group_key");

        GroupName = getIntent().getExtras().getString("group_name");

        GroupThumbImage = getIntent().getExtras().getString("group_thumb_img");

        messageReceiverName = getIntent().getExtras().getString("user_name");

        Type = (String) getIntent().getExtras().get("type");

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        ChatToolBar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);

        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Images");

        MessageVideoStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Videos");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionBar != null)
        {
            actionBar.setDisplayShowCustomEnabled(true);
        }

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        if (Type.equals("single"))
        {
            userNameTitle.setText(messageReceiverName);

            rootRef.child("Users").child(ReceivedIds.get(0)).addValueEventListener(new ValueEventListener()
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


            rootRef.child("Messages").child(online_user_id).child(ReceivedIds.get(0)).addChildEventListener(new ChildEventListener()
            {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    MessagesModel messagesModel = dataSnapshot.getValue(MessagesModel.class);

                    messageList.add(messagesModel);

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
            userMessagesListRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(ChatActivity.this)
                    .color(Color.TRANSPARENT)
                    .size(10)
                    .build());
            messageAdapter = new MessageWithDateAdapter(messageList,ChatActivity.this);
            userMessagesListRecyclerView.setAdapter(messageAdapter);

        }
        else if (Type.equals("group"))
        {
            userNameTitle.setText(GroupName);

            Picasso.with(ChatActivity.this).load(GroupThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                    .into(userChatProfileImage, new Callback()
                    {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            Picasso.with(ChatActivity.this).load(GroupThumbImage).placeholder(R.drawable.vadim).into(userChatProfileImage);
                        }
                    });

            String s = android.text.TextUtils.join(",",MembersNames);

            userLastSeen.setText(s);


            rootRef.child("Group_Messages").child(GroupKey).child(online_user_id).addChildEventListener(new ChildEventListener()
            {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    MessagesModel messagesModel = dataSnapshot.getValue(MessagesModel.class);

                    messageList.add(messagesModel);

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
            messageAdapter = new MessageWithDateAdapter(messageList,ChatActivity.this);
            userMessagesListRecyclerView.setAdapter(messageAdapter);

        }


        SendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (Type.equals("single"))
                {
                    SendMessage();
                }
                else if (Type.equals("group"))
                {
                    SendGroupMessage();
                }
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


    private void SendMessage()
    {
        String messageText = InputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Please write your message",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message_sender_ref = "Messages/" + online_user_id + "/" + ReceivedIds.get(0);

            String message_receiver_ref = "Messages/" + ReceivedIds.get(0) + "/" + online_user_id;

            DatabaseReference user_message_key = rootRef.child("Messages").child(online_user_id).child(ReceivedIds.get(0)).push();

            String message_push_id = user_message_key.getKey();

            Map<String,Object> messageTextBody = new HashMap<>();

            DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
            String time = df.format(Calendar.getInstance().getTime());

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_TEXT);
            messageTextBody.put("time", time);
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

    private void SendGroupMessage()
    {
        String messageText = InputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Please write your message",Toast.LENGTH_SHORT).show();
        }
        else
        {
            final ArrayList<String> GroupMessages = new ArrayList<>();

            ReceivedIds.add(online_user_id);

            for (String members : ReceivedIds)
            {
                GroupMessages.add("Group_Messages/" + GroupKey + "/" + members);
            }

            DatabaseReference user_message_key = rootRef.child("Group_Messages").child(GroupKey).child(online_user_id).push();

            String message_push_id = user_message_key.getKey();

            Map<String,Object> messageTextBody = new HashMap<>();

            DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
            String time = df.format(Calendar.getInstance().getTime());

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_TEXT);
            messageTextBody.put("time", time);
            messageTextBody.put("from",online_user_id);

            Map<String,Object> messageBodyDetails = new HashMap<>();

            for (String membersRef : GroupMessages)
            {
                messageBodyDetails.put(membersRef + "/" + message_push_id, messageTextBody);
            }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data!= null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            final Uri ImageUri = result.getUri();

            if (Type.equals("single"))
            {

                final String message_sender_ref = "Messages/" + online_user_id + "/" + ReceivedIds.get(0);
                final String message_receiver_ref = "Messages/" + ReceivedIds.get(0) + "/" + online_user_id;

                DatabaseReference user_message_key = rootRef.child("Messages").child(online_user_id).child(ReceivedIds.get(0)).push();

                final String message_push_id = user_message_key.getKey();

                StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

                DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
                final String time = df.format(Calendar.getInstance().getTime());

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
                                messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_IMAGE);
                                messageTextBody.put("time", time);
                                messageTextBody.put("from",online_user_id);

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
            else if (Type.equals("group"))
            {
                final ArrayList<String> GroupMessages = new ArrayList<>();

                ReceivedIds.add(online_user_id);

                for (String members : ReceivedIds)
                {
                    GroupMessages.add("Group_Messages/" + GroupKey + "/" + members);
                }

                DatabaseReference user_message_key = rootRef.child("Group_Messages").child(GroupKey).child(online_user_id).push();

                final String message_push_id = user_message_key.getKey();

                StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

                DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
                final String time = df.format(Calendar.getInstance().getTime());

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
                                messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_IMAGE);
                                messageTextBody.put("time", time);
                                messageTextBody.put("from",online_user_id);

                                Map<String,Object> messageBodyDetails = new HashMap<>();

                                for (String membersRef : GroupMessages)
                                {
                                    messageBodyDetails.put(membersRef + "/" + message_push_id, messageTextBody);
                                }

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
                    });
                }
            }
        }
    }

    public class MessageWithDateAdapter extends RecyclerView.Adapter
    {
        private ArrayList<MessagesModel> userMessagesModelList;
        private Context context;


        MessageWithDateAdapter(ArrayList<MessagesModel> userMessagesModelList, Context context)
        {
            this.userMessagesModelList = userMessagesModelList;
            this.context = context;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view;

            if (viewType == MessagesModel.MESSAGE_TYPE_TEXT)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_text_layout_items, parent, false);

                return new MessageTextViewHolder(view);
            }
            else if (viewType == MessagesModel.MESSAGE_TYPE_IMAGE)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_image_layout_items, parent, false);

                return new MessageImageViewHolder(view);
            }
            else if (viewType == MessagesModel.MESSAGE_TYPE_VIDEO)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_video_layout_items, parent, false);

                return new MessageVideoViewHolder(view);
            }
            else if (viewType == MessagesModel.MESSAGE_TYPE_DAY_DATE)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_date_layout_items, parent, false);

                return new MessageDateViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position)
        {
            final MessagesModel object = userMessagesModelList.get(position);
            final String fromUserId = object.getFrom();

            DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

            switch (object.getType())
            {
                case MessagesModel.MESSAGE_TYPE_TEXT:

                    usersDatabaseReference.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String userThumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                            if (fromUserId.equals(online_user_id))
                            {
                                ((MessageTextViewHolder)holder).MessageIncomingTextProfileImage.setVisibility(View.GONE);
                            }
                            else
                            {
                                ((MessageTextViewHolder)holder).MessageIncomingTextProfileImage.setVisibility(View.VISIBLE);

                                ((MessageTextViewHolder)holder).setIncomingTextProfileImg(context,userThumbImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageTextViewHolder)holder).OnlineReceivedTextView.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineReceivedTextTime.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineSendTextView.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setVisibility(View.VISIBLE);

                        ((MessageTextViewHolder)holder).OnlineSendTextView.setText(object.getMessage());
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setText(object.getTime());
                    }
                    else
                    {
                        ((MessageTextViewHolder)holder).OnlineSendTextView.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineReceivedTextView.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineReceivedTextTime.setVisibility(View.VISIBLE);

                        ((MessageTextViewHolder)holder).OnlineReceivedTextView.setText(object.getMessage());
                        ((MessageTextViewHolder)holder).OnlineReceivedTextTime.setText(object.getTime());
                    }
                    break;
                case MessagesModel.MESSAGE_TYPE_IMAGE:

                    usersDatabaseReference.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String userThumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                            if (fromUserId.equals(online_user_id))
                            {
                                ((MessageImageViewHolder)holder).MessageIncomingImageProfileImage.setVisibility(View.GONE);
                            }
                            else
                            {
                                ((MessageImageViewHolder)holder).MessageIncomingImageProfileImage.setVisibility(View.VISIBLE);

                                ((MessageImageViewHolder)holder).setIncomingImageProfileImg(context,userThumbImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageImageViewHolder)holder).OnlineReceivedImageImageView.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineReceivedImageTime.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder)holder).setOnlineSendImageImageView(context,object.getMessage());
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setText(object.getTime());

                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v, object.getMessage() , null);
                            }
                        });

                    }
                    else
                    {
                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineReceivedImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineReceivedImageTime.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder)holder).setOnlineReceivedImageImageView(context,object.getMessage());
                        ((MessageImageViewHolder)holder).OnlineReceivedImageTime.setText(object.getTime());

                        ((MessageImageViewHolder)holder).OnlineReceivedImageImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v, object.getMessage() , null);
                            }
                        });
                    }
                    break;
            }
        }

        @Override
        public int getItemCount()
        {
            return userMessagesModelList.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            int i = userMessagesModelList.get(position).getType();

            switch (i)
            {
                case MessagesModel.MESSAGE_TYPE_TEXT:
                    return MessagesModel.MESSAGE_TYPE_TEXT;
                case MessagesModel.MESSAGE_TYPE_IMAGE:
                    return MessagesModel.MESSAGE_TYPE_IMAGE;
                case MessagesModel.MESSAGE_TYPE_VIDEO:
                    return MessagesModel.MESSAGE_TYPE_VIDEO;
                case MessagesModel.MESSAGE_TYPE_DAY_DATE:
                    return MessagesModel.MESSAGE_TYPE_DAY_DATE;
                default:
                    return -1;
            }
        }

        public class MessageTextViewHolder extends RecyclerView.ViewHolder
        {
            TextView OnlineSendTextView,OnlineReceivedTextView,OnlineSendTextTime,OnlineReceivedTextTime;

            ImageView MessageIncomingTextProfileImage;

            View v;

            MessageTextViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendTextView = v.findViewById(R.id.online_send_text_view);

                OnlineReceivedTextView = v.findViewById(R.id.online_received_text_view);

                OnlineSendTextTime = v.findViewById(R.id.online_send_text_time);

                OnlineReceivedTextTime = v.findViewById(R.id.online_received_text_time);

                MessageIncomingTextProfileImage = v.findViewById(R.id.message_incoming_text_profile_img);
            }

            void setIncomingTextProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_text_profile_img);

                Picasso.with(context).load(incomingThumbImg).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(incoming_thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(incomingThumbImg).placeholder(R.drawable.vadim).into(incoming_thumb_img);
                            }
                        });
            }
        }

        public class MessageImageViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineSendImageImageView,OnlineReceivedImageImageView;

            TextView OnlineSendImageTime,OnlineReceivedImageTime;

            ImageView MessageIncomingImageProfileImage;

            View v;

            MessageImageViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendImageImageView = v.findViewById(R.id.online_send_image_image_view);

                OnlineReceivedImageImageView = v.findViewById(R.id.online_received_image_image_view);

                OnlineSendImageTime = v.findViewById(R.id.online_send_image_time);

                OnlineReceivedImageTime = v.findViewById(R.id.online_received_image_time);

                MessageIncomingImageProfileImage = v.findViewById(R.id.message_incoming_image_profile_img);
            }

            void setIncomingImageProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_image_profile_img);

                Picasso.with(context).load(incomingThumbImg).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(incoming_thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(incomingThumbImg).placeholder(R.drawable.vadim).into(incoming_thumb_img);
                            }
                        });
            }

            void setOnlineSendImageImageView(final Context context, final String sendImage)
            {
                final ImageView send_img = v.findViewById(R.id.online_send_image_image_view);

                Picasso.with(context).load(sendImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(send_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(sendImage).placeholder(R.drawable.vadim).into(send_img);
                            }
                        });
            }

            void setOnlineReceivedImageImageView(final Context context, final String receivedImage)
            {
                final ImageView received_img = v.findViewById(R.id.online_received_image_image_view);

                Picasso.with(context).load(receivedImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(received_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(receivedImage).placeholder(R.drawable.vadim).into(received_img);
                            }
                        });
            }
        }

        public class MessageVideoViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineSendVideoImageView,OnlineReceivedVideoImageView,OnlineSendVideoPlayButton,OnlineReceivedVideoPlayButton;

            TextView OnlineSendVideoTime,OnlineReceivedVideoTime;

            View v;

            MessageVideoViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendVideoImageView = v.findViewById(R.id.online_send_video_image_view);

                OnlineReceivedVideoImageView = v.findViewById(R.id.online_received_video_image_view);

                OnlineSendVideoTime = v.findViewById(R.id.online_send_video_time);

                OnlineReceivedVideoTime = v.findViewById(R.id.online_received_video_time);

                OnlineSendVideoPlayButton = v.findViewById(R.id.online_send_video_play_button);

                OnlineReceivedVideoPlayButton = v.findViewById(R.id.online_received_video_play_button);
            }

            void setIncomingVideoProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_video_profile_img);

                Picasso.with(context).load(incomingThumbImg).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(incoming_thumb_img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(context).load(incomingThumbImg).placeholder(R.drawable.vadim).into(incoming_thumb_img);
                            }
                        });
            }
        }

        public class MessageDateViewHolder extends RecyclerView.ViewHolder
        {
            TextView OnlineDateLine,OnlineChattingDayDateTextView;

            View v;

            public MessageDateViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineDateLine = v.findViewById(R.id.online_date_line);

                OnlineChattingDayDateTextView = v.findViewById(R.id.online_chatting_day_date_text_view);
            }
        }
    }
}
