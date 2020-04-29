package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.converter.JsonParser;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.dnjsr.bakingcat.SignUpActivity.GET_GALLERY_IMAGE;

public class ModifyProfileActivity extends AppCompatActivity { //프로필 수정화면
    ImageView modify_imageview_profile;
    TextView modify_textview_id;
    TextInputEditText modify_edittext_nicname;
    Button modify_button_modify;
    String modifiedProfileImageUri;
    Uri imageUri;
    Bitmap bitmap;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("프로필 편집");


        modify_imageview_profile = findViewById(R.id.modify_imageview_profile);
        modify_textview_id = findViewById(R.id.modify_textview_id);
        modify_edittext_nicname = findViewById(R.id.modify_edittext_nicname);
        modify_button_modify = findViewById(R.id.modify_button_modify);



        //modify_imageview_profile.setImageURI(Uri.parse(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()));
        Glide.with(getApplicationContext()).load(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()).into(modify_imageview_profile);

        modify_textview_id.setText(CurrentUserInfo.getCurrentUserInfo().getUserId());
        modify_edittext_nicname.setHint(CurrentUserInfo.getCurrentUserInfo().getUserNicname());//원래 닉네임

        modify_imageview_profile.setOnClickListener(new View.OnClickListener() { //프로필 사진 변경
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent,GET_GALLERY_IMAGE);
            }
        });

        modify_button_modify.setOnClickListener(new View.OnClickListener() { //수정버튼 눌럿을 때
            @Override
            public void onClick(View v) { //프로필 수정
                final Map<String,Object> changedInfo = new HashMap<>();
                if(!modify_edittext_nicname.getText().toString().equals("")){ //닉네임이 빈칸 아닐 때만 hashmap에 수정할 아이디 넣어줌.
                    changedInfo.put("userNicname",modify_edittext_nicname.getText().toString());
                }
                if(!modify_edittext_nicname.getText().toString().equals("")&&imageUri == null){
                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).updateChildren(changedInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    CurrentUserInfo.setCurrentUserInfo(dataSnapshot.getValue(UserInfo.class));
                                    Toast.makeText(ModifyProfileActivity.this, "프로필이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
                if(imageUri!=null){
                    final StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(CurrentUserInfo.getCurrentUserInfo().getUserId());
                    imageStorageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() { //firestorage에 사진 올리기.
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) { // firestorage에 사진이 올라가면]
                            Log.d("qwer","storage에 사진 올라감");
                            imageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { // 다운로드 url 받아오기.
                                @Override
                                public void onSuccess(Uri uri) {
                                    changedInfo.put("userProfileImageUri",uri.toString()); //다운로드 url을 받아와서 수정 hashmap에 넣어줌.
                                    Log.d("qwer","올라간 download url"+ uri.toString());
                                    if(!(modify_edittext_nicname.getText().toString().equals("")&&imageUri==null)){//둘다 빈칸이 아닐경우에만
                                        FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).updateChildren(changedInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override//프로필사진과 닉네임 디비에 업데이트
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userId").equalTo(CurrentUserInfo.getCurrentUserInfo().getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override //현재유저를 디비에서 가져옴
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for(DataSnapshot item : dataSnapshot.getChildren()){
                                                            UserInfo userInfo = item.getValue(UserInfo.class);
                                                            userInfo.setId(item.getKey());
                                                            CurrentUserInfo.setCurrentUserInfo(userInfo); //현재유저객체에 넣어줌.
                                                        }
                                                        Toast.makeText(ModifyProfileActivity.this, "프로필이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Toast.makeText(ModifyProfileActivity.this, "업데이트 실패", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });


                }

            }
        });

        modify_button_modify.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ImageActivity.class);
                intent.putExtra("image",modifiedProfileImageUri);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "프로필변경을 취소합니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == GET_GALLERY_IMAGE){
                imageUri = data.getData();
                modifiedProfileImageUri = data.getDataString();  //uri 경로를 string형태로 받아와서 Uri클래스에서 파싱.

                /*bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.parse(modifiedProfileImageUri)); //uri -->bitmap으로 바꿈.
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                modify_imageview_profile.setImageURI(imageUri);



            }
        }
    }



}
