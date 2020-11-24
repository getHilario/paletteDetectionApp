package com.example.myapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UploadCallback {

    private ImageView image_view;
    private Button button_upload;
    private ProgressBar progress_bar;
    private static final int REQUEST_CODE_IMAGE_PICKER = 10;
    private Uri selectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image_view = (ImageView) findViewById(R.id.imageView);
        button_upload = (Button)findViewById(R.id.button_upload);

        image_view.setOnClickListener(this);
        button_upload.setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                openImageChooser();
                break;

            case R.id.button_upload:
                try {
                    uploadImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadImage() throws IOException {
        if (selectedImage == null) {
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = "";
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImage, "r", null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(selectedImage, null, null, null, null);
            //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                name = cursor.getString(column_index);
            }

        } catch (Exception e) {
        }
        FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
        File file = new File(getCacheDir(),name);
        FileOutputStream outputStream = new FileOutputStream(file);

        copyTo(inputStream,outputStream);

        progress_bar = (ProgressBar)findViewById(R.id.progress_bar);
        progress_bar.setProgress(0);

        UploadRequestBody requestBody = new UploadRequestBody(file,"image",this);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        RequestBody desc = RequestBody.create(MediaType.parse( "multipart/form-data"),"This is a new image");

        Retrofit retrofit = ClientAPI.getApiClient();
        MyAPI uploadApis = retrofit.create(MyAPI.class);
        Call call = uploadApis.uploadImage(body,desc);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Toast.makeText(MainActivity.this,"Uploading...",Toast.LENGTH_LONG).show();
                //img.setVisibility(View.VISIBLE);
                //img_title.setVisibility(View.VISIBLE);
                //chooseBtn.setVisibility(View.VISIBLE);
                //uploadBtn.setVisibility(View.INVISIBLE);
                progress_bar.setProgress(100);


            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(MainActivity.this, "Upload Failed" + t.getMessage(), Toast.LENGTH_SHORT);

            }
        });
    }

    private void copyTo(InputStream source, OutputStream target) throws IOException {
            byte[] buf = new byte[8192];
            int length;
            while ((length = source.read(buf)) > 0) {
                target.write(buf, 0, length);
            }
        }


    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
                //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                //img.setImageBitmap(bitmap);
                //img.setVisibility(View.VISIBLE);
                //img_title.setVisibility(View.VISIBLE);
                //chooseBtn.setVisibility(View.GONE);
                //uploadBtn.setVisibility(View.VISIBLE);
                image_view.setImageURI(selectedImage);
        }
    }

    @Override
    public void onProgressUpdate(int percent) {
        progress_bar = (ProgressBar)findViewById(R.id.progress_bar);
        progress_bar.setProgress(percent);

    }
}