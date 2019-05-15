package com.kia.restarter.RxHelper;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;

import rx.Observable;


public final class ContentObservable {

    private ContentObservable() {
        throw new AssertionError("No instances");
    }

    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter) {
        return Observable.create(new OnSubscribeBroadcastRegister(context, filter, null, null));

    }

    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter, String broadcastPermission, Handler schedulerHandler) {
        return Observable.create(new OnSubscribeBroadcastRegister(context, filter, broadcastPermission, schedulerHandler));
    }

    public static Observable<Intent> fromLocalBroadcast(Context context, IntentFilter filter) {
        return Observable.create(new OnSubscribeLocalBroadcastRegister(context, filter));
    }

    public static Observable<String> fromSharedPreferencesChanges(SharedPreferences sharedPreferences) {
        return Observable.create(new OnSubscribeSharedPreferenceChange(sharedPreferences));
    }

    public static Observable<Cursor> fromCursor(final Cursor cursor) {
        return Observable.create(new OnSubscribeCursor(cursor));
    }
}