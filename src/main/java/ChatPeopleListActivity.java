package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatPeopleListActivity extends AppCompatActivity {
    String roomId;
    List<String> chatUserId = new ArrayList<>();
    List<UserInfo> chatUserList = new ArrayList<>();
    Handler handler;
    final static int CALL_USERLIST = 50;
    RecyclerView chatpeoplelist_recyclerview;
    ChatPeopleListAdapter chatPeopleListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_people_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        chatpeoplelist_recyclerview = findViewById(R.id.chatpeoplelist_recyclerview);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("대화상대");
        actionBar.setDisplayHomeAsUpEnabled(true);
        roomId = getIntent().getStringExtra("roomId");
        getUserIdList();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == CALL_USERLIST){
                    getUserList();
                }
            }
        };
        chatPeopleListAdapter = new ChatPeopleListAdapter(chatUserList);
        chatpeoplelist_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatpeoplelist_recyclerview.setAdapter(chatPeopleListAdapter);
    }

    public class ChatPeopleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserInfo> adapterChatUserList;
        public ChatPeopleListAdapter(List<UserInfo> chatUserList) {
            adapterChatUserList = chatUserList;
        }

        public class ChatPeopleListViewHolder extends RecyclerView.ViewHolder{
            ImageView itemlikepeople_imageview_profile;
            TextView itemlikepeople_textview_me;
            TextView itemlikepeople_textview_name;

            public ChatPeopleListViewHolder(@NonNull View itemView) {
                super(itemView);
                itemlikepeople_imageview_profile = itemView.findViewById(R.id.itemlikepeople_imageview_profile);
                itemlikepeople_textview_me = itemView.findViewById(R.id.itemlikepeople_textview_me);
                itemlikepeople_textview_name = itemView.findViewById(R.id.itemlikepeople_textview_name);
            }
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_likepeople,viewGroup,false);
            return new ChatPeopleListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder,final int i) {
            if(adapterChatUserList.get(i).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){
                ((ChatPeopleListViewHolder)viewHolder).itemlikepeople_textview_me.setVisibility(View.VISIBLE);
            }else{
                ((ChatPeopleListViewHolder)viewHolder).itemlikepeople_textview_me.setVisibility(View.INVISIBLE);
            }
            ((ChatPeopleListViewHolder)viewHolder).itemlikepeople_textview_name.setText(adapterChatUserList.get(i).getUserNicname());
            Glide.with(viewHolder.itemView.getContext())
                    .load(adapterChatUserList.get(i).getUserProfileImageUri())
                    .apply(new RequestOptions().circleCrop()).into(((ChatPeopleListViewHolder)viewHolder).itemlikepeople_imageview_profile);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
                    intent.putExtra("userId",adapterChatUserList.get(i).getId());
                    startActivity(intent);
                    finish();
                }
            });

        }

        @Override
        public int getItemCount() {
            return adapterChatUserList.size();
        }
    }


    public void getUserIdList(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,Boolean> userList = (Map<String, Boolean>) dataSnapshot.getValue();
                for(String userId : userList.keySet()){
                    chatUserId.add(userId);
                }
                handler.sendEmptyMessage(CALL_USERLIST);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getUserList(){
        for(int i = 0 ; i <chatUserId.size(); i++){
            final String userId = chatUserId.get(i);
            FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    chatUserList.add(userInfo);
                    chatPeopleListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
