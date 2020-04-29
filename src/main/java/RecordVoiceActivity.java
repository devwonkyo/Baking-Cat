package com.example.dnjsr.bakingcat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.ChatInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordVoiceActivity extends AppCompatActivity {
    ImageButton recordvoice_imageview_recording;
    MediaRecorder recorder;
    TextView recordvoice_textview_recording;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    ProgressDialog progressDialog;
    String roomId;//음성메시지 보낼 채팅방 아이디
    List<String> userList = new ArrayList<>();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
    public void setUserList(){//채팅방에 보낼 유저리스트 , 개인 읽음 처리 하기 위해 아이디 가져옴
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue(); //방안의 유저리스트를 가져옴
                for(String otherUserId : users.keySet()){//아이디만 빼서 리스트에 넣음.
                    userList.add(otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        roomId = getIntent().getStringExtra("roomId");
        setUserList();
        progressDialog = new ProgressDialog(this);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        recordvoice_imageview_recording = findViewById(R.id.recordvoice_imageview_recording);
        recordvoice_textview_recording = findViewById(R.id.recordvoice_textview_recording);
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/recorded_audio.3gp";



        recordvoice_imageview_recording.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == event.ACTION_DOWN){
                    startRecording();
                    recordvoice_imageview_recording.setImageResource(R.drawable.icon_useingmic);
                    recordvoice_textview_recording.setText("녹음중 입니다..");
                }else if(event.getAction() == event.ACTION_UP){
                    stopRecording();
                    recordvoice_imageview_recording.setImageResource(R.drawable.icon_nonemic);
                    recordvoice_textview_recording.setText("녹음 완료.");
                }
                return false;
            }
        });

    }


    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {

        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        uploadingRecord();
    }


    public void uploadingRecord(){
        progressDialog.setMessage("음성메시지를 전송중입니다...");
        progressDialog.show();
        Uri uri = Uri.fromFile(new File(fileName)); //파일 경로 uri

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//날짜로 이름 설정
        final StorageReference storagePath = FirebaseStorage.getInstance().getReference().child("chatRecordes").child(timeStamp+"_audio.3gp"); //storage경로 설정
        storagePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {//경로에 파일 올림
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {//파일 올리기 성공한다면
                storagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {// uri올린 다운로드 url을 가져옴.
                    @Override
                    public void onSuccess(Uri uri) {
                        String recordUrl = uri.toString();
                        ChatInfo chatInfo = new ChatInfo();
                        chatInfo.setMessage("음성메시지");
                        chatInfo.setId(CurrentUserInfo.getCurrentUserInfo().getId());
                        chatInfo.setCurrentTime(getCurrentTime());
                        Map<String,Boolean> readUsers = new HashMap<>();
                        readUsers.put(CurrentUserInfo.getCurrentUserInfo().getId(),true);
                        for(String otherUserId : userList){
                            readUsers.put(otherUserId,false);
                        }
                        chatInfo.setReadUsers(readUsers);
                        chatInfo.setRecordMessageUrl(recordUrl);

                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).child("messages").push().setValue(chatInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordVoiceActivity.this, "음성메시지를 전송했습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    }
                });
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
}
