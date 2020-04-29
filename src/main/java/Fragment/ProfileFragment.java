package com.example.dnjsr.bakingcat.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.CommentActivity;
import com.example.dnjsr.bakingcat.FriendActivity;
import com.example.dnjsr.bakingcat.LikeListActivity;
import com.example.dnjsr.bakingcat.ModifyPostActivity;
import com.example.dnjsr.bakingcat.ModifyProfileActivity;
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.WritePostActivity;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
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
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class ProfileFragment extends Fragment {
    ImageView profilefragment_imageview_profile;
    TextView profilefragment_textview_friendnum;
    TextView profilefragment_textview_friend;
    TextView profilefragment_textview_name;
    TextView profilefragment_textview_postnum;
    TextView profilefragment_textview_noposts;
    Button profilefragment_button_modifyprofile;
    RecyclerView profilefragment_recyclerview;
    FloatingActionButton profilefragment_button_writepost;
    ProfileFragmentAdapter profileFragmentAdapter;
    List<PostInfo> myPostList = new ArrayList<>();
    List<String> friendList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        profilefragment_imageview_profile = view.findViewById(R.id.profilefragment_imageview_profile);
        profilefragment_textview_friendnum = view.findViewById(R.id.profilefragment_textview_friendnum);
        profilefragment_textview_postnum = view.findViewById(R.id.profilefragment_textview_postnum);
        profilefragment_textview_friend = view.findViewById(R.id.profilefragment_textview_friend);
        profilefragment_textview_name = view.findViewById(R.id.profilefragment_textview_name);
        profilefragment_textview_noposts = view.findViewById(R.id.profilefragment_textview_noposts);
        profilefragment_button_writepost = view.findViewById(R.id.profilefragment_button_writepost);
        profilefragment_button_modifyprofile =view.findViewById(R.id.profilefragment_button_modifyprofile);
        profilefragment_recyclerview =view.findViewById(R.id.profilefragment_recyclerview);



        profilefragment_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        profileFragmentAdapter = new ProfileFragmentAdapter(myPostList);                                          //리스트(데이터) 생성자로 넣어주어야함.
        profilefragment_recyclerview.setAdapter(profileFragmentAdapter);

        //profilefragment_imageview_profile.setImageURI(Uri.parse(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()));//프로필 사진 현재 유저에서 설정
        //profilefragment_textview_name.setText(CurrentUserInfo.getCurrentUserInfo().getUserNicname());//이름 현재유저에서 설정

        profilefragment_button_modifyprofile.setOnClickListener(new View.OnClickListener() { //프로필 수정 버튼
            @Override
            public void onClick(View v) {//프로필 수정 클릭
                startActivity(new Intent(getContext(),ModifyProfileActivity.class));
            }
        });


        profilefragment_textview_friend.setOnClickListener(new View.OnClickListener() { //친구목록, 친구요청 액티비티이동 버튼
            @Override
            public void onClick(View v) {//친구 목록클릭
                startActivity(new Intent(getContext(),FriendActivity.class));
            }
        });

        profilefragment_button_writepost.setOnClickListener(new View.OnClickListener() { //게시물 작성 액티비티이동 버튼.
            @Override
            public void onClick(View v) {//글쓰기 클릭.
                startActivity(new Intent(getContext(),WritePostActivity.class));
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("userId").equalTo(CurrentUserInfo.getCurrentUserInfo().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPostList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    PostInfo postInfo = item.getValue(PostInfo.class);
                    myPostList.add(0,postInfo);
                }
                profileFragmentAdapter.notifyDataSetChanged();
                profilefragment_textview_postnum.setText(String.valueOf(myPostList.size()));
                if(myPostList.isEmpty()){
                    profilefragment_textview_noposts.setVisibility(View.VISIBLE);
                }else{
                    profilefragment_textview_noposts.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        friendList = CurrentUserInfo.getCurrentUserInfo().getUserFriendList();
        profilefragment_textview_friendnum.setText(String.valueOf(friendList.size()));

        Glide.with(this).load(CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(profilefragment_imageview_profile);
        profilefragment_textview_name.setText(CurrentUserInfo.getCurrentUserInfo().getUserNicname());//이름 현재유저에서 설정
    }

    public class ProfileFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<PostInfo> adapterPostList;
        public ProfileFragmentAdapter(List<PostInfo> myPostList) {
            adapterPostList = myPostList;
        }

        public class ProfileFragmentViewHolder extends RecyclerView.ViewHolder{
            ImageView newsfeed_imageview_profile;  //프로필사진
            ImageView newsfeed_imageview_option;  //옵션버튼
            TextView newsfeed_textview_name;// 작성자이름
            ImageView newsfeed_imageview_image;//뉴스피드 이미지
            ImageView newsfeed_imageview_likeimage;//좋아요이미지
            ImageView newsfeed_imageview_commentimage;//댓글창이동
            TextView newsfeed_textview_likenum;//좋아요 숫자
            TextView newsfeed_textview_writername;//작성자 이름
            TextView newsfeed_textview_post;// 뉴스피드 내용
            TextView newsfeed_textview_morecomment;// 댓글 더보기

            public ProfileFragmentViewHolder(@NonNull View itemView) {
                super(itemView);
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
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_newsfeed,viewGroup,false);
            return new ProfileFragmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
            Glide.with(viewHolder.itemView.getContext()).load(adapterPostList.get(i).getPostImageUrl()).into(((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_image); //뉴스피드사진
            FirebaseDatabase.getInstance().getReference().child("users").child(adapterPostList.get(i).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_name.setText(userInfo.getUserNicname());   //작성자
                    ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_writername.setText(userInfo.getUserNicname());//작성자
                    Glide.with(getContext()).load(userInfo.getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_profile);//작성자 사진
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_likenum.setText("좋아요 "+ adapterPostList.get(i).getPostLikeList().size() +"개");//좋아요textview
            ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_likenum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),LikeListActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });

            ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_post.setText(adapterPostList.get(i).getPostContents());

            if(adapterPostList.get(i).getPostLikeList().contains(CurrentUserInfo.getCurrentUserInfo().getId())){ //좋아요 누른 리스트에 내가 있으면
                adapterPostList.get(i).setPostLike(true);//true설정
            }else{
                adapterPostList.get(i).setPostLike(false);//false 설정
            }

            if(adapterPostList.get(i).getPostLike()){ //좋아요누른 것이 true 되어있다면.
                ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);//빨강설정
            }else{//false라면
                ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(null);//노랑설정
            }

            ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setOnClickListener(new View.OnClickListener() {//좋아요이미지 눌렀을 때
                @Override
                public void onClick(View v) {
                    if(adapterPostList.get(i).getPostLike()){ //좋아요가 눌려있으면 좋아요리스트에서 삭제.
                        FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PostInfo postInfo = dataSnapshot.getValue(PostInfo.class);
                                postInfo.getPostLikeList().remove(CurrentUserInfo.getCurrentUserInfo().getId());//리스트에서 삭제
                                FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(null);//노랑설정
                                        Toast.makeText(getContext(), "좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{//좋아요가 안눌려있으면 좋아요 리스트에 추가.
                        FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PostInfo postInfo = dataSnapshot.getValue(PostInfo.class);
                                postInfo.getPostLikeList().add(CurrentUserInfo.getCurrentUserInfo().getId());
                                FirebaseDatabase.getInstance().getReference().child("posts").child(adapterPostList.get(i).getPostId()).setValue(postInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_likeimage.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);//빨강설정
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
            ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_morecomment.setText("댓글 "+adapterPostList.get(i).getComments().size()+"개 더보기");
            ((ProfileFragmentViewHolder)viewHolder).newsfeed_textview_morecomment.setOnClickListener(new View.OnClickListener() { //댓글 더보기 눌렀을 때
                @Override
                public void onClick(View v) { //댓글 더 보기 눌렀을 때
                    Intent intent =new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });

            ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_commentimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//댓글 창 눌렀을 때
                    Intent intent =new Intent(v.getContext(),CommentActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });

            ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.item_image);
                    ImageView item_imageview_image = dialog.findViewById(R.id.item_imageview_image);
                    FloatingActionButton item_button_close = dialog.findViewById(R.id.item_button_close);
                    Glide.with(v.getContext()).load(adapterPostList.get(i).getPostImageUrl()).into(item_imageview_image);
                    item_button_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });


            ((ProfileFragmentViewHolder)viewHolder).newsfeed_imageview_option.setOnClickListener(new View.OnClickListener() { //옵션 버튼 눌럿을 경우
                @Override
                public void onClick(View v) {
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

                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterPostList.size();
        }
    }
}
