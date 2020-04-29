package com.example.dnjsr.bakingcat.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Handler;

public class AsyncTaskStopThread extends Thread {
    Boolean stop = false;
    String imageUrl;

    public AsyncTaskStopThread(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        super.run();
        Bitmap mBitmap = null;
        InputStream in = null;
        OutputStream outStream = null;
        String extStorageDirectory =
                Environment.getExternalStorageDirectory().toString();

        File file = new File(extStorageDirectory, "/req_images");

        for(int i = 0 ; i< 3; i++){
            if(stop){
                break;
            }
            else{
                if(i == 0){
                    try {
                        in = new java.net.URL(imageUrl).openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mBitmap = BitmapFactory.decodeStream(in);

                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else if(i==1){
                  file.mkdirs();
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "Image-" + n + ".jpg";
                    File files = new File(file, fname);
                    if (files.exists())
                        files.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(files);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                        Log.d("qwer","저장완료");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(i==2){

                }
            }
        }
    }
}
