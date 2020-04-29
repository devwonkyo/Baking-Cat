package com.example.dnjsr.bakingcat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ModifyRoomNameActivity extends AppCompatActivity {
    ActionBar actionBar;
    EditText modifyroomname_edittext_roomname;
    TextView modifyroomname_textview_namenum;
    String roomName;
    String roomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_room_name);
        modifyroomname_edittext_roomname = findViewById(R.id.modifyroomname_edittext_roomname);
        modifyroomname_textview_namenum = findViewById(R.id.modifyroomname_textview_namenum);
        actionBar = getSupportActionBar();
        actionBar.setTitle("채팅방 이름 설정");

        roomName = getIntent().getStringExtra("roomName");
        roomId = getIntent().getStringExtra("roomId");
        modifyroomname_edittext_roomname.setText(roomName);
        modifyroomname_textview_namenum.setText(roomName.length()+"/30");

        modifyroomname_edittext_roomname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                modifyroomname_textview_namenum.setText(s.length()+"/30");
                if(s.length()==0){
                    modifyroomname_edittext_roomname.setHint(roomName);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ok,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_ok){
            Map<String,Object> updateRoomInfo = new HashMap<>();
            updateRoomInfo.put("customRoomName",true);
            updateRoomInfo.put("roomName",modifyroomname_edittext_roomname.getText().toString());
            if(modifyroomname_edittext_roomname.getText().toString().length()>30){
                Toast.makeText(ModifyRoomNameActivity.this, "채팅방 이름을 30자 이하로 설정해주세요.", Toast.LENGTH_SHORT).show();
            }else{
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(roomId).updateChildren(updateRoomInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ModifyRoomNameActivity.this, "채팅방 이름을 새로 설정했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
