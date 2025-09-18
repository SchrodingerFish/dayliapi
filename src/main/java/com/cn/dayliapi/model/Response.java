package com.cn.dayliapi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@lombok.Data
public class Response {

    @SerializedName("request_id")
    @Expose
    private String requestId;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("usage")
    @Expose
    private Integer usage;
}
