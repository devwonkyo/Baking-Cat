package com.example.dnjsr.bakingcat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmStore;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.ChatInfo;
import com.example.dnjsr.bakingcat.info.ChatRoomInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatRoomActivity extends AppCompatActivity {
    ActionBar actionBar;
    ImageView groupchatroom_image_option;
    RecyclerView groupchatroom_recyclerview;
    EditText groupchatroom_edittext_message;
    Button groupchatroom_button_messagesend;
    List<ChatInfo> allChatList = new ArrayList<>();
    List<String> otherUsersId = new ArrayList<>();
    List<String> userNicnameList = new ArrayList<>();
    GroupChatRoomAdapter groupChatRoomAdapter;
    String roomId;
    Handler handler;
    DatabaseReference dbReference;
    ChildEventListener childListener;
    MediaPlayer mediaPlayer;
    ChatRoomInfo chatRoomInfo;
    String roomNum;
    final static int REFRESH_CHATTING = 500;
    final static int SEND_GALLERY_IMAGE_GROUP = 501;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_room);
        actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        groupchatroom_recyclerview = findViewById(R.id.groupchatroom_recyclerview);
        groupchatroom_image_option = findViewById(R.id.groupchatroom_image_option);
        groupchatroom_edittext_message = findViewById(R.id.groupchatroom_edittext_message);
        groupchatroom_button_messagesend = findViewById(R.id.groupchatroom_button_messagesend);
        roomId = getIntent().getStringExtra("roomId");
        roomNum = getIntent().getStringExtra("roomNum");
        setRoomInfo();
        actionBar.setTitle("그룹채팅 "+ roomNum);
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue(); //방안의 유저리스트를 가져옴
                for(String otherUserId : users.keySet()){//아이디만 빼서 리스트에 넣음.
                    otherUsersId.add(otherUserId);
                }
                groupChatRoomAdapter = new GroupChatRoomAdapter(allChatList);
                groupchatroom_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                groupchatroom_recyclerview.setAdapter(groupChatRoomAdapter);
                setChatting();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        updateRoomName();

        groupchatroom_button_messagesend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupchatroom_edittext_message.getText().toString().equals("")){
                    Toast.makeText(GroupChatRoomActivity.this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(CurrentUserInfo.getCurrentUserInfo().getId());
                    chatInfo.setMessage(groupchatroom_edittext_message.getText().toString());
                    chatInfo.setCurrentTime(getCurrentTime());
                    Map<String,Boolean> readusers = new HashMap<>();
                    for(String userId : otherUsersId){
                        readusers.put(userId,false);//다른 유저 읽은 사람 리스트에  안읽었다고 표시 후 넣음
                    }
                    readusers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);//내 아이디는 읽었다고 표시후 넣음.
                    chatInfo.setReadUsers(readusers);

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").push().setValue(chatInfo);

                    groupchatroom_edittext_message.setText("");
                    groupchatroom_recyclerview.scrollToPosition(allChatList.size()-1);
                }

            }
        });

        groupchatroom_image_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = {"갤러리","음성메시지"};
                AlertDialog.Builder adBuilder =new AlertDialog.Builder(GroupChatRoomActivity.this);
                adBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                                imageIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                                imageIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                startActivityForResult(imageIntent,SEND_GALLERY_IMAGE_GROUP);
                                break;
                            case 1:
                                Intent recordIntent = new Intent(getApplicationContext(),RecordVoiceActivity.class);
                                recordIntent.putExtra("roomId",roomId);
                                startActivity(recordIntent);
                                break;
                        }
                    }
                }).show();
            }
        });


    }

    public void setRoomInfo(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomInfo = dataSnapshot.getValue(ChatRoomInfo.class);
                if(chatRoomInfo.getCustomRoomName()){
                    actionBar.setTitle(chatRoomInfo.getRoomName()+" "+roomNum);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode ==SEND_GALLERY_IMAGE_GROUP){
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                final StorageReference sendGroupMessageImage = FirebaseStorage.getInstance().getReference().child("chatImages").child(timeStamp);
                sendGroupMessageImage.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        sendGroupMessageImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ChatInfo chatInfo = new ChatInfo();
                                chatInfo.setId(CurrentUserInfo.getCurrentUserInfo().getId());//아이디
                                chatInfo.setImageMessageUrl(uri.toString());//보낼 이미지
                                chatInfo.setCurrentTime(getCurrentTime());//보내는 시간
                                chatInfo.setMessage("사진");
                                Map<String,Boolean> readUsers = new HashMap<>();
                                readUsers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);//읽은사람
                                for(String otherUser : otherUsersId){
                                    readUsers.put(otherUser,false);
                                }
                                chatInfo.setReadUsers(readUsers);//읽은사람 셋팅

                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").push().setValue(chatInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        handler.sendEmptyMessage(REFRESH_CHATTING);
                                    }
                                });
                            }
                        });

                    }
                });

            }
        }
    }

    public void setChatting(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allChatList.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    ChatInfo chatInfo = item.getValue(ChatInfo.class);
                    String messageKey = item.getKey();
                    //allChatList.add(chatInfo);
                    Map<String,Object> readuser = new HashMap<>();
                    readuser.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").child(messageKey).child("readUsers").updateChildren(readuser);
                }
                handler.sendEmptyMessage(REFRESH_CHATTING);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void updateRoomName(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,Boolean> users = (HashMap<String, Boolean>) dataSnapshot.getValue();
                for(String id : users.keySet()){
                    if(users.get(id).equals(true)){
                        FirebaseDatabase.getInstance().getReference().child("users").child(id).child("userNicname").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String nicName = (String) dataSnapshot.getValue();
                                userNicnameList.add(nicName);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                allChatList.clear();
                if(msg.what == REFRESH_CHATTING){
                    dbReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages");
                    childListener = dbReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                            allChatList.add(chatInfo);
                            Map<String,Object> readUser = new HashMap<>();
                            readUser.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").child(dataSnapshot.getKey()).child("readUsers").updateChildren(readUser);
                            groupChatRoomAdapter.notifyDataSetChanged();
                            groupchatroom_recyclerview.scrollToPosition(groupChatRoomAdapter.getItemCount()-1);

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    allChatList.clear();
                                    for(DataSnapshot item : dataSnapshot.getChildren()){
                                        ChatInfo chatInfo = item.getValue(ChatInfo.class);
                                        allChatList.add(chatInfo);
                                    }
                                    groupChatRoomAdapter.notifyDataSetChanged();
                                    groupchatroom_recyclerview.scrollToPosition(groupChatRoomAdapter.getItemCount()-1);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
    }

    public String getCurrentTime(){                             //날짜 설정
        Calendar currentCalendar = Calendar.getInstance(); //캘린더객체 사용하여 시간 받아오기.
        String currentTime;
        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCalendar.get(Calendar.MINUTE);
        if(currentHour>11){ // 12시 이후는 오후
            currentTime = "오후";
            currentHour -= 12;
        }
        else{//12전은 오전
            currentTime = "오전";
        }
        if(currentMinute<10) { //분 단위가 한자리수 일때 두자리로 표현
            currentTime = currentTime+currentHour+":0"+currentMinute;
        }else{
            currentTime = currentTime+currentHour+":"+currentMinute;
        }
        return currentTime;
    }


    public class GroupChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<ChatInfo> adapterChatList;
        public GroupChatRoomAdapter(List<ChatInfo> allChatList) {
            adapterChatList = allChatList;
        }


        public class SendImageeViewHolder extends RecyclerView.ViewHolder{//보내는 이미지 뷰홀더
            ImageView sendimageitem_imageview_message;
            TextView sendimageitem_textview_messagetime;
            TextView sendimageitem_textview_unreadcount;

            public SendImageeViewHolder(@NonNull View itemView) {
                super(itemView);
                sendimageitem_imageview_message  = itemView.findViewById(R.id.sendimageitem_imageview_message);
                sendimageitem_textview_messagetime  = itemView.findViewById(R.id.sendimageitem_textview_messagetime);
                sendimageitem_textview_unreadcount  = itemView.findViewById(R.id.sendimageitem_textview_unreadcount);
            }
        }

        public class SendMessageViewHolder extends RecyclerView.ViewHolder{
            BubbleTextView itemsendmessage_textview_message;
            TextView itemsendmessage_textview_messagetime;
            TextView itemsendmessage_textview_unreadcount;
            public SendMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                itemsendmessage_textview_message  = itemView.findViewById(R.id.itemsendmessage_textview_message);
                itemsendmessage_textview_messagetime  = itemView.findViewById(R.id.itemsendmessage_textview_messagetime);
                itemsendmessage_textview_unreadcount  = itemView.findViewById(R.id.itemsendmessage_textview_unreadcount);
            }
        }

        public class SendRecordViewHolder extends RecyclerView.ViewHolder{
            ImageView sendrecorditem_imageview_message;
            TextView sendrecorditem_textview_messagetime;
            TextView sendimageitem_textview_unreadcount;
            LinearLayout sendrecorditem_linearlayout;
            public SendRecordViewHolder(@NonNull View itemView) {
                super(itemView);
                sendrecorditem_textview_messagetime  = itemView.findViewById(R.id.sendrecorditem_textview_messagetime);
                sendimageitem_textview_unreadcount  = itemView.findViewById(R.id.sendimageitem_textview_unreadcount);
                sendrecorditem_imageview_message  = itemView.findViewById(R.id.sendrecorditem_imageview_message);
                sendrecorditem_linearlayout  = itemView.findViewById(R.id.sendrecorditem_linearlayout);
            }
        }


        public class ReciveMessageViewHolder extends RecyclerView.ViewHolder{
            ImageView recivemessageitem_imageview_profile;
            TextView recivemessageitem_textview_name;
            BubbleTextView  recivemessageitem_textview_message;
            TextView recivemessageitem_textview_messagetime;
            TextView recivemessageitem_textview_unreadcount;
            public ReciveMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                recivemessageitem_imageview_profile =itemView.findViewById(R.id.recivemessageitem_imageview_profile);
                recivemessageitem_textview_name =itemView.findViewById(R.id.recivemessageitem_textview_name);
                recivemessageitem_textview_message =itemView.findViewById(R.id.recivemessageitem_textview_message);
                recivemessageitem_textview_messagetime =itemView.findViewById(R.id.recivemessageitem_textview_messagetime);
                recivemessageitem_textview_unreadcount =itemView.findViewById(R.id.recivemessageitem_textview_unreadcount);
            }
        }

        public class ReciveImageViewHolder extends RecyclerView.ViewHolder{ //받는 이미지 뷰홀더
            ImageView reciveimageitem_imageview_profile;
            TextView reciveimageitem_textview_name;
            ImageView  reciveimageitem_imageview_message;
            TextView reciveimageitem_textview_messagetime;
            TextView reciveimageitem_textview_unreadcount;

            public ReciveImageViewHolder(@NonNull View itemView) {
                super(itemView);
                reciveimageitem_imageview_profile =itemView.findViewById(R.id.reciveimageitem_imageview_profile);
                reciveimageitem_textview_name =itemView.findViewById(R.id.reciveimageitem_textview_name);
                reciveimageitem_imageview_message =itemView.findViewById(R.id.reciveimageitem_imageview_message);
                reciveimageitem_textview_messagetime =itemView.findViewById(R.id.reciveimageitem_textview_messagetime);
                reciveimageitem_textview_unreadcount =itemView.findViewById(R.id.reciveimageitem_textview_unreadcount);
            }
        }

        public class ReciveRecordViewHolder extends RecyclerView.ViewHolder{ //받는 이미지 뷰홀더
            ImageView reciverecorditem_imageview_profile;
            TextView reciverecorditem_textview_name;
            ImageView  reciverecorditem_imageview_message;
            TextView reciverecorditem_textview_messagetime;
            TextView reciverecorditem_textview_unreadcount;
            LinearLayout reciverecorditem_linearlayout;

            public ReciveRecordViewHolder(@NonNull View itemView) {
                super(itemView);
                reciverecorditem_imageview_profile =itemView.findViewById(R.id.reciverecorditem_imageview_profile);
                reciverecorditem_textview_name =itemView.findViewById(R.id.reciverecorditem_textview_name);
                reciverecorditem_imageview_message =itemView.findViewById(R.id.reciverecorditem_imageview_message);
                reciverecorditem_textview_messagetime =itemView.findViewById(R.id.reciverecorditem_textview_messagetime);
                reciverecorditem_textview_unreadcount =itemView.findViewById(R.id.reciverecorditem_textview_unreadcount);
                reciverecorditem_linearlayout =itemView.findViewById(R.id.reciverecorditem_linearlayout);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(adapterChatList.get(position).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){ //내가 보낸 메시지일 때
                if(!adapterChatList.get(position).getImageMessageUrl().equals("empty")){ //이미지메시지가 들어있을 때 -->이미지 메시지일 때
                    return 0;
                }else if(!adapterChatList.get(position).getRecordMessageUrl().equals("empty")){//이미지 메시지가 아니고,음성메시지일 때
                    return 2;
                }else{//텍스트 메시지 일 때.
                    return 1;
                }
            }else{// 내가 받은 메시지 일 때
                if(!adapterChatList.get(position).getImageMessageUrl().equals("empty")){ //받은 메시지가 이미지 메시지일 때.
                    return 3;
                }else if(!adapterChatList.get(position).getRecordMessageUrl().equals("empty")){//받은 메시지가 이미지 메시지가 아니고,음성메시지일 때
                    return 5;
                }else{//텍스트 메시지 일 때.
                    return 4;
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view;
            switch (i){
                case 0: //내가 보낸 이미지 메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendimage,viewGroup,false);
                    return new SendImageeViewHolder(view);

                case 1://내가 보낸 텍스트 메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendmessage,viewGroup,false);
                    return new SendMessageViewHolder(view);

                case 2://내가 보낸 음성메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendrecord,viewGroup,false);
                    return new SendRecordViewHolder(view);

                case 3://내가 받은 이미지 메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reciveimage,viewGroup,false);
                    return new ReciveImageViewHolder(view);

                case 4://내가 받은 텍스트 메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recivemessage,viewGroup,false);
                    return new ReciveMessageViewHolder(view);

                case 5://내가 받은 음성 메시지
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reciverecord,viewGroup,false);
                    return new ReciveRecordViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            int readcount = 0;
            if(adapterChatList.get(i).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){//내가 보낸 메시지일 때
                if(!adapterChatList.get(i).getImageMessageUrl().equals("empty")){//이미지메시지 일 때

                    Glide.with(viewHolder.itemView.getContext()).load(adapterChatList.get(i).getImageMessageUrl()).into(((SendImageeViewHolder)viewHolder).sendimageitem_imageview_message);//이미지 셋팅
                    ((SendImageeViewHolder)viewHolder).sendimageitem_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());//이미지메시지 시간 셋팅
                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){//이미지메시지 읽은 사람 숫자 셋팅
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount == 0){
                        ((SendImageeViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("");
                    }else{
                        ((SendImageeViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText(String.valueOf(readcount));
                    }

                    ((SendImageeViewHolder)viewHolder).sendimageitem_imageview_message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //이미지를 클릭 했을 때.
                            Intent intent = new Intent(v.getContext(),ImageActivity.class);
                            intent.putExtra("imageUrl",adapterChatList.get(i).getImageMessageUrl());
                            intent.putExtra("userId",adapterChatList.get(i).getId());
                            startActivity(intent);
                        }
                    });

                }else if(!adapterChatList.get(i).getRecordMessageUrl().equals("empty")){//내가 보낸 음성메시지 일 때.
                    ((SendRecordViewHolder)viewHolder).sendrecorditem_linearlayout.setOnClickListener(new View.OnClickListener() {//재생을 눌럿을 때.
                        @Override
                        public void onClick(View v) {
                            ((SendRecordViewHolder)viewHolder).sendrecorditem_imageview_message.setImageResource(R.drawable.icon_hearing);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(adapterChatList.get(i).getRecordMessageUrl());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.start();

                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                    ((SendRecordViewHolder)viewHolder).sendrecorditem_imageview_message.setImageResource(R.drawable.icon_play);
                                }
                            });
                        }
                    });

                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){//안읽은사람 계산
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount==0){
                        ((SendRecordViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("");//모두 읽었을 때.
                    }else{
                        ((SendRecordViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText(String.valueOf(readcount));//안읽은 사람 수.
                    }
                    ((SendRecordViewHolder)viewHolder).sendrecorditem_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());//음성메시지 보낸 시간 셋팅.

                }
                else{//텍스트 메시지 일 때

                    ((SendMessageViewHolder)viewHolder).itemsendmessage_textview_message.setText(adapterChatList.get(i).getMessage()); //텍스트메시지 셋팅
                    ((SendMessageViewHolder)viewHolder).itemsendmessage_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());//텍스트 시간 셋팅
                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){ //안읽은사람 계산.
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount == 0){//텍스트 읽은 횟수 셋팅
                        ((SendMessageViewHolder)viewHolder).itemsendmessage_textview_unreadcount.setText(""); //모두 읽었을 경우
                    }else{
                        ((SendMessageViewHolder)viewHolder).itemsendmessage_textview_unreadcount.setText(String.valueOf(readcount));
                    }

                }
            }else{//내가 받은 메시지 일 때.
                if(!adapterChatList.get(i).getImageMessageUrl().equals("empty")){//받은 이미지 메시지 일 때

                    FirebaseDatabase.getInstance().getReference().child("users").child(adapterChatList.get(i).getId()).addListenerForSingleValueEvent(new ValueEventListener() {//이미지 메시지의 프로필 닉네임.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                            Glide.with(getApplicationContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ReciveImageViewHolder)viewHolder).reciveimageitem_imageview_profile);
                            ((ReciveImageViewHolder)viewHolder).reciveimageitem_textview_name.setText(userInfo.getUserNicname());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Glide.with(viewHolder.itemView.getContext()).load(adapterChatList.get(i).getImageMessageUrl()).into(((ReciveImageViewHolder)viewHolder).reciveimageitem_imageview_message);
                    ((ReciveImageViewHolder)viewHolder).reciveimageitem_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());//받은 이미지 메시지시간 셋팅
                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){ //받은 이미지 메시지읽은 사람.
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount == 0){
                        ((ReciveImageViewHolder)viewHolder).reciveimageitem_textview_unreadcount.setText("");
                    }else{
                        ((ReciveImageViewHolder)viewHolder).reciveimageitem_textview_unreadcount.setText(String.valueOf(readcount));
                    }

                    ((ReciveImageViewHolder)viewHolder).reciveimageitem_imageview_message.setOnClickListener(new View.OnClickListener() { //이미지를 클릭 했을 때
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(),ImageActivity.class);
                            intent.putExtra("imageUrl",adapterChatList.get(i).getImageMessageUrl());
                            intent.putExtra("userId",adapterChatList.get(i).getId());
                            startActivity(intent);
                        }
                    });

                }else if(!adapterChatList.get(i).getRecordMessageUrl().equals("empty")){//받은 음성메시지 일때.
                    FirebaseDatabase.getInstance().getReference().child("users").child(adapterChatList.get(i).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //닉네임 , 프로필사진 데이터베이스에서 가져와서 설정.
                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                            Glide.with(viewHolder.itemView.getContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_profile);
                            ((ReciveRecordViewHolder)viewHolder).reciverecorditem_textview_name.setText(userInfo.getUserNicname());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    ((ReciveRecordViewHolder)viewHolder).reciverecorditem_linearlayout.setOnClickListener(new View.OnClickListener() {//듣기버튼 눌렀을 경우.
                        @Override
                        public void onClick(View v) {
                            ((ReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_message.setImageResource(R.drawable.icon_hearing);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(adapterChatList.get(i).getRecordMessageUrl());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.start();

                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                    ((ReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_message.setImageResource(R.drawable.icon_play);
                                }
                            });
                        }
                    });

                    ((ReciveRecordViewHolder)viewHolder).reciverecorditem_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());
                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount == 0){
                        ((ReciveRecordViewHolder)viewHolder).reciverecorditem_textview_unreadcount.setText("");
                    }else{
                        ((ReciveRecordViewHolder)viewHolder).reciverecorditem_textview_unreadcount.setText(String.valueOf(readcount));
                    }


                }
                else{//받은 텍스트 메시지 일 때.

                    FirebaseDatabase.getInstance().getReference().child("users").child(adapterChatList.get(i).getId()).addListenerForSingleValueEvent(new ValueEventListener() {//텍스트 메시지의 프로필 닉네임.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                            Glide.with(getApplicationContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ReciveMessageViewHolder)viewHolder).recivemessageitem_imageview_profile);
                            ((ReciveMessageViewHolder)viewHolder).recivemessageitem_textview_name.setText(userInfo.getUserNicname());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    ((ReciveMessageViewHolder)viewHolder).recivemessageitem_textview_message.setText(adapterChatList.get(i).getMessage()); //받은 텍스트 메시지 셋팅
                    ((ReciveMessageViewHolder)viewHolder).recivemessageitem_textview_messagetime.setText(adapterChatList.get(i).getCurrentTime());//받은 텍스트 메시지시간 셋팅
                    for(String id : adapterChatList.get(i).getReadUsers().keySet()){ //받은 텍스트 메시지읽은 사람.
                        if(!adapterChatList.get(i).getReadUsers().get(id)){
                            readcount++;
                        }
                    }
                    if(readcount == 0){
                        ((ReciveMessageViewHolder)viewHolder).recivemessageitem_textview_unreadcount.setText("");
                    }else{
                        ((ReciveMessageViewHolder)viewHolder).recivemessageitem_textview_unreadcount.setText(String.valueOf(readcount));
                    }

                }
            }
        }

        @Override
        public int getItemCount() {
            return adapterChatList.size();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        dbReference.removeEventListener(childListener);
        String roomName = "";
        for(String userNicname : userNicnameList){
            if(roomName.equals("")){
                roomName +=userNicname;
            }else{
                roomName +=","+userNicname;
            }
        }
        Map<String,Object> updateRoom = new HashMap<>();
        if(!chatRoomInfo.getCustomRoomName()){ //방 이름을 정하지 않았을 경우에만.
            updateRoom.put("roomName",roomName); //대화방애들이름으로 설정.
        }
        updateRoom.put("roomPeopleNumber",String.valueOf(userNicnameList.size()));
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).updateChildren(updateRoom);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.sendEmptyMessage(REFRESH_CHATTING);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_userlist,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.action_showuserlist:
                Intent intent = new Intent(getApplicationContext(),ChatPeopleListActivity.class);
                intent.putExtra("roomId",roomId);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
