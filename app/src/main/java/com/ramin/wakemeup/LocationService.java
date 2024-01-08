package com.ramin.wakemeup;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;


public class LocationService extends Service {
    private static final String TAG = "LocationService";

    private static final int NOTIFICATION_ID = 1;
    private int counter = 0;
    private Handler handler;
    private Runnable incrementRunnable;

    private boolean isRunning = true; // Flag to control the execution of run()

    private int proximity = 40;

    private String locationString;

    private String currentLocationString = "Service idle!";

    private Intent broadcastIntent;

    Location targetLocation;
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
                if(!isRunning){
                    return;
                }
                Log.d(TAG, "Counter: " + counter);

                updateNotification();
                // Schedule the next execution after 5 seconds
                handler.postDelayed(this, 10000);

                CheckLocation();

                broadcastIntent = new Intent("LOCATION_COUNTER_UPDATE");
                broadcastIntent.putExtra("counter", counter);
                broadcastIntent.putExtra("currentLocation",currentLocationString);
                sendBroadcast(broadcastIntent);
            }
        };
        createNotificationChannel();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            locationString = intent.getStringExtra("location");
            proximity = intent.getIntExtra("proximity",40);
        }
        double[] targetLoc = convertStringToCoordinates(locationString);
        targetLocation = new Location("destination");
        double lat = targetLoc[0];
        double lon = targetLoc[1];
        targetLocation.setLatitude(lat);
        targetLocation.setLongitude(lon);
        handler.postDelayed(incrementRunnable, 500);
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
                NotificationManager.IMPORTANCE_LOW

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
                .setContentText(currentLocationString)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public void CheckLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation
                (Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener(location -> {
                    if(location == null){
                        currentLocationString = "Error getting location!";
                    }
                    else{
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        currentLocationString = latitude + " , " + longitude;
                        double distance = location.distanceTo(targetLocation);
                        if(distance <= proximity){
                            stopForeground(true);
                            PlayRingtone();
                            isRunning = false;
                        }
                    }
                });
    }

    private double[] convertStringToCoordinates(String coordinatesString) {
        double[] coordinates = new double[2];

        try {
            // Split the coordinates string into latitude and longitude
            String[] parts = coordinatesString.split(",");
            if (parts.length == 2) {
                coordinates[0] = Double.parseDouble(parts[0]); // Latitude
                coordinates[1] = Double.parseDouble(parts[1]); // Longitude
                return coordinates;
            }
        } catch (NumberFormatException e) {
            // Handle the case where parsing the coordinates as doubles fails
            e.printStackTrace();
        }

        return null; // Return null for invalid coordinates string
    }

}
