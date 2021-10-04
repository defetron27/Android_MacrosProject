package com.deffe.macros.grindersouls;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private Uri uri;
    private String Type;
    private String GroupKey;
    private ArrayList<String> ReceivedIds = new ArrayList<>();

    private ImageView GifImageView;
    private ImageButton SendGif;
    private String online_user_id;

    private DatabaseReference RootRef;

    private StorageReference MessageGifStorageRef;

    private NotificationManager notificationManager;

    private NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_play);

        GifImageView = findViewById(R.id.gif_image_view);
        SendGif = findViewById(R.id.send_gif);

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
            SendGif.setVisibility(View.GONE);

            uri = Uri.parse(getIntent().getExtras().getString("gifUri"));

            GlideApp.with(GifPlayActivity.this).load(uri).centerCrop().into(GifImageView);
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();

        online_user_id = firebaseAuth.getCurrentUser().getUid();

        MessageGifStorageRef = FirebaseStorage.getInstance().getReference().child("Messages").child("Gifs");

        if (uri != null)
        {
            GlideApp.with(GifPlayActivity.this).load(uri).into(GifImageView);
        }

        SendGif.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
        });
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

    private void updateSingleDateRef(String online_key,String receiver_key)
    {
        storeDateRef(online_key,receiver_key);

        final String message_sender_ref = "Messages/" + online_key + "/" + receiver_key;
        final String message_receiver_ref = "Messages/" + receiver_key + "/" + online_key;

        DatabaseReference date_key = RootRef.child("Messages").child(online_key).child(receiver_key).push();

        final String date_push_id = date_key.getKey();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("message","");
        messageDate.put("seen",true);
        messageDate.put("type", MessageTypesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("time", getTodayDate());
        messageDate.put("ref", "");
        messageDate.put("size","");
        messageDate.put("from",online_key);
        messageDate.put("key",date_push_id);
        messageDate.put("duration","");

        Map<String,Object> messageBodyDetails = new HashMap<>();

        messageBodyDetails.put(message_sender_ref + "/" + date_push_id, messageDate);

        messageBodyDetails.put(message_receiver_ref + "/" + date_push_id, messageDate);

        RootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
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

    private void updateGroupDateRef(String online_key,String group_key,ArrayList<String> Ids)
    {
        storeDateRef(online_key,group_key);

        final ArrayList<String> GroupMessages = new ArrayList<>();

        Ids.add(online_key);

        for (String members : Ids)
        {
            GroupMessages.add("Group_Messages/" + group_key + "/" + members);
        }

        DatabaseReference date_key = RootRef.child("Group_Messages").child(group_key).child(online_key).push();

        String date_push_id = date_key.getKey();

        Map<String,Object> messageDate = new HashMap<>();

        messageDate.put("message","");
        messageDate.put("seen",true);
        messageDate.put("type", MessageTypesModel.MESSAGE_TYPE_DAY_DATE);
        messageDate.put("time", getTodayDate());
        messageDate.put("ref", "");
        messageDate.put("size","");
        messageDate.put("from","");
        messageDate.put("key",date_push_id);
        messageDate.put("duration","");

        Map<String,Object> messageBodyDetails = new HashMap<>();

        for (String membersRef : GroupMessages)
        {
            messageBodyDetails.put(membersRef + "/" + date_push_id, messageDate);
        }

        RootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener()
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

    private void storeSingleGifDetails(final String online, ArrayList<String> Receivers, final Uri finalUri, final int uriType)
    {
        final String message_sender_ref = "Messages/" + online + "/" + Receivers.get(0);
        final String message_receiver_ref = "Messages/" + Receivers.get(0) + "/" + online;

        DatabaseReference user_message_key = RootRef.child("Messages").child(online).child(Receivers.get(0)).push();

        final String message_push_id = user_message_key.getKey();

        final StorageReference filePath = MessageGifStorageRef.child(message_push_id + ".gif");

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(GifPlayActivity.this);
        builder.setContentTitle("Grinders send gif");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        filePath.putFile(finalUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(2, builder.build());

                    final String downloadUrl = task.getResult().toString();

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            long sizeInMb = storageMetadata.getSizeBytes();

                            String size = getStringSizeFromFile(sizeInMb);

                            Map<String, Object> messageTextBody = new HashMap<>();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("seen", false);
                            messageTextBody.put("type", uriType);
                            messageTextBody.put("ref", finalUri.toString());
                            messageTextBody.put("time", time);
                            messageTextBody.put("from", online);
                            messageTextBody.put("size", size);
                            messageTextBody.put("key", message_push_id);
                            messageTextBody.put("duration","");
                            messageTextBody.put("exe","gif");

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    notificationManager.cancel(1);
                                    builder = null;

                                    if (databaseError != null)
                                    {
                                        Log.d("Chat_Log", databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GifPlayActivity.this, "Upload failed, Try again", Toast.LENGTH_SHORT).show();
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
                notificationManager.notify(2, builder.build());

                Toast.makeText(GifPlayActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(2, builder.build());
            }
        });
    }

    private void storeGroupGifDetails(final String online, ArrayList<String> Receivers, final Uri finalUri,String groupKey, final int uriType)
    {
        final ArrayList<String> GroupMessages = new ArrayList<>();

        Receivers.add(online);

        for (String members : Receivers) {
            GroupMessages.add("Group_Messages/" + groupKey + "/" + members);
        }

        DatabaseReference user_message_key = null;

        if (GroupKey != null && online_user_id != null)
        {
            user_message_key = RootRef.child("Group_Messages").child(GroupKey).child(online_user_id).push();
        }
        else
        {
            Toast.makeText(this, "group" + groupKey, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "online" + online, Toast.LENGTH_SHORT).show();
        }

        final String message_push_id = user_message_key.getKey();

        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        final String time = df.format(Calendar.getInstance().getTime());

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(GifPlayActivity.this);
        builder.setContentTitle("Grinders send gif");
        builder.setSmallIcon(R.drawable.notifcation_upload).setOngoing(true).setAutoCancel(false);

        final StorageReference filePath = MessageGifStorageRef.child(message_push_id + ".gif");

        filePath.putFile(finalUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    builder.setContentText("Upload Completed").setProgress(0, 0, false);
                    notificationManager.notify(2, builder.build());

                    final String downloadUrl = task.getResult().toString();

                    filePath.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>()
                    {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
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
                            messageTextBody.put("duration","");
                            messageTextBody.put("exe","gif");

                            Map<String, Object> messageBodyDetails = new HashMap<>();

                            for (String membersRef : GroupMessages) {
                                messageBodyDetails.put(membersRef + "/" + message_push_id, messageTextBody);
                            }

                            RootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                {

                                    notificationManager.cancel(1);
                                    builder = null;

                                    if (databaseError != null)
                                    {
                                        Log.d("Chat_Log", databaseError.getMessage());
                                    }
                                }
                            });
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
                notificationManager.notify(2, builder.build());

                Toast.makeText(GifPlayActivity.this, "Error...  " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                builder.setProgress(100, (int) progress, false).setContentInfo((int) progress + "%")
                        .setContentText(getStringSizeFromFile(taskSnapshot.getBytesTransferred()) + " / " + getStringSizeFromFile(taskSnapshot.getTotalByteCount()));
                notificationManager.notify(2, builder.build());
            }
        });

    }
}

