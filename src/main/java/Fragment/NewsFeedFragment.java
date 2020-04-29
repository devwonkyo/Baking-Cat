package com.example.dnjsr.bakingcat.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PostProcessor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.CommentActivity;
import com.example.dnjsr.bakingcat.LikeListActivity;
import com.example.dnjsr.bakingcat.ModifyPostActivity;
import com.example.dnjsr.bakingcat.ProfileActivity;
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.asynctask.DownloadAsyncTask;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.PostInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class NewsFeedFragment extends Fragment {
    RecyclerView recyclerView;
    NewsFeedFragmentAdapter newsFeedAdapter;
    TextView newsfeedfragment_textview_nonewsfeed;
    List<PostInfo> allPostList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { //뉴스피드 화면 생성
        View view = inflater.inflate(R.layout.fragment_newsfeed,container,false);
        recyclerView = view.findViewById(R.id.newsfeedfragment_recyclerview);
        newsfeedfragment_textview_nonewsfeed = view.findViewById(R.id.newsfeedfragment_textview_nonewsfeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsFeedAdapter = new NewsFeedFragmentAdapter(allPostList);
        recyclerView.setAdapter(newsFeedAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFriendList").addValueEventListener(new ValueEventListener() {//친구목록이 바뀔 경우
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CurrentUserInfo.getCurrentUserInfo().setUserFollowerList((List<String>) dataSnapshot.getValue());//현재 친구목록 갱신

                FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(new ValueEventListener() {// 모든 것 가져옴.
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allPostList.clear();

                        for(DataSnapshot item : dataSnapshot.getChildren()){

                            PostInfo postInfo = item.getValue(PostInfo.class);

                            if(CurrentUserInfo.getCurrentUserInfo().getUserFriendList().contains(postInfo.getUserId())){ //친구리스트 목록중 게시물작성자가 있을 경우
                                allPostList.add(0,postInfo);//게시물 추가.
                            }else if(CurrentUserInfo.getCurrentUserInfo().getId().equals(postInfo.getUserId())){//현재 유저와 게시물 작성자가 같을 경우
                                allPostList.add(0,postInfo);//게시물 추가.
                            }

                        }
                        newsFeedAdapter.notifyDataSetChanged(); //데이터 체인지.
                        if(allPostList.isEmpty()){//뉴스피드 리스트가 없다면
                            newsfeedfragment_textview_nonewsfeed.setVisibility(View.VISIBLE);//게시물 없다는 텍스트 보여줌
                        }else{//있으면
                            newsfeedfragment_textview_nonewsfeed.setVisibility(View.INVISIBLE);//텍스트 안보여줌.
                        }
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

    public class NewsFeedFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<PostInfo> adapterPostList;
        public NewsFeedFragmentAdapter(List<PostInfo> allPostList) {//데이터리스트 입력
            adapterPostList = allPostList;
        }

        public class NewsFeedFragmentViewHolder extends RecyclerView.ViewHolder{
            ImageView newsfeed_imageview_profile;  //프로필사진
            ImageView newsfeed_imageview_option;  //옵션버튼
            TextView newsfeed_textview_name;// 작성자이름
            ImageView newsfeed_imageview_image;//뉴스피드 이미지
            ImageView newsfeed_imageview_downloadcancel;//다운로드 취소이미지
            ImageView newsfeed_imageview_likeimage;//좋아요이미지
            ImageView newsfeed_imageview_commentimage;//댓글창이동
            TextView newsfeed_textview_likenum;//좋아요 숫자
            TextView newsfeed_textview_download;//다운로드중 textview
            TextView newsfeed_textview_writername;//작성자 이름
            TextView newsfeed_textview_post;// 뉴스피드 내용
            TextView newsfeed_textview_morecomment;// 댓글 더보기
            ProgressBar newsfeed_progressbar_download;//다운로드 프로그레스 바

            public NewsFeedFragmentViewHolder(@NonNull View itemView) {  //itemview의 객체에 접글 할 수 있는 뷰홀더
                super(itemView);
                newsfeed_textview_download = itemView.findViewById(R.id.newsfeed_textview_download);
                newsfeed_progressbar_download = itemView.findViewById(R.id.newsfeed_progressbar_download);
                newsfeed_imageview_downloadcancel = itemView.findViewById(R.id.newsfeed_imageview_downloadcancel);
                newsfeed_imageview_profile = itemView.findViewById(R.id.newsfeed_imageview_profile);
                newsfeed_imageview_option = itemView.findViewById(R.id.newsfeed_imageview_option);
                newsfeed_textview_name = itemView.findViewById(R.id.newsfeed_textview_name);
                newsfeed_imageview_image = itemView.findViewById(R.id.newsfeed_imageview_image);
                newsfeed_imageview_likeimage = itemView.findViewById(R.id.newsfeed_imageview_likeimage);
                newsfeed_imageview_commentimage = itemView.findViewById(R.id.newsfeed_imageview_commentimage);
                newsfeed_textview_likenum = itemView.findViewById(R.id.newsfeed_textview_likenum);
                newsfeed_textview_writername = itemView.findViewById(R.id.newsfeed_textview_writername);
                newsfeed_textview_post = itemView.findViewById(R.id.newsfeed_textview_post);
                newsfeed_textview_morecomment = itemView.findViewById(R.id.newsfeed_textview_morecomment);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) { //뷰 홀더 선택해서 생성.
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_newsfeed,viewGroup,false);
            NewsFeedFragmentViewHolder customViewHolder = new NewsFeedFragmentViewHolder(view);
            return customViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_downloadcancel.setVisibility(View.INVISIBLE);
            Glide.with(viewHolder.itemView.getContext()).load(adapterPostList.get(i).getPostImageUrl()).into(((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image);//게시물 사진
            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_post.setText(adapterPostList.get(i).getPostContents());//게시물 내용
            FirebaseDatabase.getInstance().getReference().child("users").child(adapterPostList.get(i).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_name.setText(userInfo.getUserNicname());   //작성자
                    ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_writername.setText(userInfo.getUserNicname());//댓글쪽 작성자
                    Glide.with(viewHolder.itemView.getContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_profile);//작성자 프로필
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_likenum.setText("좋아요 "+adapterPostList.get(i).getPostLikeList().size()+"개"); //좋아요 숫자.
            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_likenum.setOnClickListener(new View.OnClickListener() { //좋아요 리스트로 이동
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),LikeListActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());//게시물 아이디 넘기고 화면전환.
                    startActivity(intent);
                }
            });


            if(adapterPostList.get(i).getPostLikeList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){ //좋아요 누른 리스트에 내가 있으면
                adapterPostList.get(i).setPostLike(true);//true설정
            }else{
                adapterPostList.get(i).setPostLike(false);//false 설정
            }

            if(adapterPostList.get(i).getPostLike()){ //좋아요누른 것이 true 되어있다면.
                ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);//빨강설정
            }else{//false라면
                ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(null);//노랑설정
            }


            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setOnClickListener(new View.OnClickListener() {//좋아요이미지 눌렀을 때
                @Override
                public void onClick(View v) {
                    if(adapterPostList.get(i).getPostLike()){//좋아요가 눌려있을 때 클릭하면
                        FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PostInfo postInfo = dataSnapshot.getValue(PostInfo.class);//좋아요 리스트 불러옴
                                postInfo.getPostLikeList().remove(CurrentUserInfo.getCurrentUserInfo().getId());//내 아이디를 좋아요리스트에서 삭제후
                                FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override//데이터베이스에 좋아요리스트 다시 업로드
                                    public void onComplete(@NonNull Task<Void> task) {//업로드가 성공하면
                                        ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(null);//좋아요 버튼 노랑설정
                                        Toast.makeText(getContext(), "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{//좋아요가 안 눌려있을 때 클릭하면
                        FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PostInfo postInfo = dataSnapshot.getValue(PostInfo.class);//좋아요리스트를 디비에서 가져온후
                                postInfo.getPostLikeList().add(CurrentUserInfo.getCurrentUserInfo().getId());//좋아요 리스트에 추가.
                                if(postInfo.getComments().isEmpty()){
                                    Log.d("qwer","empty");
                                }else{
                                    Log.d("qwer","notempty");
                                }
                                FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override//수정된 좋아요리스트 디비에 추가.
                                    public void onComplete(@NonNull Task<Void> task) {//성공했다면
                                        ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);//좋아요 버튼 빨강설정
                                        Toast.makeText(getContext(), "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
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



            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_morecomment.setText("댓글 "+adapterPostList.get(i).getComments().size()+"개 더보기");
            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_morecomment.setOnClickListener(new View.OnClickListener() { //댓글 더보기 눌렀을 때
                @Override
                public void onClick(View v) { //댓글 더 보기 눌렀을 때
                    Intent intent = new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_commentimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//댓글 창 눌렀을 때
                    Intent intent = new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_option.setOnClickListener(new View.OnClickListener() { //옵션 길게 클릭했을 경우
                @Override
                public void onClick(View v) {
                    if(adapterPostList.get(i).getUserId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){//현재유저와 글쓴이가 같을 경우 게시물 삭제 수정.
                        CharSequence[] item = {"게시물 수정","게시물 삭제"};
                        android.support.v7.app.AlertDialog.Builder adBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                        adBuilder.setItems(item, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0: //수정
                                        Intent intent = new Intent(getContext(),ModifyPostActivity.class);
                                        intent.putExtra("postId",adapterPostList.get(i).getPostId());
                                        startActivity(intent);
                                        break;
                                    case 1://삭제
                                        android.support.v7.app.AlertDialog.Builder innerBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
                                        innerBuilder.setTitle("NOTICE").setMessage("정말로 삭제하시겠습니까?")
                                                .setNegativeButton("네", new DialogInterface.OnClickListener() {//한번 더 물어보기.
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getContext(), "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }).show();
                                        break;
                                }
                            }
                        }).show();
                    }else{//다를경우 팔로우취소
                        CharSequence[] menu = {"팔로우 취소"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setItems(menu, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                                            userInfo.getUserFriendList().remove(adapterPostList.get(i).getUserId());
                                            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(getContext(), "팔로우를 취소했습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }).show();
                    }

                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image.setOnClickListener(new View.OnClickListener() { //이미지 클릭했을 경우.
                @Override
                public void onClick(View v) { //이미지를 클릭 했을 경우
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.item_image);
                    ImageView item_imageview_image = dialog.findViewById(R.id.item_imageview_image);
                    FloatingActionButton item_button_close = dialog.findViewById(R.id.item_button_close);
                    Glide.with(v.getContext()).load(adapterPostList.get(i).getPostImageUrl()).into(item_imageview_image);
                    item_button_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(viewHolder.itemView.getContext(), "이미지를 길게 클릭해 다운로드 받을 수 있습니다.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image.setOnLongClickListener(new View.OnLongClickListener() {//이미지 길게 클릭했을 경우 다운로드.
                @Override
                public boolean onLongClick(View v) {
                    CharSequence[] item = {"다운로드"};
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(getContext());
                    adBuilder.setItems(item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY); //어둡게
                            //((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.DST);//원래대로
                            DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask(((NewsFeedFragmentViewHolder)viewHolder).newsfeed_textview_download,((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_image,((NewsFeedFragmentViewHolder)viewHolder).newsfeed_progressbar_download,((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_downloadcancel,getContext());
                            downloadAsyncTask.execute(adapterPostList.get(i).getPostImageUrl());
                        }
                    }).show();

                    return false;
                }
            });

            ((NewsFeedFragmentViewHolder)viewHolder).newsfeed_imageview_profile.setOnClickListener(new View.OnClickListener() {//작성자 프로필 눌렀을 때.
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),ProfileActivity.class);
                    intent.putExtra("userId",adapterPostList.get(i).getUserId());
                    startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return adapterPostList.size();
        }// 수만큼 반복.

    }
}
