package fr.fruitice.mail;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by florian on 3/14/18.
 */

public class NotificationSender {
    private final static String TAG = "NotificationSender";
    private Context mContext;

    final String CHANNEL_MAIL = "fr.fruitice.mail.channelnotifmail";
    public final String CHANNEL_STD = "fr.fruitice.mail.channelnotifstd";

    final String GROUP_MAIL = "fr.fruitice.mail.groupnotifmail";

    NotificationSender(Context context) {
        this.mContext = context;
    }

    void sendNotif(int notifId, Notification notif) {
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.d(TAG, "cannot get service");
            return;
        }
        Log.d(TAG, "got service");

        notificationManager.notify(notifId, notif);
        Log.d(TAG, "notified");
    }

    void createChannels() {

    }
}
