package com.kia.restarter.api;

import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.kia.restarter.BuildConfig;
import com.kia.restarter.model.EventLog;
import com.kia.restarter.model.Schedule;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import rx.schedulers.Schedulers;

public class ApiService {

    private static final String TAG = ApiService.class.getName();
    ApiWebService service;
    private String baseUrl;
    static ApiService apiService;
    private Retrofit retrofit;

    public static ApiService getInstance() {
        Log.i(TAG, "FUNCTION : getInstance");
        if (apiService == null) {
            Log.i(TAG, "FUNCTION : getInstance => Instance is null, going to instantiate");
            apiService = new ApiService();
            apiService.init();
            return apiService;
        } else {
            Log.i(TAG, "FUNCTION : getInstance => Instance is not null, going to return instance");
            return apiService;
        }
    }

    public void init() {
        Log.i(TAG, "FUNCTION : init");
        baseUrl = BuildConfig.BASE_URL;
        OkHttpClient httpClient = new OkHttpClient();
        httpClient = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
        try {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();
            service = retrofit.create(ApiWebService.class);
        } catch (Exception e) {
            Log.e(TAG, "FUNCTION : init => Error: " + e.toString());
            e.printStackTrace();
        }
        Log.i(TAG, "FUNCTION : init => Here");
    }

    public Observable<Schedule> getSchedule(String clientId) {
        Log.i(TAG, "FUNCTION : getSchedule");
        return service.getSchedule(clientId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retry(2)
                .timeout(10, TimeUnit.SECONDS);
    }

    public Observable<Void> sendLog(EventLog eventLog) {
        Log.i(TAG, "FUNCTION : getSchedule");
        return service.sendLog(eventLog);
    }

    interface ApiWebService {
        @GET("Schedule")
        Observable<Schedule> getSchedule(@Query("clientId") String clientId);

        @POST("Log/OpenEvent")
        Observable<Void> sendLog(@Body EventLog eventLog);
    }

}
