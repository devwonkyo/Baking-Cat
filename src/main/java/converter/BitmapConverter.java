package com.example.dnjsr.bakingcat.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapConverter {

    public static String getBitmapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream); //스트림에 bitmap을 저장.
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByte,Base64.NO_WRAP);
    }

    public static Bitmap getStringToBitmap(String userProfileImage){
        byte[] decodeByteArray = Base64.decode(userProfileImage,Base64.NO_WRAP);
        Bitmap decodeBitmap = BitmapFactory.decodeByteArray(decodeByteArray,0,decodeByteArray.length);
        return decodeBitmap;
    }
}
