package com.deffe.macros.grindersouls;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

public class VideoPlayActivity extends AppCompatActivity
{
    private VideoView videoView;
    private GrindersMediaController videoController;
    private Uri VideoUri;
    private ImageView PlayVideo,PauseVideo;

    private int position = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        VideoUri = Uri.parse(getIntent().getExtras().getString("videoUri"));

        PlayVideo = findViewById(R.id.play_video);

        PauseVideo = findViewById(R.id.pause_video);

        videoView = findViewById(R.id.videoView);

        videoController = new GrindersMediaController(this,(FrameLayout) findViewById(R.id.controller));
        videoView.setMediaController(videoController);
        videoView.setVideoURI(VideoUri);


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                videoView.seekTo(position);


                if (position == 100)
                {
                    PlayVideo.setVisibility(View.VISIBLE);
                    PauseVideo.setVisibility(View.GONE);

                    videoView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            videoView.start();
                            PlayVideo.setVisibility(View.GONE);
                            PauseVideo.setVisibility(View.GONE);
                        }
                    });
                }
                else
                {
                    PauseVideo.setVisibility(View.VISIBLE);
                    PlayVideo.setVisibility(View.GONE);


                    videoView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            videoView.pause();
                            PlayVideo.setVisibility(View.GONE);
                            PauseVideo.setVisibility(View.VISIBLE);
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("position",videoView.getCurrentPosition());
        videoView.pause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        position = savedInstanceState.getInt("position");
        videoView.seekTo(position);

    }
}
