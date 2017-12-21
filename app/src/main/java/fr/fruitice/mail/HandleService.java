package fr.fruitice.mail;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by florian on 05/03/2017.
 */

public class HandleService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("HandleService", "started");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(intent.getIntExtra("notifId", 0));
        new Query(this) {
            @Override
            public void result(String data) {
                Log.d("HandleService", data);
            }
        }.post("/msg/" + intent.getStringExtra("id") + "/" + "setas" + intent.getStringExtra("action"), "");
        return super.onStartCommand(intent, flags, startId);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("HandleService", "binded");
        return null;
    }
}
