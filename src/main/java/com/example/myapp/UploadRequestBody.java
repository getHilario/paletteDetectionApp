package com.example.myapp;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class UploadRequestBody extends RequestBody {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private File file;
    private String contentType;
    private UploadCallback callback;

    public UploadRequestBody(File file, String contentType, UploadCallback callback) {
        this.file = file;
        //this.contentType = contentType;
        this.callback = callback;
    }

    //@javax.annotation.Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long length = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        FileInputStream in = new FileInputStream(file);
        long uploaded = 0;
        try{
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while((read = in.read(buffer)) != -1)
            {
                handler.post(new ProgressUpdate(uploaded,length));
                uploaded+=read;
                sink.write(buffer,0, read);
            }
        }
        finally{
            in.close();
        }

    }
    private class ProgressUpdate implements Runnable{
        private long uploaded;
        //private long fileLength;
        private long total;

        public ProgressUpdate(long uploaded, long length) {
            this.uploaded = uploaded;
            this.total = length;
        }

        @Override
        public void run() {
            callback.onProgressUpdate((int)(100*uploaded/total));
        }
    }
}
