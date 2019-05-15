package com.kia.restarter.RxHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

class OnSubscribeBroadcastRegister implements Observable.OnSubscribe<Intent> {

    private final Context context;
    private final IntentFilter intentFilter;
    private final String broadcastPermission;
    private final Handler schedulerHandler;

    public OnSubscribeBroadcastRegister(Context context, IntentFilter intentFilter, String broadcastPermission, Handler schedulerHandler) {
        this.context = context;
        this.intentFilter = intentFilter;
        this.broadcastPermission = broadcastPermission;
        this.schedulerHandler = schedulerHandler;
    }

    @Override
    public void call(final Subscriber<? super Intent> subscriber) {
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                subscriber.onNext(intent);
            }
        };

        subscriber.add(Subscriptions.create(() -> {
            try {
                context.unregisterReceiver(broadcastReceiver);
            } catch (Throwable ignored) {
            }
        }));
        context.registerReceiver(broadcastReceiver, intentFilter, broadcastPermission, schedulerHandler);

    }
}