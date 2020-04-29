package com.example.dnjsr.bakingcat.currentUserInfo;

import com.example.dnjsr.bakingcat.info.UserInfo;

public class CurrentUserInfo {
    public static UserInfo currentUserInfo = new UserInfo();

    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }

    public static void setCurrentUserInfo(UserInfo currentUserInfo) {
        CurrentUserInfo.currentUserInfo = currentUserInfo;
    }
}
