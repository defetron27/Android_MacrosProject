package com.deffe.macros.profile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    protected static final int CAMERA_CODE = 0x0;

    private Uri selectedImageUri;

    protected String selectedImagePath;
    protected String selectedOutputPath;

    protected static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY = 0x3;
    protected static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA = 0x4;

    @SuppressLint("ObsoleteSdkInt")
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;


    private static final String PHOTO_PATH = "MacrosGallery";

    ImageView addCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openBottomSheet(View view)
    {
        View view1 = getLayoutInflater().inflate(R.layout.bottom_sheet,null);

        final Dialog bottomSheet = new Dialog(MainActivity.this,R.style.MaterialDialogSheet);
        bottomSheet.setContentView(view1);
        bottomSheet.setCancelable(true);
        bottomSheet.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomSheet.getWindow().setGravity(Gravity.BOTTOM);
        bottomSheet.show();


        addCamera = (ImageView) bottomSheet.findViewById(R.id.choose_camera_image);

        addCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(MainActivity.this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            getOutputMediaFile());
                    photoPickerIntent.putExtra("outputFormat",
                            Bitmap.CompressFormat.JPEG.toString());
                    startActivityForResult(
                            Intent.createChooser(photoPickerIntent, getString(R.string.upload_picker_title)),
                            CAMERA_CODE);
                }
            }
        });

    }

    protected Uri getOutputMediaFile()
    {

        if (isSDCARDMounted())
        {
            File mediaStorageDir = new File
                    (
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PHOTO_PATH
                    );
            // Create a storage directory if it does not exist

            if (!mediaStorageDir.exists())
            {
                if (!mediaStorageDir.mkdirs())
                {
                    Log.d("MediaAbstractActivity", getString(R.string.directory_create_fail));
                    return null;
                }
            }

            // Create a media file name

            @SuppressLint("SimpleDateFormat")
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            File mediaFile;

            selectedOutputPath = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";

            Log.d("MediaAbstractActivity", "selected camera path " + selectedOutputPath);

            mediaFile = new File(selectedOutputPath);

            return Uri.fromFile(mediaFile);
        }
        else
        {
            return null;
        }
    }

    protected boolean isSDCARDMounted()
    {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA:
            {
                // If request is cancelled, the result arrays are empty.
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFile());
                    photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                    // photoPickerIntent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(photoPickerIntent, getString(R.string.upload_picker_title)), CAMERA_CODE);

                }
                else
                {
                    Toast.makeText(this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case RESULT_OK:
                selectedImagePath = selectedOutputPath;
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1 ,1).start(this);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
