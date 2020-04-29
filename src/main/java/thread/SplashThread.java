package com.example.dnjsr.bakingcat.thread;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.dnjsr.bakingcat.LoginActivity;
import com.example.dnjsr.bakingcat.SplashActivity;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class SplashThread extends Thread{

    Handler handler;
    Activity activity;

    public SplashThread(Activity activity,Handler handler) { //intent 시작할 context와 , intent객체 받아옴.
        this.handler = handler;
        this.activity =  activity;
    }

    @Override
    public void run() {
        super.run();
        SharedPreferences appData = activity.getSharedPreferences("appData",MODE_PRIVATE);
        String autoLoginId  = appData.getString("autoLogin","0");

        if(autoLoginId.equals("0")){
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }else{
            FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userId").equalTo(autoLoginId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserInfo userInfo = null;
                        for(DataSnapshot item : dataSnapshot.getChildren()){
                            userInfo = item.getValue(UserInfo.class);
                            CurrentUserInfo.setCurrentUserInfo(userInfo);
                        }
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        message.obj = userInfo.getUserId();
                        handler.sendMessage(message);
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
