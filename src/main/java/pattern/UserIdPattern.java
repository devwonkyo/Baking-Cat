package com.example.dnjsr.bakingcat.pattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserIdPattern {
    Matcher matcher;
    Pattern pattern;

    public boolean checkUserId(String userId){
        pattern = Pattern.compile("(^[0-9a-zA-Z]{4,16}$)"); //영문,숫자 포함 4~16자리
        matcher = pattern.matcher(userId);
        return matcher.find();// 패턴에 일치하면 true 틀리면 false
    }
}
