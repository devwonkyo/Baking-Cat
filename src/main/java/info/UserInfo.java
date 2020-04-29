package com.example.dnjsr.bakingcat.info;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    String id;
    String userId;
    String userNicname;
    String userPassword;
    String userProfileImage;
    String userProfileImageUri;
    String userPushToken;
    List<String> userFriendList = new ArrayList<>();
    List<String> userFollowerList = new ArrayList<>();
    List<String> userChatRoomList = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserNicname() {
        return userNicname;
    }

    public void setUserNicname(String userNicname) {
        this.userNicname = userNicname;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getUserProfileImageUri() {
        return userProfileImageUri;
    }

    public void setUserProfileImageUri(String userProfileImageUri) {
        this.userProfileImageUri = userProfileImageUri;
    }

    public List<String> getUserFriendList() {
        return userFriendList;
    }

    public void setUserFriendList(List<String> userFriendList) {
        this.userFriendList = userFriendList;
    }

    public List<String> getUserFollowerList() {
        return userFollowerList;
    }

    public void setUserFollowerList(List<String> userFollowerList) {
        this.userFollowerList = userFollowerList;
    }

    public List<String> getUserChatRoomList() {
        return userChatRoomList;
    }

    public void setUserChatRoomList(List<String> userChatRoomList) {
        this.userChatRoomList = userChatRoomList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserPushToken() {
        return userPushToken;
    }

    public void setUserPushToken(String userPushToken) {
        this.userPushToken = userPushToken;
    }
}
