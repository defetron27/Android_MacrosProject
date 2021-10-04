package com.deffe.macros.grindersouls;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String clickAction = remoteMessage.getNotification().getClickAction();
        String fromSenderId = remoteMessage.getData().get("from_sender_id");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationBody);

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("visit_user_id", fromSenderId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        int notificationId = (int) System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            notificationManager.notify(notificationId,builder.build());
        }
    }
}
