package fr.fruitice.mail;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
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
            int notifId = (int) System.currentTimeMillis();

            Intent clickIntent = new Intent(this, MailActivity.class);
            Intent readIntent = new Intent(this, HandleService.class);
            Intent doneIntent = new Intent(this, HandleService.class);

            clickIntent.putExtra("id", remoteMessage.getData().get("id"));
            readIntent.putExtra("id", remoteMessage.getData().get("id"));
            readIntent.putExtra("notifId", notifId);
            readIntent.putExtra("action", "read");
            doneIntent.putExtra("id", remoteMessage.getData().get("id"));
            doneIntent.putExtra("notifId", notifId);
            doneIntent.putExtra("action", "done");

            PendingIntent clickPIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), clickIntent, 0);
            PendingIntent readPIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), readIntent, 0);
            PendingIntent donePIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), doneIntent, 0);

            android.support.v4.app.NotificationCompat.Action a = new NotificationCompat.Action.Builder(
                    R.drawable.ic_check_black_24dp, "Read", readPIntent).build();

            android.support.v4.app.NotificationCompat.Action b = new NotificationCompat.Action.Builder(
                    R.drawable.ic_eye_black_24dp, "Done", donePIntent).build();

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            Notification n  = new NotificationCompat.Builder(this)
                    .setContentTitle(remoteMessage.getData().get("subject"))
                    .setContentText(remoteMessage.getData().get("text"))
                    .setSubText(remoteMessage.getData().get("name"))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("text")))
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentIntent(clickPIntent)
                    .setCategory(Notification.CATEGORY_EMAIL)
                    .setColor(getResources().getColor(R.color.colorPrimary, getTheme()))
                    .setContentIntent(clickPIntent)
                    .setSound(uri)
                    .setWhen(remoteMessage.getSentTime())
                    .setVibrate(new long[]{0, 200, 200, 200})
                    .setLights(Color.rgb(0, 0, 0), 1000, 1000)
                    .setAutoCancel(true)
                    .addAction(a).addAction(b).build();

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(notifId, n);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
