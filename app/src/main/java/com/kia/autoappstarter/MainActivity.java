package com.kia.autoappstarter;


import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    Button stopBtn;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"FUNCTION : onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setOnClickListeners();
        if(AppStarterService.thisService == null) {
            startAppStarterService();
        }
    }

    private void setOnClickListeners() {
        Log.i(TAG,"FUNCTION : setOnClickListeners");
        stopBtn.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on stopBtn click");
            stopService(new Intent(this,AppStarterService.class));
        });
    }

    private void findViews() {
        Log.i(TAG,"FUNCTION : findViews");
        stopBtn = findViewById(R.id.stop_btn);
    }

    private void startAppStarterService() {
        Log.i(TAG,"FUNCTION : startAppStarterService");
        startService(new Intent(this, AppStarterService.class));
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"FUNCTION : onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"FUNCTION : onDestroy");
        super.onDestroy();
    }
}
