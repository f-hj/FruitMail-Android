package fr.fruitice.mail;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by florian on 27/12/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String TAG = "FBMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Intent clickIntent = new Intent(this, MailActivity.class);
            clickIntent.putExtra("id", remoteMessage.getData().get("id"));
            PendingIntent clickPIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), clickIntent, 0);

            Intent intent = new Intent(this, MailActivity.class);
            PendingIntent readPIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
            PendingIntent donePIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            android.support.v4.app.NotificationCompat.Action a = new NotificationCompat.Action.Builder(
                    R.drawable.ic_send_white_24dp, "Read", readPIntent).build();

            android.support.v4.app.NotificationCompat.Action b = new NotificationCompat.Action.Builder(
                    R.drawable.ic_send_white_24dp, "Done", donePIntent).build();

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            Notification n  = new NotificationCompat.Builder(this)
                    .setContentTitle(remoteMessage.getData().get("subject"))
                    .setContentText(remoteMessage.getData().get("text"))
                    .setSubText(remoteMessage.getData().get("name"))
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentIntent(clickPIntent)
                    .setCategory(Notification.CATEGORY_EMAIL)
                    .setColor(getResources().getColor(R.color.colorPrimary, getTheme()))
                    .setContentIntent(clickPIntent)
                    .setSound(uri)
                    .setWhen(remoteMessage.getSentTime())
                    .setVibrate(new long[]{200})
                    .setLights(getResources().getColor(R.color.colorPrimary, getTheme()), 1000, 1000)
                    .addAction(a).addAction(b).build();

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, n);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
