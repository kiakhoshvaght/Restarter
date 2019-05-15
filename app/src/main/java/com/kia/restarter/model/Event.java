package com.kia.restarter.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {
    @SerializedName("packageName")
    @Expose
    private String packageName;
    @SerializedName("openCount")
    @Expose
    private Integer openCount;

    public Event(String packageName, int openCount){
        this.packageName = packageName;
        this.openCount = openCount;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getOpenCount() {
        return openCount;
    }

    public void setOpenCount(Integer openCount) {
        this.openCount = openCount;
    }
}