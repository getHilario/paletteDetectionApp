package com.example.myapp;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientAPI {

    private static final String BaseUrl="http://10.0.2.2:5000/";
    //private static final String BaseUrl="http://10.0.2.2/ImageUploader/";
    private static Retrofit retrofit;


    public static Retrofit getApiClient()
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if(retrofit==null)
        {
            retrofit = new Retrofit.Builder().baseUrl(BaseUrl).
                    addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }
        return retrofit;
    }
}
