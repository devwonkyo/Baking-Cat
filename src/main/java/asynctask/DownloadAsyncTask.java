package com.example.dnjsr.bakingcat.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.thread.AsyncTaskStopThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadAsyncTask extends AsyncTask<Object,Void,Void> {
    TextView newsfeed_textview_download;
    ImageView newsfeed_imageview_image;
    ProgressBar newsfeed_progressbar_download;
    ImageView newsfeed_imageview_downloadcancel;
    Context context;



    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        newsfeed_progressbar_download.setVisibility(View.INVISIBLE);
        newsfeed_textview_download.setVisibility(View.INVISIBLE);
        newsfeed_imageview_downloadcancel.setVisibility(View.INVISIBLE);
        newsfeed_imageview_image.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.DST);//원래대로
        Toast.makeText(context, "이미지 다운로드를 완료했습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
    }

    public DownloadAsyncTask(TextView textView, ImageView imageView, ProgressBar progressBar, ImageView imageView2, Context context) {
        super();
        newsfeed_textview_download = textView;
        newsfeed_imageview_image = imageView;
        newsfeed_progressbar_download = progressBar;
        newsfeed_imageview_downloadcancel = imageView2;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Object... objects) {
        InputStream in = null;
        Bitmap mBitmap;
        try {
            in = new java.net.URL(objects[0].toString()).openStream();
            Log.d("qwer","url input완료");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBitmap = BitmapFactory.decodeStream(in);
        Log.d("qwer","비트맵 디코딩 완료");
        try {
            in.close();
            Log.d("qwer","inputstream 종료완료");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String root = Environment.getExternalStorageDirectory().toString();// 외부저장소 경로얻기
        File myDir = new File(root+"/saved_images");// 경로+디렉터리이름

        myDir.mkdirs();//디렉터리 생성

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //개별적인 파일 이름 위한 날짜받아오기.
        String fname = "Shutta_"+ timeStamp +".jpg"; //다운로드+날짜+확장자



        File file = new File(myDir,fname);//디렉터리에 파일이름 생성
        if (file.exists()){//파일이 존재하면
            file.delete ();//삭제
        }

        if(isCancelled()){
            Log.d("qwer","종료");
            return null;
        }
        try {//파일이 없으면
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();//저장후 종료
            out.close();
            Log.d("qwer","저장완료");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        newsfeed_imageview_image.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY);//어둡게
        newsfeed_progressbar_download.setVisibility(View.VISIBLE);
        newsfeed_textview_download.setVisibility(View.VISIBLE);
        newsfeed_imageview_downloadcancel.setVisibility(View.VISIBLE);
        newsfeed_imageview_downloadcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel(true);
            }
        });
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d("qwer","취소성공");
        newsfeed_progressbar_download.setVisibility(View.INVISIBLE);
        newsfeed_textview_download.setVisibility(View.INVISIBLE);
        newsfeed_imageview_downloadcancel.setVisibility(View.INVISIBLE);
        newsfeed_imageview_image.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.DST);//원래대로
        Toast.makeText(context, "다운로드가 취소되었습니다.", Toast.LENGTH_SHORT).show();
    }


}