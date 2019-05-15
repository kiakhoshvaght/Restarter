package com.kia.restarter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kia.restarter.RxHelper.ContentObservable;
import com.kia.restarter.api.ApiService;
import com.kia.restarter.model.Event;
import com.kia.restarter.model.EventLog;
import com.kia.restarter.model.Schedule;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static com.kia.restarter.Constants.State.STATE_AUTOMATIC_ASKING;
import static com.kia.restarter.Constants.State.STATE_AUTOMATIC_DOING;
import static com.kia.restarter.Constants.State.STATE_MANUAL;
import static com.kia.restarter.Constants.State.STATE_STOP;


public class AppStarterService extends Service {

    private static final String TAG = AppStarterService.class.getName();
    static AppStarterService thisService;
    CountDownTimer countDownTimer;
    static int timesToStart = 0;
    static String packageToBeStarted;
    static boolean automaticMode = false;
    static boolean isStopped = true;
    static boolean hasLeftAny = false;
    static Schedule schedule;
    static String deviceImei;
    private Subscription gettingScheduleSubscription;
    private static String state;
    private Subscription handleBroadcastsSubscription;
    private Subscription timerForStatingAppSubscription;

    static void stopService(Context context) {
        context.stopService(new Intent(context, AppStarterService.class));
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "FUNCTION : onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "FUNCTION : onStartCommand");
        thisService = this;
        runServiceAsForeground();
        handleBroadcasts();

        setState();
        doCurrentStateJobs();

        return START_NOT_STICKY;
    }

    private void setState() {
        Log.i(TAG, "FUNCTION : setState");
        if(isStopped) {
            state = STATE_STOP;
        } else if (automaticMode) {
            Log.i(TAG, "FUNCTION : setState => automaticMode");
            if(hasLeftAny){
                Log.i(TAG, "FUNCTION : setState => automaticMode => Has left some");
                state = STATE_AUTOMATIC_DOING;
            } else {
                Log.i(TAG, "FUNCTION : setState => automaticMode => Has NOT left any");
                state = STATE_AUTOMATIC_ASKING;
            }
        } else {
            Log.i(TAG, "FUNCTION : setState => NOT automaticMode");
            if(hasLeftAny){
                Log.i(TAG, "FUNCTION : setState => manualMode => Has left some");
                state = STATE_MANUAL;
            } else if(timesToStart>0){
                Log.i(TAG, "FUNCTION : setState => manualMode => Has NOT left any, but there's some in timesToStart, going to set schedule manually");
                setupScheduleManually();
            } else {
                Log.i(TAG, "FUNCTION : setState => manualMode => Has NOT left any, going to stop");
                state = STATE_STOP;
            }
        }
    }

    private void doCurrentStateJobs() {
        Log.i(TAG, "FUNCTION : doCurrentStateJobs");
        Log.i(TAG, "FUNCTION : doCurrentStateJobs => State: " + state);
        switch (state){
            case STATE_AUTOMATIC_ASKING:
                unsubscribeIfNotNull(timerForStatingAppSubscription);
                setupTimerForGettingSchedule();
                break;
            case STATE_AUTOMATIC_DOING:
                unsubscribeIfNotNull(gettingScheduleSubscription);
                setupTimerForStartingApp();
                break;
            case STATE_MANUAL:
                unsubscribeIfNotNull(gettingScheduleSubscription);
                setupTimerForStartingApp();
                break;
            case STATE_STOP:
                unsubscribeIfNotNull(gettingScheduleSubscription);
                unsubscribeIfNotNull(timerForStatingAppSubscription);
                break;
        }
    }

    private void setupScheduleManually() {
        Log.i(TAG, "FUNCTION : setupScheduleManually");
        schedule = null;
        schedule = new Schedule();
        schedule.setEvents(new ArrayList<Event>());
        schedule.getEvents().add(new Event(packageToBeStarted,timesToStart));
        timesToStart = 0;
        packageToBeStarted = "";
        hasLeftAny = true;
        setState();
    }

