package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class VideoPlayActivity extends Activity implements PlaybackPreparer {

    private DatabaseReference PostsReference;

    private StorageReference VideoReference;

    private PlayerView playerView;

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

        ImageButton PostVideoOk = findViewById(R.id.post_video_ok);

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        PostsReference = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(online_user_id);

        VideoReference = FirebaseStorage.getInstance().getReference().child("Posts");

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

                StorageReference storageReference = VideoReference.child("Videos").child(upload_key+".mp4");

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
                                                    startActivity(new Intent(VideoPlayActivity.this,EditImageActivity.class));
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
    public void onStart()
    {
        super.onStart();

        if (Util.SDK_INT > 23)
        {
            initializePlayer();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null)
        {
            initializePlayer();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (Util.SDK_INT <= 23)
        {
            releasePlayer();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (Util.SDK_INT > 23)
        {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
            uri = Uri.parse(intent.getExtras().getString("videoUri"));
            VideoType = intent.getExtras().getString("type");
            OriginalUri = Uri.parse(intent.getExtras().getString("videoOriginalUri"));
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

        if (haveResumePosition)
        {
            player.seekTo(resumeWindow, resumePosition);
        }
        player.prepare(mediaSource, !haveResumePosition, false);
        inErrorState = false;
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
}