package com.example.dnjsr.bakingcat.sharedPreferencesManage;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class DataManager {

    SharedPreferences appData;

    public void putUserInfo(Context context,String userInfo){
        appData = context.getSharedPreferences("appData",MODE_PRIVATE);
        SharedPreferences.Editor editor = appData.edit();
        editor.remove("user");
        editor.putString("user",userInfo);
        editor.commit();
    }

    public String getUserInfo(Context context){
        appData = context.getSharedPreferences("appData",MODE_PRIVATE);
        return appData.getString("user","0");

    }

}
