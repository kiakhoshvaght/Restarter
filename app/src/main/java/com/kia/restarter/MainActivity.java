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
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    Button stopBtn, startBtn, exit;
    EditText packageNameEt, numbersToStartEt;
    Switch automatic;
    private Subscription broadcastSubscription;

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
        Log.i(TAG,"FUNCTION : exit");
        ApiService.getInstance().getSchedule("123").subscribe(new Subscriber<Schedule>() {
            @Override
            public void onCompleted() {
                Log.i(TAG,"FUNCTION : exit => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG,"FUNCTION : exit => onError:" + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(Schedule schedule) {
                Log.i(TAG,"FUNCTION : exit => onNext: " + schedule.getEvents().get(0).getPackageName() + " " + schedule.getEvents().get(0).getOpenCount());
            }
        });
    }

    private void initializeUi() {
        Log.i(TAG,"FUNCTION : initializeUi");
        stopBtn.setEnabled(false);
    }

    private void handleBroadcasts() {
        Log.i(TAG, "FUNCTION : handleBroadcasts");
        broadcastSubscription = ContentObservable.fromBroadcast(this,getIntentFilter("set_count", "stopped", "package_name")).subscribe(new Subscriber<Intent>() {
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
        exit.setOnClickListener(view -> {
            Log.i(TAG,"FUNCTION : setOnClickListeners => on exit click");
            AppStarterService.stopService(this);
            finish();
        });
    }

    private void goToStopState() {
        Log.i(TAG,"FUNCTION : goToStopState");
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
        if(!AppStarterService.automaticMode) {
            Log.i(TAG,"FUNCTION : goToStartState => is automatic mode and going to put package name and count to service");
            AppStarterService.timesToStart = Integer.parseInt(numbersToStartEt.getText().toString());
            AppStarterService.packageToBeStarted = packageNameEt.getText().toString();
        }
        sendBroadcast(new Intent(Action.START));
    }

    private void goToManualState() {
        Log.i(TAG,"FUNCTION : goToManualState");
        AppStarterService.automaticMode = false;
    }

    private void goToAutomaticState() {
        Log.i(TAG,"FUNCTION : goToAutomaticState");
        AppStarterService.automaticMode = true;
    }

    private void findViews() {
        Log.i(TAG,"FUNCTION : findViews");
        stopBtn = findViewById(R.id.stop_btn);
        startBtn = findViewById(R.id.start_btn);
        numbersToStartEt = findViewById(R.id.times_to_start_et);
        packageNameEt = findViewById(R.id.package_name_et);
        automatic = findViewById(R.id.manual_automatic_sb);
        exit = findViewById(R.id.exit_btn);
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
        unsubscribeIfNotNull(broadcastSubscription);
        super.onDestroy();
    }

    public static IntentFilter getIntentFilter(@NonNull final String... actions) {
        return new IntentFilter() {{
            for (String action : actions) {
                addAction(action);
            }
        }};
    }

    private void unsubscribeIfNotNull(Subscription subscription) {
        Log.i(TAG, "FUNCTION : unsubscribeIfNotNull");
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
