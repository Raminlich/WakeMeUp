package com.ramin.wakemeup;
import android.content.BroadcastReceiver;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView noticeText;
    Button startServiceButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noticeText = findViewById(R.id.noticeTextId);
        startServiceButton = findViewById(R.id.startServiceButtonId);
        SetStartServiceButton();
        BroadcastReceiver locationCounterReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("LOCATION_COUNTER_UPDATE")) {
                    int counterValue = intent.getIntExtra("counter", 0);
                    updateCounter(counterValue);
                }
            }
        };
        IntentFilter filter = new IntentFilter("LOCATION_COUNTER_UPDATE");
        registerReceiver(locationCounterReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void SetStartServiceButton(){
        startServiceButton.setOnClickListener(v -> {
            startForegroundService(new Intent(this, LocationService.class));
        });
    }

    private void updateCounter(int counterValue) {
        // Update your UI with the new counter value
        noticeText.setText("Counter: " + counterValue);
    }
}