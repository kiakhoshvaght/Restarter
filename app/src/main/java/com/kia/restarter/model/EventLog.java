package com.kia.restarter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventLog {

    @SerializedName("packageName")
    @Expose
    private String packageName;
    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("clientId")
    @Expose
    private String clientId;

    public EventLog(String packageName, int count, String deviceId){
        this.packageName = packageName;
        this.count = count;
        this.clientId = deviceId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Object getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}