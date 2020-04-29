package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.converter.JsonParser;
import com.example.dnjsr.bakingcat.sharedPreferencesManage.DataManager;
import com.example.dnjsr.bakingcat.thread.SplashThread;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
        Intent intent;
        DataManager dataManager;
        JsonParser jsonParser;
        List<UserInfo> userList;
        Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0://자동로그인 x -->로그인 activity
                        //Log.d("qwer","스플레시 액티비티 자동로그인x");
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();
                        break;
                    case 1://자동로그인 -->메인 activitiy
                        //Log.d("qwer","스플레시 액티비티 자동로그인");
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("id",msg.obj.toString());
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };

        SplashThread splashThread = new SplashThread(this,handler);
        //Log.d("qwer","스플레시 액티비티 시작");
        splashThread.start();

    }

    @Override
    protected void onStart() {
        //Log.d("qwer","스플레시 액티비티 start");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("qwer","스플레시 액티비티 resume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("life","스플레시 액티비티 pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("life","스플레시 액티비티 stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("life","스플레시 액티비티 destroy");
    }
}
