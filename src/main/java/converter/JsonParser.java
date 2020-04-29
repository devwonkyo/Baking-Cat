package com.example.dnjsr.bakingcat.converter;

import com.example.dnjsr.bakingcat.info.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public ArrayList<UserInfo> jsonArrayToUserList(String json){
        List<UserInfo> userList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String userId = jsonObject.getString("userId");
                String userPassword = jsonObject.getString("userPassword");
                String userNicname = jsonObject.getString("userNicname");
//                String userProfileImage = jsonObject.getString("userProfileImage");
                String userProfileImageUri = jsonObject.getString("userProfileImageUri");


                UserInfo userInfo = new UserInfo();


                if(jsonObject.has("userFriendList")){
                    JSONArray userFriendList = jsonObject.getJSONArray("userFriendList");//friendId
                    List<String> friendList = new ArrayList<>();

                    for(int j = 0; j<userFriendList.length();j++){
                        JSONObject friendIdObject = userFriendList.getJSONObject(j);
                        String friendId = friendIdObject.getString("friendId");
                        friendList.add(friendId);

                    }
                    userInfo.setUserFriendList(friendList);
                }




                userInfo.setUserId(userId);
                userInfo.setUserPassword(userPassword);
                userInfo.setUserNicname(userNicname);
                //userInfo.setUserProfileImage(userProfileImage);
                userInfo.setUserProfileImageUri(userProfileImageUri);
                userList.add(userInfo);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (ArrayList<UserInfo>) userList;
    }

    public String userListToJsonArray(ArrayList<UserInfo> userInfoList){
        JSONArray jsonArray = new JSONArray();

        for(int i = 0; i<userInfoList.size();i++){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId",userInfoList.get(i).getUserId());
                jsonObject.put("userPassword",userInfoList.get(i).getUserPassword());
                jsonObject.put("userNicname",userInfoList.get(i).getUserNicname());
                //jsonObject.put("userProfileImage",userInfoList.get(i).getUserProfileImage());
                jsonObject.put("userProfileImageUri",userInfoList.get(i).getUserProfileImageUri());


                if(!userInfoList.get(i).getUserFriendList().isEmpty()){
                    JSONArray friendArray = new JSONArray();//친구리스트 배열
                    for(int j = 0 ; j<userInfoList.get(i).getUserFriendList().size();j++){
                        JSONObject friendObject = new JSONObject(); //친구아이디
                        friendObject.put("friendId",userInfoList.get(i).getUserFriendList().get(j));
                        friendArray.put(friendObject);
                    }
                    jsonObject.put("userFriendList",friendArray);
                }





                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }
}
