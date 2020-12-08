package com.example.myapp;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("color_8")
    private String color_8;

    @SerializedName("outline_bw")
    private String outline_bw;

    @SerializedName("outline_color")
    private String outline_color;

    @SerializedName("palette")
    private String[] palette;

    public String getColor_8()
    {
        return color_8;
    }
    public String getOutline_bw()
    {
        return outline_bw;
    }
    public String getOutline_color()
    {
        return outline_color;
    }
    public String[] getPalette()
    {
        return palette;
    }

}
