package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.PostInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    ImageView profileactivity_imageview_profile;
    TextView profileactivity_textview_postnum;
    TextView profileactivity_textview_followernum;
    TextView profileactivity_textview_folloingnum;
    TextView profileactivity_textview_name;
    Button profileactivity_button_modifyprofile;
    Button profileactivity_button_chat;
    RecyclerView profileactivity_recyclerview;
    List<PostInfo> postList = new ArrayList<>();
    String userId;
    ProfileAdapter profileAdapter;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        userId = getIntent().getStringExtra("userId");
        actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        profileactivity_imageview_profile = findViewById(R.id.profileactivity_imageview_profile);
        profileactivity_textview_postnum = findViewById(R.id.profileactivity_textview_postnum);
        profileactivity_textview_followernum = findViewById(R.id.profileactivity_textview_followernum);
        profileactivity_textview_folloingnum = findViewById(R.id.profileactivity_textview_folloingnum);
        profileactivity_textview_name = findViewById(R.id.profileactivity_textview_name);
        profileactivity_button_modifyprofile = findViewById(R.id.profileactivity_button_modifyprofile);
        profileactivity_button_chat = findViewById(R.id.profileactivity_button_chat);
        profileactivity_recyclerview = findViewById(R.id.profileactivity_recyclerview);
        if(userId.equals(CurrentUserInfo.getCurrentUserInfo().getId())){
            profileactivity_button_chat.setVisibility(View.GONE);
        }else{
            profileactivity_button_modifyprofile.setVisibility(View.GONE);
        }
        setUserInfo();//유저이름,프로필,팔로우,팔로워 셋팅
        setPostList();//유저가 작성한 포스트 셋팅

        GridLayoutManager myGridLayoutManager = new GridLayoutManager(this,3);
        profileAdapter = new ProfileAdapter(postList);
        profileactivity_recyclerview.setLayoutManager(myGridLayoutManager);
        profileactivity_recyclerview.setAdapter(profileAdapter);

        profileactivity_button_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChatRoomActivity.class);
                intent.putExtra("otherUserId",userId); //선택한 아이디
                startActivity(intent);
            }
        });

        profileactivity_button_modifyprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),ModifyProfileActivity.class));
            }
        });

    }

    public void setUserInfo(){
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                Glide.with(getApplicationContext())
                        .load(userInfo.getUserProfileImageUri())
                        .apply(new RequestOptions().circleCrop())
                        .into(profileactivity_imageview_profile);

                profileactivity_textview_followernum.setText(String.valueOf(userInfo.getUserFriendList().size()));
                profileactivity_textview_folloingnum.setText(String.valueOf(userInfo.getUserFollowerList().size()));
                profileactivity_textview_name.setText(userInfo.getUserNicname());
                actionBar.setTitle(userInfo.getUserId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setPostList(){
        FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    PostInfo postInfo = item.getValue(PostInfo.class);
                    postList.add(postInfo);
                }
                profileactivity_textview_postnum.setText(String.valueOf(postList.size()));
                profileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public class ProfileViewHolder extends RecyclerView.ViewHolder{
            ImageView itempostimgae_imageview_image;
            public ProfileViewHolder(@NonNull View itemView) {
                super(itemView);
                itempostimgae_imageview_image = itemView.findViewById(R.id.itempostimgae_imageview_image);
            }
        }

        List<PostInfo> adapterPostList;
        public ProfileAdapter(List<PostInfo> postList) {
            adapterPostList = postList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gridpostimage,viewGroup,false);
            return new ProfileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder,final int i) {
            Glide.with(viewHolder.itemView.getContext()).load(adapterPostList.get(i).getPostImageUrl()).into(((ProfileViewHolder)viewHolder).itempostimgae_imageview_image);

            ((ProfileViewHolder)viewHolder).itempostimgae_imageview_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),PostActivity.class);
                    intent.putExtra("postId",adapterPostList.get(i).getPostId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterPostList.size();
        }
    }

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
}
