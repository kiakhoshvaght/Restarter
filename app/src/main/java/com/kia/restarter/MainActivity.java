package com.kia.restarter;


import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.kia.restarter.api.ApiService;
import com.kia.restarter.model.Schedule;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    Button stopBtn, startBtn;
    EditText packageNameEt, numbersToStartEt;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"FUNCTION : onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setOnClickListeners();
        initializeUi();

    }

    @Override
    protected void onResume() {
        super.onResume();
        testApi();
    }

    private void testApi() {
        Log.i(TAG,"FUNCTION : testApi");
        ApiService.getInstance().getSchedule("123").subscribe(new Subscriber<Schedule>() {
            @Override
            public void onCompleted() {
                Log.i(TAG,"FUNCTION : testApi => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"FUNCTION : testApi => onError:" + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(Schedule schedule) {
                Log.i(TAG,"FUNCTION : testApi => onNext: " + schedule.getEvents().get(0).getPackageName() + " " + schedule.getEvents().get(0).getOpenCount());
            }
        });
    }

    private void initializeUi() {
        Log.i(TAG,"FUNCTION : initializeUi");
        stopBtn.setEnabled(false);
    }

    private void setOnClickListeners() {
        Log.i(TAG,"FUNCTION : setOnClickListeners");
        stopBtn.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on stopBtn click");
            stopService(new Intent(this,AppStarterService.class));
        });
        startBtn.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on startBtn click");
            startAppStarterService();
        });
    }

    private void findViews() {
        Log.i(TAG,"FUNCTION : findViews");
        stopBtn = findViewById(R.id.stop_btn);
        startBtn = findViewById(R.id.start_btn);
        numbersToStartEt = findViewById(R.id.times_to_start_et);
        packageNameEt = findViewById(R.id.package_name_et);
    }

    private void startAppStarterService() {
        Log.i(TAG,"FUNCTION : startAppStarterService");
        if(AppStarterService.thisService == null) {
            startService(new Intent(this, AppStarterService.class));
        }
        AppStarterService.timesToStart = Integer.parseInt(numbersToStartEt.getText().toString());
        AppStarterService.packageToBeStarted = packageNameEt.getText().toString();
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
