package com.deffe.macros.status;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

public class VideoAssest extends Activity implements TextureView.SurfaceTextureListener
{

    private static final String TAG = VideoAssest.class.getName();

    private static final String FILE_NAME = "bbb.mp4";

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_assest);

        initView();
    }

    private void initView()
    {
        TextureView textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        Surface surface1 = new Surface(surface);

        try
        {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(FILE_NAME);

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());

            mediaPlayer.setSurface(surface1);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer mp)
                {
                    mediaPlayer.start();
                }
            });
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG,e.getMessage());
        }
        catch (SecurityException e)
        {
            Log.d(TAG,e.getMessage());
        }
        catch (IllegalStateException e)
        {
            Log.d(TAG,e.getMessage());
        }
        catch (IOException e)
        {
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
