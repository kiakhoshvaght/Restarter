package com.kia.restarter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventLog {

    @SerializedName("packageName")
    @Expose
    private Object packageName;
    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("clientId")
    @Expose
    private Object clientId;

    public Object getPackageName() {
        return packageName;
    }

    public void setPackageName(Object packageName) {
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

    public void setClientId(Object clientId) {
        this.clientId = clientId;
    }
}