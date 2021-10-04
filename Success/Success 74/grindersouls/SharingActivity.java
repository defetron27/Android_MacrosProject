package com.deffe.macros.grindersouls;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
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
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class SharingActivity extends AppCompatActivity implements View.OnClickListener
{
    private FirebaseAuth firebaseAuth;

    private String online_user_id;

    private DatabaseReference FriendsListRef,UsersRef;

    private StorageReference MessageImageStorageRef,MessageAudioStorageRef,MessageVideoStorageRef,MessageDocumentStorageRef;

    private ArrayList<String> Friends = new ArrayList<>();

    private ArrayList<String> FriendsSinceDate = new ArrayList<>();

    private ArrayList<String> selectedFriendsToSharing;

    private AllFriendsAdapter allFriendsAdapter;

    private boolean is_in_action_mode = false;

    private DatabaseReference ChatsVideoReference;

    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);

        final Intent shareIntent = getIntent();
        final String action = shareIntent.getAction();
        final String type = shareIntent.getType();

        firebaseAuth = FirebaseAuth.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        FriendsListRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsListRef.keepSynced(true);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);

        ChatsVideoReference = FirebaseDatabase.getInstance().getReference();
        ChatsVideoReference.keepSynced(true);

        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Images");
        MessageAudioStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Audios");
        MessageVideoStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Videos");
        MessageDocumentStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Documents");

        FloatingActionButton SelectedFriendsToSharingButton = findViewById(R.id.sharing_friends_fab);

        RecyclerView friendsRecyclerView = findViewById(R.id.sharing_friends_list);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        allFriendsAdapter = new AllFriendsAdapter(Friends,FriendsSinceDate,SharingActivity.this);
        friendsRecyclerView.setAdapter(allFriendsAdapter);

        FriendsListRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Friends.clear();
                FriendsSinceDate.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Friends.add(snapshot.getKey());
                    FriendsSinceDate.add(snapshot.child("date").getValue().toString());
                }
                allFriendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        SelectedFriendsToSharingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectedFriendsToSharing = new ArrayList<>();

                selectedFriendsToSharing.addAll(allFriendsAdapter.SelectedFriends);

                if (Intent.ACTION_SEND.equals(action) && type !=null)
                {
                    if (type.equals("text/plain"))
                    {
                        String link = shareIntent.getStringExtra(Intent.EXTRA_TEXT);

                        if (link != null)
                        {
                            for (int i=0; i<selectedFriendsToSharing.size(); i++)
                            {
                                if (checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                {
                                    sharingLink(online_user_id,selectedFriendsToSharing.get(i),link);
                                }
                                else if (!checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                {
                                    sharingLink(online_user_id,selectedFriendsToSharing.get(i),link);
                                }
                            }

                            onBackPressed();
                        }
                    }
                    else if (type.startsWith("application/"))
                    {
                        switch (type)
                        {
                            case "application/pdf":
                                {
                                Uri pdfUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                                Uri storageRef;

                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(pdfUri.toString());

                                String fileName = getFileName(pdfUri);

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/GrindersSouls/Grinders Chat/Documents");

                                if (!localFile.exists()) {
                                    localFile.mkdirs();
                                }

                                final Uri pdf_thumbnail = generateImageFromPdf(pdfUri);

                                try {
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                    File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                    Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                    storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, pdfUri)), mediaFile);

                                    for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                        if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingPdf(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, pdf_thumbnail, i);
                                        } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingPdf(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, pdf_thumbnail, i);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                onBackPressed();
                                break;
                            }
                            case "application/zip": {
                                Uri zipUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                                Uri storageRef;

                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(zipUri.toString());

                                String fileName = getFileName(zipUri);

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/GrindersSouls/Grinders Chat/Documents");

                                if (!localFile.exists()) {
                                    localFile.mkdirs();
                                }

                                try {
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                    File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                    Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                    storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, zipUri)), mediaFile);

                                    for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                        if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                onBackPressed();
                                break;
                            }
                            case "application/txt": {
                                Uri txtUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                                Uri storageRef;

                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(txtUri.toString());

                                String fileName = getFileName(txtUri);

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/GrindersSouls/Grinders Chat/Documents");

                                if (!localFile.exists()) {
                                    localFile.mkdirs();
                                }

                                try {
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                    File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                    Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                    storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, txtUri)), mediaFile);

                                    for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                        if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                onBackPressed();
                                break;
                            }
                            case "application/vnd.android.package-archive": {
                                Uri apkUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                                Uri storageRef;

                                String fileName = getFileName(apkUri);

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/GrindersSouls/Grinders Chat/Documents");

                                if (!localFile.exists()) {
                                    localFile.mkdirs();
                                }

                                try {
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                    File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + ".apk");

                                    storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, apkUri)), mediaFile);

                                    for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                        if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, "apk", i);
                                        } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, "apk", i);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                onBackPressed();
                                break;
                            }
                            default: {
                                Uri pdfUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                                Uri storageRef;

                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(pdfUri.toString());

                                Toast.makeText(SharingActivity.this, "exe " + fileExtension, Toast.LENGTH_SHORT).show();

                                Toast.makeText(SharingActivity.this, "type " + type, Toast.LENGTH_LONG).show();

                           /* String fileName = getFileName(pdfUri);

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Documents");

                            if (!localFile.exists())
                            {
                                localFile.mkdirs();
                            }

                            try
                            {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this,pdfUri)), mediaFile);

                                for (int i=0; i<selectedFriendsToSharing.size(); i++)
                                {
                                    if (checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,fileExtension,i);
                                    }
                                    else if (!checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,fileExtension,i);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                            onBackPressed();*/
                                break;
                            }
                        }

                    }
                    else if (type.startsWith("image/"))
                    {
                        Uri imageUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                        String fileName = getFileName(imageUri);

                        Uri storageRef;

                        if (imageUri != null)
                        {
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

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this,imageUri)), mediaFile);

                                for (int i=0; i<selectedFriendsToSharing.size(); i++)
                                {
                                    if (checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingImage(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                    else if (!checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingImage(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                            onBackPressed();
                        }
                    }
                    else if (type.startsWith("audio/"))
                    {
                        Uri audioUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                        String fileName = getFileName(audioUri);

                        Uri storageRef;

                        if (audioUri != null)
                        {
                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Audios");

                            if (!localFile.exists())
                            {
                                localFile.mkdirs();
                            }
                            try
                            {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "AUD_" + timeStamp + ".mp3");

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this,audioUri)), mediaFile);

                                for (int i=0; i<selectedFriendsToSharing.size(); i++)
                                {
                                    if (checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingAudio(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                    else if (!checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingAudio(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                            onBackPressed();
                        }
                    }
                    else if (type.startsWith("video/"))
                    {
                        Uri videoUri = shareIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                        String fileName = getFileName(videoUri);

                        Uri storageRef;

                        if (videoUri != null)
                        {
                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Videos");

                            if (!localFile.exists())
                            {
                                localFile.mkdirs();
                            }
                            try
                            {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this,videoUri)), mediaFile);

                                for (int i=0; i<selectedFriendsToSharing.size(); i++)
                                {
                                    if (checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingVideo(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                    else if (!checkDate(online_user_id,selectedFriendsToSharing.get(i)))
                                    {
                                        sharingVideo(online_user_id,selectedFriendsToSharing.get(i),storageRef,fileName,i);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                            onBackPressed();
                        }
                    }
                }
                else if (Intent.ACTION_VIEW.equals(action) && type !=null)
                {
                    switch (type) {
                        case "pdf": {
                            Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                            String fileName = shareIntent.getExtras().getString("file_name");

                            Uri storageRef;

                            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(storageUri.toString());

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Documents");

                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }

                            final Uri pdf_thumbnail = generateImageFromPdf(storageUri);

                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, storageUri)), mediaFile);

                                for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                    if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingPdf(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, pdf_thumbnail, i);
                                    } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingPdf(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, pdf_thumbnail, i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            onBackPressed();
                            break;
                        }
                        case "zip":
                            {
                                Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                                String fileName = shareIntent.getExtras().getString("file_name");

                                Uri storageRef;

                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(storageUri.toString());

                                final File localFile = new File(Environment.getExternalStorageDirectory()
                                        + "/GrindersSouls/Grinders Chat/Documents");

                                if (!localFile.exists()) {
                                    localFile.mkdirs();
                                }

                                try {
                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                    File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                    Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                    storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, storageUri)), mediaFile);

                                    for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                        if (checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                        {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        }
                                        else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                        {
                                            sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                onBackPressed();
                                break;
                        }
                        case "txt":
                        {
                            Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                            String fileName = shareIntent.getExtras().getString("file_name");

                            Uri storageRef;

                            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(storageUri.toString());



                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Documents");

                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }

                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + "." + fileExtension);

                                Toast.makeText(SharingActivity.this, "EXE " + fileExtension, Toast.LENGTH_SHORT).show();

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, storageUri)), mediaFile);

                                for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                    if (checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                    }
                                    else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, fileExtension, i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            onBackPressed();
                            break;
                        }
                        case "apk":
                        {
                            Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                            String fileName = shareIntent.getExtras().getString("file_name");

                            Uri storageRef;

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Documents");

                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }

                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "DOC_" + timeStamp + ".apk");

                                storageRef = copyFileToFolder(new File(getFilePathFromUri(SharingActivity.this, storageUri)), mediaFile);

                                for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                    if (checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, "apk", i);
                                    }
                                    else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i)))
                                    {
                                        sharingDocument(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, "apk", i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            onBackPressed();
                            break;
                        }
                        case "image":
                            {
                            Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                                String fileName = shareIntent.getExtras().getString("file_name");

                            Uri storageRef;

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Images");

                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }
                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

                                File file = new File(storageUri.getPath());

                                storageRef = copyFileToFolder(file, mediaFile);

                                for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                    if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingImage(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, i);
                                    } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingImage(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            onBackPressed();
                            break;
                        }
                        case "video": {
                            Uri storageUri = Uri.parse(shareIntent.getExtras().getString("uri"));
                            String fileName = shareIntent.getExtras().getString("file_name");

                            Uri storageRef;

                            final File localFile = new File(Environment.getExternalStorageDirectory()
                                    + "/GrindersSouls/Grinders Chat/Videos");

                            if (!localFile.exists()) {
                                localFile.mkdirs();
                            }
                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

                                File mediaFile = new File(localFile.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

                                File file = new File(storageUri.getPath());

                                storageRef = copyFileToFolder(file, mediaFile);

                                for (int i = 0; i < selectedFriendsToSharing.size(); i++) {
                                    if (checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingVideo(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, i);
                                    } else if (!checkDate(online_user_id, selectedFriendsToSharing.get(i))) {
                                        sharingVideo(online_user_id, selectedFriendsToSharing.get(i), storageRef, fileName, i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            onBackPressed();
                            break;
                        }
                    }
                }
            }
        });
    }

    public class AllFriendsAdapter extends RecyclerView.Adapter<AllFriendsAdapter.FriendsViewHolder>
    {
        ArrayList<String> Friends;
        ArrayList<String> FriendsSinceDate;
        SharingActivity sharingActivity;
        Context context;

        ArrayList<String> SelectedFriends = new ArrayList<>();

        AllFriendsAdapter(ArrayList<String> friends, ArrayList<String> friendsSinceDate, Context context)
        {
            Friends = friends;
            FriendsSinceDate = friendsSinceDate;
            sharingActivity = (SharingActivity) context;
            this.context = context;
        }

        @NonNull
        @Override
        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.user_friends_list_items_layout_for_activity,parent,false);

            return new FriendsViewHolder(view,sharingActivity);
        }

        @Override
        public void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position)
        {
            final String userFriendId = Friends.get(position);
            final String friendSinceDate = "Friends Since:\n"+ "    " + FriendsSinceDate.get(position);

            UsersRef.child(userFriendId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final String friendName = (String) dataSnapshot.child("user_name").getValue();
                    final String friendThumbImage = (String) dataSnapshot.child("user_thumb_img").getValue();

                    holder.userName.setText(friendName);
                    holder.setUser_thumb_img(SharingActivity.this,friendThumbImage);
                    holder.userFriendSinceDate.setText(friendSinceDate);

                    if (!sharingActivity.is_in_action_mode)
                    {
                        holder.selectUserToSharingCheckBox.setVisibility(View.VISIBLE);
                        holder.selectUserToSharingCheckBox.setChecked(false);
                    }
                    else
                    {
                        holder.selectUserToSharingCheckBox.setVisibility(View.VISIBLE);
                        holder.selectUserToSharingCheckBox.setChecked(false);
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (holder.selectUserToSharingCheckBox.isChecked())
                            {
                                SelectedFriends.remove(Friends.get(position));

                                holder.selectUserToSharingCheckBox.setChecked(false);
                            }
                            else if (!holder.selectUserToSharingCheckBox.isChecked())
                            {
                                SelectedFriends.add(Friends.get(position));

                                holder.selectUserToSharingCheckBox.setChecked(true);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return Friends.size();
        }

        class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            View v;

            TextView userName,userFriendSinceDate;

            CheckBox selectUserToSharingCheckBox;

            SharingActivity sharingActivity;

            FriendsViewHolder(View itemView,SharingActivity sharingActivity)
            {
                super(itemView);

                this.sharingActivity = sharingActivity;

                v = itemView;

                userName = v.findViewById(R.id.user_friend_list_name);
                userFriendSinceDate = v.findViewById(R.id.user_friend_list_since_date);
                selectUserToSharingCheckBox = v.findViewById(R.id.select_user_to_add_to_group_check_box);

                selectUserToSharingCheckBox.setOnClickListener(this);
            }

            void setUser_thumb_img(final Context c, final String user_thumb_img)
            {
                final CircleImageView thumb_img = v.findViewById(R.id.user_friend_list_image);

                Picasso.with(c).load(user_thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.vadim)
                        .into(thumb_img, new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {

                            }

                            @Override
                            public void onError()
                            {
                                Picasso.with(c).load(user_thumb_img).placeholder(R.drawable.vadim).into(thumb_img);
                            }
                        });
            }

            @Override
            public void onClick(View v)
            {
                sharingActivity.prepareSelection(v,getAdapterPosition());
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        is_in_action_mode = true;

        allFriendsAdapter.notifyDataSetChanged();
    }

    public void prepareSelection(View view, int position)
    {

        if (((CheckBox)view).isChecked())
        {
            allFriendsAdapter.SelectedFriends.add(Friends.get(position));
        }
        else
        {
            allFriendsAdapter.SelectedFriends.remove(Friends.get(position));
        }
    }

    private void sharingLink(String online, String Receiver, String messageTextLink)
    {
        String message_sender_ref = "Messages/" + online + "/" + Receiver;

        String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        String message_push_id = user_message_key.getKey();

        Map<String,Object> messageTextBody = new HashMap<>();

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        messageTextBody.put("message",messageTextLink);
        messageTextBody.put("seen",false);
        messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_TEXT_LINK);
        messageTextBody.put("time", time);
        messageTextBody.put("from",online);
        messageTextBody.put("ref","");
        messageTextBody.put("size","");
        messageTextBody.put("key",message_push_id);
        messageTextBody.put("duration","");
        messageTextBody.put("exe","link");
        messageTextBody.put("file_name","");

        Map<String,Object> messageBodyDetails = new HashMap<>();

        messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

        messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

        ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
            {
                if(databaseError != null)
                {
                    Log.d("Chat_Log", databaseError.getMessage());
                }
            }
        });
    }

    private void sharingDocument(final String online, String Receiver, final Uri storage, final String fileName, final String fileExtension, final int uploadingId)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receiver;
        final String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageDocumentStorageRef.child(message_push_id + "." + fileExtension);

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(SharingActivity.this);
        builder.setContentTitle("Grinders send pdf");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(storage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(uploadingId, builder.build());

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                    {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            long sizeInMb = storageMetadata.getSizeBytes();

                            String size = getStringSizeFromFile(sizeInMb);

                            Map<String, Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_DOCUMENT);
                            messageTextBody.put("ref", storage.toString());
                            messageTextBody.put("time", time);
                            messageTextBody.put("from", online);
                            messageTextBody.put("size", size);
                            messageTextBody.put("key", message_push_id);
                            messageTextBody.put("exe",fileExtension);
                            messageTextBody.put("file_name",fileName);

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    notificationManager.cancel(uploadingId);
                                    builder = null;

                                    if (databaseError != null) {
                                        Log.d("Chat_Log", databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SharingActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(uploadingId, builder.build());

                Toast.makeText(SharingActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(uploadingId, builder.build());
            }
        });
    }

    private void sharingPdf(final String online, String Receiver, final Uri storage, final String fileName, final Uri thumbnail, final int uploadingId)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receiver;
        final String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageDocumentStorageRef.child(message_push_id + ".pdf");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(SharingActivity.this);
        builder.setContentTitle("Grinders send pdf");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(storage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                    StorageReference reference = MessageDocumentStorageRef.child("PDF_Thumbnails").child(message_push_id + ".png");

                    reference.putFile(thumbnail).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                builder.setContentText("Upload Completed").setProgress(0, 0, false);
                                notificationManager.notify(uploadingId, builder.build());

                                final String download_thumb = task.getResult().getDownloadUrl().toString();

                                filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                                {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        long sizeInMb = storageMetadata.getSizeBytes();

                                        String size = getStringSizeFromFile(sizeInMb);

                                        Map<String, Object> messageTextBody = new HashMap<>();

                                        messageTextBody.put("message", downloadUrl);
                                        messageTextBody.put("seen", false);
                                        messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_DOCUMENT);
                                        messageTextBody.put("ref", storage.toString());
                                        messageTextBody.put("time", time);
                                        messageTextBody.put("from", online);
                                        messageTextBody.put("size", size);
                                        messageTextBody.put("key", message_push_id);
                                        messageTextBody.put("exe","pdf");
                                        messageTextBody.put("file_name",fileName);
                                        messageTextBody.put("pdf_thumbnail",download_thumb);

                                        Map<String, Object> messageBodyDetails = new HashMap<>();

                                        messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                                        messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                                        ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                notificationManager.cancel(uploadingId);
                                                builder = null;

                                                if (databaseError != null) {
                                                    Log.d("Chat_Log", databaseError.getMessage());
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SharingActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(uploadingId, builder.build());

                Toast.makeText(SharingActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(uploadingId, builder.build());
            }
        });
    }

    private void sharingImage(final String online, String Receiver, final Uri storage, final String fileName, final int uploadingId)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receiver;
        final String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(SharingActivity.this);
        builder.setContentTitle("Grinders send image");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(storage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(uploadingId, builder.build());

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            long sizeInMb = storageMetadata.getSizeBytes();

                            String size = getStringSizeFromFile(sizeInMb);

                            Map<String, Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_IMAGE);
                            messageTextBody.put("ref", storage.toString());
                            messageTextBody.put("time", time);
                            messageTextBody.put("from", online);
                            messageTextBody.put("size", size);
                            messageTextBody.put("key", message_push_id);
                            messageTextBody.put("exe","jpg");
                            messageTextBody.put("file_name",fileName);

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    notificationManager.cancel(uploadingId);
                                    builder = null;

                                    if (databaseError != null) {
                                        Log.d("Chat_Log", databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SharingActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(uploadingId, builder.build());

                Toast.makeText(SharingActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(uploadingId, builder.build());
            }
        });
    }

    private void sharingAudio(final String online, String Receiver, final Uri storage, final String fileName, final int uploadingId)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receiver;
        final String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageAudioStorageRef.child(message_push_id + ".mp3");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SharingActivity.this,storage);

        String audioDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(audioDuration);

        final String duration = String.format(Locale.getDefault(),"%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));

        retriever.release();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(SharingActivity.this);
        builder.setContentTitle("Grinders send Audio");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(storage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(uploadingId, builder.build());

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            long sizeInMb = storageMetadata.getSizeBytes();

                            String size = getStringSizeFromFile(sizeInMb);

                            Map<String, Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_AUDIO);
                            messageTextBody.put("ref", storage.toString());
                            messageTextBody.put("time", time);
                            messageTextBody.put("from", online);
                            messageTextBody.put("size", size);
                            messageTextBody.put("key", message_push_id);
                            messageTextBody.put("duration",duration);
                            messageTextBody.put("exe","mp3");
                            messageTextBody.put("file_name",fileName);

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    notificationManager.cancel(uploadingId);
                                    builder = null;

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

                            Toast.makeText(SharingActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(uploadingId, builder.build());

                Toast.makeText(SharingActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(uploadingId, builder.build());
            }
        });
    }

    private void sharingVideo(final String online, String Receiver, final Uri storage, final String fileName, final int uploadingId)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receiver;
        final String message_receiver_ref = "Messages/" + Receiver + "/" + online;

        DatabaseReference user_message_key = ChatsVideoReference.child("Messages").child(online).child(Receiver).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageVideoStorageRef.child(message_push_id + ".mp4");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SharingActivity.this,storage);

        String videoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long timeInMillisec = Long.parseLong(videoDuration);

        final String duration = String.format(Locale.getDefault(),"%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)));

        retriever.release();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(SharingActivity.this);
        builder.setContentTitle("Grinders send video");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(storage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();

                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(uploadingId, builder.build());

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            long sizeInMb = storageMetadata.getSizeBytes();

                            String size = getStringSizeFromFile(sizeInMb);

                            Map<String, Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", MessageTypesModel.MESSAGE_TYPE_VIDEO);
                            messageTextBody.put("ref", storage.toString());
                            messageTextBody.put("time", time);
                            messageTextBody.put("from", online);
                            messageTextBody.put("size", size);
                            messageTextBody.put("key", message_push_id);
                            messageTextBody.put("duration",duration);
                            messageTextBody.put("exe","mp4");
                            messageTextBody.put("file_name",fileName);

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    notificationManager.cancel(uploadingId);
                                    builder = null;

                                    if (databaseError != null) {
                                        Log.d("Chat_Log", databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SharingActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(uploadingId, builder.build());

                Toast.makeText(SharingActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(uploadingId, builder.build());
            }
        });
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

        DatabaseReference date_key = ChatsVideoReference.child("Messages").child(online_key).child(receiver_key).push();

        final String date_push_id = date_key.getKey();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("type", MessageTypesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("date", getTodayDate());
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);

        Map<String,Object> messageBodyDetails = new HashMap<>();

        messageBodyDetails.put(message_sender_ref + "/" + date_push_id, messageDate);

        messageBodyDetails.put(message_receiver_ref + "/" + date_push_id, messageDate);

        ChatsVideoReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
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

    private boolean checkDate(String online,String receiver)
    {
        String today = readDateRef(SharingActivity.this,online,receiver);

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

    private String getStringSizeFromFile(long size)
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

    public String getFileName(Uri uri)
    {
        String result = null;

        if (uri.getScheme().equals("content"))
        {
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);

            try
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            }
        }

        if (result == null)
        {
            result = uri.getPath();

            int cut = result.lastIndexOf("/");

            if (cut != -1)
            {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    private Uri generateImageFromPdf(Uri pdfUri)
    {
        int pageNumber = 0;

        PdfiumCore pdfiumCore = new PdfiumCore(this);

        Bitmap bitmap = null;

        Uri uri = null;

        try
        {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri,"r");

            PdfDocument pdfDocument = pdfiumCore.newDocument(fileDescriptor);

            pdfiumCore.openPage(pdfDocument,pageNumber);

            int width = pdfiumCore.getPageWidthPoint(pdfDocument,pageNumber);

            int height = pdfiumCore.getPageHeightPoint(pdfDocument,pageNumber);

            bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

            pdfiumCore.renderPageBitmap(pdfDocument,bitmap,pageNumber,0,0,width,height);

            pdfiumCore.closeDocument(pdfDocument);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error.." + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (bitmap != null)
        {
            uri = getImageUriFromBitmap(SharingActivity.this,bitmap);
        }

        return uri;
    }

    private Uri getImageUriFromBitmap(Context context, Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,null,null);

        return Uri.parse(path);
    }
}