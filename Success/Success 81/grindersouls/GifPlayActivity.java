package com.deffe.macros.grindersouls;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

public class GifPlayActivity extends AppCompatActivity
{
    private final static String TAG = GifPlayActivity.class.getSimpleName();

    private Uri uri;
    private String Type;
    private String GroupKey;
    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private String online_user_id;

    private FirebaseFirestore rootRef;

    private StorageReference MessageGifStorageRef;

    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_play);

        ImageView gifImageView = findViewById(R.id.gif_image_view);
        ImageButton sendGif = findViewById(R.id.send_gif);

        String purpose = getIntent().getExtras().getString("purpose");

        if (purpose != null && purpose.equals("upload"))
        {
            uri = Uri.parse(getIntent().getExtras().getString("gifUri"));
            Type = getIntent().getType();
            ReceivedIds = getIntent().getStringArrayListExtra("ids");
            GroupKey = getIntent().getExtras().getString("group_key");
        }
        else if (purpose != null && purpose.equals("play"))
        {
            sendGif.setVisibility(View.GONE);

            uri = Uri.parse(getIntent().getExtras().getString("gifUri"));

            GlideApp.with(GifPlayActivity.this).load(uri).centerCrop().into(gifImageView);
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseFirestore.getInstance();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        MessageGifStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Gifs");

        if (uri != null)
        {
            GlideApp.with(GifPlayActivity.this).load(uri).into(gifImageView);
        }

        sendGif.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (NetworkStatus.isConnected(GifPlayActivity.this))
                {
                    if (NetworkStatus.isConnectedFast(GifPlayActivity.this))
                    {
                        if (!isUploading)
                        {
                            if (uri != null)
                            {
                                if (Type.equals("single"))
                                {
                                    if (checkSingleDate(online_user_id,ReceivedIds.get(0)))
                                    {
                                        storeSingleGifDetails(online_user_id, ReceivedIds, uri, MessageTypesModel.MESSAGE_TYPE_GIF);

                                        onBackPressed();
                                    }
                                    else if (!checkSingleDate(online_user_id,ReceivedIds.get(0)))
                                    {
                                        storeSingleGifDetails(online_user_id, ReceivedIds, uri, MessageTypesModel.MESSAGE_TYPE_GIF);

                                        onBackPressed();
                                    }
                                }
                                else if (Type.equals("group"))
                                {
                                    if (checkGroupDate(online_user_id, GroupKey, ReceivedIds))
                                    {
                                        storeGroupGifDetails(online_user_id, ReceivedIds, uri, GroupKey, MessageTypesModel.MESSAGE_TYPE_GIF);

                                        onBackPressed();
                                    }
                                    else if (!checkGroupDate(online_user_id, GroupKey, ReceivedIds))
                                    {
                                        storeGroupGifDetails(online_user_id, ReceivedIds, uri, GroupKey, MessageTypesModel.MESSAGE_TYPE_GIF);

                                        onBackPressed();
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(GifPlayActivity.this, "uri null", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(GifPlayActivity.this, "Please wait another gif is sending", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.gif_play_activity),"Poor Connection,Please wait or try again",Snackbar.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Snackbar.make(findViewById(R.id.gif_play_activity),"No Internet Connection",Snackbar.LENGTH_LONG).show();
                }
            }
        });
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

    private String getTodayDate()
    {
        DateFormat todayDate = new SimpleDateFormat("d MMM yyyy", Locale.US);

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

    private String readDateRef(Context context, String online_key, String receiver_key)
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
        String today = readDateRef(GifPlayActivity.this,online,receiver);

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
        String today = readDateRef(GifPlayActivity.this,online,groupKey);

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
        messageDate.put("date", FieldValue.serverTimestamp());

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
                                Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                Log.e(TAG,task.getException().getMessage());
                                Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,e.toString());
                    Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                }
            });
        }
    }

    private void storeSingleGifDetails(final String online, final ArrayList<String> Receivers, final Uri finalUri, final int uriType)
    {
        DocumentReference date_key = rootRef.collection("Messages").document(online).collection(Receivers.get(0)).document();

        isUploading = true;

        final String message_push_id = date_key.getId();
        final StorageReference filePath = MessageGifStorageRef.child(message_push_id + ".gif");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = NotificationManagerCompat.from(GifPlayActivity.this);
        builder = new NotificationCompat.Builder(GifPlayActivity.this,CHAT_GIF_UPLOAD_CHANNEL_ID);
        builder.setContentTitle("Sending Gif")
                .setSmallIcon(R.drawable.notifcation_upload)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(true);

       UploadTask uploadTask = filePath.putFile(finalUri);

       uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
       {
           @Override
           public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
           {
               if (!task.isSuccessful())
               {
                   Log.e(TAG,task.getException().toString());
                   Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                   Toast.makeText(GifPlayActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();
               }
               return filePath.getDownloadUrl();
           }
       }).addOnCompleteListener(new OnCompleteListener<Uri>()
       {
           @Override
           public void onComplete(@NonNull Task<Uri> task)
           {
               if (task.isSuccessful()) {

                   builder.setContentText("Upload Completed");
                   notificationManager.notify(CHAT_GIF_UPLOAD_ID, builder.build());

                   final String downloadUrl = task.getResult().toString();

                   filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                   {
                       @Override
                       public void onSuccess(StorageMetadata storageMetadata) {
                           long sizeInMb = storageMetadata.getSizeBytes();

                           String size = getStringSizeFromFile(sizeInMb);

                           final Map<String, Object> messageTextBody = new HashMap<>();

                           messageTextBody.put("message", downloadUrl);
                           messageTextBody.put("seen", false);
                           messageTextBody.put("type", uriType);
                           messageTextBody.put("ref", finalUri.toString());
                           messageTextBody.put("time", time);
                           messageTextBody.put("from", online);
                           messageTextBody.put("size", size);
                           messageTextBody.put("key", message_push_id);
                           messageTextBody.put("exe","gif");
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

                                               notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                                               builder = null;

                                               Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                               Log.e(TAG,task.getException().getMessage());
                                               Crashlytics.log(Log.ERROR,TAG,task.getException().getMessage());
                                           }
                                           if (task.isSuccessful())
                                           {
                                               Toast.makeText(GifPlayActivity.this,"Gif sent Successfully",Toast.LENGTH_SHORT).show();

                                               isUploading = false;

                                               notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                                               builder = null;
                                           }
                                       }
                                   }).addOnFailureListener(new OnFailureListener()
                                   {
                                       @Override
                                       public void onFailure(@NonNull Exception e)
                                       {
                                           isUploading = false;

                                           notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                                           builder = null;

                                           Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
                                   isUploading = false;

                                   notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                                   builder = null;

                                   Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
                           isUploading = false;

                           notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                           builder = null;

                           Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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
               isUploading = false;

               builder.setContentTitle("Uploading failed").setOngoing(false);
               notificationManager.notify(CHAT_GIF_UPLOAD_ID, builder.build());

               notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
               builder = null;

               Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
               Log.e(TAG,e.toString());
               Crashlytics.log(Log.ERROR,TAG,e.getMessage());
           }
       });
    }

    private void storeGroupGifDetails(final String online, final ArrayList<String> Receivers, final Uri finalUri, final String groupKey, final int uriType)
    {
        Receivers.add(online);

        DocumentReference user_message_key = rootRef.collection("Group_Messages").document(groupKey).collection(online).document();

        isUploading = true;

        final String message_push_id = user_message_key.getId();

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = NotificationManagerCompat.from(GifPlayActivity.this);
        builder = new NotificationCompat.Builder(GifPlayActivity.this,CHAT_GIF_UPLOAD_CHANNEL_ID);
        builder.setContentText("Sending Gif")
                .setSmallIcon(R.drawable.notifcation_upload)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .setAutoCancel(true);

        final StorageReference filePath = MessageGifStorageRef.child(message_push_id + ".gif");

        UploadTask uploadTask = filePath.putFile(finalUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                if (!task.isSuccessful())
                {
                    Log.e(TAG,task.getException().toString());
                    Crashlytics.log(Log.ERROR,TAG,task.getException().toString());
                    Toast.makeText(GifPlayActivity.this, "Error while uploading image in storage " + task.getException().toString(), Toast.LENGTH_SHORT).show();

                    notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                    builder = null;

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
                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(CHAT_GIF_UPLOAD_ID, builder.build());

                    final String downloadUrl = task.getResult().toString();

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
                            messageTextBody.put("type", uriType);
                            messageTextBody.put("time", time);
                            messageTextBody.put("ref", finalUri.toString());
                            messageTextBody.put("size", size);
                            messageTextBody.put("from", online);
                            messageTextBody.put("key",message_push_id);
                            messageTextBody.put("exe","gif");
                            messageTextBody.put("date",FieldValue.serverTimestamp());

                            for (String membersRef : Receivers)
                            {
                                rootRef.collection("Group_Messages").document(groupKey).collection(membersRef).document(message_push_id).set(messageTextBody).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        isUploading = false;

                                        notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                                        builder = null;

                                        Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG,e.toString());
                                        Crashlytics.log(Log.ERROR,TAG,e.getMessage());
                                    }
                                });
                            }

                            notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                            builder = null;

                            isUploading = false;
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                            builder = null;

                            isUploading = false;

                            Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
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

                builder.setContentTitle("Uploading failed").setOngoing(false);
                notificationManager.notify(CHAT_GIF_UPLOAD_ID, builder.build());

                notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
                builder = null;

                isUploading = false;

                Toast.makeText(GifPlayActivity.this, "Error while storing documents", Toast.LENGTH_SHORT).show();
                Log.e(TAG,e.toString());
                Crashlytics.log(Log.ERROR,TAG,e.getMessage());  notificationManager.cancel(CHAT_GIF_UPLOAD_ID);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (isUploading)
        {
           isUploading = false
        }

    }
}

