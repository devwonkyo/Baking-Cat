package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.PostInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    String postId;
    ImageView newsfeed_imageview_profile;
    TextView newsfeed_textview_name;
    ImageView newsfeed_imageview_image;
    ImageView newsfeed_imageview_likeimage;
    ImageView newsfeed_imageview_commentimage;
    TextView newsfeed_textview_likenum;
    TextView newsfeed_textview_writername;
    TextView newsfeed_textview_post;
    TextView newsfeed_textview_morecomment;
    PostInfo postInfo;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        postId = getIntent().getStringExtra("postId");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("사진");
        actionBar.setDisplayHomeAsUpEnabled(true);
        newsfeed_imageview_profile = findViewById(R.id.postnewsfeed_imageview_profile);
        newsfeed_textview_name = findViewById(R.id.postnewsfeed_textview_name);
        newsfeed_imageview_image = findViewById(R.id.postnewsfeed_imageview_image);
        newsfeed_imageview_likeimage = findViewById(R.id.postnewsfeed_imageview_likeimage);
        newsfeed_imageview_commentimage = findViewById(R.id.postnewsfeed_imageview_commentimage);
        newsfeed_textview_likenum = findViewById(R.id.postnewsfeed_textview_likenum);
        newsfeed_textview_writername = findViewById(R.id.postnewsfeed_textview_writername);
        newsfeed_textview_post = findViewById(R.id.postnewsfeed_textview_post);
        newsfeed_textview_morecomment = findViewById(R.id.postnewsfeed_textview_morecomment);

        setPost();

        newsfeed_imageview_likeimage.setOnClickListener(new View.OnClickListener() { //좋아요 이미지 클릭.
            @Override
            public void onClick(View v) {
                if(postInfo.getPostLikeList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){//클릭시 내가 좋아요 누른 상태라면  좋아요 취소후 리스트에서삭제 업데이트
                    FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("postLikeList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()!=null){
                                List<String> likeList = (List<String>) dataSnapshot.getValue();
                                likeList.remove(CurrentUserInfo.getCurrentUserInfo().getId());
                                FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("postLikeList").setValue(likeList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(PostActivity.this, "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{//좋아요리스트에 없으면  리스트에 추가후 업데이트
                    FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("postLikeList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> likeList = new ArrayList<>();
                            if(dataSnapshot.getValue()!=null){
                                likeList = (List<String>) dataSnapshot.getValue();
                            }
                            likeList.add(CurrentUserInfo.getCurrentUserInfo().getId());
                            FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("postLikeList").setValue(likeList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(PostActivity.this, "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        newsfeed_textview_likenum.setOnClickListener(new View.OnClickListener() { //좋아요 리스트로 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LikeListActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });

        newsfeed_textview_morecomment.setOnClickListener(new View.OnClickListener() {//댓글 창으로 이동.
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });

        newsfeed_imageview_commentimage.setOnClickListener(new View.OnClickListener() {//댓글 창으로 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });


    }

    public void setPost(){
        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postInfo = dataSnapshot.getValue(PostInfo.class);
                FirebaseDatabase.getInstance().getReference().child("users").child(postInfo.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {//유저 정보 입력.
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        Glide.with(getApplicationContext())
                                .load(userInfo.getUserProfileImageUri())
                                .apply(new RequestOptions().circleCrop())
                                .into(newsfeed_imageview_profile);
                        newsfeed_textview_name.setText(userInfo.getUserNicname());
                        newsfeed_textview_writername.setText(userInfo.getUserNicname());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(postInfo.getPostLikeList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){
                    newsfeed_imageview_likeimage.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);//빨강표시
                }else{
                    newsfeed_imageview_likeimage.setColorFilter(null);//검정표시.
                }

                Glide.with(getApplicationContext())
                        .load(postInfo.getPostImageUrl())
                        .into(newsfeed_imageview_image);
                newsfeed_textview_likenum.setText(String.valueOf("좋아요 "+ postInfo.getPostLikeList().size()+"개"));
                newsfeed_textview_post.setText(postInfo.getPostContents());
                newsfeed_textview_morecomment.setText("댓글 "+postInfo.getComments().size()+"개 더보기");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
