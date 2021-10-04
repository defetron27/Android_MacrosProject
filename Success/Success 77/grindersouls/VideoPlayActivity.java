package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceActivity;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.crashlytics.android.Crashlytics;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import is.arontibo.library.ElasticDownloadView;

public class VideoPlayActivity extends Activity implements PlaybackPreparer
{
    private static final String POST_VIDEO_UPLOADING_CHANNEL_ID = "com.deffe.macros.grindersouls.UPLOADING_VIDEO";
    private static final int POST_VIDEO_UPLOADING_ID = (int) ((new Date().getTime() / 100L) % Integer.MAX_VALUE);

    private boolean isUploading = false;

    private CollectionReference postsReference;
    private StorageReference VideoReference;

    private ArrayList<String> userFriends = new ArrayList<>();

    private PlayerView playerView;
    public static final String ACTION_VIEW = "com.deffe.macros.grindersouls.action.VIEW";
    private boolean inErrorState;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory factory;

    private FileDataSource fileDataSource;
    private Uri uri;

    private String VideoType;
    private GrinderLoadingProgressBar grinderLoadingProgressBar = new GrinderLoadingProgressBar();

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

    private ImageButton PostVideoOk;

    private NotificationManagerCompat notificationManager;

    private NotificationCompat.Builder builder;

    private static final String TAG = VideoPlayActivity.class.getSimpleName();

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

        setContentView(R.layout.activity_video_play);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        PostVideoOk = findViewById(R.id.post_video_ok);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        CollectionReference userFriendsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("Friends");
        final CollectionReference friendsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts");
        postsReference = FirebaseFirestore.getInstance().collection("Friend_Notifications_And_Posts").document(online_user_id).collection("User_Posts");

        VideoReference = FirebaseStorage.getInstance().getReference().child("Posts").child("Videos").child(online_user_id);

        playerView = findViewById(R.id.exo_player_view);
        playerView.requestFocus();

