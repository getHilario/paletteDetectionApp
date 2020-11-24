package com.example.myapp;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("image")
    private String image;

    public String getResponse() {
        return image;
    }
}
