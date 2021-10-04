package com.deffe.macros.status;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE = 300;
    private static final int TAKE_IMAGE_FROM_GALLERY_REQUEST_CODE = 400;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private String selectedVideoPath;

    private Uri fileUri;

    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture,btnRecordVideo,btnTakeVideoFromGallery,btnTakeImageFromGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPreview = findViewById(R.id.imgPreview);
        videoPreview = findViewById(R.id.videoPreview);
        btnCapturePicture = findViewById(R.id.btnCapturePicture);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);

        btnTakeVideoFromGallery = findViewById(R.id.takeVideoFromGallery);
        btnTakeImageFromGallery = findViewById(R.id.takeImageFromGallery);

        Button videoTexture = findViewById(R.id.goto_texture);

        videoTexture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this,VideoAssest.class));
            }
        });

        btnCapturePicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                captureImage();
            }
        });

        btnRecordVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recordVideo();
            }
        });

        btnTakeImageFromGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryImageIntent.setType("image/*");
                startActivityForResult(galleryImageIntent,TAKE_IMAGE_FROM_GALLERY_REQUEST_CODE);
            }
        });

        btnTakeVideoFromGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryVideoIntent.setType("video/*");
                startActivityForResult(galleryVideoIntent,TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE);

            }
        });

        if (!isDeviceSupportCamera())
        {
            Toast.makeText(this, "Your does not support camera", Toast.LENGTH_SHORT).show();

            finish();
        }

    }

    private boolean isDeviceSupportCamera()
    {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    private void captureImage()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(cameraIntent,CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    private void recordVideo()
    {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(videoIntent,CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                previewCaptureImage();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "User cancelled image capture", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                previewVideo();
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
        else if (requestCode == TAKE_IMAGE_FROM_GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                videoPreview.setVisibility(View.GONE);

                imgPreview.setVisibility(View.VISIBLE);

                Picasso.with(MainActivity.this).load(data.getData()).noPlaceholder().into(imgPreview);
            }
        }
        else if (requestCode == TAKE_VIDEO_FROM_GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Uri videoUri = data.getData();

                imgPreview.setVisibility(View.GONE);

                videoPreview.setVisibility(View.VISIBLE);

                videoPreview.setVideoURI(videoUri);
                videoPreview.start();

            }
        }
    }

    private void previewCaptureImage()
    {
        try
        {
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);

            imgPreview.setImageBitmap(bitmap);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void previewVideo()
    {
        try
        {
            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(fileUri.getPath());

            videoPreview.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type)
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(IMAGE_DIRECTORY_NAME,"Oops! failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        }
        else if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }
        else
        {
            return null;
        }

        return mediaFile;
    }
}