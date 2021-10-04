package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.deffe.macros.grindersouls.Actions.EmojIconActions;
import com.deffe.macros.grindersouls.Helper.EmojiconEditText;
import com.deffe.macros.grindersouls.Helper.EmojiconTextView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.text.format.DateFormat.*;

public class ChatActivity extends AppCompatActivity
{
    private final static String TAG = ChatActivity.class.getSimpleName();

    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;

    private EmojiconEditText InputMessageText;
    private View rootView;
    private EmojIconActions emojIcon;

    private ImageButton emojiButton;

    private FirebaseFirestore rootRef;

    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private ArrayList<String> MembersNames = new ArrayList<>();

    private String GroupName;

    private String GroupThumbImage;

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private String GroupKey;

    private String messageReceiverName;

    private ArrayList<MessageTypesModel> messageList = new ArrayList<>();

    private StorageReference MessageImageStorageRef;

    private String online_user_id;

    private String Type;

    private Uri fileUri;

    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Intent chatIntent = getIntent();
        final String action = chatIntent.getAction();
        final String type = chatIntent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null)
        {
            if (type.equals("single"))
            {
                Type = type;

                ReceivedIds = chatIntent.getStringArrayListExtra("ids");
                messageReceiverName = chatIntent.getExtras().getString("user_name");
            }
            else if (type.equals("group"))
            {
                Type = type;

                ReceivedIds = chatIntent.getStringArrayListExtra("ids");
                MembersNames = chatIntent.getStringArrayListExtra("members_names");
                GroupKey = chatIntent.getExtras().getString("group_key");
                GroupName = chatIntent.getExtras().getString("group_name");
                GroupThumbImage = chatIntent.getExtras().getString("group_thumb_img");
            }
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        Toolbar chatToolBar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolBar);

        rootRef = FirebaseFirestore.getInstance();

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.chat_emoji);
        InputMessageText = findViewById(R.id.input_message);

        InputMessageText.setKeyBoardInputCallbackListener(new EmojiconEditText.KeyBoardInputCallbackListener()
        {
            @Override
            public void onCommitContent(InputContentInfoCompat inputContentInfoCompat, int flags, Bundle opts)
            {
                Uri uri = inputContentInfoCompat.getContentUri();

                Toast.makeText(ChatActivity.this, "Please wait while processing your gif", Toast.LENGTH_LONG).show();

                if (Type != null)
                {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("ids",ReceivedIds);

                    Intent videoIntent = new Intent(ChatActivity.this, GifPlayActivity.class);
                    videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                    videoIntent.setType(Type);
                    videoIntent.putExtra("gifUri", uri.toString());
                    videoIntent.putExtra("purpose", "upload");
                    videoIntent.putExtra("group_key", GroupKey);
                    videoIntent.putExtras(bundle);
                    startActivity(videoIntent);
                }
            }
        });

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

        TextView userNameTitle = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = findViewById(R.id.custom_profile_img);

        ImageButton sendMessageButton = findViewById(R.id.send_message_btn);
        ImageButton selectImageButton = findViewById(R.id.select_img);

        ImageButton AddFiles = findViewById(R.id.add_files);

        if (Type != null)
        {
            LinearLayoutManager linearLayoutManager;
            if (Type.equals("single"))
            {
                userNameTitle.setText(messageReceiverName);

                rootRef.collection("Users").document(ReceivedIds.get(0)).addSnapshotListener(new EventListener<DocumentSnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }

                        if (documentSnapshot != null && documentSnapshot.exists())
                        {
                            final String userThumb = documentSnapshot.getString("user_thumb_img");

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

                            if (documentSnapshot.contains("online"))
                            {
                                final String online_status = documentSnapshot.get("online").toString();

                                if (online_status.equals("true"))
                                {
                                    userLastSeen.setText("Online");
                                }
                                else
                                {
                                    long last_seen = Long.parseLong(online_status);
                                    String lastSeenDisplayTime = LastSeenTime.getTimeAgo(last_seen, getApplicationContext());
                                    userLastSeen.setText(lastSeenDisplayTime);
                                }
                            }
                        }
                    }
                });

                final RecyclerView userMessagesListRecyclerView = findViewById(R.id.messages_list_of_users);
                linearLayoutManager = new LinearLayoutManager(this);
                userMessagesListRecyclerView.setHasFixedSize(true);
                userMessagesListRecyclerView.setLayoutManager(linearLayoutManager);
                final MessageWithDateAdapter messageAdapter = new MessageWithDateAdapter(messageList, ChatActivity.this);
                userMessagesListRecyclerView.setAdapter(messageAdapter);

                Query query = rootRef.collection("Messages").document(online_user_id).collection(ReceivedIds.get(0)).orderBy("date", Query.Direction.ASCENDING);

                query.addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }

                        if (queryDocumentSnapshots != null)
                        {
                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges())
                            {
                                if (documentChange.getType() == DocumentChange.Type.ADDED)
                                {
                                    MessageTypesModel messageTypesModel = documentChange.getDocument().toObject(MessageTypesModel.class);

                                    messageList.add(messageTypesModel);

                                    userMessagesListRecyclerView.smoothScrollToPosition(documentChange.getNewIndex());

                                    messageAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
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

                String s = android.text.TextUtils.join(",", MembersNames);

                userLastSeen.setText(s);

                RecyclerView userMessagesListRecyclerView = findViewById(R.id.messages_list_of_users);
                linearLayoutManager = new LinearLayoutManager(this);
                userMessagesListRecyclerView.setHasFixedSize(true);
                userMessagesListRecyclerView.setLayoutManager(linearLayoutManager);
                final MessageWithDateAdapter messageAdapter = new MessageWithDateAdapter(messageList, ChatActivity.this);
                userMessagesListRecyclerView.setAdapter(messageAdapter);

                Query query = rootRef.collection("Group_Messages").document(GroupKey).collection(online_user_id).orderBy("date", Query.Direction.ASCENDING);

                query.addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                        }

                        if (queryDocumentSnapshots != null)
                        {
                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges())
                            {
                                if (documentChange.getType() == DocumentChange.Type.ADDED)
                                {
                                    MessageTypesModel messageTypesModel = documentChange.getDocument().toObject(MessageTypesModel.class);

                                    messageList.add(messageTypesModel);

                                    messageAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }
        }

        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(ChatActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(ChatActivity.this))
                    {
                        if (Type != null)
                        {
                            SendSingleOrGroupMessage(Type);
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.root_view),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.root_view),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        AddFiles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(ChatActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(ChatActivity.this))
                    {
                        if (Type != null)
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
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.root_view),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.root_view),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(ChatActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(ChatActivity.this))
                    {
                        if (!isUploading)
                        {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1 ,1)
                                    .start(ChatActivity.this);
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this, "Please wait another image is sending", Toast.LENGTH_LONG).show();
                        }

                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.root_view),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.root_view),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

   /* private void enableSwipe()
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT)
                {
                    final MessageTypesModel deleteModel = messageList.get(position);
                    final int deletePosition = position;
                    messageAdapter.removeItems(position);

                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(),"removed from RecyclerView",Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            messageAdapter.restoreItems(deleteModel,deletePosition);
                        }
                    });
                    snackbar.setActionTextColor(Color.GREEN);
                    snackbar.show();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
            {
                Bitmap icon;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
                {
                    View view = viewHolder.itemView;

                    float height = (float) view.getBottom() - (float) view.getTop();
                    float width = height / 3;

                    if (dX > 0)
                    {
                        paint.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) view.getLeft(),(float) view.getTop(),dX,(float) view.getBottom());
                        c.drawRect(background,paint);
                        icon = BitmapFactory.decodeResource(getResources(),R.drawable.delete);
                        RectF icon_dest = new RectF((float) view.getLeft() + width, (float) view.getTop() + width, (float) view.getLeft()+2 * width, (float) view.getBottom()-width);
                        c.drawBitmap(icon,null,icon_dest,paint);
                    }
                    else
                    {
                        paint.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) view.getLeft(),(float) view.getTop(),dX,(float) view.getBottom());
                        c.drawRect(background,paint);
                        icon = BitmapFactory.decodeResource(getResources(),R.drawable.delete);
                        RectF icon_dest = new RectF((float) view.getLeft() + width, (float) view.getTop() + width, (float) view.getLeft()+2 * width, (float) view.getBottom()-width);
                        c.drawBitmap(icon,null,icon_dest,paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(userMessagesListRecyclerView);
    }*/

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
            InputMessageText.setText("");
            if (type.equals("single"))
            {
                if (checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleTextPlainDetails(online_user_id,ReceivedIds,messageText);
                }
                else if (!checkSingleDate(online_user_id,ReceivedIds.get(0)))
                {
                    storeSingleTextPlainDetails(online_user_id,ReceivedIds,messageText);
                }
            }
            else if (type.equals("group"))
            {
                if (checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupTextPlainDetails(online_user_id,ReceivedIds,messageText,GroupKey);
                }
                else if (!checkGroupDate(online_user_id,GroupKey,ReceivedIds))
                {
                    storeGroupTextPlainDetails(online_user_id,ReceivedIds,messageText,GroupKey);
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

                if (Type != null)
                {
                    Intent videoIntent = new Intent(ChatActivity.this, VideoPlayActivity.class);
                    videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                    videoIntent.setType(Type);
                    videoIntent.putExtra("videoUri", newVideoUri.toString());
                    videoIntent.putExtra("videoOriginalUri", newVideoUri.toString());
                    videoIntent.putExtra("purpose", "upload");
                    videoIntent.putExtra("group_key", GroupKey);
                    videoIntent.putExtra("type", "camera");
                    videoIntent.putExtra("activity","ChatActivity");
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

                    if (Type != null)
                    {
                        Intent videoIntent = new Intent(ChatActivity.this, VideoPlayActivity.class);
                        videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                        videoIntent.setType(Type);
                        videoIntent.putExtra("videoUri", fileUri);
                        videoIntent.putExtra("videoOriginalUri", videoUri.toString());
                        videoIntent.putExtra("purpose", "upload");
                        videoIntent.putExtra("type","gallery");
                        videoIntent.putExtra("group_key", GroupKey);
                        videoIntent.putExtra("activity","ChatActivity");
                        videoIntent.putExtras(bundle);
                        startActivity(videoIntent);
                    }
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
        private ArrayList<MessageTypesModel> userMessageTypesModelList;
        private Context context;
        private LayoutInflater layoutInflater;

        MessageWithDateAdapter(ArrayList<MessageTypesModel> userMessageTypesModelList, Context context)
        {
            this.userMessageTypesModelList = userMessageTypesModelList;
            this.context = context;
        }

        public void removeItems(int position)
        {
            userMessageTypesModelList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,userMessageTypesModelList.size());
        }

        public void restoreItems(MessageTypesModel messageTypesModel,int position)
        {
            userMessageTypesModelList.add(position,messageTypesModel);
            notifyItemInserted(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view;

            if (viewType == MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_text_plain_layout_items, parent, false);

                return new MessageTextPlainViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_TEXT_LINK)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_text_link_layout_items, parent, false);

                return new MessageTextLinkViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_DOCUMENT)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_document_layout_items, parent, false);

                return new MessageDocumentViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_IMAGE)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_image_layout_items, parent, false);

                return new MessageImageViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_GIF)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_gif_layout_items, parent, false);

                return new MessageGifViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_AUDIO)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_audio_layout_items, parent, false);

                return new MessageAudioViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_VIDEO)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_send_video_layout_items, parent, false);

                return new MessageVideoViewHolder(view);
            }
            else if (viewType == MessageTypesModel.MESSAGE_TYPE_DAY_DATE)
            {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_date_layout_items, parent, false);

                return new MessageDateViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position)
        {
            final MessageTypesModel object = userMessageTypesModelList.get(position);
            final String fromUserId = object.getFrom();

            final String time = object.getTime();

            DocumentReference usersDatabaseReference = FirebaseFirestore.getInstance().collection("Users").document(fromUserId);

            switch (object.getType())
            {
                case MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN:

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageTextPlainViewHolder) holder).MessageIncomingTextPlainProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageTextPlainViewHolder) holder).MessageIncomingTextPlainProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageTextPlainViewHolder) holder).setIncomingTextPlainProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainView.setVisibility(View.GONE);
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainTime.setVisibility(View.GONE);
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainView.setVisibility(View.VISIBLE);
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainTime.setVisibility(View.VISIBLE);
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainMessageLinearLayout.setVisibility(View.VISIBLE);

                        if (object.isSeen())
                        {
                            ((MessageTextPlainViewHolder) holder).TextPlainStored.setVisibility(View.GONE);
                            ((MessageTextPlainViewHolder) holder).TextPlainReceived.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            ((MessageTextPlainViewHolder) holder).TextPlainReceived.setVisibility(View.GONE);
                            ((MessageTextPlainViewHolder) holder).TextPlainStored.setVisibility(View.VISIBLE);
                        }

                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainView.setText(object.getMessage());
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainTime.setText(time);
                    }
                    else
                    {
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainView.setVisibility(View.GONE);
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainTime.setVisibility(View.GONE);
                        ((MessageTextPlainViewHolder) holder).OnlineSendTextPlainMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainView.setVisibility(View.VISIBLE);
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainTime.setVisibility(View.VISIBLE);
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainMessageLinearLayout.setVisibility(View.VISIBLE);

                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainView.setText(object.getMessage());
                        ((MessageTextPlainViewHolder) holder).OnlineReceivedTextPlainTime.setText(time);
                    }
                    break;

                case MessageTypesModel.MESSAGE_TYPE_TEXT_LINK:

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageTextLinkViewHolder) holder).MessageIncomingTextLinkProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageTextLinkViewHolder) holder).MessageIncomingTextLinkProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageTextLinkViewHolder) holder).setIncomingTextLinkProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkView.setVisibility(View.GONE);
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkTime.setVisibility(View.GONE);
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkView.setVisibility(View.VISIBLE);
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkTime.setVisibility(View.VISIBLE);
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkMessageLinearLayout.setVisibility(View.VISIBLE);

                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkView.setText(object.getMessage());
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkTime.setText(time);

                        if (object.isSeen())
                        {
                            ((MessageTextLinkViewHolder) holder).TextLinkStored.setVisibility(View.GONE);
                            ((MessageTextLinkViewHolder) holder).TextLinkReceived.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            ((MessageTextLinkViewHolder) holder).TextLinkReceived.setVisibility(View.GONE);
                            ((MessageTextLinkViewHolder) holder).TextLinkStored.setVisibility(View.VISIBLE);
                        }

                        final Uri linkUri = Uri.parse(object.getMessage());

                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW,linkUri);
                                startActivity(intent);
                            }
                        });
                    }
                    else
                    {
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkView.setVisibility(View.GONE);
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkTime.setVisibility(View.GONE);
                        ((MessageTextLinkViewHolder) holder).OnlineSendTextLinkMessageLinearLayout.setVisibility(View.GONE);

                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkView.setVisibility(View.VISIBLE);
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkTime.setVisibility(View.VISIBLE);
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkMessageLinearLayout.setVisibility(View.VISIBLE);

                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkView.setText(object.getMessage());
                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkTime.setText(time);

                        final Uri linkUri = Uri.parse(object.getMessage());

                        ((MessageTextLinkViewHolder) holder).OnlineReceivedTextLinkView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW,linkUri);
                                startActivity(intent);
                            }
                        });
                    }
                    break;
                case MessageTypesModel.MESSAGE_TYPE_DOCUMENT:

                    final String documentResult = getChatLocalStorageRef(context,object.getKey());

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageDocumentViewHolder) holder).MessageIncomingDocumentProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageDocumentViewHolder) holder).MessageIncomingDocumentProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageDocumentViewHolder) holder).setIncomingDocumentProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (object.getExe().equals("pdf"))
                    {
                        if (fromUserId.equals(online_user_id))
                        {
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.VISIBLE);

                            if (object.isSeen())
                            {
                                ((MessageDocumentViewHolder) holder).DocumentPdfStored.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentPdfReceived.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).DocumentPdfReceived.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentPdfStored.setVisibility(View.VISIBLE);
                            }

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTitle.setText(object.getFile_name());

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfImageView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                                    pdfIntent.setDataAndType(Uri.parse(object.getRef()),"application/pdf");
                                    PackageManager packageManager = getPackageManager();
                                    List<ResolveInfo> activities = packageManager.queryIntentActivities(pdfIntent,0);

                                    if (activities.size() > 0)
                                    {
                                        startActivity(pdfIntent);
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "No default app found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                           /* ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfSharing.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                    sharingIntent.setAction(Intent.ACTION_VIEW);
                                    sharingIntent.setType("pdf");
                                    sharingIntent.putExtra("uri",object.getRef());
                                    sharingIntent.putExtra("file_name",object.getFile_name());
                                    startActivity(sharingIntent);
                                }
                            });*/
                        }
                        else
                        {
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.VISIBLE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfTitle.setText(object.getFile_name());

                            if (documentResult == null)
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSize.setVisibility(View.VISIBLE);
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSize.setText(object.getSize());

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSize.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getMessage());

                                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                                + "/" + "GrindersSouls/Grinders Chat", "Documents");

                                        if (!localFile.exists())
                                        {
                                            localFile.mkdirs();
                                        }

                                        final File downloadFile = new File(localFile, "DOC_" + System.currentTimeMillis() + ".pdf");

                                        downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    final Uri file = Uri.fromFile(downloadFile);

                                                    setChatLocalStorageRef(context, object.getKey(), file.toString());

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSize.setVisibility(View.GONE);

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfImageView.setOnClickListener(new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                            Intent pdfIntent = new Intent(Intent.ACTION_VIEW,file);
                                                            pdfIntent.setType("application/pdf");
                                                            PackageManager packageManager = getPackageManager();
                                                            List<ResolveInfo> activities = packageManager.queryIntentActivities(pdfIntent,0);

                                                            if (activities.size() > 0)
                                                            {
                                                                startActivity(pdfIntent);
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(context, "No default app found", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfSize.setVisibility(View.GONE);

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfImageView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent pdfIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(getChatLocalStorageRef(context,object.getKey())));
                                        pdfIntent.setType("application/pdf");
                                        PackageManager packageManager = getPackageManager();
                                        List<ResolveInfo> activities = packageManager.queryIntentActivities(pdfIntent,0);

                                        if (activities.size() > 0)
                                        {
                                            startActivity(pdfIntent);
                                        }
                                        else
                                        {
                                            Toast.makeText(context, "No default app found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                    if (object.getExe().equals("zip"))
                    {
                        if (fromUserId.equals(online_user_id))
                        {
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.VISIBLE);

                            if (object.isSeen())
                            {
                                ((MessageDocumentViewHolder) holder).DocumentZipStored.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentZipReceived.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).DocumentZipReceived.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentZipStored.setVisibility(View.VISIBLE);
                            }

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTitle.setText(object.getFile_name());

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipImageView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent zipIntent = new Intent(Intent.ACTION_VIEW);
                                    zipIntent.setDataAndType(Uri.parse(object.getRef()),"application/zip");
                                    startActivity(zipIntent);
                                }
                            });
/*
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipSharing.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                    sharingIntent.setAction(Intent.ACTION_VIEW);
                                    sharingIntent.setType("zip");
                                    sharingIntent.putExtra("uri",object.getRef());
                                    sharingIntent.putExtra("file_name",object.getFile_name());
                                    startActivity(sharingIntent);
                                }
                            });*/
                        }
                        else
                        {
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.VISIBLE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipTitle.setText(object.getFile_name());

                            if (documentResult == null)
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSize.setVisibility(View.VISIBLE);
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSize.setText(object.getSize());

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSize.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getMessage());

                                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                                + "/" + "GrindersSouls/Grinders Chat", "Documents");

                                        if (!localFile.exists())
                                        {
                                            localFile.mkdirs();
                                        }

                                        final File downloadFile = new File(localFile, "DOC_" + System.currentTimeMillis() + ".zip");

                                        downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    final Uri file = Uri.fromFile(downloadFile);

                                                    setChatLocalStorageRef(context, object.getKey(), file.toString());

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSize.setVisibility(View.GONE);

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipImageView.setOnClickListener(new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v)
                                                        {

                                                            Intent zipIntent = new Intent(Intent.ACTION_VIEW);
                                                            zipIntent.setDataAndType(file,"application/zip");
                                                            startActivity(zipIntent);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipSize.setVisibility(View.GONE);

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipImageView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent zipIntent = new Intent(Intent.ACTION_VIEW);
                                        zipIntent.setDataAndType(Uri.parse(getChatLocalStorageRef(context,object.getKey())),"application/zip");
                                        startActivity(zipIntent);
                                    }
                                });
                            }
                        }
                    }
                    if (object.getExe().equals("txt"))
                    {
                        if (fromUserId.equals(online_user_id))
                        {
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.VISIBLE);

                            if (object.isSeen())
                            {
                                ((MessageDocumentViewHolder) holder).DocumentTxtStored.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentTxtReceived.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).DocumentTxtReceived.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentTxtStored.setVisibility(View.VISIBLE);
                            }

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTitle.setText(object.getFile_name());

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtImageView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent txtIntent = new Intent(Intent.ACTION_VIEW);
                                    txtIntent.setDataAndType(Uri.parse(object.getRef()),"application/txt");
                                    startActivity(txtIntent);
                                }
                            });

                      /*      ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipSharing.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                    sharingIntent.setAction(Intent.ACTION_VIEW);
                                    sharingIntent.setType("txt");
                                    sharingIntent.putExtra("uri",object.getRef());
                                    sharingIntent.putExtra("file_name",object.getFile_name());
                                    startActivity(sharingIntent);
                                }
                            });*/
                        }
                        else
                        {
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.VISIBLE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtTitle.setText(object.getFile_name());

                            if (documentResult == null)
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSize.setVisibility(View.VISIBLE);
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSize.setText(object.getSize());

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSize.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getMessage());

                                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                                + "/" + "GrindersSouls/Grinders Chat", "Documents");

                                        if (!localFile.exists())
                                        {
                                            localFile.mkdirs();
                                        }

                                        final File downloadFile = new File(localFile, "DOC_" + System.currentTimeMillis() + ".txt");

                                        downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    final Uri file = Uri.fromFile(downloadFile);

                                                    setChatLocalStorageRef(context, object.getKey(), file.toString());

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSize.setVisibility(View.GONE);

                                                    Intent txtIntent = new Intent(Intent.ACTION_VIEW);
                                                    txtIntent.setDataAndType(file,"application/txt");
                                                    startActivity(txtIntent);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtSize.setVisibility(View.GONE);

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtImageView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent txtIntent = new Intent(Intent.ACTION_VIEW);
                                        txtIntent.setDataAndType(Uri.parse(getChatLocalStorageRef(context,object.getKey())),"application/txt");
                                        startActivity(txtIntent);
                                    }
                                });
                            }
                        }
                    }
                    if (object.getExe().equals("apk"))
                    {
                        if (fromUserId.equals(online_user_id))
                        {
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.VISIBLE);

                            if (object.isSeen())
                            {
                                ((MessageDocumentViewHolder) holder).DocumentApkStored.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentApkReceived.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).DocumentApkReceived.setVisibility(View.GONE);
                                ((MessageDocumentViewHolder) holder).DocumentApkStored.setVisibility(View.VISIBLE);
                            }

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTitle.setText(object.getFile_name());

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkImageView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent apkIntent = new Intent(Intent.ACTION_VIEW);
                                    apkIntent.setDataAndType(Uri.parse(object.getRef()),"application/vnd.android.package-archive");
                                    startActivity(apkIntent);
                                }
                            });

                          /*  ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkSharing.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                    sharingIntent.setAction(Intent.ACTION_VIEW);
                                    sharingIntent.setType("apk");
                                    sharingIntent.putExtra("uri",documentResult);
                                    sharingIntent.putExtra("file_name",object.getFile_name());
                                    startActivity(sharingIntent);
                                }
                            });*/
                        }
                        else
                        {
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkImageView.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkSharing.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTime.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkTitle.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentApkMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentZipMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentTxtMessageLinearLayout.setVisibility(View.GONE);
                            ((MessageDocumentViewHolder) holder).OnlineSendDocumentPdfMessageLinearLayout.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSharing.setVisibility(View.GONE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkImageView.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTime.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTitle.setVisibility(View.VISIBLE);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkMessageLinearLayout.setVisibility(View.VISIBLE);

                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTime.setText(time);
                            ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkTitle.setText(object.getFile_name());

                            if (documentResult == null)
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSize.setVisibility(View.VISIBLE);
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSize.setText(object.getSize());

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSize.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getMessage());

                                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                                + "/" + "GrindersSouls/Grinders Chat", "Documents");

                                        if (!localFile.exists())
                                        {
                                            localFile.mkdirs();
                                        }

                                        final File downloadFile = new File(localFile, "DOC_" + System.currentTimeMillis() + ".apk");

                                        downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    final Uri file = Uri.fromFile(downloadFile);

                                                    setChatLocalStorageRef(context, object.getKey(), file.toString());

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSize.setVisibility(View.GONE);

                                                    ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkImageView.setOnClickListener(new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                            Intent apkIntent = new Intent(Intent.ACTION_VIEW);
                                                            apkIntent.setDataAndType(Uri.parse(getChatLocalStorageRef(context,object.getKey())),"application/vnd.android.package-archive");
                                                            startActivity(apkIntent);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else
                            {
                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkSize.setVisibility(View.GONE);

                                ((MessageDocumentViewHolder) holder).OnlineReceivedDocumentApkImageView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent apkIntent = new Intent(Intent.ACTION_VIEW);
                                        apkIntent.setDataAndType(Uri.parse(getChatLocalStorageRef(context,object.getKey())),"application/vnd.android.package-archive");
                                        startActivity(apkIntent);
                                    }
                                });
                            }
                        }
                    }

                    break;
                case MessageTypesModel.MESSAGE_TYPE_IMAGE:

                    final String imageResult = getChatLocalStorageRef(context,object.getKey());

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");


                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageImageViewHolder) holder).MessageIncomingImageProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageImageViewHolder) holder).MessageIncomingImageProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageImageViewHolder) holder).setIncomingImageProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageImageViewHolder) holder).OnlineReceivedImageImageView.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineReceivedImageTime.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineReceivedImageSharing.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineReceivedImageMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageImageViewHolder) holder).OnlineSendImageSharing.setVisibility(View.GONE);

                        ((MessageImageViewHolder) holder).OnlineSendImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder) holder).OnlineSendImageTime.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder) holder).OnlineSendImageMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder) holder).setOnlineSendImageImageView(context,object.getRef());
                        ((MessageImageViewHolder) holder).OnlineSendImageTime.setText(time);

                        ((MessageImageViewHolder) holder).OnlineSendImageImageView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent imageIntent = new Intent(context,PostImageActivity.class);
                                imageIntent.putExtra("imageUri",object.getRef());
                                imageIntent.putExtra("purpose","view");
                                context.startActivity(imageIntent);
                            }
                        });

                       /* ((MessageImageViewHolder) holder).OnlineSendImageSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("image");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    else
                    {
                        ((MessageImageViewHolder) holder).OnlineSendImageImageView.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineSendImageTime.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineSendImageSharing.setVisibility(View.GONE);
                        ((MessageImageViewHolder) holder).OnlineSendImageMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageImageViewHolder) holder).OnlineReceivedImageSharing.setVisibility(View.GONE);

                        ((MessageImageViewHolder) holder).OnlineReceivedImageImageView.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder) holder).OnlineReceivedImageTime.setVisibility(View.VISIBLE);
                        ((MessageImageViewHolder) holder).OnlineReceivedImageMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageImageViewHolder) holder).OnlineReceivedImageTime.setText(time);

                        if (imageResult == null)
                        {StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(object.getMessage());

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/" + "GrindersSouls/Grinders Chat", "Images");

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
                                        final Uri file = Uri.fromFile(downloadFile);

                                        setChatLocalStorageRef(context, object.getKey(), file.toString());

                                        ((MessageImageViewHolder) holder).setOnlineReceivedImageImageView(context, Uri.fromFile(downloadFile));

                                        ((MessageImageViewHolder) holder).OnlineReceivedImageImageView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                Intent videoIntent = new Intent(ChatActivity.this,VideoPlayActivity.class);
                                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                                videoIntent.putExtra("purpose","play");
                                                videoIntent.putExtra("videoUri", file);
                                                startActivity(videoIntent);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            ((MessageImageViewHolder) holder).setOnlineReceivedImageImageView(context, Uri.parse(imageResult));

                            ((MessageImageViewHolder) holder).OnlineReceivedImageImageView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent imageIntent = new Intent(context,PostImageActivity.class);
                                    imageIntent.putExtra("imageUri",getChatLocalStorageRef(context,object.getKey()));
                                    imageIntent.putExtra("purpose","view");
                                    context.startActivity(imageIntent);
                                }
                            });
                        }
                        /*((MessageImageViewHolder) holder).OnlineReceivedImageSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("image");
                                sharingIntent.putExtra("uri", getChatLocalStorageRef(context,object.getKey()));
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    break;
                case MessageTypesModel.MESSAGE_TYPE_GIF:

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageGifViewHolder) holder).MessageIncomingGifProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageGifViewHolder) holder).MessageIncomingGifProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageGifViewHolder) holder).setIncomingGifProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageGifViewHolder) holder).OnlineReceivedGifImageView.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifTime.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifPlayButton.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifSharing.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageGifViewHolder) holder).OnlineSendGifSharing.setVisibility(View.GONE);

                        ((MessageGifViewHolder) holder).OnlineSendGifImageView.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineSendGifTime.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineSendGifPlayButton.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineSendGifMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageGifViewHolder) holder).OnlineSendGifTime.setText(time);

                        GlideApp.with(ChatActivity.this).load(object.getRef()).centerCrop().into(((MessageGifViewHolder) holder).OnlineSendGifImageView);

                        ((MessageGifViewHolder) holder).OnlineSendGifPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent videoIntent = new Intent(ChatActivity.this,GifPlayActivity.class);
                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                videoIntent.putExtra("purpose","play");
                                videoIntent.putExtra("gifUri", object.getRef());
                                startActivity(videoIntent);
                            }
                        });

                        /*((MessageGifViewHolder) holder).OnlineSendGifSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("video");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    else
                    {
                        ((MessageGifViewHolder) holder).OnlineSendGifImageView.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineSendGifTime.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineSendGifPlayButton.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineSendGifSharing.setVisibility(View.GONE);
                        ((MessageGifViewHolder) holder).OnlineSendGifMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageGifViewHolder) holder).OnlineReceivedGifSharing.setVisibility(View.GONE);

                        ((MessageGifViewHolder) holder).OnlineReceivedGifImageView.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifTime.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifPlayButton.setVisibility(View.VISIBLE);
                        ((MessageGifViewHolder) holder).OnlineReceivedGifMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageGifViewHolder) holder).OnlineReceivedGifTime.setText(time);

                        GlideApp.with(ChatActivity.this).load(object.getRef()).centerCrop().into(((MessageGifViewHolder) holder).OnlineReceivedGifImageView);

                        ((MessageGifViewHolder) holder).OnlineReceivedGifPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent videoIntent = new Intent(ChatActivity.this,GifPlayActivity.class);
                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                videoIntent.putExtra("purpose","play");
                                videoIntent.putExtra("gifUri", object.getRef());
                                startActivity(videoIntent);
                            }
                        });

                        ((MessageGifViewHolder) holder).OnlineReceivedGifSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("video");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });
                    }
                    break;

                case MessageTypesModel.MESSAGE_TYPE_AUDIO:

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageAudioViewHolder) holder).MessageIncomingAudioProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageAudioViewHolder) holder).MessageIncomingAudioProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageAudioViewHolder) holder).setIncomingAudioProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioHeadSet.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTime.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioPlayButton.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioDuration.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioSharing.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioMessageLinearLayout.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTitle.setVisibility(View.GONE);

                        ((MessageAudioViewHolder) holder).OnlineSendAudioSharing.setVisibility(View.GONE);

                        ((MessageAudioViewHolder) holder).OnlineSendAudioHeadSet.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioTime.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioPlayButton.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioDuration.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioMessageLinearLayout.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioTitle.setVisibility(View.VISIBLE);

                        ((MessageAudioViewHolder) holder).OnlineSendAudioTime.setText(time);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioDuration.setText(object.getDuration());
                        ((MessageAudioViewHolder) holder).OnlineSendAudioTitle.setText(object.getFile_name());

                        ((MessageAudioViewHolder) holder).OnlineSendAudioPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(object.getRef()), "audio/*");
                                startActivity(intent);
                            }
                        });

                       /* ((MessageAudioViewHolder) holder).OnlineSendAudioSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("audio");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    else
                    {
                        ((MessageAudioViewHolder) holder).OnlineSendAudioHeadSet.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioTime.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioPlayButton.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioDuration.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioSharing.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioMessageLinearLayout.setVisibility(View.GONE);
                        ((MessageAudioViewHolder) holder).OnlineSendAudioTitle.setVisibility(View.GONE);

                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioSharing.setVisibility(View.GONE);

                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioHeadSet.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTime.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioPlayButton.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioDuration.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioMessageLinearLayout.setVisibility(View.VISIBLE);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTitle.setVisibility(View.VISIBLE);

                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTime.setText(time);
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioDuration.setText(object.getDuration());
                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioTitle.setText(object.getFile_name());

                        ((MessageAudioViewHolder) holder).OnlineReceivedAudioPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (object.getRef() != null)
                                {
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(object.getRef()), "audio/*");
                                    startActivity(intent);
                                }
                            }
                        });

                       /* ((MessageAudioViewHolder) holder).OnlineReceivedAudioSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("audio");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    break;

                case MessageTypesModel.MESSAGE_TYPE_VIDEO:

                    usersDatabaseReference.addSnapshotListener(new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
                        {
                            if (e != null)
                            {
                                Toast.makeText(ChatActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                            {
                                String userThumbImage = documentSnapshot.getString("user_thumb_img");

                                if (fromUserId.equals(online_user_id))
                                {
                                    ((MessageVideoViewHolder) holder).MessageIncomingVideoProfileImage.setVisibility(View.GONE);
                                }
                                else
                                {
                                    ((MessageVideoViewHolder) holder).MessageIncomingVideoProfileImage.setVisibility(View.VISIBLE);

                                    ((MessageVideoViewHolder) holder).setIncomingVideoProfileImg(context,userThumbImage);
                                }
                            }
                        }
                    });

                    if (fromUserId.equals(online_user_id))
                    {
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoImageView.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoTime.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoPlayButton.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoSharing.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageVideoViewHolder) holder).OnlineSendVideoSharing.setVisibility(View.GONE);

                        ((MessageVideoViewHolder) holder).OnlineSendVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoTime.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoPlayButton.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageVideoViewHolder) holder).OnlineSendVideoTime.setText(time);

                        ((MessageVideoViewHolder) holder).setIncomingVideoImage(ChatActivity.this,object.getVideo_thumbnail());

                        ((MessageVideoViewHolder) holder).OnlineSendVideoPlayButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent videoIntent = new Intent(ChatActivity.this,VideoPlayActivity.class);
                                videoIntent.setAction(VideoPlayActivity.ACTION_VIEW);
                                videoIntent.putExtra("purpose","play");
                                videoIntent.putExtra("videoUri", object.getRef());
                                startActivity(videoIntent);
                            }
                        });

                       /* ((MessageVideoViewHolder) holder).OnlineSendVideoSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("video");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });*/
                    }
                    else
                    {
                        ((MessageVideoViewHolder) holder).OnlineSendVideoImageView.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoTime.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoPlayButton.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoSharing.setVisibility(View.GONE);
                        ((MessageVideoViewHolder) holder).OnlineSendVideoMessageRelativeLayout.setVisibility(View.GONE);

                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoSharing.setVisibility(View.GONE);

                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoImageView.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoTime.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoPlayButton.setVisibility(View.VISIBLE);
                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoMessageRelativeLayout.setVisibility(View.VISIBLE);

                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoTime.setText(time);

                        GlideApp.with(ChatActivity.this).load(object.getRef()).centerCrop().into(((MessageVideoViewHolder) holder).OnlineReceivedVideoImageView);

                        ((MessageVideoViewHolder) holder).OnlineReceivedVideoSharing.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent sharingIntent = new Intent(ChatActivity.this,SharingActivity.class);
                                sharingIntent.setAction(Intent.ACTION_VIEW);
                                sharingIntent.setType("video");
                                sharingIntent.putExtra("uri",object.getRef());
                                startActivity(sharingIntent);
                            }
                        });
                    }
                    break;

                case MessageTypesModel.MESSAGE_TYPE_DAY_DATE:

                    ((MessageDateViewHolder) holder).OnlineChattingDayDateTextView.setVisibility(View.VISIBLE);

                    try
                    {
                        String today = formatToYesterdayOrToday(object.getToday_date());

                        if (today.equals("Today"))
                        {
                            storeDateRef(online_user_id,object.getFrom());

                            ((MessageDateViewHolder) holder).OnlineChattingDayDateTextView.setText(today);
                        }
                        else
                        {
                            ((MessageDateViewHolder) holder).OnlineChattingDayDateTextView.setText(today);
                        }
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
            return userMessageTypesModelList.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            int i = userMessageTypesModelList.get(position).getType();

            switch (i)
            {
                case MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN:
                    return MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN;
                case MessageTypesModel.MESSAGE_TYPE_TEXT_LINK:
                    return MessageTypesModel.MESSAGE_TYPE_TEXT_LINK;
                case MessageTypesModel.MESSAGE_TYPE_DOCUMENT:
                    return MessageTypesModel.MESSAGE_TYPE_DOCUMENT;
                case MessageTypesModel.MESSAGE_TYPE_IMAGE:
                    return MessageTypesModel.MESSAGE_TYPE_IMAGE;
                case MessageTypesModel.MESSAGE_TYPE_GIF:
                    return MessageTypesModel.MESSAGE_TYPE_GIF;
                case MessageTypesModel.MESSAGE_TYPE_AUDIO:
                    return MessageTypesModel.MESSAGE_TYPE_AUDIO;
                case MessageTypesModel.MESSAGE_TYPE_VIDEO:
                    return MessageTypesModel.MESSAGE_TYPE_VIDEO;
                case MessageTypesModel.MESSAGE_TYPE_DAY_DATE:
                    return MessageTypesModel.MESSAGE_TYPE_DAY_DATE;
                default:
                    return -1;
            }
        }

        public class MessageTextPlainViewHolder extends RecyclerView.ViewHolder
        {
            EmojiconTextView OnlineSendTextPlainView,OnlineReceivedTextPlainView;
            TextView OnlineSendTextPlainTime,OnlineReceivedTextPlainTime;
            CircleImageView MessageIncomingTextPlainProfileImage;
            LinearLayout OnlineSendTextPlainMessageLinearLayout,OnlineReceivedTextPlainMessageLinearLayout;
            CircleImageView TextPlainStored,TextPlainReceived;

            View v;

            MessageTextPlainViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendTextPlainView = v.findViewById(R.id.online_send_text_plain_view);
                OnlineReceivedTextPlainView = v.findViewById(R.id.online_received_text_plain_view);
                OnlineSendTextPlainTime = v.findViewById(R.id.online_send_text_plain_time);
                OnlineReceivedTextPlainTime = v.findViewById(R.id.online_received_text_plain_time);
                MessageIncomingTextPlainProfileImage = v.findViewById(R.id.message_incoming_text_plain_profile_img);
                OnlineSendTextPlainMessageLinearLayout = v.findViewById(R.id.online_send_text_plain_message_linear_layout);
                OnlineReceivedTextPlainMessageLinearLayout = v.findViewById(R.id.online_received_text_plain_message_linear_layout);
                TextPlainStored = v.findViewById(R.id.text_plain_stored);
                TextPlainReceived = v.findViewById(R.id.text_plain_received);
            }

            void setIncomingTextPlainProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_text_plain_profile_img);

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

        public class MessageTextLinkViewHolder extends RecyclerView.ViewHolder
        {
            TextView OnlineSendTextLinkView,OnlineReceivedTextLinkView,OnlineSendTextLinkTime,OnlineReceivedTextLinkTime;
            CircleImageView MessageIncomingTextLinkProfileImage;
            ImageButton OnlineSendTextLinkSharing,OnlineReceivedTextLinkSharing;
            LinearLayout OnlineSendTextLinkMessageLinearLayout,OnlineReceivedTextLinkMessageLinearLayout;
            CircleImageView TextLinkStored,TextLinkReceived;

            View v;

            MessageTextLinkViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendTextLinkView = v.findViewById(R.id.online_send_text_link_view);
                OnlineReceivedTextLinkView = v.findViewById(R.id.online_received_text_link_view);
                OnlineSendTextLinkTime = v.findViewById(R.id.online_send_text_link_time);
                OnlineReceivedTextLinkTime = v.findViewById(R.id.online_received_text_link_time);
                MessageIncomingTextLinkProfileImage = v.findViewById(R.id.message_incoming_text_link_profile_img);
                OnlineSendTextLinkMessageLinearLayout = v.findViewById(R.id.online_send_text_link_message_linear_layout);
                OnlineReceivedTextLinkMessageLinearLayout = v.findViewById(R.id.online_received_text_link_message_linear_layout);
                TextLinkStored = v.findViewById(R.id.text_link_stored);
                TextLinkReceived = v.findViewById(R.id.text_link_received);
            }

            void setIncomingTextLinkProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_text_link_profile_img);

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

        public class MessageDocumentViewHolder extends RecyclerView.ViewHolder
        {
            CircleImageView MessageIncomingDocumentProfileImage;

            ImageView OnlineSendDocumentPdfImageView,OnlineReceivedDocumentPdfImageView;
            ImageButton OnlineSendDocumentPdfSharing,OnlineReceivedDocumentPdfSharing;
            TextView OnlineSendDocumentPdfTime,OnlineReceivedDocumentPdfTime,OnlineSendDocumentPdfTitle,OnlineReceivedDocumentPdfTitle;
            LinearLayout OnlineSendDocumentPdfMessageLinearLayout,OnlineReceivedDocumentPdfMessageLinearLayout;
            CircleImageView DocumentPdfStored,DocumentPdfReceived;
            TextView OnlineReceivedDocumentPdfSize;

            ImageView OnlineSendDocumentZipImageView,OnlineReceivedDocumentZipImageView;
            ImageButton OnlineSendDocumentZipSharing,OnlineReceivedDocumentZipSharing;
            TextView OnlineSendDocumentZipTime,OnlineReceivedDocumentZipTime,OnlineSendDocumentZipTitle,OnlineReceivedDocumentZipTitle;
            LinearLayout OnlineSendDocumentZipMessageLinearLayout,OnlineReceivedDocumentZipMessageLinearLayout;
            CircleImageView DocumentZipStored,DocumentZipReceived;
            TextView OnlineReceivedDocumentZipSize;

            ImageView OnlineSendDocumentTxtImageView,OnlineReceivedDocumentTxtImageView;
            ImageButton OnlineSendDocumentTxtSharing,OnlineReceivedDocumentTxtSharing;
            TextView OnlineSendDocumentTxtTime,OnlineReceivedDocumentTxtTime,OnlineSendDocumentTxtTitle,OnlineReceivedDocumentTxtTitle;
            LinearLayout OnlineSendDocumentTxtMessageLinearLayout,OnlineReceivedDocumentTxtMessageLinearLayout;
            CircleImageView DocumentTxtStored,DocumentTxtReceived;
            TextView OnlineReceivedDocumentTxtSize;

            ImageView OnlineSendDocumentApkImageView,OnlineReceivedDocumentApkImageView;
            ImageButton OnlineSendDocumentApkSharing,OnlineReceivedDocumentApkSharing;
            TextView OnlineSendDocumentApkTime,OnlineReceivedDocumentApkTime,OnlineSendDocumentApkTitle,OnlineReceivedDocumentApkTitle;
            LinearLayout OnlineSendDocumentApkMessageLinearLayout,OnlineReceivedDocumentApkMessageLinearLayout;
            CircleImageView DocumentApkStored,DocumentApkReceived;
            TextView OnlineReceivedDocumentApkSize;

            View view;

            MessageDocumentViewHolder(View itemView)
            {
                super(itemView);

                view = itemView;

                MessageIncomingDocumentProfileImage = view.findViewById(R.id.message_incoming_document_profile_img);

                OnlineSendDocumentPdfImageView = view.findViewById(R.id.online_send_document_pdf_image_view);
                OnlineReceivedDocumentPdfImageView = view.findViewById(R.id.online_received_document_pdf_image_view);
                OnlineSendDocumentPdfSharing = view.findViewById(R.id.online_send_document_pdf_sharing);
                OnlineReceivedDocumentPdfSharing = view.findViewById(R.id.online_received_document_pdf_sharing);
                OnlineSendDocumentPdfTime = view.findViewById(R.id.online_send_document_pdf_time);
                OnlineReceivedDocumentPdfTime = view.findViewById(R.id.online_received_document_pdf_time);
                OnlineSendDocumentPdfTitle = view.findViewById(R.id.online_send_document_pdf_title);
                OnlineReceivedDocumentPdfTitle = view.findViewById(R.id.online_received_document_pdf_title);
                OnlineSendDocumentPdfMessageLinearLayout = view.findViewById(R.id.online_send_document_pdf_message_linear_layout);
                OnlineReceivedDocumentPdfMessageLinearLayout = view.findViewById(R.id.online_received_document_pdf_message_linear_layout);
                DocumentPdfStored = view.findViewById(R.id.document_pdf_stored);
                DocumentPdfReceived = view.findViewById(R.id.document_pdf_received);
                OnlineReceivedDocumentPdfSize = view.findViewById(R.id.online_received_document_pdf_size);

                OnlineSendDocumentZipImageView = view.findViewById(R.id.online_send_document_zip_image_view);
                OnlineReceivedDocumentZipImageView = view.findViewById(R.id.online_received_document_zip_image_view);
                OnlineSendDocumentZipSharing = view.findViewById(R.id.online_send_document_zip_sharing);
                OnlineReceivedDocumentZipSharing = view.findViewById(R.id.online_received_document_zip_sharing);
                OnlineSendDocumentZipTime = view.findViewById(R.id.online_send_document_zip_time);
                OnlineReceivedDocumentZipTime = view.findViewById(R.id.online_received_document_zip_time);
                OnlineSendDocumentZipTitle = view.findViewById(R.id.online_send_document_zip_title);
                OnlineReceivedDocumentZipTitle = view.findViewById(R.id.online_received_document_zip_title);
                OnlineSendDocumentZipMessageLinearLayout = view.findViewById(R.id.online_send_document_zip_message_linear_layout);
                OnlineReceivedDocumentZipMessageLinearLayout = view.findViewById(R.id.online_received_document_zip_message_linear_layout);
                DocumentZipStored = view.findViewById(R.id.document_zip_stored);
                DocumentZipReceived = view.findViewById(R.id.document_zip_received);
                OnlineReceivedDocumentZipSize = view.findViewById(R.id.online_received_document_zip_size);

                OnlineSendDocumentTxtImageView = view.findViewById(R.id.online_send_document_txt_image_view);
                OnlineReceivedDocumentTxtImageView = view.findViewById(R.id.online_received_document_txt_image_view);
                OnlineSendDocumentTxtSharing = view.findViewById(R.id.online_send_document_txt_sharing);
                OnlineReceivedDocumentTxtSharing = view.findViewById(R.id.online_received_document_txt_sharing);
                OnlineSendDocumentTxtTime = view.findViewById(R.id.online_send_document_txt_time);
                OnlineReceivedDocumentTxtTime = view.findViewById(R.id.online_received_document_txt_time);
                OnlineSendDocumentTxtTitle = view.findViewById(R.id.online_send_document_txt_title);
                OnlineReceivedDocumentTxtTitle = view.findViewById(R.id.online_received_document_txt_title);
                OnlineSendDocumentTxtMessageLinearLayout = view.findViewById(R.id.online_send_document_txt_message_linear_layout);
                OnlineReceivedDocumentTxtMessageLinearLayout = view.findViewById(R.id.online_received_document_txt_message_linear_layout);
                DocumentTxtStored = view.findViewById(R.id.document_txt_stored);
                DocumentTxtReceived = view.findViewById(R.id.document_txt_received);
                OnlineReceivedDocumentTxtSize = view.findViewById(R.id.online_received_document_txt_size);

                OnlineSendDocumentApkImageView = view.findViewById(R.id.online_send_document_apk_image_view);
                OnlineReceivedDocumentApkImageView = view.findViewById(R.id.online_received_document_apk_image_view);
                OnlineSendDocumentApkSharing = view.findViewById(R.id.online_send_document_apk_sharing);
                OnlineReceivedDocumentApkSharing = view.findViewById(R.id.online_received_document_apk_sharing);
                OnlineSendDocumentApkTime = view.findViewById(R.id.online_send_document_apk_time);
                OnlineReceivedDocumentApkTime = view.findViewById(R.id.online_received_document_apk_time);
                OnlineSendDocumentApkTitle = view.findViewById(R.id.online_send_document_apk_title);
                OnlineReceivedDocumentApkTitle = view.findViewById(R.id.online_received_document_apk_title);
                OnlineSendDocumentApkMessageLinearLayout = view.findViewById(R.id.online_send_document_apk_message_linear_layout);
                OnlineReceivedDocumentApkMessageLinearLayout = view.findViewById(R.id.online_received_document_apk_message_linear_layout);
                DocumentApkStored = view.findViewById(R.id.document_apk_stored);
                DocumentApkReceived = view.findViewById(R.id.document_apk_received);
                OnlineReceivedDocumentApkSize = view.findViewById(R.id.online_received_document_apk_size);

            }

            void setIncomingDocumentProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = view.findViewById(R.id.message_incoming_document_profile_img);

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

        public class MessageImageViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineReceivedImageImageView,OnlineSendImageImageView;
            TextView OnlineSendImageTime,OnlineReceivedImageTime;
            CircleImageView MessageIncomingImageProfileImage;
            ImageButton OnlineSendImageSharing,OnlineReceivedImageSharing;
            RelativeLayout OnlineSendImageMessageRelativeLayout,OnlineReceivedImageMessageRelativeLayout;
            CircleImageView ImageStored,ImageReceived;

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
                OnlineSendImageSharing = v.findViewById(R.id.online_send_image_sharing);
                OnlineReceivedImageSharing = v.findViewById(R.id.online_received_image_sharing);
                OnlineSendImageMessageRelativeLayout = v.findViewById(R.id.online_send_image_message_relative_layout);
                OnlineReceivedImageMessageRelativeLayout = v.findViewById(R.id.online_received_image_message_relative_layout);
                ImageStored = v.findViewById(R.id.image_stored);
                ImageReceived = v.findViewById(R.id.image_received);
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

            void setOnlineReceivedImageImageView(final Context context, final Uri receivedImage)
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

        public class MessageGifViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineSendGifImageView,OnlineReceivedGifImageView,OnlineSendGifPlayButton,OnlineReceivedGifPlayButton;
            TextView OnlineSendGifTime,OnlineReceivedGifTime,OnlineSendGifDuration,OnlineReceivedGifDuration;
            CircleImageView MessageIncomingGifProfileImage;
            ImageButton OnlineSendGifSharing,OnlineReceivedGifSharing;
            RelativeLayout OnlineSendGifMessageRelativeLayout,OnlineReceivedGifMessageRelativeLayout;
            CircleImageView GifStored,GifReceived;

            View v;

            MessageGifViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendGifImageView = v.findViewById(R.id.online_send_gif_image_view);
                OnlineReceivedGifImageView = v.findViewById(R.id.online_received_gif_image_view);
                OnlineSendGifTime = v.findViewById(R.id.online_send_gif_time);
                OnlineReceivedGifTime = v.findViewById(R.id.online_received_gif_time);
                OnlineSendGifDuration = v.findViewById(R.id.online_send_gif_duration);
                OnlineReceivedGifDuration = v.findViewById(R.id.online_received_gif_duration);
                OnlineSendGifPlayButton = v.findViewById(R.id.online_send_gif_play_button);
                OnlineReceivedGifPlayButton = v.findViewById(R.id.online_received_gif_play_button);
                MessageIncomingGifProfileImage = v.findViewById(R.id.message_incoming_gif_profile_img);
                OnlineSendGifSharing = v.findViewById(R.id.online_send_gif_sharing);
                OnlineReceivedGifSharing = v.findViewById(R.id.online_received_gif_sharing);
                OnlineSendGifMessageRelativeLayout = v.findViewById(R.id.online_send_gif_message_relative_layout);
                OnlineReceivedGifMessageRelativeLayout = v.findViewById(R.id.online_received_gif_message_relative_layout);
                GifStored = v.findViewById(R.id.gif_stored);
                GifReceived = v.findViewById(R.id.gif_received);
            }

            void setIncomingGifProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_gif_profile_img);

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

        public class MessageAudioViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineSendAudioPlayButton,OnlineReceivedAudioPlayButton,OnlineSendAudioHeadSet,OnlineReceivedAudioHeadSet;
            TextView OnlineSendAudioDuration,OnlineReceivedAudioDuration,OnlineSendAudioTime,OnlineReceivedAudioTime;
            CircleImageView MessageIncomingAudioProfileImage;
            ImageButton OnlineSendAudioSharing,OnlineReceivedAudioSharing;
            LinearLayout OnlineSendAudioMessageLinearLayout,OnlineReceivedAudioMessageLinearLayout;
            CircleImageView AudioStored,AudioReceived;
            TextView OnlineReceivedAudioSize;
            TextView OnlineSendAudioTitle,OnlineReceivedAudioTitle;

            View v;

            MessageAudioViewHolder(View itemView)
            {
                super(itemView);

                v = itemView;

                OnlineSendAudioHeadSet = v.findViewById(R.id.online_send_audio_head_set);
                OnlineReceivedAudioHeadSet = v.findViewById(R.id.online_received_audio_head_set);
                OnlineSendAudioPlayButton = v.findViewById(R.id.online_send_audio_play_button);
                OnlineReceivedAudioPlayButton = v.findViewById(R.id.online_received_audio_play_button);
                OnlineSendAudioDuration = v.findViewById(R.id.online_send_audio_duration);
                OnlineReceivedAudioDuration = v.findViewById(R.id.online_received_audio_duration);
                OnlineSendAudioTime = v.findViewById(R.id.online_send_audio_time);
                OnlineReceivedAudioTime = v.findViewById(R.id.online_received_audio_time);
                MessageIncomingAudioProfileImage = v.findViewById(R.id.message_incoming_audio_profile_img);
                OnlineSendAudioSharing = v.findViewById(R.id.online_send_audio_sharing);
                OnlineReceivedAudioSharing = v.findViewById(R.id.online_received_audio_sharing);
                OnlineSendAudioMessageLinearLayout = v.findViewById(R.id.online_send_audio_message_linear_layout);
                OnlineReceivedAudioMessageLinearLayout = v.findViewById(R.id.online_received_audio_message_linear_layout);
                AudioStored = v.findViewById(R.id.audio_stored);
                AudioReceived = v.findViewById(R.id.audio_received);
                OnlineReceivedAudioSize = v.findViewById(R.id.online_received_audio_size);
                OnlineSendAudioTitle = v.findViewById(R.id.online_send_audio_title);
                OnlineReceivedAudioTitle = v.findViewById(R.id.online_received_audio_title);
            }

            void setIncomingAudioProfileImg(final Context context, final String incomingThumbImg)
            {
                final CircleImageView incoming_thumb_img = v.findViewById(R.id.message_incoming_audio_profile_img);

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

        public class MessageVideoViewHolder extends RecyclerView.ViewHolder
        {
            ImageView OnlineSendVideoImageView,OnlineReceivedVideoImageView,OnlineSendVideoPlayButton,OnlineReceivedVideoPlayButton;
            TextView OnlineSendVideoTime,OnlineReceivedVideoTime,OnlineSendVideoDuration,OnlineReceivedVideoDuration;
            CircleImageView MessageIncomingVideoProfileImage;
            ImageButton OnlineSendVideoSharing,OnlineReceivedVideoSharing;
            RelativeLayout OnlineSendVideoMessageRelativeLayout,OnlineReceivedVideoMessageRelativeLayout;
            CircleImageView VideoStored,VideoReceived;
            TextView OnlineReceivedVideoSize;

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
                OnlineSendVideoSharing = v.findViewById(R.id.online_send_video_sharing);
                OnlineReceivedVideoSharing = v.findViewById(R.id.online_received_video_sharing);
                OnlineSendVideoMessageRelativeLayout = v.findViewById(R.id.online_send_video_message_relative_layout);
                OnlineReceivedVideoMessageRelativeLayout = v.findViewById(R.id.online_received_video_message_relative_layout);
                VideoStored = v.findViewById(R.id.video_stored);
                VideoReceived = v.findViewById(R.id.video_received);
                OnlineReceivedVideoSize = v.findViewById(R.id.online_received_video_size);
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

            void setIncomingVideoImage(final Context context, final String incomingThumbImg)
            {
                final ImageView incoming_thumb_img = v.findViewById(R.id.online_send_video_image_view);

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

    private void updateSingleDateRef(final String online_key, final String receiver_key)
    {
        storeDateRef(online_key,receiver_key);

        DocumentReference date_key = rootRef.collection("Messages").document(online_key).collection(receiver_key).document();

        final String date_push_id = date_key.getId();

        final Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("type", MessageTypesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("today_date", getTodayDate());
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);
        messageDate.put("date",FieldValue.serverTimestamp());

        rootRef.collection("Messages").document(online_key).collection(receiver_key).document(date_push_id).set(messageDate).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    rootRef.collection("Messages").document(receiver_key).collection(online_key).document(date_push_id).set(messageDate).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (!task.isSuccessful())
                            {
                                Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,task.getException().getMessage());
                                Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                Log.e(TAG,e.toString());
                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
            }
        });
    }

    private void updateGroupDateRef(String online_key,String group_key,ArrayList<String> Ids)
    {
        storeDateRef(online_key,group_key);

        Ids.add(online_key);

        DocumentReference date_key = rootRef.collection("Group_Messages").document(group_key).collection(online_key).document();

        final String date_push_id = date_key.getId();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("type", MessageTypesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("today_date", getTodayDate());
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);
        messageDate.put("date",FieldValue.serverTimestamp());

        for (String membersRef : Ids)
        {
            rootRef.collection("Group_Messages").document(group_key).collection(membersRef).document(date_push_id).set(messageDate).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }


    private void storeSingleTextPlainDetails(final String online, final ArrayList<String> Receivers, String messageText)
    {
        DocumentReference date_key = rootRef.collection("Messages").document(online).collection(Receivers.get(0)).document();

        final String message_push_id = date_key.getId();

        final Map<String,Object> messageTextBody = new HashMap<>();

        DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        messageTextBody.put("message",messageText);
        messageTextBody.put("seen",false);
        messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN);
        messageTextBody.put("time", time);
        messageTextBody.put("from",online);
        messageTextBody.put("key",message_push_id);
        messageTextBody.put("date",FieldValue.serverTimestamp());

        rootRef.collection("Messages").document(online).collection(Receivers.get(0)).document(message_push_id).set(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                rootRef.collection("Messages").document(Receivers.get(0)).collection(online).document(message_push_id).set(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,task.getException().getMessage());
                            Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.toString());
                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                Log.e(TAG,e.toString());
                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
            }
        });
    }

    private void storeGroupTextPlainDetails(String online,ArrayList<String> Receivers,String messageText,String groupKey)
    {
        Receivers.add(online);

        DocumentReference user_message_key = rootRef.collection("Group_Messages").document(groupKey).collection(online).document();

        String message_push_id = user_message_key.getId();

        DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        Map<String,Object> messageTextBody = new HashMap<>();
        messageTextBody.put("message",messageText);
        messageTextBody.put("seen",false);
        messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN);
        messageTextBody.put("time", time);
        messageTextBody.put("from",online);
        messageTextBody.put("key",message_push_id);
        messageTextBody.put("date",FieldValue.serverTimestamp());

        for (String membersRef : Receivers)
        {
            rootRef.collection("Group_Messages").document(groupKey).collection(membersRef).document(message_push_id).set(messageTextBody).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }

    private void storeSingleImageDetails(final String online, final ArrayList<String> Receivers, Uri ImageUri, Uri storageUri)
    {
        DocumentReference user_message_key = rootRef.collection("Messages").document(online).collection(Receivers.get(0)).document();

        final String message_push_id = user_message_key.getId();

        final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

        final Uri finalStorageUri = storageUri;

        if (ImageUri != null)
        {
            isUploading = true;

            UploadTask uploadTask = filePath.putFile(ImageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        Log.e(TAG,task.getException().toString());
                        Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                        Toast.makeText(ChatActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();

                        isUploading = false;
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        final String downloadUrl = task.getResult().toString();

                        filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                        {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata)
                            {
                                long sizeInMb = storageMetadata.getSizeBytes();

                                String size = getStringSizeFromFile(sizeInMb);

                                final Map<String,Object> messageTextBody = new HashMap<>();

                                DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
                                final String time = df.format(Calendar.getInstance().getTime());

                                messageTextBody.put("message",downloadUrl);
                                messageTextBody.put("seen",false);
                                messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_IMAGE);
                                messageTextBody.put("time", time);
                                messageTextBody.put("ref", finalStorageUri.toString());
                                messageTextBody.put("size",size);
                                messageTextBody.put("from",online);
                                messageTextBody.put("key",message_push_id);
                                messageTextBody.put("exe","jpg");
                                messageTextBody.put("date",FieldValue.serverTimestamp());

                                rootRef.collection("Messages").document(online).collection(Receivers.get(0)).document(message_push_id).set(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        rootRef.collection("Messages").document(Receivers.get(0)).collection(online).document(message_push_id).set(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (!task.isSuccessful())
                                                {
                                                    isUploading = false;

                                                    Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG,task.getException().getMessage());
                                                    Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                                }
                                                if (task.isSuccessful())
                                                {
                                                    Toast.makeText(ChatActivity.this,"Picture sent Successfully",Toast.LENGTH_SHORT).show();

                                                    isUploading = false;
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                                Log.e(TAG,e.toString());
                                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());

                                                isUploading = false;
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());

                                        isUploading = false;
                                    }
                                });
                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this,"Picture not sent.Try Again",Toast.LENGTH_SHORT).show();

                        isUploading = false;
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    isUploading = false;

                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(ChatActivity.this, "Error while uploading image in storage " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void storeGroupImageDetails(final String online, final ArrayList<String> Receivers, Uri ImageUri, Uri storageUri, final String groupKey)
    {
        Receivers.add(online);

        DocumentReference user_message_key = rootRef.collection("Group_Messages").document(groupKey).collection(online).document();

        final String message_push_id = user_message_key.getId();

        final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

        final Uri finalStorageUri = storageUri;

        if (ImageUri != null)
        {
            isUploading = true;

            UploadTask uploadTask =  filePath.putFile(ImageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        Log.e(TAG,task.getException().toString());
                        Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                        Toast.makeText(ChatActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();

                        isUploading = false;
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        final String downloadUrl = task.getResult().toString();

                        filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                        {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata)
                            {
                                long getSize = storageMetadata.getSizeBytes();

                                String size = getStringSizeFromFile(getSize);

                                DateFormat df = new SimpleDateFormat("h:mm a",Locale.US);
                                final String time = df.format(Calendar.getInstance().getTime());

                                Map<String,Object> messageTextBody = new HashMap<>();

                                messageTextBody.put("message",downloadUrl);
                                messageTextBody.put("seen",false);
                                messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_IMAGE);
                                messageTextBody.put("time", time);
                                messageTextBody.put("ref", finalStorageUri.toString());
                                messageTextBody.put("size",size);
                                messageTextBody.put("from",online);
                                messageTextBody.put("key",message_push_id);
                                messageTextBody.put("exe","jpg");
                                messageTextBody.put("date",FieldValue.serverTimestamp());

                                for (String membersRef : Receivers)
                                {
                                    rootRef.collection("Group_Messages").document(groupKey).collection(membersRef).document(message_push_id).set(messageTextBody).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG,e.toString());
                                            Crashlytics.log(Log.ERROR,TAG,e.getMessage());

                                            isUploading = false;
                                        }
                                    });
                                }

                                isUploading = false;
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,e.toString());
                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());

                                isUploading = false;
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this,"Picture not sent.Try Again",Toast.LENGTH_SHORT).show();

                        isUploading = false;
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    isUploading = false;

                    Toast.makeText(ChatActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (isUploading)
        {
            isUploading = false;
        }
    }

   /* @Override
    protected void onStart()
    {
        super.onStart();

        if (Type.equals("single"))
        {
            final DatabaseReference OnlineRef = FirebaseDatabase.getInstance().getReference().child("Message").child(online_user_id).child(ReceivedIds.get(0));

            OnlineRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        int type = (int) snapshot.child("type").getValue();

                        if (type == MessageTypesModel.MESSAGE_TYPE_TEXT_PLAIN)
                        {
                            OnlineRef.child(snapshot.getKey()).child("seen").setValue(true);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }*/

    private String getChatLocalStorageRef(Context context, String key)
    {
        String get = null;
        String result;

        SharedPreferences postPreference = context.getSharedPreferences("chats_storage_ref",MODE_PRIVATE);

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

    private void setChatLocalStorageRef(Context context,String key, String path)
    {
        SharedPreferences preferences = context.getSharedPreferences("chats_storage_ref",MODE_PRIVATE);

        if (preferences != null)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key,path);
            editor.apply();
        }
    }
}