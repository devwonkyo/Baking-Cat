package com.example.dnjsr.bakingcat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class WritePostActivity extends AppCompatActivity { //게시물 작성 액티비티
    final static int GET_GALLERY_IMAGE_POST = 1000;
    ImageView writepost_imageview_postimage;
    TextView writepost_edittext_post;
    TextView writepost_textview;
    Button writepost_button_upload;
    Handler handler;
    final static int WRITE_POST_COMPLETE = 1000;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("게시글 작성");
        writepost_imageview_postimage = findViewById(R.id.writepost_imageview_postimage);
        writepost_textview = findViewById(R.id.writepost_textview);
        writepost_edittext_post = findViewById(R.id.writepost_edittext_post);
        writepost_button_upload = findViewById(R.id.writepost_button_upload);

        final ProgressDialog progressDialog = new ProgressDialog(WritePostActivity.this);
        progressDialog.setMessage("게시물 업로드 중입니다...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) { //게시물이 다 올라간다면
                super.handleMessage(msg);
                if(msg.what == WRITE_POST_COMPLETE){
                    progressDialog.dismiss();//다이얼로그 없애고
                    Toast.makeText(WritePostActivity.this, "게시물이 업로드 되었습니다.", Toast.LENGTH_SHORT).show(); //토스트메시지
                    finish();//액티비티 종료
                }
            }
        };

        writepost_imageview_postimage.setOnClickListener(new View.OnClickListener() { //업로드할 이미지
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent,GET_GALLERY_IMAGE_POST);
            }
        });

        writepost_button_upload.setOnClickListener(new View.OnClickListener() {//게시확인 버튼
            @Override
            public void onClick(View v) {

                if(!writepost_edittext_post.getText().toString().equals("")&&imageUri!=null){ //이미지와 게시글이 모두 입력 되었을 경우에만
                    progressDialog.show();
                    final String postId = FirebaseDatabase.getInstance().getReference().child("posts").push().getKey();
                    final StorageReference postStorage = FirebaseStorage.getInstance().getReference().child("postImages").child(postId); //POSTID에 맞는 이미지 업로드 후
                    postStorage.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {//성공할 시.

                            postStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { //다운로드 url을 받아옴.
                                @Override
                                public void onSuccess(Uri uri) {
                                    String postImageUrl = uri.toString();
                                    PostInfo postInfo = new PostInfo();
                                    postInfo.setPostId(postId); //게시물아이디
                                    postInfo.setUserId(CurrentUserInfo.getCurrentUserInfo().getId());//게시물 글 쓴이 아이디
                                    postInfo.setPostContents(writepost_edittext_post.getText().toString());//게시물 내용
                                    postInfo.setPostImageUrl(postImageUrl);//게시물 사진.

                                    FirebaseDatabase.getInstance().getReference().child("posts").child(postId).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            handler.sendEmptyMessage(WRITE_POST_COMPLETE);
                                        }
                                    });

                                }
                            });
                        }
                    });

                }
                else{
                    Toast.makeText(WritePostActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
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
        if(resultCode == RESULT_OK){

            if(requestCode == GET_GALLERY_IMAGE_POST){

                imageUri = data.getData();
                writepost_imageview_postimage.setImageURI(imageUri);
                writepost_textview.setText("사진 설정 완료");
                writepost_textview.setTextColor(Color.parseColor("#0fa60f"));
            }
        }
    }
}
