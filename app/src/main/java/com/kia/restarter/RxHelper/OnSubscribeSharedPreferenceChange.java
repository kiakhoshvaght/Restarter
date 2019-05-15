package com.kia.restarter.RxHelper;

import android.content.SharedPreferences;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

class OnSubscribeSharedPreferenceChange implements Observable.OnSubscribe<String> {

    private final SharedPreferences sharedPreferences;

    public OnSubscribeSharedPreferenceChange(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void call(final Subscriber<? super String> subscriber) {
        final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> subscriber.onNext(key);

        subscriber.add(Subscriptions.create(() -> sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)));

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
}