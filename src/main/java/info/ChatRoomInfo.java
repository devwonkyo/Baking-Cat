package com.example.dnjsr.bakingcat.info;

import android.text.BoringLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomInfo {
    String lastMessageTime;
    String roomId;
    String roomName;
    String roomPeopleNumber;
    Boolean customRoomName = false;
    Map<String,Boolean> users = new HashMap<>();
    Map<String,ChatInfo> messages = new HashMap<>();

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }


    public Boolean getCustomRoomName() {
        return customRoomName;
    }

    public void setCustomRoomName(Boolean customRoomName) {
        this.customRoomName = customRoomName;
    }

    public String getRoomPeopleNumber() {
        return roomPeopleNumber;
    }

    public void setRoomPeopleNumber(String roomPeopleNumber) {
        this.roomPeopleNumber = roomPeopleNumber;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomId() {
       return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Map<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Boolean> users) {
        this.users = users;
    }

    public Map<String, ChatInfo> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, ChatInfo> messages) {
        this.messages = messages;
    }
}
