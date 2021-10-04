package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import is.arontibo.library.ElasticDownloadView;

public class ChatVideoPlayActivity extends Activity implements PlaybackPreparer
{

    private DatabaseReference ChatsVideoReference;

    private StorageReference VideoReference,MessageVideoStorageRef;

    private PlayerView playerView;

    private ImageView BeforeDownloadImageView;

    private ElasticDownloadView ChatVideoDownloadOrUpload;

    public static final String ACTION_VIEW = "com.deffe.macros.grindersouls.action.VIEW";

    private boolean inErrorState;

    private SimpleExoPlayer player;

    private DefaultTrackSelector trackSelector;

    private DataSource.Factory factory;

    private FileDataSource fileDataSource;

    private Uri uri;

    private String VideoType;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;

    private String online_user_id;

    private FirebaseAuth firebaseAuth;

    private EventLogger eventLogger;

    private Uri OriginalUri;

    private ImageButton ChatSendVideoOk;

    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private String ChatType;

    private String GroupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        shouldAutoPlay = true;
        clearResumePosition();

        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER)
        {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        setContentView(R.layout.activity_chat_video_play);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        ChatSendVideoOk = findViewById(R.id.chat_send_video_ok);

        ChatVideoDownloadOrUpload = findViewById(R.id.chat_video_download_or_upload);

        BeforeDownloadImageView = findViewById(R.id.before_chat_video_download_image_view);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        ChatsVideoReference = FirebaseDatabase.getInstance().getReference();

        MessageVideoStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Videos");

        VideoReference = FirebaseStorage.getInstance().getReference();

        playerView = findViewById(R.id.chat_video_exo_player_view);
        playerView.requestFocus();

        ChatSendVideoOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                releasePlayer();

                playerView.setVisibility(View.GONE);
                ChatSendVideoOk.setVisibility(View.GONE);

                BeforeDownloadImageView.setVisibility(View.GONE);
                ChatVideoDownloadOrUpload.setVisibility(View.VISIBLE);

                ChatVideoDownloadOrUpload.startIntro();

                if (ChatType.equals("single"))
                {
                    final String message_sender_ref = "Messages/" + online_user_id + "/" + ReceivedIds.get(0);
                    final String message_receiver_ref = "Messages/" + ReceivedIds.get(0) + "/" + online_user_id;

                    DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online_user_id).child(ReceivedIds.get(0)).push();

                    final String message_push_id = user_message_key.getKey();

                    final StorageReference filePath = MessageVideoStorageRef.child(message_push_id + ".mp4");

                    DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                    final String time = df.format(Calendar.getInstance().getTime());

                    if (uri != null)
                    {
                        Uri finalUri;

                        if (VideoType.equals("gallery"))
                        {
                            finalUri = OriginalUri;

                            Toast.makeText(ChatVideoPlayActivity.this, "Inner Gallery " + finalUri, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            finalUri = uri;

                            Toast.makeText(ChatVideoPlayActivity.this, "Inner Camera " + finalUri, Toast.LENGTH_SHORT).show();

                        }

                        filePath.putFile(finalUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                    ChatVideoDownloadOrUpload.success();

                                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                    {
                                        @Override
                                        public void onSuccess(StorageMetadata storageMetadata)
                                        {
                                            long sizeInMb = storageMetadata.getSizeBytes();

                                            String size = getStringSizeFromFile(sizeInMb);

                                            Map<String, Object> messageTextBody = new HashMap<>();

                                            messageTextBody.put("message", downloadUrl);
                                            messageTextBody.put("seen", false);
                                            messageTextBody.put("type", MessagesModel.MESSAGE_TYPE_VIDEO);
                                            messageTextBody.put("storage_ref", uri.toString());
                                            messageTextBody.put("time", time);
                                            messageTextBody.put("from", online_user_id);
                                            messageTextBody.put("size", size);

                                            Map<String, Object> messageBodyDetails = new HashMap<>();

                                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
                                                        Log.d("Chat_Log", databaseError.getMessage());


                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(ChatVideoPlayActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(ChatVideoPlayActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                ChatVideoDownloadOrUpload.fail();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                ChatVideoDownloadOrUpload.setProgress((int) progress);

                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(ChatVideoPlayActivity.this, "Uri null", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (ChatType.equals("group"))
                {
                    final ArrayList<String> GroupMessages = new ArrayList<>();

                    ReceivedIds.add(online_user_id);

                    for (String members : ReceivedIds) {
                        GroupMessages.add("Group_Messages/" + GroupKey + "/" + members);
                    }

                    DatabaseReference user_message_key = ChatsVideoReference.child("Group_Messages").child(GroupKey).child(online_user_id).push();

                    final String message_push_id = user_message_key.getKey();

                    DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                    final String time = df.format(Calendar.getInstance().getTime());

                    final StorageReference filePath = MessageVideoStorageRef.child(message_push_id + ".jpg");

                    if (uri != null) {
                        Uri finalUri;

                        if (VideoType.equals("gallery")) {
                            finalUri = OriginalUri;

                            Toast.makeText(ChatVideoPlayActivity.this, "Inner Gallery " + finalUri, Toast.LENGTH_SHORT).show();
                        } else {
                            finalUri = uri;

                            Toast.makeText(ChatVideoPlayActivity.this, "Inner Camera " + finalUri, Toast.LENGTH_SHORT).show();

                        }

                        filePath.putFile(finalUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful()) {
                                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                    {
                                        @Override
                                        public void onSuccess(StorageMetadata storageMetadata) {
                                            long sizeInMb = storageMetadata.getSizeBytes();

                                            String size = getStringSizeFromFile(sizeInMb);

                                            Map<String, Object> messageTextBody = new HashMap<>();

                                            messageTextBody.put("message", downloadUrl);
                                            messageTextBody.put("seen", false);
                                            messageTextBody.put("type", MessagesModel.MESSAGE_TYPE_VIDEO);
                                            messageTextBody.put("time", time);
                                            messageTextBody.put("storage_ref", uri.toString());
                                            messageTextBody.put("size", size);
                                            messageTextBody.put("from", online_user_id);

                                            Map<String, Object> messageBodyDetails = new HashMap<>();

                                            for (String membersRef : GroupMessages) {
                                                messageBodyDetails.put(membersRef + "/" + message_push_id, messageTextBody);
                                            }

                                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
                                                        Log.d("Chat_Log", databaseError.getMessage());


                                                    }
                                                }
                                            });
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatVideoPlayActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                                ChatVideoDownloadOrUpload.fail();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                ChatVideoDownloadOrUpload.setProgress((int) progress);

                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            initializePlayer();
        }
        else
        {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public void preparePlayback()
    {
        initializePlayer();
    }

    private void initializePlayer()
    {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;

        if (needNewPlayer)
        {
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

            eventLogger = new EventLogger(trackSelector);

            player = ExoPlayerFactory.newSimpleInstance(ChatVideoPlayActivity.this, trackSelector);
            player.addListener(new PlayerEventListener());
            player.addListener(eventLogger);
            player.addMetadataOutput(eventLogger);
            player.addAudioDebugListener(eventLogger);
            player.addVideoDebugListener(eventLogger);
            player.setPlayWhenReady(shouldAutoPlay);

            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);

        }

        String action = intent.getAction();

        if (action != null && action.equals(ACTION_VIEW))
        {
            String purpose = intent.getExtras().getString("purpose");

            if (purpose != null && purpose.equals("upload"))
            {
                uri = Uri.parse(intent.getExtras().getString("videoUri"));
                VideoType = intent.getExtras().getString("type");
                OriginalUri = Uri.parse(intent.getExtras().getString("videoOriginalUri"));
                ReceivedIds = intent.getStringArrayListExtra("ids");
                GroupKey = intent.getExtras().getString("group_key");
                ChatType = intent.getExtras().getString("chat_type");

                playerView.setVisibility(View.VISIBLE);
                ChatSendVideoOk.setVisibility(View.VISIBLE);

                BeforeDownloadImageView.setVisibility(View.GONE);
                ChatVideoDownloadOrUpload.setVisibility(View.GONE);

                buildDataFromFileSource(uri);
            }
           /* else if (purpose != null && purpose.equals("play"))
            {
                uri = Uri.parse(intent.getExtras().getString("videoUri"));

                playerView.setVisibility(View.VISIBLE);

                PostVideoOk.setVisibility(View.GONE);
                BeforeDownloadImageView.setVisibility(View.GONE);
                DownloadingProgressBar.setVisibility(View.GONE);
                DownloadingPercentage.setVisibility(View.GONE);
                Downloading.setVisibility(View.GONE);

                buildDataFromFileSource(uri);
            }
            else if (purpose != null && purpose.equals("download"))
            {
                String downloadUrl = intent.getExtras().getString("videoUri");
                final String key = intent.getExtras().getString("key");

                String result = getVideoLocalStorageRef(this,key);

                GlideApp.with(ChatVideoPlayActivity.this).load(downloadUrl).centerCrop().into(BeforeDownloadImageView);

                if (result == null)
                {
                    if (downloadUrl != null)
                    {
                        BeforeDownloadImageView.setVisibility(View.VISIBLE);
                        DownloadingProgressBar.setVisibility(View.VISIBLE);
                        DownloadingPercentage.setVisibility(View.VISIBLE);
                        Downloading.setVisibility(View.VISIBLE);

                        playerView.setVisibility(View.GONE);
                        PostVideoOk.setVisibility(View.GONE);

                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);

                        final File localFile = new File(Environment.getExternalStorageDirectory()
                                + "/" + "GrindersSouls/Grinders Posts","Videos");

                        if (!localFile.exists())
                        {
                            localFile.mkdirs();
                        }

                        final File downloadFile = new File(localFile,"VID_" + System.currentTimeMillis()  + ".mp4");

                        downloadRef.getFile(downloadFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                            {
                                Toast.makeText(ChatVideoPlayActivity.this, "file downloaded", Toast.LENGTH_SHORT).show();

                                BeforeDownloadImageView.setVisibility(View.GONE);
                                DownloadingProgressBar.setVisibility(View.GONE);
                                DownloadingPercentage.setVisibility(View.GONE);
                                Downloading.setVisibility(View.GONE);
                                PostVideoOk.setVisibility(View.GONE);

                                playerView.setVisibility(View.VISIBLE);

                                Uri file = Uri.fromFile(downloadFile);

                                String path = getFilePathFromUri(ChatVideoPlayActivity.this,file);

                                addVideoPostRefInStorage(key,path);

                                uri = Uri.parse(path);

                                if (uri != null)
                                {
                                    buildDataFromFileSource(uri);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(ChatVideoPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                BeforeDownloadImageView.setVisibility(View.GONE);
                                DownloadingProgressBar.setVisibility(View.GONE);
                                DownloadingPercentage.setVisibility(View.GONE);
                                Downloading.setVisibility(View.GONE);
                                PostVideoOk.setVisibility(View.GONE);
                            }


                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot)
                            {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                DownloadingPercentage.setText((int)progress + "%...");

                                DownloadingProgressBar.setProgress((int) progress);
                            }
                        });
                    }
                }
                else
                {
                    BeforeDownloadImageView.setVisibility(View.GONE);
                    DownloadingProgressBar.setVisibility(View.GONE);
                    DownloadingPercentage.setVisibility(View.GONE);
                    Downloading.setVisibility(View.GONE);
                    PostVideoOk.setVisibility(View.GONE);

                    playerView.setVisibility(View.VISIBLE);

                    uri = Uri.parse(result);

                    buildDataFromFileSource(uri);
                }
            }*/
        }
        else
        {
            Toast.makeText(this, "Unexpected Intent Action " + action, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Util.maybeRequestReadExternalStoragePermission(this, uri))
        {
            return;
        }

    }

    public static String getVideoLocalStorageRef(Context context, String key)
    {
        String get = null;
        String result;

        SharedPreferences postPreference = context.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

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

    private void addVideoPostRefInStorage(String key, String path)
    {
        SharedPreferences preferences = this.getSharedPreferences("posts_storage_ref",MODE_PRIVATE);

        if (preferences != null)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key,path);
            editor.apply();
        }
    }

    private void buildDataFromFileSource(Uri uri)
    {
        DataSpec dataSpec = new DataSpec(uri);


        fileDataSource = new FileDataSource();

        try
        {
            fileDataSource.open(dataSpec);
        }
        catch (FileDataSource.FileDataSourceException e)
        {
            e.printStackTrace();
        }


        factory = new DataSource.Factory()
        {
            @Override
            public DataSource createDataSource()
            {
                return fileDataSource;
            }
        };

        final MediaSource mediaSource = new ExtractorMediaSource.Factory(factory).createMediaSource(fileDataSource.getUri(),mainHandler,eventLogger);


        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;

        if (player != null)
        {
            if (haveResumePosition)
            {
                player.seekTo(resumeWindow, resumePosition);
            }

            player.prepare(mediaSource, !haveResumePosition, false);
            inErrorState = false;
        }

    }

    private class PlayerEventListener extends Player.DefaultEventListener
    {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
        {
           /* if (playbackState == Player.STATE_ENDED)
            {
            }*/
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason)
        {
            if (inErrorState)
            {
                updateResumePosition();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);


    }

    private void releasePlayer()
    {
        if (player != null)
        {
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;

            eventLogger = null;
        }
    }

    private void updateResumePosition()
    {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = Math.max(0, player.getContentPosition());
    }

    private void clearResumePosition()
    {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
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
            return decimalFormat.format(size / sizeKb) + "Kb";
        }
        else if (size < sizeGb)
        {
            return decimalFormat.format(size / sizeMb) + "Mb";
        }
        else if (size < sizeTera)
        {
            return decimalFormat.format(size / sizeGb) + "Gb";
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
}