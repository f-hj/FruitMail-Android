package fr.fruitice.mail;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
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
        if (remoteMessage.getData().size() <= 0) {
            return;
        }
        NotificationSender mNotificationSender = new NotificationSender(this);

        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        int notifId = (int) System.currentTimeMillis();
        if (remoteMessage.getData().get("type").equals("notif")) {

            NotificationCompat.Builder n = new NotificationCompat.Builder(this, mNotificationSender.CHANNEL_STD)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("text"))
                    .setSubText(remoteMessage.getData().get("sub"))
                    .setWhen(remoteMessage.getSentTime());

            if (remoteMessage.getData().get("url") != null) {
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                notificationIntent.setData(Uri.parse(remoteMessage.getData().get("url")));
                PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                n.setContentIntent(pi);
            }

            mNotificationSender.sendNotif(notifId, n.build());
            return;
        }


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

        NotificationCompat.Action a = new NotificationCompat.Action.Builder(
                R.drawable.ic_check_black_24dp, "Read", readPIntent).build();

        NotificationCompat.Action b = new NotificationCompat.Action.Builder(
                R.drawable.ic_eye_black_24dp, "Done", donePIntent).build();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification n  = new NotificationCompat.Builder(this, mNotificationSender.CHANNEL_MAIL)
                .setGroup(mNotificationSender.GROUP_MAIL)
                .setContentTitle(remoteMessage.getData().get("subject"))
                .setContentText(remoteMessage.getData().get("text"))
                .setSubText(remoteMessage.getData().get("name"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("text")))
                .setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(clickPIntent)
                .setCategory(Notification.CATEGORY_EMAIL)
                .setColor(getResources().getColor(R.color.colorPrimary, getTheme()))
                .setContentIntent(clickPIntent)
                .setWhen(remoteMessage.getSentTime())
                .setAutoCancel(true)
                .addAction(a).addAction(b).build();

        Log.d(TAG, "built");
        mNotificationSender.sendNotif(notifId, n);

    }

}
