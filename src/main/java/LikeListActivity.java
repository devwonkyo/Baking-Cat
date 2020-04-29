package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LikeListActivity extends AppCompatActivity {
    List<UserInfo> likeUserList = new ArrayList<>();
    RecyclerView likelist_recyclerview;
    LikeListAdapter likeListAdapter;
    String postId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        likelist_recyclerview = findViewById(R.id.likelist_recyclerview);
        postId = getIntent().getStringExtra("postId");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("좋아요");
        Log.d("qwer",postId);
        likelist_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        likeListAdapter = new LikeListAdapter(likeUserList);
        likelist_recyclerview.setAdapter(likeListAdapter);

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

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("posts").child(postId).child("postLikeList").addListenerForSingleValueEvent(new ValueEventListener() {//좋아요 누른 사람 리스트 가져옴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likeUserList.clear();
                List<String> postLikeList = (List<String>) dataSnapshot.getValue();
                for(final String userId : postLikeList){//리스트한명씩 돌면서 서버에서 객체를 가져옴
                    FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                            likeUserList.add(userInfo);//가져온 객체 추가.
                            likeListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class LikeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserInfo> adapterLikeUserList;

        public LikeListAdapter(List<UserInfo> likeUserList) {
            this.adapterLikeUserList = likeUserList;
        }

        public class LikeListViewHolder extends RecyclerView.ViewHolder {
            ImageView itemlikepeople_imageview_profile;
            TextView itemlikepeople_textview_name;
            TextView itemlikepeople_textview_me;
            public LikeListViewHolder(View view) {
                super(view);
                itemlikepeople_imageview_profile = view.findViewById(R.id.itemlikepeople_imageview_profile);
                itemlikepeople_textview_name = view.findViewById(R.id.itemlikepeople_textview_name);
                itemlikepeople_textview_me = view.findViewById(R.id.itemlikepeople_textview_me);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_likepeople,viewGroup,false);
            return new LikeListViewHolder(view);
        }



        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder,final int i) {
            Glide.with(viewHolder.itemView.getContext()).load(adapterLikeUserList.get(i).getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((LikeListViewHolder)viewHolder).itemlikepeople_imageview_profile);
            if(adapterLikeUserList.get(i).getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){
                ((LikeListViewHolder)viewHolder).itemlikepeople_textview_me.setVisibility(View.VISIBLE);
            }
            ((LikeListViewHolder)viewHolder).itemlikepeople_textview_name.setText(adapterLikeUserList.get(i).getUserNicname());
            ((LikeListViewHolder)viewHolder).itemlikepeople_imageview_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
                    intent.putExtra("userId",adapterLikeUserList.get(i).getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return adapterLikeUserList.size();
        }


    }
}
