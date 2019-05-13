package com.kia.autoappstarter;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class AppStarterService extends Service {

    private static final String TAG = AppStarterService.class.getName();
    static AppStarterService thisService;
    CountDownTimer countDownTimer;

    static void stopService(Context context){
        context.stopService(new Intent(context,AppStarterService.class));
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"FUNCTION : onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"FUNCTION : onStartCommand");
        thisService = this;
        runServiceAsForeground();
        setupCountDownTimer();
        return START_NOT_STICKY;
    }

    private void setupCountDownTimer() {
        Log.i(TAG,"FUNCTION : setupCountDownTimer");
        countDownTimer = new CountDownTimer(999999999,10000) {
            @Override
            public void onTick(long l) {
                Log.i(TAG,"FUNCTION : onTick");
                startTheApp();
//                closeAppAfterTheTime();
            }


            @Override
            public void onFinish() {
                Log.i(TAG,"FUNCTION : onFinish");
                setupCountDownTimer();
            }
        }.start();
    }

    private void startTheApp() {
        Log.i(TAG,"FUNCTION : startTheApp");
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("ir.sakoo");
        launchIntent.putExtra("to-be-closed","true");
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private void runServiceAsForeground() {
        Log.i(TAG,"FUNCTION : runServiceAsForeground");
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("App Starter Service")
                .setContentText("Starting app ...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"FUNCTION : onDestroy");
        countDownTimer.cancel();
        super.onDestroy();
    }
}