    private void getSchedule() {
        Log.i(TAG, "FUNCTION : getSchedule");
        ApiService.getInstance().getSchedule(deviceImei).subscribe(new Subscriber<Schedule>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "FUNCTION : getSchedule => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "FUNCTION : getSchedule => onError:" + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(Schedule schedule) {
                Log.i(TAG, "FUNCTION : getSchedule => onNext: " + schedule.getEvents().get(0).getPackageName() + " " + schedule.getEvents().get(0).getOpenCount());
                AppStarterService.schedule = schedule;
                if(schedule.getEvents().get(0).getOpenCount()>0){
                    hasLeftAny = true;
                    setState();
                    doCurrentStateJobs();
                    sendBroadcast(new Intent("package_name").putExtra("package", schedule.getEvents().get(0).getPackageName()));
                    sendBroadcast(new Intent("set_count").putExtra("count", schedule.getEvents().get(0).getOpenCount()));
                }
            }
        });
    }

    private void setupTimerForGettingSchedule() {
        Log.i(TAG, "FUNCTION : setupTimerForGettingSchedule");
        Log.i(TAG, "FUNCTION : setupTimerForGettingSchedule => is automatic mode");
        gettingScheduleSubscription = Observable.interval(10, TimeUnit.SECONDS).subscribe(aLong -> {
            Log.i(TAG, "FUNCTION : setupTimerForGettingSchedule => is automatic mode => on 10 time units passed!");
            getSchedule();
        });
    }

    private void handleBroadcasts() {
        Log.i(TAG, "FUNCTION : handleBroadcasts");
        handleBroadcastsSubscription = ContentObservable.fromBroadcast(this, getIntentFilter("stop", "start", "automatic", "manual")).subscribe(new Subscriber<Intent>() {
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
                Log.i(TAG, "FUNCTION : handleBroadcasts => onNext");
                Log.i(TAG, "FUNCTION : handleBroadcasts => onNext => Intent: " + intent.getAction());
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case "automatic":
                            automaticMode = true;
                            isStopped = false;
                            setupTimerForGettingSchedule();
                            setState();
                            doCurrentStateJobs();
                            break;
                        case "stop":
                            isStopped = true;
                            automaticMode = false;
                            timesToStart = 0;
                            hasLeftAny = false;
                            sendBroadcast(new Intent("set_count").putExtra("count", 0));
                            stopAllSubscriptions();
                            setState();
                            doCurrentStateJobs();
                            break;
                        case "manual":
                            automaticMode = false;
                            unsubscribeIfNotNull(gettingScheduleSubscription);
                            setState();
                            doCurrentStateJobs();
                            break;
                        case "start":
                            isStopped = false;
                            setState();
                            doCurrentStateJobs();
                            break;
                    }
                }
            }
        });
    }

    private void stopAllSubscriptions() {
        Log.i(TAG, "FUNCTION : stopAllSubscriptions");
        unsubscribeIfNotNull(gettingScheduleSubscription);
        unsubscribeIfNotNull(timerForStatingAppSubscription);
    }

    private void unsubscribeIfNotNull(Subscription subscription) {
        Log.i(TAG, "FUNCTION : unsubscribeIfNotNull");
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void setupTimerForStartingApp() {
        Log.i(TAG, "FUNCTION : setupTimerForStartingApp");
        timerForStatingAppSubscription = Observable.interval(10, TimeUnit.SECONDS)
                .doOnUnsubscribe(() -> Log.i(TAG, "FUNCTION : setupTimerForStartingApp => Starting app subscription unsubscribed"))
                .subscribe(aLong -> {
                    Log.i(TAG, "FUNCTION : setupTimerForStartingApp => on 10 seconds pass");
                    if(schedule.getEvents().get(0).getOpenCount()>0){
                        Log.i(TAG, "FUNCTION : setupTimerForStartingApp => on 10 seconds pass => Package: " + schedule.getEvents().get(0).getPackageName());
                        Log.i(TAG, "FUNCTION : setupTimerForStartingApp => on 10 seconds pass => OpenCount: " + schedule.getEvents().get(0).getOpenCount());
                        sendBroadcast(new Intent("set_count").putExtra("count", schedule.getEvents().get(0).getOpenCount()));
                        schedule.getEvents().get(0).setOpenCount(schedule.getEvents().get(0).getOpenCount() - 1);
                        startTheApp(schedule.getEvents().get(0).getPackageName());
                        sendLogToServer();
                    } else {
                        Log.i(TAG, "FUNCTION : setupTimerForStartingApp => on 10 seconds pass => There's no iteration for package at index 0");
                        sendBroadcast(new Intent("set_count").putExtra("count", schedule.getEvents().get(0).getOpenCount()));
                        if(schedule.getEvents().size()>1){
                            Log.i(TAG, "FUNCTION : setupTimerForStartingApp => Package: " + schedule.getEvents().get(0).getPackageName() + " Finished starting, Going for next package");
                            schedule.getEvents().remove(0);
                            sendBroadcast(new Intent("package_name").putExtra("package", schedule.getEvents().get(0).getPackageName()));
                        } else {
                            Log.i(TAG, "FUNCTION : setupTimerForStartingApp => Package: " + schedule.getEvents().get(0).getPackageName() + " Finished starting and there's no packages left!");
                            hasLeftAny = false;
                            setState();
                            doCurrentStateJobs();
                            sendBroadcast(new Intent("stopped"));
                        }
                    }
                });
    }

    private void sendLogToServer() {
        Log.i(TAG, "FUNCTION : setupTimerForStartingApp");
        ApiService.getInstance().sendLog(new EventLog(schedule.getEvents().get(0).getPackageName(),1,deviceImei)).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "FUNCTION : setupTimerForStartingApp => onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "FUNCTION : setupTimerForStartingApp => Error: " + e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(Void aVoid) {
                Log.i(TAG, "FUNCTION : setupTimerForStartingApp => onNext");
            }
        });
    }

    private void startTheApp(String packageName) {
        Log.i(TAG, "FUNCTION : startTheApp => " + packageName);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.putExtra("to-be-closed", "true");
            startActivity(launchIntent);
        }
    }

    private void runServiceAsForeground() {
        Log.i(TAG, "FUNCTION : runServiceAsForeground");
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
        Log.i(TAG, "FUNCTION : onDestroy");
        stopAllSubscriptions();
        unsubscribeIfNotNull(handleBroadcastsSubscription);
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
