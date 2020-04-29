package com.example.dnjsr.bakingcat.info;

import android.text.BoringLayout;

import java.util.HashMap;
import java.util.Map;

public class ChatInfo {
    String id;
    String message;
    String currentTime;
    String imageMessageUrl ="empty";
    String recordMessageUrl ="empty";
    Map<String,Boolean> readUsers = new HashMap<>();

    public String getRecordMessageUrl() {
        return recordMessageUrl;
    }

    public void setRecordMessageUrl(String recordMessageUrl) {
        this.recordMessageUrl = recordMessageUrl;
    }

    public String getImageMessageUrl() {
        return imageMessageUrl;
    }

    public void setImageMessageUrl(String imageMessageUrl) {
        this.imageMessageUrl = imageMessageUrl;
    }

    public Map<String, Boolean> getReadUsers() {
        return readUsers;
    }

    public void setReadUsers(Map<String, Boolean> readUsers) {
        this.readUsers = readUsers;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
