package com.example.dnjsr.bakingcat;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
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
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.ChatInfo;
import com.example.dnjsr.bakingcat.info.ChatRoomInfo;
import com.example.dnjsr.bakingcat.info.NotificationInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.leibnik.chatimageview.ChatImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.ws.RealWebSocket;






public class ChatRoomActivity extends AppCompatActivity {
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    ImageView chatroom_image_selectoption;
    RecyclerView chatroom_recyclerview;
    EditText chatroom_edittext_message;
    Button chatroom_button_messagesend;
    ChatRoomAdapter chatRoomAdapter;
    List<ChatInfo> messageList;
    String roomId;
    String otherUserId;
    UserInfo otherUserInfo;
    ActionBar actionBar;
    DatabaseReference dbReference;
    ChildEventListener childListener;
    Handler handler;
    MediaPlayer mediaPlayer;
    final static int LOADING_HANDLER_NUM = 300;
    final static int SEND_GALLERY_IMAGE = 301;
    boolean existRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        otherUserId = getIntent().getStringExtra("otherUserId");
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        chatroom_recyclerview = findViewById(R.id.chatroom_recyclerview);
        chatroom_edittext_message = findViewById(R.id.chatroom_edittext_message);
        chatroom_button_messagesend = findViewById(R.id.chatroom_button_messagesend);
        chatroom_image_selectoption = findViewById(R.id.chatroom_image_selectoption);
        messageList = new ArrayList<>();


        FirebaseDatabase.getInstance().getReference().child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUserInfo = dataSnapshot.getValue(UserInfo.class);
                actionBar.setTitle(otherUserInfo.getUserNicname());
                Log.d("qwer","채팅방 otheruserid "+otherUserInfo.getId());
                chatroom_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                chatRoomAdapter = new ChatRoomAdapter((ArrayList<ChatInfo>) messageList);
                chatroom_recyclerview.setAdapter(chatRoomAdapter);
                checkRoom();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatroom_image_selectoption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = {"갤러리","음성메시지"};
                AlertDialog.Builder adBuilder =new AlertDialog.Builder(ChatRoomActivity.this);
                adBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                                imageIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                                imageIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                startActivityForResult(imageIntent,SEND_GALLERY_IMAGE);
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
        //checkRoom();




