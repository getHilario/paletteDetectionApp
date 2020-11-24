package com.example.myapp;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MyAPI {
    @Multipart
    //@POST("Api.php?apicall=upload")
    @POST("api/upload")
        //Call<imageClass> uploadImage(@Field("title") String title,@Field("image") String image);
        Call<RequestBody> uploadImage(@Part MultipartBody.Part image, @Part("desc") RequestBody requestBody);
    //Call<RequestBody> uploadImage(@Part MultipartBody.Part part);
}

