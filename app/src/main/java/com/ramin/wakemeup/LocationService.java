package com.ramin.wakemeup;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private static final String TAG = "LocationService";

    private static final int NOTIFICATION_ID = 1;
    private int counter = 0;
    private Handler handler;
    private Runnable incrementRunnable;

    private boolean isRunning = true; // Flag to control the execution of run()


    public void PlayRingtone(){
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        if(ringtone != null){
            ringtone.play();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        incrementRunnable = new Runnable() {
            @Override
            public void run() {
                // Increment the counter
                if(!isRunning) return;
                counter++;
                Log.d(TAG, "Counter: " + counter);

                updateNotification();
                // Schedule the next execution after 5 seconds
                handler.postDelayed(this, 2000);

                if(counter > 10){
                    stopForeground(true);
                    PlayRingtone();
                    isRunning = false;
                }

                Intent broadcastIntent = new Intent("LOCATION_COUNTER_UPDATE");
                broadcastIntent.putExtra("counter", counter);
                sendBroadcast(broadcastIntent);
            }
        };
        createNotificationChannel();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the incrementing process when the service is started
        handler.postDelayed(incrementRunnable, 5000);
        showNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Remove the callback when the service is destroyed
        handler.removeCallbacks(incrementRunnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service does not provide binding, so return null
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "location_service_channel",
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void showNotification() {
        Notification.Builder builder;

        builder = new Notification.Builder(this, "location_service_channel");

        builder.setContentTitle("Location Service")
                .setContentText("Counter: " + counter)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void updateNotification() {
        Notification.Builder builder;

        builder = new Notification.Builder(this, "location_service_channel");

        builder.setContentTitle("Location Service")
                .setContentText("Counter: " + counter)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
