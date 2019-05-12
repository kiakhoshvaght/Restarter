package com.kia.autoappstarter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class AppStarterService extends Service {

    Service thisService;
    CountDownTimer countDownTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        runServiceAsForeground();
        setupCountDownTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupCountDownTimer() {
        countDownTimer = new CountDownTimer(999999999,10000) {
            @Override
            public void onTick(long l) {
                startTheApp();
            }


            @Override
            public void onFinish() {
                setupCountDownTimer();
            }
        }.start();
    }

    private void startTheApp() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.ironmen");
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private void runServiceAsForeground() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("App Starter Service")
                .setContentText("Starting app ...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }
}
