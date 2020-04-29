package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.ChatInfo;
import com.example.dnjsr.bakingcat.info.ChatRoomInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
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

public class FriendSelectActivity extends AppCompatActivity {
    EditText friendselect_edittext_id;
    RecyclerView friendselect_recyclerview;
    Button friendselect_button_plus;
    List<UserInfo> friendList = new ArrayList<>();
    FriendSelectAdapter friendSelectAdapter;
    List<String> otherUserId = new ArrayList<>();
    List<String> otherUserName = new ArrayList<>();


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() { //데이터 바뀔때마다 모든 유저 불러옴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    UserInfo userInfo = item.getValue(UserInfo.class);
                    if(CurrentUserInfo.getCurrentUserInfo().getUserFriendList().contains(userInfo.getId())){ //친구목록에 있는 사람만 친구리스트에 추가
                        friendList.add(userInfo);
                    }
                    if(userInfo.getUserId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){ //내 아이디일 경우 CurrentUser갱신
                        CurrentUserInfo.setCurrentUserInfo(userInfo);
                    }
                }
                friendSelectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) { //친구 선택해서 채팅방 생성 화면
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_select);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("대화상대 초대");

        friendselect_edittext_id = findViewById(R.id.friendselect_edittext_id);
        friendselect_button_plus = findViewById(R.id.friendselect_button_plus);
        friendselect_recyclerview = findViewById(R.id.friendselect_recyclerview);

        friendselect_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        friendSelectAdapter = new FriendSelectAdapter(friendList);//데이터 넘겨줘야함.List를 생성자에 넣어줘야함.
        friendselect_recyclerview.setAdapter(friendSelectAdapter);

        friendselect_button_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //추가 버튼.

                if(otherUserId.size()==1){ // 체크버튼이 1개이하 -->개인톡
                    Intent intent = new Intent(getApplicationContext(),ChatRoomActivity.class);
                    intent.putExtra("otherUserId",otherUserId.get(0)); //선택한 아이디
                    startActivity(intent);
                    finish();
                }else if(otherUserId.size()==0){//체크 버튼이 1개 초과 -->단체 톡
                    Toast.makeText(FriendSelectActivity.this, "대화상대를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    Map<String,Boolean> chatUsers = new HashMap<>();
                    for(String userId : otherUserId){
                        chatUsers.put(userId,true); //유저정보 입력
                    }
                    chatUsers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                    final ChatRoomInfo chatRoomInfo = new ChatRoomInfo();
                    final String roomId = FirebaseDatabase.getInstance().getReference().child("chatrooms").push().getKey(); //채팅방 만들고 키를 가져옴
                    chatRoomInfo.setUsers(chatUsers);//채팅방정보에 유저정보 넣기
                    chatRoomInfo.setRoomId(roomId);//채팅방 정보에 채팅방 id넣기

                   String roomName = "";
                    for(int i = 0 ; i<otherUserName.size(); i++){
                        if(i == 0 ){
                            roomName+=otherUserName.get(i);
                        }else{
                            roomName+=","+otherUserName.get(i);
                        }
                    }
                    roomName+=","+CurrentUserInfo.getCurrentUserInfo().getUserNicname();
                    chatRoomInfo.setRoomName(roomName);
                    chatRoomInfo.setRoomPeopleNumber(String.valueOf(otherUserId.size()+1));



                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).setValue(chatRoomInfo).addOnCompleteListener(new OnCompleteListener<Void>() {//디비 채팅방에 채팅방정보 넣기.
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {//성공하면 인텐트.
                            Intent intent = new Intent(getApplicationContext(),GroupChatRoomActivity.class);
                            intent.putExtra("roomId",roomId);
                            intent.putExtra("roomNum",String.valueOf(chatRoomInfo.getUsers().size()));
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        });

        friendselect_edittext_id.addTextChangedListener(new TextWatcher() { //텍스트가 바뀔 때 마다 필터링.
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFilteredFriendList(s.toString());
            }
        });
    }

    public void setFilteredFriendList(String friendName){
        List<UserInfo> filteredFriendList = new ArrayList<>();
        if(friendName.equals("")){
            filteredFriendList = friendList;
        }else{
            for(UserInfo userInfo : friendList){
                if(userInfo.getUserNicname().toLowerCase().contains(friendName)){ //이름
                    filteredFriendList.add(userInfo);
                }
            }
        }
        friendSelectAdapter.dataChange(filteredFriendList);
    }




    public class FriendSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserInfo> adapterFriendList;
        public FriendSelectAdapter(List<UserInfo> friendList) {// 데이터 입력
            adapterFriendList = friendList;
        }

        public class FriendSelectViewHolder extends RecyclerView.ViewHolder{ //친구 선택 아이템 객체에 접근하는 뷰홀더
            ImageView itemfriendselect_imageview_profile;
            TextView itemfriendselect_textview_name;
            CheckBox itemfriendselect_checkbox_selectfriend;
            Button friendselect_button_plus;

            public FriendSelectViewHolder(@NonNull View itemView) {
                super(itemView);
                itemfriendselect_imageview_profile = itemView.findViewById(R.id.itemfriendselect_imageview_profile); //프로필사진
                itemfriendselect_textview_name = itemView.findViewById(R.id.itemfriendselect_textview_name);//닉네임
                itemfriendselect_checkbox_selectfriend = itemView.findViewById(R.id.itemfriendselect_checkbox_selectfriend);//체크
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) { //친구 선택 아이템을 리사이클러뷰에 붙임.
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friendselect,viewGroup,false);
            return new FriendSelectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) { //데이터 삽입
            ((FriendSelectViewHolder)viewHolder).itemfriendselect_checkbox_selectfriend.setChecked(false);
            Glide.with(viewHolder.itemView.getContext()).load(adapterFriendList.get(i).getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((FriendSelectViewHolder)viewHolder).itemfriendselect_imageview_profile);
            ((FriendSelectViewHolder)viewHolder).itemfriendselect_textview_name.setText(adapterFriendList.get(i).getUserNicname());
            ((FriendSelectViewHolder)viewHolder).itemfriendselect_checkbox_selectfriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { //체크 됐을 때
                    if(isChecked){
                        otherUserId.add(adapterFriendList.get(i).getId());
                        otherUserName.add(adapterFriendList.get(i).getUserNicname());
                    }else{
                        otherUserId.remove(adapterFriendList.get(i).getId());
                        otherUserName.remove(adapterFriendList.get(i).getUserNicname());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterFriendList.size();
        }


        public void dataChange(List<UserInfo> filteredFriendList){
            adapterFriendList= filteredFriendList;
            notifyDataSetChanged();
        }
    }
}
