package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private static final int UPLOAD_VIDEO = 10;

    private ArrayList<String> MembersNames = new ArrayList<>();

    private String GroupName;

    private String GroupThumbImage;

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private String GroupKey;

    private String messageReceiverName;

    private ImageButton SendMessageButton,SelectImageButton;
    private EditText InputMessageText;

    private RecyclerView userMessagesListRecyclerView;

    private ArrayList<MessagesModel> messageList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageWithDateAdapter messageAdapter;

    private StorageReference MessageImageStorageRef;

    private String online_user_id;

    private String Type;

    private Uri fileUri;

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

        ImageButton AddFiles = findViewById(R.id.add_files);

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

        AddFiles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                View view1 = getLayoutInflater().inflate(R.layout.bottom_sheet,null);

                final Dialog bottomSheet = new Dialog(ChatActivity.this,R.style.MaterialDialogSheet);
                bottomSheet.setContentView(view1);
                bottomSheet.setCancelable(true);
                bottomSheet.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,150);
                bottomSheet.getWindow().setGravity(Gravity.BOTTOM);
                bottomSheet.show();

                ImageView recordVideo = bottomSheet.findViewById(R.id.record_video);
                ImageView galleryVideo = bottomSheet.findViewById(R.id.choose_video_from_gallery);

                galleryVideo.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent galleryVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                        galleryVideoIntent.setType("video/*");
                        galleryVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(galleryVideoIntent,TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE);
                    }
                });

                recordVideo.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(videoIntent,CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
                    }
                });
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

    public Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private static File getOutputMediaFile(int type)
    {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/" + "GrindersSouls/Grinders Chat", "Videos");

        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d("Videos","Oops! failed create " + "Videos" + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }
        else
        {
            return null;
        }

        return mediaFile;
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

                final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

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

                                filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata)
                                    {
                                        long sizeInMb = storageMetadata.getSizeBytes();

                                        String size = getStringSizeFromFile(sizeInMb);

                                        Map<String,Object> messageTextBody = new HashMap<>();

                                        messageTextBody.put("message",downloadUrl);
                                        messageTextBody.put("seen",false);
                                        messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_IMAGE);
                                        messageTextBody.put("time", time);
                                        messageTextBody.put("storage_ref",ImageUri.toString());
                                        messageTextBody.put("size",size);
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
                                });

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

                final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

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

                                filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata)
                                    {
                                        long getSize = storageMetadata.getSizeBytes();

                                        String size = getStringSizeFromFile(getSize);

                                        Map<String,Object> messageTextBody = new HashMap<>();

                                        messageTextBody.put("message",downloadUrl);
                                        messageTextBody.put("seen",false);
                                        messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_IMAGE);
                                        messageTextBody.put("time", time);
                                        messageTextBody.put("storage_ref",ImageUri.toString());
                                        messageTextBody.put("size",size);
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
                                });


                            }
                        }
                    });
                }
            }
        }
        else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri videoUri = data.getData();

                File file = null;

                if (videoUri != null)
                {
                    file = new File(videoUri.getPath());
                }

                Uri newVideoUri = Uri.fromFile(file);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("ids",ReceivedIds);

                Intent videoIntent = new Intent(ChatActivity.this,ChatVideoPlayActivity.class);
                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                videoIntent.putExtra("videoUri",newVideoUri.toString());
                videoIntent.putExtra("videoOriginalUri",newVideoUri.toString());
                videoIntent.putExtra("purpose","upload");
                videoIntent.putExtra("type","camera");
                videoIntent.putExtra("chat_type",Type);
                videoIntent.putExtras(bundle);
                startActivity(videoIntent);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "User cancelled video recording", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri videoUri = data.getData();

                String fileUri = getFilePathFromUri(ChatActivity.this,videoUri);

                if (videoUri != null)
                {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("ids",ReceivedIds);

                    Intent videoIntent = new Intent(ChatActivity.this, ChatVideoPlayActivity.class);
                    videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                    videoIntent.putExtra("videoUri", fileUri);
                    videoIntent.putExtra("videoOriginalUri", videoUri.toString());
                    videoIntent.putExtra("type", "gallery");
                    videoIntent.putExtra("purpose","upload");
                    videoIntent.putExtra("chat_type",Type);
                    videoIntent.putExtras(bundle);
                    startActivityForResult(videoIntent,UPLOAD_VIDEO);
                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "User cancelled video recording", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static String getStringSizeFromFile(long size)
    {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeMb;
        float sizeTera = sizeGb * sizeGb;

        if (size < sizeMb)
        {
            return decimalFormat.format(size / sizeKb) + " Kb";
        }
        else if (size < sizeGb)
        {
            return decimalFormat.format(size / sizeMb) + " Mb";
        }
        else if (size < sizeTera)
        {
            return decimalFormat.format(size / sizeGb) + " Gb";
        }
        return "";
    }

    private String getFilePathFromUri(Context context,Uri contentUri)
    {
        String filePath = null;

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat)
        {
            filePath = generateFromKitKat(context, contentUri);
        }

        if (filePath != null)
        {
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(contentUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

                filePath = cursor.getString(column_index);
            }

            cursor.close();
        }
        return filePath == null ? contentUri.getPath() : filePath;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String generateFromKitKat(Context context, Uri contentUri)
    {
        String filePath = null;

        if (DocumentsContract.isDocumentUri(context,contentUri))
        {
            String wholeID = DocumentsContract.getDocumentId(contentUri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};
            String sel = MediaStore.Video.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,column,sel,new String[]{id},null);

            int columnIndex = 0;

            if (cursor != null)
            {
                columnIndex = cursor.getColumnIndex(column[0]);
            }

            if (cursor != null && cursor.moveToFirst())
            {
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }

        }

        return filePath;
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

                case MessagesModel.MESSAGE_TYPE_VIDEO:

                    usersDatabaseReference.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String userThumbImage = dataSnapshot.child("user_thumb_img").getValue().toString();

                            if (fromUserId.equals(online_user_id))
                            {
                                ((MessageVideoViewHolder)holder).MessageIncomingVideoProfileImage.setVisibility(View.GONE);
                            }
                            else
                            {
                                ((MessageVideoViewHolder)holder).MessageIncomingVideoProfileImage.setVisibility(View.VISIBLE);

                                ((MessageVideoViewHolder)holder).setIncomingVideoProfileImg(context,userThumbImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoImageView.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoTime.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setVisibility(View.VISIBLE);

                        GlideApp.with(ChatActivity.this).load(object.getMessage()).centerCrop().into(((MessageVideoViewHolder)holder).OnlineSendVideoImageView);

                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setText(object.getTime());
                    }
                    else
                    {
                        ((MessageVideoViewHolder)holder).OnlineSendVideoImageView.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoTime.setVisibility(View.VISIBLE);

                        GlideApp.with(ChatActivity.this).load(object.getMessage()).centerCrop().into(((MessageVideoViewHolder)holder).OnlineReceivedVideoImageView);

                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoTime.setText(object.getTime());
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
            ImageView OnlineReceivedImageImageView;

            RoundedImageView OnlineSendImageImageView;

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
                final RoundedImageView send_img = v.findViewById(R.id.online_send_image_image_view);

                Transformation transformation = new RoundedTransformationBuilder()
                        .cornerRadius(0,10)
                        .cornerRadius(2,10)
                        .cornerRadius(3,10)
                        .oval(false)
                        .build();

                Picasso.with(context).load(sendImage).fit().transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
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

            ImageView MessageIncomingVideoProfileImage;

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

                MessageIncomingVideoProfileImage = v.findViewById(R.id.message_incoming_video_profile_img);
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
