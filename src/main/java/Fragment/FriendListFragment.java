package com.example.dnjsr.bakingcat.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.dnjsr.bakingcat.R;
import com.example.dnjsr.bakingcat.converter.BitmapConverter;
import com.example.dnjsr.bakingcat.converter.JsonParser;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.example.dnjsr.bakingcat.info.UserInfo;
import com.example.dnjsr.bakingcat.sharedPreferencesManage.DataManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {
    EditText friendlistfragment_edittext_searchfriend;
    RecyclerView friendlistfragment_recyclerview;
    FriendListFragmentAdapter friendListFragmentAdapter;
    List<UserInfo> friendList = new ArrayList<>();
    TextView friendlistfragment_textview_nofriend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendlist,container,false);
        friendlistfragment_edittext_searchfriend = view.findViewById(R.id.friendlistfragment_edittext_searchfriend);
        friendlistfragment_textview_nofriend = view.findViewById(R.id.friendlistfragment_textview_nofriend);
        friendlistfragment_recyclerview = view.findViewById(R.id.friendlistfragment_recyclerview);
        friendlistfragment_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        friendListFragmentAdapter = new FriendListFragmentAdapter(friendList); //생성자로 리스트(데이터) 넘겨 주어야함.
        friendlistfragment_recyclerview.setAdapter(friendListFragmentAdapter);

        friendlistfragment_edittext_searchfriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setFilteredFriendList(s.toString());
            }
        });

        return view;
    }

    public void setFilteredFriendList(String friendName){
        List<UserInfo> filteredFriendList = new ArrayList<>();
        if(friendName.equals("")){
            filteredFriendList = friendList;
        }else{
            for(UserInfo userInfo : friendList){
                if(userInfo.getUserNicname().toLowerCase().contains(friendName)){ //이름
                    filteredFriendList.add(userInfo);
                }
            }
        }
        friendListFragmentAdapter.dataChange(filteredFriendList);
    }

    @Override
    public void onResume() {
        super.onResume();
        friendlistfragment_edittext_searchfriend.setText("");
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() { //데이터 바뀔때마다 모든 유저 불러옴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendList.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    UserInfo userInfo = item.getValue(UserInfo.class);
                    if(CurrentUserInfo.getCurrentUserInfo().getUserFriendList().contains(userInfo.getId())){ //친구목록에 있는 사람만 친구리스트에 추가
                        friendList.add(userInfo);
                    }
                    if(userInfo.getId().equals(CurrentUserInfo.getCurrentUserInfo().getId())){ //내 아이디일 경우 CurrentUser갱신
                        CurrentUserInfo.setCurrentUserInfo(userInfo);
                    }
                }
                friendListFragmentAdapter.notifyDataSetChanged();
                if(friendList.isEmpty()){
                    friendlistfragment_textview_nofriend.setVisibility(View.VISIBLE);
                }else{
                    friendlistfragment_textview_nofriend.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public class FriendListFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UserInfo> adapterFriendList;
         public FriendListFragmentAdapter(List<UserInfo> friendList) {
            adapterFriendList = friendList;
         }

         public class FriendListFragmentViewHolder extends RecyclerView.ViewHolder{
             ImageView frienddeleteitem_imageview_profile;
             TextView frienddeleteitem_textview_name;
             Button frienddeleteitem_button_delete;

             public FriendListFragmentViewHolder(@NonNull View itemView) {
                 super(itemView);
                 frienddeleteitem_imageview_profile = itemView.findViewById(R.id.frienddeleteitem_imageview_profile);
                 frienddeleteitem_textview_name = itemView.findViewById(R.id.frienddeleteitem_textview_name);
                 frienddeleteitem_button_delete = itemView.findViewById(R.id.frienddeleteitem_button_delete);
             }
         }

         @NonNull
         @Override
         public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
             View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_frienddelete,viewGroup,false);
             return new FriendListFragmentViewHolder(view);
         }

         @Override
         public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
             Glide.with(viewHolder.itemView.getContext()).load(adapterFriendList.get(i).getUserProfileImageUri()).apply(new RequestOptions().circleCrop()).into(((FriendListFragmentViewHolder)viewHolder).frienddeleteitem_imageview_profile);
             ((FriendListFragmentViewHolder)viewHolder).frienddeleteitem_textview_name.setText(adapterFriendList.get(i).getUserNicname());


             ((FriendListFragmentViewHolder)viewHolder).frienddeleteitem_button_delete.setOnClickListener(new View.OnClickListener() { //친구삭제버튼 눌럿을 경우
                 @Override
                 public void onClick(View v) { //팔로우 취소 누를 경우
                     FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFriendList").addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             List<String> friendList = (List<String>) dataSnapshot.getValue();
                             friendList.remove(adapterFriendList.get(i).getId());
                             FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).child("userFriendList").setValue(friendList);
                             Toast.makeText(getContext(), adapterFriendList.get(i).getUserId() +" 님을 더 이상 팔로잉하지 않습니다.", Toast.LENGTH_SHORT).show();
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });

                     }

             });
         }
        public void dataChange(List<UserInfo> filteredFriendList){
            adapterFriendList= filteredFriendList;
            notifyDataSetChanged();
        }

         @Override
         public int getItemCount() {
             return adapterFriendList.size();
         }

     }
}