        userFriendsReference.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e)
            {
                if (e != null)
                {
                    Toast.makeText(VideoPlayActivity.this, "Error while getting documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
                if (queryDocumentSnapshots != null)
                {
                    userFriends.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments())
                    {
                        userFriends.add(snapshot.getId());
                    }
                }
            }
        });

        PostVideoOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(VideoPlayActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(VideoPlayActivity.this))
                    {
                        if (!isUploading)
                        {
                            isUploading = true;

                            grinderLoadingProgressBar.showLoadingBar(VideoPlayActivity.this,"Please wait... video is uploading");

                            releasePlayer();

                            playerView.setVisibility(View.GONE);
                            PostVideoOk.setVisibility(View.GONE);

                            DocumentReference uploadKey = postsReference.document();

                            final String upload_key = uploadKey.getId();

                            final StorageReference storageReference = VideoReference.child(upload_key+".mp4");

                            if (uri != null)
                            {
                                Uri finalUri = null;
                                Uri storageUri = null;

                                if (VideoType.equals("gallery"))
                                {

                                    final File localFile = new File(Environment.getExternalStorageDirectory()
                                            + "/GrindersSouls/Grinders Posts/Videos");

                                    if (!localFile.exists())
                                    {
                                        localFile.mkdirs();
                                    }
                                    try
                                    {
                                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                        File mediaFile = new File(localFile.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

                                        storageUri = copyFileToFolder(new File(getFilePathFromUri(VideoPlayActivity.this,OriginalUri)), mediaFile);
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if (storageUri != null)
                                    {
                                        finalUri = storageUri;
                                    }

                                    Toast.makeText(VideoPlayActivity.this, "Inner Gallery "+ finalUri, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    finalUri = uri;

                                    Toast.makeText(VideoPlayActivity.this, "Inner Camera "+ finalUri, Toast.LENGTH_SHORT).show();
                                }

                         /*       notificationManager = NotificationManagerCompat.from(VideoPlayActivity.this);
                                builder = new NotificationCompat.Builder(VideoPlayActivity.this,POST_VIDEO_UPLOADING_CHANNEL_ID);
                                builder.setContentTitle("Post Video Uploading")
                                        .setSmallIcon(R.drawable.notifcation_upload)
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setOngoing(false)
                                        .setAutoCancel(true);*/

                                grinderLoadingProgressBar.hideLoadingBar();
                                onBackPressed();

                                final Uri finalUri1 = finalUri;

                                if (finalUri != null)
                                {
                                    UploadTask uploadTask = storageReference.putFile(finalUri);

                                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                                    {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                                        {
                                            if (!task.isSuccessful())
                                            {

                                                Log.e(TAG,task.getException().toString());
                                                Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                                                Toast.makeText(VideoPlayActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                            return storageReference.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull final Task<Uri> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                                {
                                                    @Override
                                                    public void onSuccess(StorageMetadata storageMetadata)
                                                    {
                                                        long sizeInMb = storageMetadata.getSizeBytes();

                                                        String size = getStringSizeFromFile(sizeInMb);

                                                     /*   builder.setContentText("Upload Completed").setProgress(0,0,false);
                                                        notificationManager.notify(POST_VIDEO_UPLOADING_ID,builder.build());
*/
                                                        final String downloadUrl = task.getResult().toString();

                                                        Map<String, Object> postDetails = new HashMap<>();

                                                        postDetails.put("post",downloadUrl);
                                                        postDetails.put("type","1");
                                                        postDetails.put("ref",finalUri1.toString());
                                                        postDetails.put("uploaded_time", FieldValue.serverTimestamp());
                                                        postDetails.put("post_key",upload_key);
                                                        postDetails.put("post_user_key",online_user_id);
                                                        postDetails.put("size",size);

                                                        postsReference.document(upload_key).set(postDetails).addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    for (String friend : userFriends)
                                                                    {
                                                                        Map<String, Object> friendsPosts = new HashMap<>();

                                                                        friendsPosts.put(upload_key,FieldValue.serverTimestamp());

                                                                        friendsReference.document(friend).collection("Friends_Posts").document(online_user_id).set(friendsPosts);
                                                                    }

                                                                    Toast.makeText(VideoPlayActivity.this, "Video post uploaded successfully..!", Toast.LENGTH_SHORT).show();

                                                                   /* notificationManager.cancel(POST_VIDEO_UPLOADING_ID);
                                                                    builder = null;
*/
                                                                    isUploading = false;
                                                                }
                                                                if (task.isCanceled())
                                                                {
                                                                  /*  notificationManager.cancel(POST_VIDEO_UPLOADING_ID);
                                                                    builder = null;*/
                                                                    Toast.makeText(VideoPlayActivity.this, "Upload Cancelled", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                              /*  builder.setContentTitle("Uploading failed").setOngoing(false);
                                                                notificationManager.notify(POST_VIDEO_UPLOADING_ID, builder.build());
*/
                                                                Log.e(TAG,e.toString());
                                                                Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                                                Toast.makeText(VideoPlayActivity.this, "Error while storing post details, Try again " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener()
                                                {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {

                                                        Toast.makeText(VideoPlayActivity.this, "Size not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            /*builder.setContentTitle("Uploading failed").setOngoing(false);
                                            notificationManager.notify(0,builder.build());*/

                                            Toast.makeText(VideoPlayActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            else
                            {
                                Toast.makeText(VideoPlayActivity.this, "Uri null", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(VideoPlayActivity.this, "Please wait another uploading is progress..!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.login_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.login_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
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
    public void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            FirebaseFirestore.getInstance().collection("Users").document(online_user_id).update("online","true");
        }

        if (Util.SDK_INT > 23)
        {
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
    public void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null)
        {
            FirebaseFirestore.getInstance().collection("Users").document(online_user_id).update("online", FieldValue.serverTimestamp());
        }

        if (Util.SDK_INT > 23)
        {
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

            player = ExoPlayerFactory.newSimpleInstance(VideoPlayActivity.this, trackSelector);
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

                playerView.setVisibility(View.VISIBLE);
                PostVideoOk.setVisibility(View.VISIBLE);

                buildDataFromFileSource(uri);
            }
            else if (purpose != null && purpose.equals("play")) {
                uri = Uri.parse(intent.getExtras().getString("videoUri"));

                playerView.setVisibility(View.VISIBLE);

                PostVideoOk.setVisibility(View.GONE);

                buildDataFromFileSource(uri);
            }
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isUploading)
        {
            notificationManager.cancel(POST_VIDEO_UPLOADING_ID);
            builder = null;

            isUploading = false;
        }
    }
}