package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.converter.JsonParser;
import com.example.dnjsr.bakingcat.pattern.UserIdPattern;
import com.example.dnjsr.bakingcat.sharedPreferencesManage.DataManager;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    final static int GET_GALLERY_IMAGE = 0;
    ImageView signup_imageview_profile;
    TextView signup_textview_inputtextmessage;
    TextView signup_textview_checkmessage;
    TextInputEditText signup_edittext_id;
    TextInputEditText signup_edittext_nicname;
    TextInputEditText signup_edittext_password;
    TextInputEditText signup_edittext_passwordcheck;
    Button signup_button_idcheck;
    Button signup_button_register;
    DataManager dataManager;
    JsonParser jsonParser;
    boolean check[] = {false, false ,true };  //check[0] 아이디 중복 , check[1] 이미지 등록 여부 ,check[2] 아이디 정규표현식여부
    List<UserInfo> userList;
    String uriPath;
    Uri imageUri;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        dataManager = new DataManager();
        jsonParser = new JsonParser();
        signup_imageview_profile = findViewById(R.id.signup_imageview_profile);
        signup_textview_inputtextmessage = findViewById(R.id.signup_textview_inputtextmessage);
        signup_textview_checkmessage = findViewById(R.id.signup_textview_checkmessage);
        signup_edittext_id = findViewById(R.id.signup_edittext_id);
        signup_edittext_nicname = findViewById(R.id.signup_edittext_nicname);
        signup_edittext_password = findViewById(R.id.signup_edittext_password);
        signup_edittext_passwordcheck = findViewById(R.id.signup_edittext_passwordcheck);
        signup_button_idcheck = findViewById(R.id.signup_button_idcheck);
        signup_button_register = findViewById(R.id.signup_button_register);

        userList = jsonParser.jsonArrayToUserList(dataManager.getUserInfo(getApplicationContext())); //shared에서 가져온 유저리스트 객체화

        signup_imageview_profile.setOnClickListener(new View.OnClickListener() { //프로필 사진넣기 클릭
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                //intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent,GET_GALLERY_IMAGE);
            }
        });

        signup_edittext_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UserIdPattern userIdPattern = new UserIdPattern();
                if(userIdPattern.checkUserId(s.toString())){
                    signup_textview_checkmessage.setText("\""+signup_edittext_id.getText().toString()+"\""+" 는 사용가능한 아이디입니다.\n 중복을 확인해주세요.");
                    signup_textview_checkmessage.setTextColor(Color.parseColor("#0fa60f"));
                    check[2] = true;
                }else{
                    signup_textview_checkmessage.setText("4~16자의 영문,숫자로 입력해주세요.");
                    signup_textview_checkmessage.setTextColor(Color.parseColor("#bd3141"));
                    check[2] = false;
                }
            }
        });

        signup_button_idcheck.setOnClickListener(new View.OnClickListener() { //아이디 체크
            @Override
            public void onClick(View v) {//아이디 체크
                if(check[2]){                                                               //userId가 현재 입력한 값이 같은 경우의 users 안에있는 객체를 모두 불러옴
                    FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userId").equalTo(signup_edittext_id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()!=null){//현재 입력한 아이디와 일치하는 값이 있을 경우
                                signup_textview_checkmessage.setText("이미 가입되어있는 아이디입니다.");
                                signup_textview_checkmessage.setTextColor(Color.parseColor("#bd3141"));
                                check[0] = false; //아이디 체크 false(실패)
                            }else{//현재 입력한 아이디와 일치하는 값이 없을 경우
                                signup_textview_checkmessage.setText("\""+signup_edittext_id.getText().toString()+"\""+" 는 사용가능한 아이디입니다.");
                                signup_textview_checkmessage.setTextColor(Color.parseColor("#0fa60f"));
                                check[0] = true; //아이디 체크 true(통과)
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else{
                    Toast.makeText(SignUpActivity.this, "아이디는 4~16자의 영문,숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
                }



            }
        });

        signup_edittext_password.addTextChangedListener(new TextWatcher() {       //비밀번호 확인
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(signup_edittext_passwordcheck.getText().toString())){
                    signup_edittext_password.setTextColor(Color.parseColor("#0fa60f"));
                    signup_edittext_passwordcheck.setTextColor(Color.parseColor("#0fa60f"));
                }
                else{
                    signup_edittext_password.setTextColor(Color.parseColor("#bd3141"));
                    signup_edittext_passwordcheck.setTextColor(Color.parseColor("#bd3141"));
                }
            }
        });

        signup_edittext_passwordcheck.addTextChangedListener(new TextWatcher() {  //비밀번호 확인
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(signup_edittext_password.getText().toString())){
                    signup_edittext_password.setTextColor(Color.parseColor("#0fa60f"));
                    signup_edittext_passwordcheck.setTextColor(Color.parseColor("#0fa60f"));
                }
                else{
                    signup_edittext_password.setTextColor(Color.parseColor("#bd3141"));
                    signup_edittext_passwordcheck.setTextColor(Color.parseColor("#bd3141"));
                }
            }
        });


        signup_button_register.setOnClickListener(new View.OnClickListener() { //회원가입 완료.
            @Override
            public void onClick(View v) {
                if(check[0] == false){
                    Toast.makeText(getApplicationContext(), "아이디중복을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(check[1]==false){
                    Toast.makeText(SignUpActivity.this, "프로필 사진을 넣어주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(check[2]==false){
                    Toast.makeText(SignUpActivity.this, "아이디는 4~16자의 영문,숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(signup_edittext_id.getText().toString().equals("")||signup_edittext_nicname.getText().toString().equals("")
                        ||signup_edittext_password.getText().toString().equals("")||signup_edittext_passwordcheck.getText().toString().equals("")){
                    Toast.makeText(SignUpActivity.this, "모든사항을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(!signup_edittext_password.getText().toString().equals(signup_edittext_passwordcheck.getText().toString())){
                    Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                else{


                    final StorageReference profileImageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(signup_edittext_id.getText().toString());
                    profileImageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            final Intent resultIntent = new Intent(getApplicationContext(),LoginActivity.class);
                            final UserInfo userInfo = new UserInfo();//신규회원 객체 생성 정보입력

                            profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    userInfo.setUserProfileImageUri(uri.toString());
                                    userInfo.setUserId(signup_edittext_id.getText().toString());
                                    userInfo.setUserNicname(signup_edittext_nicname.getText().toString());
                                    userInfo.setUserPassword(signup_edittext_password.getText().toString());
                                    String id = FirebaseDatabase.getInstance().getReference().child("users").push().getKey();
                                    userInfo.setId(id);
                                    FirebaseDatabase.getInstance().getReference().child("users").child(id).setValue(userInfo);
                                    resultIntent.putExtra("id",signup_edittext_id.getText().toString());
                                    resultIntent.putExtra("password",signup_edittext_password.getText().toString());
                                    setResult(RESULT_OK,resultIntent);
                                    Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });

                        }
                    });


                   /* String stringImage = BitmapConverter.getBitmapToString(bitmap);
                    final UserInfo userInfo = new UserInfo();//신규회원 객체 생성 정보입력
                    userInfo.setUserId(signup_edittext_id.getText().toString());
                    userInfo.setUserNicname(signup_edittext_nicname.getText().toString());
                    userInfo.setUserPassword(signup_edittext_password.getText().toString());
                    userInfo.setUserProfileImage(stringImage);
                    userInfo.setUserProfileImageUri(null);
                    String id = FirebaseDatabase.getInstance().getReference().child("users").push().getKey();
                    userInfo.setId(id);
                    FirebaseDatabase.getInstance().getReference().child("users").child(id).setValue(userInfo);*/

                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == GET_GALLERY_IMAGE){
                imageUri = data.getData();  //uri로 경로를 직접 받아옴
                uriPath = data.getDataString();  //uri 경로를 string형태로 받아와서 Uri클래스에서 파싱.

               /* bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.parse(uriPath));  //uri를이용해 bitmap으로 변환.
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                signup_imageview_profile.setImageURI(Uri.parse(uriPath));
                check[1] = true;
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(getApplicationContext(), "회원가입을 취소하였습니다.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(check[1] == true)
            signup_textview_inputtextmessage.setText("사진 설정 완료");
        Log.d("life","회원가입 액티비티 resume");

    }

}
