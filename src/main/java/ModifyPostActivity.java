package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dnjsr.bakingcat.info.PostInfo;
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

public class ModifyPostActivity extends AppCompatActivity {
    ImageView modifypost_imageview_postimage;
    TextView modifypost_textview_notcie;//이미지를 수정해보세요
    EditText modifypost_editttext_post;
    Button modifypost_button_update;
    String postId;
    PostInfo postInfo;
    Uri imageUri;
    final static int GET_GALLERY_IMAGE_MODIFYPOST = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("게시물 수정");
        postId = getIntent().getStringExtra("postId");

        modifypost_imageview_postimage = findViewById(R.id.modifypost_imageview_postimage);
        modifypost_textview_notcie = findViewById(R.id.modifypost_textview_notcie);
        modifypost_editttext_post = findViewById(R.id.modifypost_editttext_post);
        modifypost_button_update = findViewById(R.id.modifypost_button_update);

        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postInfo = dataSnapshot.getValue(PostInfo.class);
                Glide.with(getApplicationContext()).load(postInfo.getPostImageUrl()).into(modifypost_imageview_postimage);
                modifypost_editttext_post.setText(postInfo.getPostContents());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        modifypost_imageview_postimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent,GET_GALLERY_IMAGE_MODIFYPOST);
            }
        });

        modifypost_button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modifypost_editttext_post.getText().toString().equals("")){
                    Toast.makeText(ModifyPostActivity.this, "게시물의 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    if(imageUri == null){ //이미지가 선택되지 않았을 경우 내용만 변경해서 postId를 이용해 post에 직접 접근하여 업데이트
                        postInfo.setPostContents(modifypost_editttext_post.getText().toString());
                        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ModifyPostActivity.this, "게시물을 수정했습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }else{//이미지가 선택 되었을 경우
                        final StorageReference modifyReference = FirebaseStorage.getInstance().getReference().child("postImages").child(postId);
                        modifyReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() { //이미지를 업로드 하고
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                modifyReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {//다운로드 url을 다시 받아옴
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        postInfo.setPostImageUrl(uri.toString()); //수정된 게시물 이미지 url
                                        postInfo.setPostContents(modifypost_editttext_post.getText().toString()); //수정된 게시물 내용
                                        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() { //수정에 성공한다면
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ModifyPostActivity.this, "게시물을 수정했습니다.", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    }
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode ==GET_GALLERY_IMAGE_MODIFYPOST){
                imageUri = data.getData();
                modifypost_imageview_postimage.setImageURI(data.getData());
                modifypost_textview_notcie.setText("사진선택 완료");
                modifypost_textview_notcie.setTextColor(Color.parseColor("#0fa60f"));
            }
        }
    }
}
