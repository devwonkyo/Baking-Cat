package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.converter.JsonParser;
import com.example.dnjsr.bakingcat.sharedPreferencesManage.DataManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    final static int LOGIN_RESULT = 1000;
    final static int EXIT_RESULT = 0;
    TextInputEditText login_edittext_id;
    TextInputEditText login_edittext_password;
    Button login_button_login;
    Button login_button_signup;
    CheckBox login_checkbox_saveid;
    CheckBox login_checkbox_autologin;
    SharedPreferences appData;
    DataManager dataManager;
    JsonParser jsonParser;
    List<UserInfo> userList;
    boolean exit = false;
    String uriPath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        dataManager = new DataManager();
        jsonParser = new JsonParser();

        login_edittext_id = findViewById(R.id.login_edittext_id);
        login_edittext_password = findViewById(R.id.login_edittext_password);
        login_button_login = findViewById(R.id.login_button_login);
        login_button_signup = findViewById(R.id.login_button_signup);
        login_checkbox_saveid = findViewById(R.id.login_checkbox_saveid);
        login_checkbox_autologin = findViewById(R.id.login_checkbox_autologin);

        appData = getSharedPreferences("appData",MODE_PRIVATE); //아이디저장
        String saveId = appData.getString("saveId","0");

        if(!saveId.equals("0")){//아이디 저장 되있을 때
            login_checkbox_saveid.setChecked(true);
            login_edittext_id.setText(saveId);
        }


        login_button_login.setOnClickListener(new View.OnClickListener() { //로그인
            @Override
            public void onClick(View v) {//로그인 눌렀을 때
                final SharedPreferences.Editor editor = appData.edit();


                if(!login_edittext_id.getText().toString().equals("")&&!login_edittext_password.getText().toString().equals("")){//빈칸이 없을때만 로그인 확인
                    //아이디 비밀번호 맞는지 확인.
                    FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userId").equalTo(login_edittext_id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()==null){
                                Toast.makeText(LoginActivity.this, "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                for(DataSnapshot item : dataSnapshot.getChildren()){
                                    UserInfo userInfo = item.getValue(UserInfo.class);
                                    userInfo.setId(item.getKey());
                                    if(login_edittext_password.getText().toString().equals(userInfo.getUserPassword())){ //아이디가 맞고 비밀번호 맞을 경우
                                        CurrentUserInfo.setCurrentUserInfo(userInfo);

                                        if(login_checkbox_autologin.isChecked()){ //자동로그인 체크할 시 아이디
                                            String autoLogin = login_edittext_id.getText().toString();
                                            editor.putString("autoLogin",autoLogin);
                                            editor.commit();
                                        }

                                        if(login_checkbox_saveid.isChecked()){ // 아이디저장 체크할 시
                                            String saveId = login_edittext_id.getText().toString();
                                            editor.putString("saveId",saveId);
                                            editor.commit();
                                        }else{
                                            editor.remove("saveId");
                                            editor.commit();
                                        }

                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        intent.putExtra("id",login_edittext_id.getText().toString());
                                        startActivity(intent);
                                        finish();
                                    }else{//패스워드 오류일 경우
                                        Toast.makeText(LoginActivity.this, "패스워드가 틀립니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    Toast.makeText(LoginActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }



            }
        });

        login_button_signup.setOnClickListener(new View.OnClickListener() {//회원가입
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivityForResult(intent,LOGIN_RESULT);
            }
        });


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this,ExitPopupActivity.class);
        startActivityForResult(intent,EXIT_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            switch (requestCode){
                case LOGIN_RESULT :
                    if(resultCode == RESULT_OK){
                        login_edittext_id.setText(data.getStringExtra("id"));
                        login_edittext_password.setText(data.getStringExtra("password"));
                    }
                     break;
                case EXIT_RESULT :
                    if(resultCode == RESULT_OK){
                        finish();
                    }
            }


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("life","로그인 액티비티 start");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("life","로그인 액티비티 restart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        appData = getSharedPreferences("appData",MODE_PRIVATE); // 회원가입 후 바로 로그인 가능 oncreate에만 설정하면 바로 로그인안됨.
        userList = jsonParser.jsonArrayToUserList(dataManager.getUserInfo(getApplicationContext()));
        Log.d("life","로그인 액티비티 resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("life","로그인 액티비티 pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        login_edittext_password.setText("");
        login_edittext_id.setText("");
        Log.d("life","로그인 액티비티 onstop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("life","로그인 액티비티 ondestroy");
    }
}
