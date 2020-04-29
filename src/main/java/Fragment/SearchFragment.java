package com.example.dnjsr.bakingcat.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.converter.JsonParser;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.sharedPreferencesManage.DataManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {
    ImageView searchfriendfragment_imageview_searchimage;
    EditText searchfriendfragment_edittext_searchid;
    RecyclerView searchfriendfragment_recyclerview;
    SearchFragmentAdapter searchFragmentAdapter;
    List<UserInfo> allUserList = new ArrayList<>();
    DataManager dataManager = new DataManager();
    JsonParser jsonParser = new JsonParser();

    @Override
    public void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUserList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    UserInfo userInfo = item.getValue(UserInfo.class);
                    if(!userInfo.getUserId().equals(CurrentUserInfo.getCurrentUserInfo().getUserId())){
                        allUserList.add(userInfo);
                    }else{
                        CurrentUserInfo.setCurrentUserInfo(userInfo);
                    }
                }
                searchFragmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_searchfriend,container,false);
        searchfriendfragment_imageview_searchimage = view.findViewById(R.id.searchfriendfragment_imageview_searchimage);
        searchfriendfragment_edittext_searchid = view.findViewById(R.id.searchfriendfragment_edittext_searchid);
        searchfriendfragment_recyclerview = view.findViewById(R.id.searchfriendfragment_recyclerview);
        searchfriendfragment_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        searchFragmentAdapter = new SearchFragmentAdapter(allUserList);     //어뎁터에 List객체(데이터) 넣어줘야함.
        searchfriendfragment_recyclerview.setAdapter(searchFragmentAdapter);

        searchfriendfragment_imageview_searchimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filteredUserList(searchfriendfragment_edittext_searchid.getText().toString());
            }
        });


        return view;
    }


    public void filteredUserList(String userId){
        List<UserInfo> filteredUserList = new ArrayList<>();
            for(UserInfo userInfo : allUserList){
                if(userInfo.getUserId().toLowerCase().contains(userId)){
                    filteredUserList.add(userInfo);
                }
            }
            searchFragmentAdapter.dataChange(filteredUserList);
    }

    public class SearchFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserInfo> userList;

        public SearchFragmentAdapter(List<UserInfo> userInfoList) { //데이터 저장
            userList =userInfoList;

        }

        public class SearchFragmentViewHolder extends RecyclerView.ViewHolder{
            ImageView itemfriendplus_imageview_profile;
            TextView itemfriendplus_textview_name;
            Button itemfriendplus_button_plus;

            public SearchFragmentViewHolder(@NonNull View itemView) {
                super(itemView);
                itemfriendplus_imageview_profile = itemView.findViewById(R.id.itemfriendplus_imageview_profile);
                itemfriendplus_textview_name = itemView.findViewById(R.id.itemfriendplus_textview_name);
                itemfriendplus_button_plus = itemView.findViewById(R.id.itemfriendplus_button_plus);
            }
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_friendplus,viewGroup,false);
            return new SearchFragmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(userList.get(i).getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((SearchFragmentViewHolder)viewHolder).itemfriendplus_imageview_profile);
            ((SearchFragmentViewHolder)viewHolder).itemfriendplus_textview_name.setText(userList.get(i).getUserNicname());


            ((SearchFragmentViewHolder)viewHolder).itemfriendplus_button_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("userFriendList")){//데이터베이스에 친구목록이 있을 경우
                                for(DataSnapshot item : dataSnapshot.getChildren()){//데이터 베이스에 있는 CurrentUser객체 에서
                                    if(item.getKey().equals("userFriendList")){//userFriendList키를 가진 아이템을 가져옴
                                        List<String> friendList = (List<String>) item.getValue(); //데이터베이스의 friendlist
                                        if(!friendList.contains(userList.get(i).getId())){ //friendlist가 선택한 userId를 포함하지 않을 때
                                            friendList.add(userList.get(i).getId());//친구추가
                                            Map<String,Object> friend = new HashMap<>(); //바뀐 친구목록 hashmap으로 저장.
                                            friend.put("userFriendList",friendList);
                                            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).updateChildren(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {//성공적으로 팔로잉에 성공하면 팔로우 요청 보내기.
                                                    Toast.makeText(getContext(), userList.get(i).getUserId()+" 님을 팔로잉 합니다.", Toast.LENGTH_SHORT).show();//팔로잉 성공메시지
                                                    if(!CurrentUserInfo.getCurrentUserInfo().getUserFollowerList().isEmpty()){ //나를 팔로워하는 친구가 있을 경우
                                                        FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() { //데이터베이스의 내 객체를 불러옴
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                                                                if(userInfo.getUserFollowerList().contains(userList.get(i).getId())){ //만약 팔로워 리스트에 방금 추가한 친구가 포함되어있으면
                                                                    userInfo.getUserFollowerList().remove(userList.get(i).getId());//리스트에서 삭제후
                                                                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFollowerList").setValue(userInfo.getUserFollowerList());//갱신
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }
                                                    FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userId").equalTo(userList.get(i).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override//팔로잉 요청리스트
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //팔로잉 한 유저의 아이디로 유저의 객체를 받아옴.
                                                            for(DataSnapshot item : dataSnapshot.getChildren()){
                                                                UserInfo userInfo = item.getValue(UserInfo.class); //팔로잉 한 유저의 객체
                                                                List<String> followerList;
                                                                if(userInfo.getUserFollowerList().isEmpty()){ //데이터베이스에 follower 유저가 없을 경우
                                                                    if(!userInfo.getUserFriendList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){ //상대방의 친구목록에 내가 없을 경우에만 친구요청리스트에 추가.
                                                                        followerList = new ArrayList<>();
                                                                        followerList.add(CurrentUserInfo.getCurrentUserInfo().getId()); // 상대방유저의 팔로우 리스트에 내 아이디를 추가.
                                                                        FirebaseDatabase.getInstance().getReference().child("users").child(userInfo.getId()).child("userFollowerList").setValue(followerList);
                                                                    }

                                                                }else{
                                                                    if(!userInfo.getUserFriendList().contains(CurrentUserInfo.getCurrentUserInfo().getId())) {//상대방의 친구목록에 내가 없을 경우에만 친구요청리스트에 추가.
                                                                        followerList = userInfo.getUserFollowerList();
                                                                        followerList.add(CurrentUserInfo.getCurrentUserInfo().getId()); // 팔로우 리스트에 내 아이디를 추가.
                                                                        FirebaseDatabase.getInstance().getReference().child("users").child(userInfo.getId()).child("userFollowerList").setValue(followerList);
                                                                    }
                                                                }
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            });//데이터베이스에 등록
                                        }else{//friendlist가 선택한 userId를 포함할 때
                                            Toast.makeText(getContext(), "이미 팔로잉 중 입니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            }else{//데이터 베이스에 친구목록이 없을 경우
                                List<String> friendList = new ArrayList<>();
                                friendList.add(userList.get(i).getId());
                                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFriendList").setValue(friendList); //친구리스트 처음 추가
                                Toast.makeText(getContext(), userList.get(i).getUserId()+" 님을 팔로잉 합니다.", Toast.LENGTH_SHORT).show();

                                if(!CurrentUserInfo.getCurrentUserInfo().getUserFollowerList().isEmpty()){ //나를 팔로워하는 친구가 있을 경우
                                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() { //데이터베이스의 내 객체를 불러옴
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                                            if(userInfo.getUserFollowerList().contains(userList.get(i).getId())){ //만약 팔로워 리스트에 방금 추가한 친구가 포함되어있으면
                                                userInfo.getUserFollowerList().remove(userList.get(i).getId());//리스트에서 삭제후
                                                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFollowerList").setValue(userInfo.getUserFollowerList());//갱신
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                FirebaseDatabase.getInstance().getReference().child("users").child(userList.get(i).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                                        List<String> followerList;
                                        if(userInfo.getUserFollowerList().isEmpty()){
                                            if(!userInfo.getUserFriendList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){
                                                followerList = new ArrayList<>();
                                                followerList.add(CurrentUserInfo.getCurrentUserInfo().getId()); // 팔로우 리스트에 내 아이디를 추가.
                                                FirebaseDatabase.getInstance().getReference().child("users").child(userInfo.getId()).child("userFollowerList").setValue(followerList);
                                            }
                                        }else{
                                            if(!userInfo.getUserFriendList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){
                                                followerList = userInfo.getUserFollowerList();
                                                followerList.add(CurrentUserInfo.getCurrentUserInfo().getId()); // 팔로우 리스트에 내 아이디를 추가.
                                                FirebaseDatabase.getInstance().getReference().child("users").child(userInfo.getId()).child("userFollowerList").setValue(followerList);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            final ArrayList<String> friendList = (ArrayList<String>) CurrentUserInfo.getCurrentUserInfo().getUserFriendList();

            if(friendList.contains(userList.get(i).getId())){
                ((SearchFragmentViewHolder)viewHolder).itemfriendplus_button_plus.setText("팔로잉");
                ((SearchFragmentViewHolder)viewHolder).itemfriendplus_button_plus.setBackgroundResource(R.drawable.red_round_button);
            }
            else{
                ((SearchFragmentViewHolder)viewHolder).itemfriendplus_button_plus.setText("팔로우");
                ((SearchFragmentViewHolder)viewHolder).itemfriendplus_button_plus.setBackgroundResource(R.drawable.gray_round_button);
            }

        }

        public void dataChange(List<UserInfo> filteredUserList){
            userList = filteredUserList;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }
}
