package com.example.dnjsr.bakingcat.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {
    FollowingFragmentAdapter followingFragmentAdapter;
    RecyclerView followingfragment_recyclerview;
    TextView followingfragment_textview_norequest;
    List<UserInfo> followerList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followinglist,container,false);
        followingfragment_recyclerview = view.findViewById(R.id.followingfragment_recyclerview);
        followingfragment_textview_norequest = view.findViewById(R.id.followingfragment_textview_norequest);
        followingfragment_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        followingFragmentAdapter = new FollowingFragmentAdapter(followerList);
        followingfragment_recyclerview.setAdapter(followingFragmentAdapter);

        return view;
    }

    @Override
    public void onResume() {
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() { //모든 유저를 가져옴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               followerList.clear();
               for(DataSnapshot item : dataSnapshot.getChildren()){
                   UserInfo userInfo = item.getValue(UserInfo.class);
                   if(CurrentUserInfo.getCurrentUserInfo().getUserFollowerList().contains(userInfo.getId())){  //현재 유저의 팔로워리스트에 있는 유저아이디의 객체들만 추가.
                       followerList.add(userInfo);
                   }
                   if(userInfo.getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){
                       CurrentUserInfo.setCurrentUserInfo(userInfo);//현재유저 일 경우 갱신
                   }
               }
               if(followerList.isEmpty()){
                   followingfragment_textview_norequest.setVisibility(View.VISIBLE);
               }else{
                   followingfragment_textview_norequest.setVisibility(View.INVISIBLE);
               }
               followingFragmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        super.onResume();
    }

    public class FollowingFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserInfo> followerAdapterList;
        public FollowingFragmentAdapter(List<UserInfo> followerList) { //데이터 입력 생성자
            followerAdapterList = followerList;
        }

        public class FollowingFragmentViewHolder extends RecyclerView.ViewHolder{
            ImageView itemfollowing_imageview_profile;
            TextView itemfollowing_textview_message;
            Button itemfollowing_button_refuse;
            Button itemfollowing_button_accept;

            public FollowingFragmentViewHolder(@NonNull View itemView) {
                super(itemView);
                itemfollowing_imageview_profile = itemView.findViewById(R.id.itemfollowing_imageview_profile);
                itemfollowing_button_refuse = itemView.findViewById(R.id.itemfollowing_button_refuse);
                itemfollowing_button_accept = itemView.findViewById(R.id.itemfollowing_button_accept);
                itemfollowing_textview_message = itemView.findViewById(R.id.itemfollowing_textview_message);
            }
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_following,viewGroup,false);

            return new FollowingFragmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            Glide.with(viewHolder.itemView.getContext()).load(followerAdapterList.get(i).getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((FollowingFragmentViewHolder)viewHolder).itemfollowing_imageview_profile);
            ((FollowingFragmentViewHolder)viewHolder).itemfollowing_textview_message.setText(followerAdapterList.get(i).getUserNicname()+"님이 팔로우를 요청했습니다.");


            ((FollowingFragmentViewHolder)viewHolder).itemfollowing_button_accept.setOnClickListener(new View.OnClickListener() { //친구 수락버튼
                @Override
                public void onClick(View v) { //수락했을 경우
                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                            if(!userInfo.getUserFriendList().contains(followerAdapterList.get(i).getId())){  //친구목록에 팔로워 요청한 사람이 존재하지 않을 경우에만(친구목록중복 막음)
                                userInfo.getUserFriendList().add(followerAdapterList.get(i).getId());// 팔로워 요청수락 했을 경우 친구목록으로 추가 후
                                userInfo.getUserFollowerList().remove(followerAdapterList.get(i).getId()); //팔로워 요청에서 삭제.
                                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).setValue(userInfo); //데이터베이스 현재 유저객체 수정
                                Toast.makeText(getContext(), "팔로우를 수락했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            ((FollowingFragmentViewHolder)viewHolder).itemfollowing_button_refuse.setOnClickListener(new View.OnClickListener() {// 친구 거절 버튼
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFollowerList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> followerList = (List<String>) dataSnapshot.getValue();
                            followerList.remove(followerAdapterList.get(i).getId());//삭제
                            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFollowerList").setValue(followerList);//데이터 베이스 팔로워리스트 수정
                            Toast.makeText(getContext(), "팔로우를 거절했습니다.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return followerAdapterList.size();
        }
    }
}
