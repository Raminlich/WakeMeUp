package com.ramin.wakemeup;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView noticeText;
    Button startServiceButton;
    Button killAppButton;
    EditText coordinateInput;
    Intent serviceIntent;
    EditText proximityInputText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noticeText = findViewById(R.id.noticeTextId);
        startServiceButton = findViewById(R.id.startServiceButtonId);
        killAppButton = findViewById(R.id.exitAppId);
        coordinateInput = findViewById(R.id.coordinateEditTextId);
        proximityInputText = findViewById(R.id.proximityTextId);
        SetStartServiceButton();
        SetKillAppButton();
        BroadcastReceiver locationCounterReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("LOCATION_COUNTER_UPDATE")) {
                    String currentLocationString = intent.getStringExtra("currentLocation");
                    updateNoticeText(currentLocationString);
                }
            }
        };
        IntentFilter filter = new IntentFilter("LOCATION_COUNTER_UPDATE");
        registerReceiver(locationCounterReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @SuppressLint("SetTextI18n")
    private void SetStartServiceButton(){
        serviceIntent= new Intent(this,LocationService.class);
        if(ServiceUtils.isServiceRunning(this,LocationService.class)){
            startServiceButton.setText("Stop!");
        }
        startServiceButton.setOnClickListener(v -> {
            String cooText =String.valueOf(coordinateInput.getText());
            String proxiText = String.valueOf(proximityInputText.getText());
            if( cooText.length() <= 1 || proxiText.length() <= 1){
                updateNoticeText("Fields are invalid!");
                return;
            }
            if(ServiceUtils.isServiceRunning(this,LocationService.class)){
                stopService(serviceIntent);
                startServiceButton.setText("Start");
                updateNoticeText("Service stopped!");
                return;
            }
            updateNoticeText("Starting service...");
            String proximityString = String.valueOf(proximityInputText.getText());
            serviceIntent.putExtra("proximity",Integer.parseInt(proximityString));
            serviceIntent.putExtra("location",coordinateInput.getText().toString());
            startForegroundService(serviceIntent);
            startServiceButton.setText("Stop!");
        });
    }

    private void SetKillAppButton(){
        killAppButton.setOnClickListener(v ->{
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }

    private void updateNoticeText(String text) {
        // Update your UI with the new counter value
        noticeText.setText(text);
    }


}


