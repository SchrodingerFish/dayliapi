package com.cn.dayliapi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@lombok.Data
public class Data {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("news")
    @Expose
    private List<String> news = null;
    @SerializedName("weiyu")
    @Expose
    private String weiyu;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("audio")
    @Expose
    private String audio;
    @SerializedName("head_image")
    @Expose
    private String headImage;

}
