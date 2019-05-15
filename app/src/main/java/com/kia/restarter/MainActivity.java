package com.kia.restarter;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.kia.restarter.Constants.Action;
import com.kia.restarter.RxHelper.ContentObservable;
import com.kia.restarter.api.ApiService;
import com.kia.restarter.model.Schedule;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    Button stopBtn, startBtn, getSchedule;
    EditText packageNameEt, numbersToStartEt;
    Switch automatic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "FUNCTION : onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setOnClickListeners();
        initializeUi();
        handleBroadcasts();
        getSchedule();
        startAppStarterService();
        getAndSetDeviceId();
    }

    private void getAndSetDeviceId() {
        Log.i(TAG, "FUNCTION : getAndSetDeviceId");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        AppStarterService.deviceImei = telephonyManager.getDeviceId();
        Log.i(TAG, "FUNCTION : getAndSetDeviceId => DeviceId: " + telephonyManager.getDeviceId());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getSchedule() {
        Log.i(TAG,"FUNCTION : getSchedule");
        ApiService.getInstance().getSchedule("123").subscribe(new Subscriber<Schedule>() {
            @Override
            public void onCompleted() {
                Log.i(TAG,"FUNCTION : getSchedule => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"FUNCTION : getSchedule => onError:" + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(Schedule schedule) {
                Log.i(TAG,"FUNCTION : getSchedule => onNext: " + schedule.getEvents().get(0).getPackageName() + " " + schedule.getEvents().get(0).getOpenCount());
            }
        });
    }

    private void initializeUi() {
        Log.i(TAG,"FUNCTION : initializeUi");
        stopBtn.setEnabled(false);
    }

    private void handleBroadcasts() {
        Log.i(TAG, "FUNCTION : handleBroadcasts");
        ContentObservable.fromBroadcast(this,getIntentFilter("set_count", "stopped", "package_name")).subscribe(new Subscriber<Intent>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "FUNCTION : handleBroadcasts => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "FUNCTION : handleBroadcasts => onError");
                e.printStackTrace();
            }

            @Override
            public void onNext(Intent intent) {
                Log.i(TAG, "FUNCTION : handleBroadcasts => onNext: " + intent.getAction());
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case "set_count":
                            Log.i(TAG, "FUNCTION : handleBroadcasts => onNext => count:  " + intent.getIntExtra("count",0));
                            numbersToStartEt.setText(intent.getIntExtra("count",0) + "");
                            break;
                        case "stopped":
                            goToStopState();
                            break;
                        case "package_name":
                            Log.i(TAG, "FUNCTION : handleBroadcasts => onNext => count:  " + intent.getStringExtra("package"));
                            packageNameEt.setText(intent.getStringExtra("package"));
                    }
                }
            }
        });
    }

    private void setOnClickListeners() {
        Log.i(TAG,"FUNCTION : setOnClickListeners");
        stopBtn.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on stopBtn click");
            goToStopState();

        });
        startBtn.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on startBtn click");
            goToStartState();

        });
        automatic.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                Log.i(TAG,"FUNCTION : setOnClickListeners => on Switch turn on");
                goToAutomaticState();
            } else {
                Log.i(TAG,"FUNCTION : setOnClickListeners => on Switch turn off");
                goToManualState();
            }
        });
        getSchedule.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on getSchedule click");
            getSchedule();
        });
    }

    private void goToStopState() {
        Log.i(TAG,"FUNCTION : goToStopState");
        AppStarterService.automaticMode = false;
        AppStarterService.isStopped = true;
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        packageNameEt.setEnabled(true);
        numbersToStartEt.setEnabled(true);
        automatic.setEnabled(true);
        sendBroadcast(new Intent(Action.STOP));
    }

    private void goToStartState() {
        Log.i(TAG,"FUNCTION : goToStartState");
        AppStarterService.isStopped = false;
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        packageNameEt.setEnabled(false);
        numbersToStartEt.setEnabled(false);
        automatic.setEnabled(false);
        AppStarterService.timesToStart = Integer.parseInt(numbersToStartEt.getText().toString());
        AppStarterService.packageToBeStarted = packageNameEt.getText().toString();
        sendBroadcast(new Intent(Action.START));
    }

    private void goToManualState() {
        Log.i(TAG,"FUNCTION : goToManualState");
        AppStarterService.automaticMode = false;
        AppStarterService.isStopped = true;
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        packageNameEt.setEnabled(true);
        numbersToStartEt.setEnabled(true);
        automatic.setEnabled(true);
        sendBroadcast(new Intent(Action.START));
    }

    private void goToAutomaticState() {
        Log.i(TAG,"FUNCTION : goToAutomaticState");
        AppStarterService.automaticMode = true;
        AppStarterService.isStopped = false;
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        packageNameEt.setEnabled(false);
        numbersToStartEt.setEnabled(false);
        automatic.setEnabled(false);
        sendBroadcast(new Intent(Action.START));
    }

    private void findViews() {
        Log.i(TAG,"FUNCTION : findViews");
        stopBtn = findViewById(R.id.stop_btn);
        startBtn = findViewById(R.id.start_btn);
        numbersToStartEt = findViewById(R.id.times_to_start_et);
        packageNameEt = findViewById(R.id.package_name_et);
        automatic = findViewById(R.id.manual_automatic_sb);
        getSchedule = findViewById(R.id.get_schedule_btn);
    }

    private void startAppStarterService() {
        Log.i(TAG,"FUNCTION : startAppStarterService");
        if(AppStarterService.thisService == null) {
            startService(new Intent(this, AppStarterService.class));
        }
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

    public static IntentFilter getIntentFilter(@NonNull final String... actions) {
        return new IntentFilter() {{
            for (String action : actions) {
                addAction(action);
            }
        }};
    }
}
