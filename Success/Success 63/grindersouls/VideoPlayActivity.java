package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VideoPlayActivity extends Activity implements PlaybackPreparer
{

    private DatabaseReference PostsReference;

    private StorageReference VideoReference;

    private PlayerView playerView;

    private ImageView BeforeDownloadImageView;

    private TextView DownloadingPercentage;

    private ProgressBar DownloadingProgressBar;

    private TextView Downloading;

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

    private ImageButton PostVideoOk;

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

        Downloading = findViewById(R.id.video_downloading_view);

        DownloadingPercentage = findViewById(R.id.video_downloading_percentage);

        DownloadingProgressBar = findViewById(R.id.video_downloading_progress_bar);

        BeforeDownloadImageView = findViewById(R.id.before_download_image_view);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        PostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);

        VideoReference = FirebaseStorage.getInstance().getReference();

        playerView = findViewById(R.id.exo_player_view);
        playerView.requestFocus();

        PostVideoOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                releasePlayer();

                DatabaseReference uploadKey = PostsReference.push();

                final String upload_key = uploadKey.getKey();

                final StorageReference storageReference = VideoReference.child("Posts").child("Videos").child(upload_key+".mp4");

                if (uri != null)
                {
                    Uri finalUri;

                    if (VideoType.equals("gallery"))
                    {
                        finalUri = OriginalUri;

                        Toast.makeText(VideoPlayActivity.this, "Inner Gallery "+ finalUri, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        finalUri = uri;

                        Toast.makeText(VideoPlayActivity.this, "Inner Camera "+ finalUri, Toast.LENGTH_SHORT).show();

                    }

                    storageReference.putFile(finalUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                PostsReference.child(upload_key).child("type").setValue(1);
                                PostsReference.child(upload_key).child("post").setValue(downloadUrl);
                                PostsReference.child(upload_key).child("storage_ref").setValue(uri.toString());
                                PostsReference.child(upload_key).child("uploaded_time").setValue(ServerValue.TIMESTAMP);
                                PostsReference.child(upload_key).child("post_key").setValue(upload_key)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
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

                                                            PostsReference.child(upload_key).child("size").setValue(size).addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        Toast.makeText(VideoPlayActivity.this, "Upload successfully..!", Toast.LENGTH_SHORT).show();

                                                                        Intent editIntent = new Intent(VideoPlayActivity.this,EditImageActivity.class);
                                                                        editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(editIntent);
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener()
                                                            {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e)
                                                                {
                                                                    Toast.makeText(VideoPlayActivity.this, "Error while storing post details, Try again", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });;
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(VideoPlayActivity.this, "Size not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });;

                                                }
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(VideoPlayActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(VideoPlayActivity.this, "Uri null", Toast.LENGTH_SHORT).show();
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

                BeforeDownloadImageView.setVisibility(View.GONE);
                DownloadingProgressBar.setVisibility(View.GONE);
                DownloadingPercentage.setVisibility(View.GONE);
                Downloading.setVisibility(View.GONE);

                buildDataFromFileSource(uri);
            }
            else if (purpose != null && purpose.equals("play"))
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

                GlideApp.with(VideoPlayActivity.this).load(downloadUrl).centerCrop().into(BeforeDownloadImageView);

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
                                Toast.makeText(VideoPlayActivity.this, "file downloaded", Toast.LENGTH_SHORT).show();

                                BeforeDownloadImageView.setVisibility(View.GONE);
                                DownloadingProgressBar.setVisibility(View.GONE);
                                DownloadingPercentage.setVisibility(View.GONE);
                                Downloading.setVisibility(View.GONE);
                                PostVideoOk.setVisibility(View.GONE);

                                playerView.setVisibility(View.VISIBLE);

                                Uri file = Uri.fromFile(downloadFile);

                                String path = getFilePathFromUri(VideoPlayActivity.this,file);

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
                                Toast.makeText(VideoPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

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

    public static String getVideoLocalStorageRef(Context context,String key)
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