        chatroom_button_messagesend.setOnClickListener(new View.OnClickListener() { //보내기
            @Override
            public void onClick(View v) {
                if(!chatroom_edittext_message.getText().toString().equals("")){// 메시지가 있을 경우에만 보내짐

                    ChatInfo sendChatInfo = new ChatInfo();//채팅 객체 생성
                    sendChatInfo.setId(CurrentUserInfo.getCurrentUserInfo().getId());//아이디 생성 뷰 홀더에서 아이디 검사후 어떤 아이템 뷰를 사용할지 결정
                    sendChatInfo.setMessage(chatroom_edittext_message.getText().toString());//현재 채팅창에 있는 텍스트 채팅객체에 넣기
                    sendChatInfo.setCurrentTime(getCurrentTime());//현재 시간 넣기.
                    Map<String,Boolean> readUsers = new HashMap<>();
                    readUsers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                    readUsers.put(otherUserId,false);
                    sendChatInfo.setReadUsers(readUsers);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").push().setValue(sendChatInfo);

                    /*chatRoomAdapter.notifyDataSetChanged();
                    chatroom_recyclerview.scrollToPosition(messageList.size()-1);*/
                    //sendGcm();
                    chatroom_edittext_message.setText("");



                }
                else{
                    Toast.makeText(ChatRoomActivity.this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*chatroom_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        chatRoomAdapter = new ChatRoomAdapter((ArrayList<ChatInfo>) messageList);
        chatroom_recyclerview.setAdapter(chatRoomAdapter);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==RESULT_OK){
            if(requestCode == SEND_GALLERY_IMAGE){
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                final StorageReference saveImageReference = FirebaseStorage.getInstance().getReference().child("chatImages").child(timeStamp); //chatImages --날짜별로 만듦
                saveImageReference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {//storage에 업로드가 성공한다면
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        saveImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ChatInfo chatInfo = new ChatInfo();

                                chatInfo.setId(CurrentUserInfo.getCurrentUserInfo().getId());//아이디
                                chatInfo.setImageMessageUrl(uri.toString());//보낼 이미지
                                chatInfo.setCurrentTime(getCurrentTime());//보내는 시간
                                chatInfo.setMessage("사진");
                                Map<String,Boolean> readUsers = new HashMap<>();
                                readUsers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);//읽은사람
                                readUsers.put(otherUserId,false);
                                chatInfo.setReadUsers(readUsers);//읽은사람 셋팅

                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").push().setValue(chatInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        handler.sendEmptyMessage(LOADING_HANDLER_NUM);
                                    }
                                });
                            }
                        });

                    }
                });
            }
        }
    }

    /*void sendGcm() {

        Gson gson = new Gson();

        String userName = CurrentUserInfo.getCurrentUserInfo().getUserNicname();
        NotificationInfo notificationInfo = new NotificationInfo();
        notificationInfo.token = otherUserInfo.getUserPushToken();
        notificationInfo.notification.title = userName;
        notificationInfo.notification.text = chatroom_edittext_message.getText().toString();
        notificationInfo.data.title = userName;
        notificationInfo.data.text = chatroom_edittext_message.getText().toString();


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationInfo));
       *//* GoogleCredential googleCredential = null;
        try {
            googleCredential = GoogleCredential
                    .fromStream(new FileInputStream("service-account.json"))
                    .createScoped(Arrays.asList(SCOPES));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            googleCredential.refreshToken();
            Log.d("qwer",googleCredential.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }*//*
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAjFHeCeI:APA91bEiFq3ELD-8y8qEYvJVixYbU3BvF8qssblsnhjXCa8rZHzp1yOa4B5C_hRTTPDGEXXWZwhFlCMKrGAAKBM_t9lLkOptvdRIoqLOg94eyjL5-DOE7yG262CukzG2v8lMhyk65gnZ")
                //.addHeader("Authorization", "key=AIzaSyDPKsIpV-AOz2faSqiA9j7q-0WJhbsXngw")
                //.url("https://fcm.googleapis.com/fcm/send")
                .url("https://fcm.googleapis.com/v1/projects/androidstudioprojects-18905/messages:send")
                //.url("https://gcm-http.googleapis.com/gcm/send")
                .post(requestBody)
                .build();
        Log.d("qwer","전");
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("qwer","fail");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("qwer",response.toString());
                }
                Log.d("qwer",response.toString());
            }
        });
        Log.d("qwer","후");


    }*/


    private void getMessage() {
        /*dbReference = FirebaseDatabase.getInstance().getReference();
        listener = dbReference.child("chatrooms").child(roomId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot!= null){
                    messageList.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        ChatInfo message = item.getValue(ChatInfo.class);
                        String messagekey = item.getKey();
                        Map<String,Object> readUser = new HashMap<>();
                        readUser.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").child(messagekey).child("readUsers").updateChildren(readUser);
                        messageList.add(message);
                        chatRoomAdapter.notifyDataSetChanged();
                    }
                    chatroom_recyclerview.scrollToPosition(messageList.size()-1);
                    //chatRoomAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/


        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatInfo message = item.getValue(ChatInfo.class);
                    String messagekey = item.getKey();
                    Map<String,Object> readUser = new HashMap<>();
                    readUser.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                    message.getReadUsers().put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").child(messagekey).child("readUsers").updateChildren(readUser);
                    messageList.add(message);
                }

                chatroom_recyclerview.scrollToPosition(messageList.size()-1);
                handler.sendEmptyMessage(LOADING_HANDLER_NUM);
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
                if(msg.what == LOADING_HANDLER_NUM){
                    messageList.clear();
                    dbReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages");
                    childListener = dbReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            //Log.d("qwer",dataSnapshot.toString());
                            ChatInfo chatInfo = dataSnapshot.getValue(ChatInfo.class);
                            messageList.add(chatInfo);
                            chatroom_recyclerview.scrollToPosition(messageList.size()-1);
                            chatRoomAdapter.notifyDataSetChanged();
                            Map<String,Object> readUser = new HashMap<>();
                            readUser.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").child(dataSnapshot.getKey()).child("readUsers").updateChildren(readUser);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            //Log.d("qwer","change"+dataSnapshot.toString());
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    messageList.clear();
                                    for(DataSnapshot item : dataSnapshot.getChildren()){
                                        ChatInfo chatInfo = item.getValue(ChatInfo.class);
                                        messageList.add(chatInfo);
                                    }
                                    chatroom_recyclerview.scrollToPosition(messageList.size()-1);
                                    chatRoomAdapter.notifyDataSetChanged();
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

    @Override
    protected void onStop() {
        super.onStop();
        dbReference.removeEventListener(childListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void checkRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+CurrentUserInfo.getCurrentUserInfo().getId()).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {//내가 나간 방 검색
            @Override//나간방 체크
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {//내가 들어간 채팅방 모두 가져옴
                    ChatRoomInfo chatRoomInfo = item.getValue(ChatRoomInfo.class);
                    if (chatRoomInfo.getUsers().containsKey(otherUserId) && chatRoomInfo.getUsers().size() == 2) { //내가 들어간 채팅방중, 상대방이 있고 인원수 2일 경우
                        Log.d("qwer","내가나온 채팅방중 상대방있고 인원수 2인경우");
                        roomId = chatRoomInfo.getRoomId();//방 아이디 가져옴.
                        chatRoomInfo.getUsers().put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).setValue(chatRoomInfo);
                        getMessage();//메시지 갱신
                        Log.d("qwer","나간방 roomid");
                    }
                }



                if(roomId == null){//나갔던 방 체크했는데 채팅중인 방이없으면
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+CurrentUserInfo.getCurrentUserInfo().getId()).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override//들어가있는 방 체크
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot item : dataSnapshot.getChildren()){//내가 들어간 채팅방 모두 가져옴
                                ChatRoomInfo chatRoomInfo = item.getValue(ChatRoomInfo.class);
                                if(chatRoomInfo.getUsers().containsKey(otherUserId)&&chatRoomInfo.getUsers().size()==2){ //내가 들어간 채팅방중, 상대방이 있고 인원수 2일 경우
                                    Log.d("qwer","내가들어간 채팅방중 상대방있고 인원수가 2인경우");
                                    roomId = chatRoomInfo.getRoomId();//방 아이디 가져옴.
                                    getMessage();//메시지 갱신
                                }

                            }

                            if(roomId == null){//나와 친구의 채팅방이 없을 경우
                                ChatRoomInfo newChatRoom = new ChatRoomInfo(); //새로운 방 생성.
                                Map<String,Boolean> users = new HashMap<>();
                                users.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);//유저입력
                                users.put(otherUserId,true);//유저입력
                                newChatRoom.setUsers(users);//방객체 유저 셋팅
                                roomId = FirebaseDatabase.getInstance().getReference().child("chatrooms").push().getKey(); //방 id 가져옴
                                newChatRoom.setRoomId(roomId);//방아이디 셋팅
                                Log.d("qwer","나와 친구의 채팅방이 없을 경우");
                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).setValue(newChatRoom);
                                getMessage();//메시지 갱신
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


    public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<ChatInfo> adapterMessageList;

        public ChatRoomAdapter(ArrayList<ChatInfo> messageList) {
            this.adapterMessageList = messageList;

        }

        public class ChatRoomSendRecordViewHolder extends RecyclerView.ViewHolder{//보내는 이미지 뷰홀더
            ImageView sendrecorditem_imageview_message;
            TextView sendrecorditem_textview_messagetime;
            TextView sendimageitem_textview_unreadcount;
            LinearLayout sendrecorditem_linearlayout;

            public ChatRoomSendRecordViewHolder(@NonNull View itemView) {
                super(itemView);
                sendrecorditem_imageview_message  = itemView.findViewById(R.id.sendrecorditem_imageview_message);
                sendrecorditem_textview_messagetime  = itemView.findViewById(R.id.sendrecorditem_textview_messagetime);
                sendimageitem_textview_unreadcount  = itemView.findViewById(R.id.sendimageitem_textview_unreadcount);
                sendrecorditem_linearlayout  = itemView.findViewById(R.id.sendrecorditem_linearlayout);
            }
        }


        public class ChatRoomSendImageeViewHolder extends RecyclerView.ViewHolder{//보내는 이미지 뷰홀더
            ImageView sendimageitem_imageview_message;
            TextView sendimageitem_textview_messagetime;
            TextView sendimageitem_textview_unreadcount;

            public ChatRoomSendImageeViewHolder(@NonNull View itemView) {
                super(itemView);
                sendimageitem_imageview_message  = itemView.findViewById(R.id.sendimageitem_imageview_message);
                sendimageitem_textview_messagetime  = itemView.findViewById(R.id.sendimageitem_textview_messagetime);
                sendimageitem_textview_unreadcount  = itemView.findViewById(R.id.sendimageitem_textview_unreadcount);
            }
        }

        public class ChatRoomSendMessageViewHolder extends RecyclerView.ViewHolder{ //보내는 메시지 뷰홀더
            BubbleTextView itemsendmessage_textview_message;
            TextView itemsendmessage_textview_messagetime;
            TextView itemsendmessage_textview_unreadcount;

            public ChatRoomSendMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                itemsendmessage_textview_message  = itemView.findViewById(R.id.itemsendmessage_textview_message);
                itemsendmessage_textview_messagetime  = itemView.findViewById(R.id.itemsendmessage_textview_messagetime);
                itemsendmessage_textview_unreadcount  = itemView.findViewById(R.id.itemsendmessage_textview_unreadcount);
            }
        }

        public class ChatRoomReciveMessageViewHolder extends RecyclerView.ViewHolder{ //받는 메시지 뷰홀더
            ImageView recivemessageitem_imageview_profile;
            TextView recivemessageitem_textview_name;
            BubbleTextView  recivemessageitem_textview_message;
            TextView recivemessageitem_textview_messagetime;
            TextView recivemessageitem_textview_unreadcount;

            public ChatRoomReciveMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                recivemessageitem_imageview_profile =itemView.findViewById(R.id.recivemessageitem_imageview_profile);
                recivemessageitem_textview_name =itemView.findViewById(R.id.recivemessageitem_textview_name);
                recivemessageitem_textview_message =itemView.findViewById(R.id.recivemessageitem_textview_message);
                recivemessageitem_textview_messagetime =itemView.findViewById(R.id.recivemessageitem_textview_messagetime);
                recivemessageitem_textview_unreadcount =itemView.findViewById(R.id.recivemessageitem_textview_unreadcount);
            }
        }


        public class ChatRoomReciveImageViewHolder extends RecyclerView.ViewHolder{ //받는 이미지 뷰홀더
            ImageView reciveimageitem_imageview_profile;
            TextView reciveimageitem_textview_name;
            ImageView  reciveimageitem_imageview_message;
            TextView reciveimageitem_textview_messagetime;
            TextView reciveimageitem_textview_unreadcount;

            public ChatRoomReciveImageViewHolder(@NonNull View itemView) {
                super(itemView);
                reciveimageitem_imageview_profile =itemView.findViewById(R.id.reciveimageitem_imageview_profile);
                reciveimageitem_textview_name =itemView.findViewById(R.id.reciveimageitem_textview_name);
                reciveimageitem_imageview_message =itemView.findViewById(R.id.reciveimageitem_imageview_message);
                reciveimageitem_textview_messagetime =itemView.findViewById(R.id.reciveimageitem_textview_messagetime);
                reciveimageitem_textview_unreadcount =itemView.findViewById(R.id.reciveimageitem_textview_unreadcount);
            }
        }

        public class ChatRoomReciveRecordViewHolder extends RecyclerView.ViewHolder{ //받는 음성메시지 뷰홀더
            ImageView reciverecorditem_imageview_profile;
            TextView reciverecorditem_textview_name;
            ImageView  reciverecorditem_imageview_message;
            TextView reciverecorditem_textview_messagetime;
            TextView reciverecorditem_textview_unreadcount;
            LinearLayout reciverecorditem_linearlayout;

            public ChatRoomReciveRecordViewHolder(@NonNull View itemView) {
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
        public int getItemViewType(int position) { //position번재의 view타입을 결정
            if(adapterMessageList.get(position).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){//보내는 뷰타입
                if(!adapterMessageList.get(position).getImageMessageUrl().equals("empty")){ //이미지메시지 일  경우
                    return 0;
                }else if(!adapterMessageList.get(position).getRecordMessageUrl().equals("empty")){//음성메시지 일 경우
                    return 2;
                }
                else{//이미지가 아니라 메시지 일 경우
                    return 1;
                }

            }else{//받는 뷰타입
                if(!adapterMessageList.get(position).getImageMessageUrl().equals("empty")){ //이미지 메시지 일 경우
                    return 3;
                }else if(!adapterMessageList.get(position).getRecordMessageUrl().equals("empty")){//음성메시지 일 경우
                    return 5;
                }
                else{//메시지 일 경우
                    return 4;
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {//int i 는 viewtype을 뜨함.
            View view;
            switch (i){
                case 0:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendimage,viewGroup,false);
                    return new ChatRoomSendImageeViewHolder(view);
                case 1:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendmessage,viewGroup,false);
                    return new ChatRoomSendMessageViewHolder(view);
                case 2:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sendrecord,viewGroup,false);
                    return new ChatRoomSendRecordViewHolder(view);
                case 3:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reciveimage,viewGroup,false);
                    return new ChatRoomReciveImageViewHolder(view);
                case 4:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recivemessage,viewGroup,false);
                    return new ChatRoomReciveMessageViewHolder(view);
                case 5:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reciverecord,viewGroup,false);
                    return new ChatRoomReciveRecordViewHolder(view);
                default:
                    return null;
            }

        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) { //int i >>몇 번째 아이템
            if(adapterMessageList.get(i).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){ //메시지 보낼 때
                if(!adapterMessageList.get(i).getImageMessageUrl().equals("empty")){//이미지 메시지 일 때

                    Glide.with(viewHolder.itemView.getContext())
                            .load(adapterMessageList.get(i).getImageMessageUrl())
                            .into( ((ChatRoomSendImageeViewHolder)viewHolder).sendimageitem_imageview_message);//이미지넣기

                    ((ChatRoomSendImageeViewHolder)viewHolder).sendimageitem_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());//메시지 시간 넣기

                    if(adapterMessageList.get(i).getReadUsers().get(otherUserId)==false){// 읽은사람이 false라면
                        ((ChatRoomSendImageeViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("1");//안읽음표시
                    }else{
                        ((ChatRoomSendImageeViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("");//읽은표시
                    }
                    ((ChatRoomSendImageeViewHolder)viewHolder).sendimageitem_imageview_message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(),ImageActivity.class);
                            intent.putExtra("imageUrl",adapterMessageList.get(i).getImageMessageUrl());
                            intent.putExtra("userId",adapterMessageList.get(i).getId());
                            startActivity(intent);
                        }
                    });

                }else if(!adapterMessageList.get(i).getRecordMessageUrl().equals("empty")){ //보내는 메시지가 음성 메시지 일 때
                    ((ChatRoomSendRecordViewHolder)viewHolder).sendrecorditem_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());//보낸 음성메시지 시간
                    ((ChatRoomSendRecordViewHolder)viewHolder).sendrecorditem_linearlayout.setOnClickListener(new View.OnClickListener() {//음성메시지 듣기 눌렀을 때.
                        @Override
                        public void onClick(View v) {
                            ((ChatRoomSendRecordViewHolder)viewHolder).sendrecorditem_imageview_message.setImageResource(R.drawable.icon_hearing);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(adapterMessageList.get(i).getRecordMessageUrl());
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
                                    ((ChatRoomSendRecordViewHolder)viewHolder).sendrecorditem_imageview_message.setImageResource(R.drawable.icon_play);
                                }
                            });
                        }
                    });
                    if(!adapterMessageList.get(i).getReadUsers().get(otherUserId)){
                        ((ChatRoomSendRecordViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("1");
                    }else{
                        ((ChatRoomSendRecordViewHolder)viewHolder).sendimageitem_textview_unreadcount.setText("");
                    }
                }
                else{//텍스트 메시지 일 때
                    ((ChatRoomSendMessageViewHolder)viewHolder).itemsendmessage_textview_message.setText(adapterMessageList.get(i).getMessage());
                    ((ChatRoomSendMessageViewHolder)viewHolder).itemsendmessage_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());
                    if(adapterMessageList.get(i).getReadUsers().get(otherUserId)==false){
                        ((ChatRoomSendMessageViewHolder)viewHolder).itemsendmessage_textview_unreadcount.setText("1");
                    }else{
                        ((ChatRoomSendMessageViewHolder)viewHolder).itemsendmessage_textview_unreadcount.setText("");
                    }
                }
            }
            else{//메시지 받을 때
                if(!adapterMessageList.get(i).getImageMessageUrl().equals("empty")){//이미지 메시지 일 때
                    Glide.with(viewHolder.itemView.getContext())
                            .load(otherUserInfo.getUserProfileImageUri())
                            .apply(new RequestOptions().circleCrop())
                            .into(((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_imageview_profile);

                    Glide.with(viewHolder.itemView.getContext()).load(adapterMessageList.get(i).getImageMessageUrl()).into(((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_imageview_message);
                    ((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_textview_name.setText(otherUserInfo.getUserNicname());
                    ((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());
                    ((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_textview_unreadcount.setVisibility(View.GONE);

                    ((ChatRoomReciveImageViewHolder)viewHolder).reciveimageitem_imageview_message.setOnClickListener(new View.OnClickListener() { //이미지 클릭시
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(),ImageActivity.class);
                            intent.putExtra("imageUrl",adapterMessageList.get(i).getImageMessageUrl());
                            intent.putExtra("userId",adapterMessageList.get(i).getId());
                            startActivity(intent);
                        }
                    });


                }else if(!adapterMessageList.get(i).getRecordMessageUrl().equals("empty")){ //받는 음성메시지 일 때.
                    Glide.with(viewHolder.itemView.getContext())
                            .load(otherUserInfo.getUserProfileImageUri())
                            .apply(new RequestOptions().circleCrop())
                            .into(((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_profile);

                    ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_textview_name.setText(otherUserInfo.getUserNicname());
                    ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());
                    if(!adapterMessageList.get(i).getReadUsers().get(otherUserId)){
                        ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_textview_unreadcount.setText("1");
                    }else{
                        ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_textview_unreadcount.setText("");
                    }
                    ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_linearlayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_message.setImageResource(R.drawable.icon_hearing);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(adapterMessageList.get(i).getRecordMessageUrl());
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
                                    ((ChatRoomReciveRecordViewHolder)viewHolder).reciverecorditem_imageview_message.setImageResource(R.drawable.icon_play);
                                }
                            });
                        }
                    });
                }
                else{//텍스트 메시지 일 때
                    Glide.with(viewHolder.itemView.getContext()).load(otherUserInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ChatRoomReciveMessageViewHolder)viewHolder).recivemessageitem_imageview_profile);
                    ((ChatRoomReciveMessageViewHolder)viewHolder).recivemessageitem_textview_message.setText(adapterMessageList.get(i).getMessage());
                    ((ChatRoomReciveMessageViewHolder)viewHolder).recivemessageitem_textview_name.setText(otherUserInfo.getUserNicname());
                    ((ChatRoomReciveMessageViewHolder)viewHolder).recivemessageitem_textview_messagetime.setText(adapterMessageList.get(i).getCurrentTime());
                    ((ChatRoomReciveMessageViewHolder)viewHolder).recivemessageitem_textview_unreadcount.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return adapterMessageList.size();
        }



    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.sendEmptyMessage(LOADING_HANDLER_NUM);
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
