package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deffe.macros.grindersouls.Actions.EmojIconActions;
import com.deffe.macros.grindersouls.Helper.EmojiconEditText;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
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

    private EmojiconEditText InputMessageText;
    private View rootView;
    private EmojIconActions emojIcon;

    private ImageButton emojiButton;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private ArrayList<String> MembersNames = new ArrayList<>();

    private String GroupName;

    private String GroupThumbImage;

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private String GroupKey;

    private String messageReceiverName;

    private ImageButton SendMessageButton,SelectImageButton;

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

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.chat_emoji);
        InputMessageText = findViewById(R.id.input_message);

        emojIcon = new EmojIconActions(this, rootView, InputMessageText, emojiButton);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

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
                SendSingleOrGroupMessage(Type);
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
                        galleryVideoIntent.setType("video/*");
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

    private void SendSingleOrGroupMessage(String type)
    {
        String messageText = InputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Please write your message",Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (type.equals("single"))
            {
                if (checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleTextDetails(online_user_id,ReceivedIds,messageText);
                }
                else if (!checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleTextDetails(online_user_id,ReceivedIds,messageText);
                }
            }
            else if (type.equals("group"))
            {
                if (checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupTextDetails(online_user_id,ReceivedIds,messageText,GroupKey);
                }
                else if (!checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupTextDetails(online_user_id,ReceivedIds,messageText,GroupKey);
                }
            }
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

            Uri storageUri = null;

            final File localFile = new File(Environment.getExternalStorageDirectory()
                    + "/GrindersSouls/Grinders Chat/Images");

            if (!localFile.exists())
            {
                localFile.mkdirs();
            }
            try
            {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                File mediaFile = new File(localFile.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

                storageUri = copyFileToFolder(new File(getFilePathFromUri(ChatActivity.this,ImageUri)), mediaFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (Type.equals("single"))
            {
                if (checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleImageDetails(online_user_id,ReceivedIds,ImageUri,storageUri);
                }
                else if (!checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleImageDetails(online_user_id,ReceivedIds,ImageUri,storageUri);
                }
            }
            else if (Type.equals("group"))
            {
                if (checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupImageDetails(online_user_id,ReceivedIds,ImageUri,storageUri,GroupKey);
                }
                else if (!checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupImageDetails(online_user_id,ReceivedIds,ImageUri,storageUri,GroupKey);
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
                videoIntent.putExtra("group_key",GroupKey);
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
                    videoIntent.putExtra("group_key",GroupKey);
                    videoIntent.putExtras(bundle);
                    startActivity(videoIntent);
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

    private Uri copyFileToFolder(File sourceFile, File destinationFile) throws IOException
    {
        FileChannel source;
        FileChannel destination;

        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destinationFile).getChannel();

        if (source != null)
        {
            destination.transferFrom(source,0,source.size());
        }

        if (source != null)
        {
            source.close();
        }
        destination.close();

        return Uri.fromFile(destinationFile);
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

            Toast.makeText(context, "Object " + object.getType(), Toast.LENGTH_SHORT).show();

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
                        ((MessageTextViewHolder)holder).OnlineReceivedTextMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextViewHolder)holder).OnlineSendTextView.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineSendTextMessageLinearLayout.setVisibility(View.VISIBLE);

                        ((MessageTextViewHolder)holder).OnlineSendTextView.setText(object.getMessage());
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setText(object.getTime());
                    }
                    else
                    {
                        ((MessageTextViewHolder)holder).OnlineSendTextView.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineSendTextTime.setVisibility(View.GONE);
                        ((MessageTextViewHolder)holder).OnlineSendTextMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextViewHolder)holder).OnlineReceivedTextView.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineReceivedTextTime.setVisibility(View.VISIBLE);
                        ((MessageTextViewHolder)holder).OnlineReceivedTextMessageLinearLayout.setVisibility(View.VISIBLE);

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
                        ((MessageImageViewHolder)holder).OnlineReceivedImageMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineSendImageMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder)holder).setOnlineSendImageImageView(context,object.getRef());
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setText(object.getTime());

                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v, object.getRef() , null);
                            }
                        });

                    }
                    else
                    {
                        ((MessageImageViewHolder)holder).OnlineSendImageImageView.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineSendImageTime.setVisibility(View.GONE);
                        ((MessageImageViewHolder)holder).OnlineSendImageMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageImageViewHolder)holder).OnlineReceivedImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineReceivedImageTime.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder)holder).OnlineReceivedImageMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder)holder).setOnlineReceivedImageImageView(context,object.getRef());
                        ((MessageImageViewHolder)holder).OnlineReceivedImageTime.setText(object.getTime());

                        ((MessageImageViewHolder)holder).OnlineReceivedImageImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                new PhotoFullPopupWindow(context, R.layout.popup_photo_full, v, object.getRef() , null);
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
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoMessageRelativeLayout.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoPlayButton.setVisibility(View.GONE);

                        ((MessageVideoViewHolder)holder).OnlineSendVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoMessageRelativeLayout.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoPlayButton.setVisibility(View.VISIBLE);

                        GlideApp.with(ChatActivity.this).load(object.getRef()).centerCrop().into(((MessageVideoViewHolder)holder).OnlineSendVideoImageView);

                        ((MessageVideoViewHolder) holder).OnlineSendVideoPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent videoIntent = new Intent(ChatActivity.this,ChatVideoPlayActivity.class);
                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                videoIntent.putExtra("purpose","play");
                                videoIntent.putExtra("videoUri", object.getRef());
                                startActivity(videoIntent);
                            }
                        });


                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setText(object.getTime());
                    }
                    else
                    {
                        ((MessageVideoViewHolder)holder).OnlineSendVideoImageView.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoTime.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoMessageRelativeLayout.setVisibility(View.GONE);
                        ((MessageVideoViewHolder)holder).OnlineSendVideoPlayButton.setVisibility(View.GONE);

                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoTime.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoMessageRelativeLayout.setVisibility(View.VISIBLE);

                        GlideApp.with(ChatActivity.this).load(object.getRef()).centerCrop().into(((MessageVideoViewHolder)holder).OnlineReceivedVideoImageView);

                        ((MessageVideoViewHolder)holder).OnlineReceivedVideoTime.setText(object.getTime());
                    }
                    break;

                case MessagesModel.MESSAGE_TYPE_DAY_DATE:

                    ((MessageDateViewHolder)holder).OnlineChattingDayDateTextView.setVisibility(View.VISIBLE);

                    try
                    {
                        ((MessageDateViewHolder)holder).OnlineChattingDayDateTextView.setText(formatToYesterdayOrToday(object.getTime()));
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
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
            LinearLayout OnlineSendTextMessageLinearLayout,OnlineReceivedTextMessageLinearLayout;

            View v;

            MessageTextViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendTextMessageLinearLayout = v.findViewById(R.id.online_send_text_message_linear_layout);
                OnlineReceivedTextMessageLinearLayout = v.findViewById(R.id.online_received_text_message_linear_layout);
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
            ImageView OnlineReceivedImageImageView,OnlineSendImageImageView;
            TextView OnlineSendImageTime,OnlineReceivedImageTime;
            RelativeLayout OnlineSendImageMessageRelativeLayout,OnlineReceivedImageMessageRelativeLayout;
            ImageView MessageIncomingImageProfileImage;

            View v;

            MessageImageViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendImageMessageRelativeLayout = v.findViewById(R.id.online_send_image_message_relative_layout);
                OnlineReceivedImageMessageRelativeLayout = v.findViewById(R.id.online_received_image_message_relative_layout);
                OnlineSendImageImageView = v.findViewById(R.id.online_send_image_image_view);
                OnlineReceivedImageImageView = v.findViewById(R.id.online_received_image_image_view);
                OnlineSendImageTime = v.findViewById(R.id.online_send_image_time);
                OnlineReceivedImageTime = v.findViewById(R.id.online_received_image_time);
                MessageIncomingImageProfileImage = v.findViewById(R.id.message_incoming_image_profile_img);
            }

            void setIncomingImageProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_image_profile_img);

                Picasso.with(context).load(incomingThumbImg).networkPolicy(NetworkPolicy.OFFLINE)
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

                Picasso.with(context).load(sendImage).networkPolicy(NetworkPolicy.OFFLINE)
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

                Picasso.with(context).load(receivedImage).networkPolicy(NetworkPolicy.OFFLINE)
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
            ImageView OnlineReceivedVideoImageView,OnlineSendVideoPlayButton,OnlineReceivedVideoPlayButton,OnlineSendVideoImageView;
            TextView OnlineSendVideoTime,OnlineReceivedVideoTime;
            ImageView MessageIncomingVideoProfileImage;
            RelativeLayout OnlineSendVideoMessageRelativeLayout,OnlineReceivedVideoMessageRelativeLayout;

            View v;

            MessageVideoViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendVideoMessageRelativeLayout = v.findViewById(R.id.online_send_video_message_relative_layout);
                OnlineReceivedVideoMessageRelativeLayout = v.findViewById(R.id.online_received_video_message_relative_layout);
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

                Picasso.with(context).load(incomingThumbImg).networkPolicy(NetworkPolicy.OFFLINE)
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

        class MessageDateViewHolder extends RecyclerView.ViewHolder
        {
            TextView OnlineChattingDayDateTextView;

            View v;

            MessageDateViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineChattingDayDateTextView = v.findViewById(R.id.online_chatting_day_date_text_view);
            }
        }
    }

    private String getTodayDate()
    {
        DateFormat todayDate = new SimpleDateFormat("d MMM yyyy",Locale.US);

        return todayDate.format(Calendar.getInstance().getTime());
    }

    private String formatToYesterdayOrToday(String date) throws ParseException
    {
        Date dateTime = new SimpleDateFormat("d MMM yyyy", Locale.US).parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        {
            return "Today";
        }
        else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR))
        {
            return "Yesterday";
        }
        else
        {
            return date;
        }
    }

    private void storeDateRef(String online_key,String receiver_key)
    {
        SharedPreferences preferences = this.getSharedPreferences(online_key,MODE_PRIVATE);

        if (preferences != null)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(receiver_key,getTodayDate());
            editor.apply();
        }
    }

    private String readDateRef(Context context,String online_key,String receiver_key)
    {
        String todayDate,checkDate = null;

        SharedPreferences date = context.getSharedPreferences(online_key,MODE_PRIVATE);

        if (date != null)
        {
            checkDate = date.getString(receiver_key,null);
        }

        if (date == null)
        {
            todayDate = null;
        }
        else
        {
            todayDate = checkDate;
        }

        return todayDate;
    }

    private void updateSingleDateRef(String online_key,String receiver_key)
    {
        storeDateRef(online_key,receiver_key);

        final String message_sender_ref = "Messages/" + online_key + "/" + receiver_key;
        final String message_receiver_ref = "Messages/" + receiver_key + "/" + online_key;

        DatabaseReference date_key = rootRef.child("Messages").child(online_key).child(receiver_key).push();

        final String date_push_id = date_key.getKey();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("message","");
        messageDate.put("seen",true);
        messageDate.put("type",MessagesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("time", getTodayDate());
        messageDate.put("ref", "");
        messageDate.put("size","");
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);

        Map<String,Object> messageBodyDetails = new HashMap<>();

        messageBodyDetails.put(message_sender_ref + "/" + date_push_id, messageDate);

        messageBodyDetails.put(message_receiver_ref + "/" + date_push_id, messageDate);

        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {
                if (databaseError != null)
                {
                    Log.d("Chat_Log", databaseError.getMessage());
                }
            }
        });
    }

    private void updateGroupDateRef(String online_key,String group_key,ArrayList<String> Ids)
    {
        storeDateRef(online_key,group_key);

        final ArrayList<String> GroupMessages = new ArrayList<>();

        Ids.add(online_key);

        for (String members : Ids)
        {
            GroupMessages.add("Group_Messages/" + group_key + "/" + members);
        }

        DatabaseReference date_key = rootRef.child("Group_Messages").child(group_key).child(online_key).push();

        String date_push_id = date_key.getKey();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("message","");
        messageDate.put("seen",true);
        messageDate.put("type",MessagesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("time", getTodayDate());
        messageDate.put("ref", "");
        messageDate.put("size","");
        messageDate.put("from","");
        messageDate.put("key",date_push_id);

        Map<String,Object> messageBodyDetails = new HashMap<>();

        for (String membersRef : GroupMessages)
        {
            messageBodyDetails.put(membersRef + "/" + date_push_id, messageDate);
        }

        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {
                if (databaseError != null)
                {
                    Log.d("Chat_Log", databaseError.getMessage());
                }
            }
        });
    }

    private boolean checkSingleDate(String online,String receiver)
    {
        String today = readDateRef(ChatActivity.this,online,receiver);

        boolean result = false;

        if (today == null)
        {
            updateSingleDateRef(online,receiver);

            result = true;
        }
        else
        {
            try
            {
                String checkDate = formatToYesterdayOrToday(today);

                if (!checkDate.equals("Today"))
                {
                    updateSingleDateRef(online,receiver);

                    result = true;
                }
                else
                {
                    result = false;
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    private boolean checkGroupDate(String online,String groupKey,ArrayList<String> groupMemberIds)
    {
        String today = readDateRef(ChatActivity.this,online,groupKey);

        boolean result = false;

        if (today == null)
        {
            updateGroupDateRef(online,groupKey,groupMemberIds);

            result = true;
        }
        else
        {
            try
            {
                String checkDate = formatToYesterdayOrToday(today);

                if (!checkDate.equals("Today"))
                {
                    updateGroupDateRef(online,groupKey,groupMemberIds);

                    result = true;
                }
                else
                {
                    result = false;
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void storeSingleTextDetails(String online,ArrayList<String> Receivers,String messageText)
    {
        String message_sender_ref = "Messages/" + online + "/" + Receivers.get(0);

        String message_receiver_ref = "Messages/" + Receivers.get(0) + "/" + online;

        DatabaseReference user_message_key = rootRef.child("Messages").child(online).child(ReceivedIds.get(0)).push();

        String message_push_id = user_message_key.getKey();

        Map<String,Object> messageTextBody = new HashMap<>();

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        messageTextBody.put("message",messageText);
        messageTextBody.put("seen",false);
        messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_TEXT);
        messageTextBody.put("time", time);
        messageTextBody.put("from",online);
        messageTextBody.put("ref","");
        messageTextBody.put("size","");
        messageTextBody.put("key",message_push_id);

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

    private void storeGroupTextDetails(String online,ArrayList<String> Receivers,String messageText,String groupKey)
    {
        final ArrayList<String> GroupMessages = new ArrayList<>();

        Receivers.add(online);

        for (String members : Receivers)
        {
            GroupMessages.add("Group_Messages/" + groupKey + "/" + members);
        }

        DatabaseReference user_message_key = rootRef.child("Group_Messages").child(groupKey).child(online).push();

        String message_push_id = user_message_key.getKey();

        Map<String,Object> messageTextBody = new HashMap<>();

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        messageTextBody.put("message",messageText);
        messageTextBody.put("seen",false);
        messageTextBody.put("type",MessagesModel.MESSAGE_TYPE_TEXT);
        messageTextBody.put("time", time);
        messageTextBody.put("from",online);
        messageTextBody.put("ref","");
        messageTextBody.put("size","");
        messageTextBody.put("key",message_push_id);

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

    private void storeSingleImageDetails(final String online, ArrayList<String> Receivers, Uri ImageUri, Uri storageUri)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receivers.get(0);
        final String message_receiver_ref = "Messages/" + Receivers.get(0) + "/" + online;

        DatabaseReference user_message_key = rootRef.child("Messages").child(online).child(Receivers.get(0)).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

        DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        final Uri finalStorageUri = storageUri;

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
                                messageTextBody.put("ref", finalStorageUri.toString());
                                messageTextBody.put("size",size);
                                messageTextBody.put("from",online);
                                messageTextBody.put("key",message_push_id);

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

    private void storeGroupImageDetails(final String online, ArrayList<String> Receivers, Uri ImageUri, Uri storageUri, String groupKey)
    {
        final ArrayList<String> GroupMessages = new ArrayList<>();

        Receivers.add(online);

        for (String members : Receivers)
        {
            GroupMessages.add("Group_Messages/" + groupKey + "/" + members);
        }

        DatabaseReference user_message_key = rootRef.child("Group_Messages").child(groupKey).child(online).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

        DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        final Uri finalStorageUri = storageUri;

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
                                messageTextBody.put("ref", finalStorageUri.toString());
                                messageTextBody.put("size",size);
                                messageTextBody.put("from",online);
                                messageTextBody.put("key",message_push_id);

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