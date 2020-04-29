package com.example.dnjsr.bakingcat.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.ChatRoomActivity;
import com.example.dnjsr.bakingcat.FriendSelectActivity;
import com.example.dnjsr.bakingcat.GroupChatRoomActivity;
import com.example.dnjsr.bakingcat.ModifyRoomNameActivity;
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.ChatInfo;
import com.example.dnjsr.bakingcat.info.ChatRoomInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatFragment extends Fragment { //채팅목록 창
    RecyclerView chatlistfragment_recyclerview;
    FloatingActionButton chatlistfragment_button_selectfriend;
    ChatFragmentAdapter chatFragmentAdapter;
    List<String> roomList = new ArrayList<>();
    List<ChatRoomInfo> chatRoomList = new ArrayList<>();
    TextView chatlistfragment_textview_nofriend;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { //채팅화면 생성
        View view = inflater.inflate(R.layout.fragment_chatlist,container,false);
        chatlistfragment_button_selectfriend = view.findViewById(R.id.chatlistfragment_button_selectfriend);
        chatlistfragment_recyclerview = view.findViewById(R.id.chatlistfragment_recyclerview);
        chatlistfragment_textview_nofriend = view.findViewById(R.id.chatlistfragment_textview_nofriend);
        chatlistfragment_button_selectfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),FriendSelectActivity.class));
            }
        });


        chatFragmentAdapter = new ChatFragmentAdapter(chatRoomList);
        chatlistfragment_recyclerview.setAdapter(chatFragmentAdapter);
        chatlistfragment_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+CurrentUserInfo.getCurrentUserInfo().getId()).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatRoomInfo chatRoomInfo = item.getValue(ChatRoomInfo.class);
                    chatRoomList.add(chatRoomInfo);
                }
                chatFragmentAdapter.notifyDataSetChanged();
                if(chatRoomList.isEmpty()){ //채팅목록이 비어있으면
                    chatlistfragment_textview_nofriend.setVisibility(View.VISIBLE);//감춤
                }else{ //안비어있으면
                    chatlistfragment_textview_nofriend.setVisibility(View.INVISIBLE);//표시
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public class ChatFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{ // 채팅목록

        List<ChatRoomInfo> adapterChatRoomList;

        public ChatFragmentAdapter(List<ChatRoomInfo> chatRoomList) { //데이터 삽입
            adapterChatRoomList = chatRoomList;
        }

        public class ChatListViewHolder extends RecyclerView.ViewHolder{ //아이템뷰 객체를 받아 아이템뷰 객체에 접근할 수 있게 해줌.
            ImageView itemchatlist_imageview_profile;
            TextView itemchatlist_textview_name;
            TextView itemchatlist_textview_lastmessage;
            TextView itemchatlist_textview_lastmessagetime;
            TextView itemchatlist_textview_peoplenum;
            TextView itemchatlist_textview_unreadcount;

            public ChatListViewHolder(@NonNull View itemView)
            {
                super(itemView);
                itemchatlist_imageview_profile = itemView.findViewById(R.id.itemchatlist_imageview_profile);
                itemchatlist_textview_name = itemView.findViewById(R.id.itemchatlist_textview_name);
                itemchatlist_textview_lastmessage = itemView.findViewById(R.id.itemchatlist_textview_lastmessage);
                itemchatlist_textview_lastmessagetime = itemView.findViewById(R.id.itemchatlist_textview_lastmessagetime);
                itemchatlist_textview_peoplenum = itemView.findViewById(R.id.itemchatlist_textview_peoplenum);
                itemchatlist_textview_unreadcount = itemView.findViewById(R.id.itemchatlist_textview_unreadcount);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chatlist,viewGroup,false);
            return new ChatListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder,final int i) {
            String otherUserId = null;
            int peopleNum = 0;
            if(adapterChatRoomList.get(i).getUsers().size()<=2){ //개인톡일 때
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_peoplenum.setText("");
                for(String id : adapterChatRoomList.get(i).getUsers().keySet()){
                    if(!id.equals(CurrentUserInfo.getCurrentUserInfo().getId())){
                        otherUserId = id;
                    }
                }
                FirebaseDatabase.getInstance().getReference().child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {//사진하고 이름입력
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserInfo otherUserInfo = dataSnapshot.getValue(UserInfo.class);
                        Glide.with(viewHolder.itemView.getContext()).load(otherUserInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ChatListViewHolder)viewHolder).itemchatlist_imageview_profile);
                        ((ChatListViewHolder)viewHolder).itemchatlist_textview_name.setText(otherUserInfo.getUserNicname());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                final int[] unreadCount = {0};
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(adapterChatRoomList.get(i).getRoomId()).child("messages").orderByChild("readUsers/"+CurrentUserInfo.getCurrentUserInfo().getId()).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot item : dataSnapshot.getChildren()){
                            unreadCount[0]++;
                        }
                        if(unreadCount[0]!= 0){
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setVisibility(View.VISIBLE);
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setText(String.valueOf(unreadCount[0]));
                        }else{
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{// 그룹 채팅일 때
                /*StorageReference image = FirebaseStorage.getInstance().getReference().child("icon_group.png");
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(viewHolder.itemView.getContext()).load(uri.toString()).apply(new RequestOptions().circleCrop()).into(((ChatListViewHolder)viewHolder).itemchatlist_imageview_profile);
                    }
                });*/
             /*   final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(msg.what == 50){
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_name.setText(msg.obj.toString());
                        }
                    }
                };
                final String[] names = {""};
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_name.setText("");
                for(String num : adapterChatRoomList.get(i).getUsers().keySet()){
                    if(adapterChatRoomList.get(i).getUsers().get(num)){
                        FirebaseDatabase.getInstance().getReference().child("users").child(num).child("userNicname").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String nic = (String) dataSnapshot.getValue();
                                if(names[0].equals("")){ //현재이름이 없을경우
                                    names[0] += nic;//이름추가
                                }else{//이름 있을 경우
                                    names[0] += ","+nic;//있는이름 + , + 이름추가.
                                    Message message = handler.obtainMessage(50,names[0]);
                                    handler.sendMessage(message);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        peopleNum++;
                    }
                }*/
                ((ChatListViewHolder)viewHolder).itemchatlist_imageview_profile.setImageResource(R.drawable.icon_group);
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_name.setText(adapterChatRoomList.get(i).getRoomName());
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_peoplenum.setText(adapterChatRoomList.get(i).getRoomPeopleNumber());


                final int[] groupUnreadCount = {0};
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(adapterChatRoomList.get(i).getRoomId()).child("messages").orderByChild("readUsers/"+CurrentUserInfo.getCurrentUserInfo().getId()).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot item : dataSnapshot.getChildren()){
                            groupUnreadCount[0]++;
                        }
                        if(groupUnreadCount[0] == 0){
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setVisibility(View.GONE);
                        }else{
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setVisibility(View.VISIBLE);
                            ((ChatListViewHolder)viewHolder).itemchatlist_textview_unreadcount.setText(String.valueOf(groupUnreadCount[0]));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            Map<String,ChatInfo> messages = new TreeMap<>(Collections.reverseOrder());
            if(adapterChatRoomList.get(i).getMessages().size()>0){
                messages.putAll(adapterChatRoomList.get(i).getMessages());
                if(messages.keySet().toArray().length > 0) {
                    String lastMessageKey = (String) messages.keySet().toArray()[0];
                    ((ChatListViewHolder)viewHolder).itemchatlist_textview_lastmessage.setText(adapterChatRoomList.get(i).getMessages().get(lastMessageKey).getMessage());
                    ((ChatListViewHolder)viewHolder).itemchatlist_textview_lastmessagetime.setText(adapterChatRoomList.get(i).getMessages().get(lastMessageKey).getCurrentTime());
                }
            }else{
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_lastmessage.setText("");
                ((ChatListViewHolder)viewHolder).itemchatlist_textview_lastmessagetime.setText("");
            }


            final String finalOtherUserId = otherUserId;
            final int finalPeopleNum = peopleNum;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//개인 채팅
                    if(adapterChatRoomList.get(i).getUsers().size()==2){
                        Intent intent = new Intent(v.getContext(),ChatRoomActivity.class);
                        intent.putExtra("otherUserId", finalOtherUserId);
                        Log.d("qwer","채팅목록 otheruserid "+finalOtherUserId);
                        startActivity(intent);
                    }
                    else{
                        //단체 채팅
                        Intent intent = new Intent(v.getContext(),GroupChatRoomActivity.class);
                        intent.putExtra("roomId",adapterChatRoomList.get(i).getRoomId());
                        intent.putExtra("roomNum",String.valueOf(adapterChatRoomList.get(i).getRoomPeopleNumber()));
                        startActivity(intent);
                    }

                }
            });


            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence[] item = {"채팅방 이름 변경","채팅방 나가기"};
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
                    adBuilder.setItems(item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 1){
                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(adapterChatRoomList.get(i).getRoomId()).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override//선택된 채팅방 유저들 가져옴
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Map<String,Boolean> users = new HashMap<>();
                                        users = (Map<String, Boolean>) dataSnapshot.getValue();
                                        users.put(CurrentUserInfo.getCurrentUserInfo().getId(),false);//내 채팅방 나감정보 수정
                                        Boolean roomEmpty = true;
                                        for(String key : users.keySet()){ //모든유저를 검색
                                            if(users.get(key)){//한명씩 검색해서 채팅방에 한명이상 들어와 있는 경우
                                                roomEmpty = false;//채팅방이 비어있지 않음
                                                break;
                                            }
                                        }
                                        if(roomEmpty){//채팅방이 비어있을 때
                                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(adapterChatRoomList.get(i).getRoomId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override//채팅방 삭제
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    notifyDataSetChanged();
                                                    Toast.makeText(getContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }else{//나만 나감 표시 설정
                                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(adapterChatRoomList.get(i).getRoomId()).child("users").setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(getContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                Intent intent = new Intent(getContext(),ModifyRoomNameActivity.class);
                                intent.putExtra("roomId",adapterChatRoomList.get(i).getRoomId());
                                intent.putExtra("roomName",adapterChatRoomList.get(i).getRoomName());
                                startActivity(intent);
                            }
                        }
                    }).show();
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterChatRoomList.size();
        }
    }
}
