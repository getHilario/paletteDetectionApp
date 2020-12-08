package com.example.myapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UploadCallback {

    //private  MyAPI mService;
    private ScrollView palette_view;
    private Button p1;
    private Button p2;
    private Button p3;
    private Button p4;
    private Button p5;
    private Button p6;
    private Button p7;
    private Button p8;
    private Button p9;
    private Button p10;
    private Button p11;
    private Button p12;
    private Button p13;
    private Button p14;
    private Button p15;
    private Button p16;


    private ImageView image_view;
    private ImageView image_view2;
    private Button button_upload;
    private Button button_outline_bw;
    private Button button_outline_clr;
    private Button button_color_8;
    private ProgressBar progress_bar;
    private TextView textView;
    private static final int REQUEST_CODE_IMAGE_PICKER = 10;
    private Uri selectedImage = null;
    private String color8 = "";
    private String outlineBw = "";
    private String outlineClr = "";

    private MyAPI getAPIUpload() {
        return ClientAPI.getApiClient().create(MyAPI.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        palette_view = (ScrollView)findViewById(R.id.paletteView);
        //buttons handeled on api response
        //p1 = (Button)findViewById(R.id.p1);
        //p2 = (Button)findViewById(R.id.p2);
        //p3 = (Button)findViewById(R.id.p3);
        //p4 = (Button)findViewById(R.id.p4);
        //p5 = (Button)findViewById(R.id.p5);
        //p6 = (Button)findViewById(R.id.p6);
        //p7 = (Button)findViewById(R.id.p7);
        //p8 = (Button)findViewById(R.id.p8);

        //initializing variables
        image_view = (ImageView)findViewById(R.id.imageView);
        image_view2 = (ImageView)findViewById(R.id.imageView2);
        button_upload = (Button)findViewById(R.id.button_upload);
        button_color_8 = (Button)findViewById(R.id.button_color);
        button_outline_bw = (Button)findViewById(R.id.button_outline_bw);
        button_outline_clr = (Button)findViewById(R.id.button_outline_color);
        progress_bar = (ProgressBar)findViewById(R.id.progress_bar);
        textView = (TextView)findViewById(R.id.textView);

        //mService = getAPIUpload();
        image_view.setOnClickListener(this);
        button_upload.setOnClickListener(this);

        button_outline_bw.setOnClickListener(this);
        button_outline_clr.setOnClickListener(this);
        button_color_8.setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //selecting image
            case R.id.imageView:
                openImageChooser();
                break;
            //prompt for file name
            case R.id.button_upload:
                View view1 = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.input_filename,null);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setView(view1);
                final EditText userInput = (EditText)view1.findViewById(R.id.input_filename_text);

                alertBuilder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String filename = new String(userInput.getText().toString());
                        try {
                            uploadImage(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Dialog dialog = alertBuilder.create();
                dialog.show();
                break;
            //display options
            case R.id.button_outline_bw:
                Picasso.get().load("http://10.0.2.2:5000/" + outlineBw).into(image_view);
                break;

            case R.id.button_outline_color:
                Picasso.get().load("http://10.0.2.2:5000/" + outlineClr).into(image_view);
                break;

            case R.id.button_color:
                Picasso.get().load("http://10.0.2.2:5000/" + color8).into(image_view);
                break;


        }
    }

    @SuppressLint("Recycle")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadImage(String fName) throws IOException {
        progress_bar.setVisibility(View.VISIBLE);
        progress_bar.setProgress(0);

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
        File file = new File(getCacheDir(), name);
        FileOutputStream outputStream = new FileOutputStream(file);
        //copying file to avoid extra permission requests
        copyTo(inputStream, outputStream);

        final UploadRequestBody requestBody = new UploadRequestBody(file, "image", this);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", name, requestBody);
        RequestBody desc = RequestBody.create(MediaType.parse("multipart/form-data"), fName);


        //Call call = uploadApis.uploadImage(body,desc);

        Retrofit retrofit = ClientAPI.getApiClient();
        MyAPI uploadApis = retrofit.create(MyAPI.class);
        Call<UploadResponse> call = uploadApis.uploadImage(body,desc);

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                Toast.makeText(MainActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();

                progress_bar.setProgress(100);
                progress_bar.setVisibility(GONE);

                //storing values from response
                //color8 string value for url of image displayed in 8 colors
                color8 = response.body().getColor_8();
                //outlineBw string value for url of black and white outlined image
                outlineBw = response.body().getOutline_bw();
                //outlineClr string value for url of black outlines on color8 image
                outlineClr = response.body().getOutline_color();

                Picasso.get().load("http://10.0.2.2:5000/" + outlineClr).into(image_view);

                textView.setText("Select Image to Upload");
                button_upload.setVisibility(GONE);
                image_view2.setVisibility(GONE);

                //---->add 2 new buttons here <---
                button_outline_bw.setVisibility(View.VISIBLE);
                button_outline_clr.setVisibility(View.VISIBLE);
                button_color_8.setVisibility(View.VISIBLE);

                //string array of color values for the palette from response
                String[] palette = response.body().getPalette();



                palette_view.setVisibility(View.VISIBLE);
                for(int i = 1; i<=palette.length; i++) {
                    Button button = (Button) findViewById(getResources().getIdentifier("p" + i, "id",
                            getPackageName()));

                    button.setText(palette[i - 1]);
                    button.setBackgroundColor(Color.parseColor(palette[i-1]));
                    button.setTextAppearance(getApplicationContext(),
                            R.style.PaletteText);
                }

            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Upload Failed" + t.getMessage(), Toast.LENGTH_SHORT).show();

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
                button_outline_bw.setVisibility(GONE);
                button_outline_clr.setVisibility(GONE);
                button_color_8.setVisibility(GONE);
                palette_view.setVisibility(GONE);
                button_upload.setVisibility(View.VISIBLE);
                image_view2.setVisibility(View.VISIBLE);
                image_view.setImageURI(selectedImage);
        }
    }

    @Override
    public void onProgressUpdate(int percent) {
        //updating progress for user
        progress_bar.setProgress(percent);
        textView.setText("Loading...");

    }


}