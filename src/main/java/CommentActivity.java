package com.example.dnjsr.bakingcat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.CommentInfo;
import com.example.dnjsr.bakingcat.info.PostInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    ImageView comment_imageview_profile;
    ImageView comment_imageview_commentprofileimage;
    TextView comment_textview_id;
    TextView comment_textview_post;
    TextView comment_textview_nocomment;
    RecyclerView comment_recyclerview;
    EditText comment_ediittext_comment;
    Button comment_button_upload;
    CommentAdapter commentAdapter;
    List<CommentInfo> commentList = new ArrayList<>();
    String postId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        postId = getIntent().getStringExtra("postId");
        comment_imageview_profile = findViewById(R.id.comment_imageview_profile);
        comment_imageview_commentprofileimage = findViewById(R.id.comment_imageview_commentprofileimage);
        comment_textview_id = findViewById(R.id.comment_textview_id);
        comment_textview_nocomment = findViewById(R.id.comment_textview_nocomment);
        comment_textview_post = findViewById(R.id.comment_textview_post);
        comment_recyclerview = findViewById(R.id.comment_recyclerview);
        comment_ediittext_comment =findViewById(R.id.comment_ediittext_comment);
        comment_button_upload = findViewById(R.id.comment_button_upload);

        comment_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        comment_recyclerview.setAdapter(commentAdapter);

        Glide.with(getApplicationContext()).load(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(comment_imageview_commentprofileimage);

        comment_button_upload.setOnClickListener(new View.OnClickListener() {//게시물 등록
            @Override
            public void onClick(View v) {
                CommentInfo commentInfo = new CommentInfo();
                commentInfo.setUserId(CurrentUserInfo.getCurrentUserInfo().getId());
                commentInfo.setComment(comment_ediittext_comment.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("comments").push().setValue(commentInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CommentActivity.this, "댓글을 등록했습니다.", Toast.LENGTH_SHORT).show();
                        comment_ediittext_comment.setText("");
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    CommentInfo commentInfo = item.getValue(CommentInfo.class);
                    commentInfo.setCommentId(item.getKey());
                    commentList.add(commentInfo);
                }
                commentAdapter.notifyDataSetChanged();
                if(commentList.isEmpty()){
                    comment_textview_nocomment.setVisibility(View.VISIBLE);
                }else{
                    comment_textview_nocomment.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PostInfo postInfo = dataSnapshot.getValue(PostInfo.class);
                comment_textview_post.setText(postInfo.getPostContents());
                FirebaseDatabase.getInstance().getReference().child("users").child(postInfo.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        Glide.with(getApplicationContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(comment_imageview_profile);
                        comment_textview_id.setText(userInfo.getUserNicname());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class CommentAdapter extends RecyclerView.Adapter{
        List<CommentInfo> adapterCommentList;
        public CommentAdapter(List<CommentInfo> commentList) {//데이터 리스트 추가 생성자
            adapterCommentList = commentList;
        }

        public class CommentViewHolder extends RecyclerView.ViewHolder{
            ImageView itemcomment_imageview_profile;
            ImageView itemcomment_imageview_modify;
            ImageView itemcomment_imageview_delete;
            TextView itemcomment_textview_id;
            TextView itemcomment_textview_comment;

            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                itemcomment_imageview_profile = itemView.findViewById(R.id.itemcomment_imageview_profile);
                itemcomment_imageview_modify = itemView.findViewById(R.id.itemcomment_imageview_modify);
                itemcomment_imageview_delete = itemView.findViewById(R.id.itemcomment_imageview_delete);
                itemcomment_textview_id = itemView.findViewById(R.id.itemcomment_textview_id);
                itemcomment_textview_comment = itemView.findViewById(R.id.itemcomment_textview_comment);
            }
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment,viewGroup,false);

            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder,final int i) {
            FirebaseDatabase.getInstance().getReference().child("users").child(adapterCommentList.get(i).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    Glide.with(viewHolder.itemView.getContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((CommentViewHolder)viewHolder).itemcomment_imageview_profile);
                    ((CommentViewHolder)viewHolder).itemcomment_textview_id.setText(userInfo.getUserNicname());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            ((CommentViewHolder)viewHolder).itemcomment_imageview_profile.setOnClickListener(new View.OnClickListener() { //댓글 프로필을 클릭했을 때.
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),ProfileActivity.class);
                    intent.putExtra("userId",adapterCommentList.get(i).getUserId());
                    startActivity(intent);
                }
            });
            ((CommentViewHolder)viewHolder).itemcomment_textview_comment.setText(adapterCommentList.get(i).getComment());

            if(adapterCommentList.get(i).getUserId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){ // 댓글 작성자가 현재 유저이면 수정 삭제 표시
                ((CommentViewHolder)viewHolder).itemcomment_imageview_delete.setVisibility(View.VISIBLE);
                ((CommentViewHolder)viewHolder).itemcomment_imageview_modify.setVisibility(View.VISIBLE);
            }else{
                ((CommentViewHolder)viewHolder).itemcomment_imageview_delete.setVisibility(View.GONE);
                ((CommentViewHolder)viewHolder).itemcomment_imageview_modify.setVisibility(View.GONE);
            }

            ((CommentViewHolder)viewHolder).itemcomment_imageview_delete.setOnClickListener(new View.OnClickListener() { //삭제 버튼 눌렀을 때.
                @Override
                public void onClick(View v) {
                    android.app.AlertDialog.Builder adBuilder = new android.app.AlertDialog.Builder(CommentActivity.this);//alertdialog 띄우고 정말 삭제 여부
                    adBuilder.setTitle("Noice").setMessage("이 댓글을 삭제 하시겠어요?").setNegativeButton("네", new DialogInterface.OnClickListener() { //예를 눌렀을 경우
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//누른 댓글의 id에 접근하여 삭제.
                            FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("comments").child(adapterCommentList.get(i).getCommentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {//삭제 성공하면 토스트 메시지.
                                    Toast.makeText(CommentActivity.this, "댓글이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setPositiveButton("아니오", new DialogInterface.OnClickListener() { //아무일도 일어나지 않고 댓글삭제 취소 토스트만 띄움
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(CommentActivity.this, "댓글 삭제를 취소했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }).show();

                }
            });

            ((CommentViewHolder)viewHolder).itemcomment_imageview_modify.setOnClickListener(new View.OnClickListener() {//수정 버튼 눌럿을 때.
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(CommentActivity.this);
                    dialog.setContentView(R.layout.item_commentmodify);
                    ImageView commentmodify_imageview_profile = dialog.findViewById(R.id.commentmodify_imageview_profile);
                    final EditText commentmodify_edittext_comment = dialog.findViewById(R.id.commentmodify_edittext_comment);
                    Button commentmodify_button_update = dialog.findViewById(R.id.commentmodify_button_update);
                    Button commentmodify_button_cancel = dialog.findViewById(R.id.commentmodify_button_cancel);

                    Glide.with(getApplicationContext()).load(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(commentmodify_imageview_profile);//작성자 프로필사진
                    commentmodify_edittext_comment.setText(adapterCommentList.get(i).getComment());//현재 댓글

                    commentmodify_button_cancel.setOnClickListener(new View.OnClickListener() {//취소 눌렀을 때
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(CommentActivity.this, "댓글 수정을 취소했습니다.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); //다이얼로그 종료
                        }
                    });

                    commentmodify_button_update.setOnClickListener(new View.OnClickListener() { //업데이트 눌럿을 때
                        @Override
                        public void onClick(View v) { //코멘트 아이디에 직접 접근해서
                            FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("comments").child(adapterCommentList.get(i).getCommentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    CommentInfo commentInfo = dataSnapshot.getValue(CommentInfo.class); //값을 가져온후
                                    commentInfo.setComment(commentmodify_edittext_comment.getText().toString());//수정후 올림.
                                    FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("comments").child(adapterCommentList.get(i).getCommentId()).setValue(commentInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(CommentActivity.this, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();//수정되면 토스트메시지
                                            dialog.dismiss();//다이얼로그 종료
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    dialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterCommentList.size();
        }
    }
}
