package com.example.dnjsr.bakingcat;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.dnjsr.bakingcat.Fragment.FollowingFragment;
import com.example.dnjsr.bakingcat.Fragment.FriendListFragment;

public class FriendActivity extends AppCompatActivity {
    FollowingFragment followingFragment;
    FriendListFragment friendListFragment;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }
        bottomNavigationView = findViewById(R.id.friend_bottomnavigationview);
        followingFragment = new FollowingFragment();
        friendListFragment = new FriendListFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.friend_framelayout,friendListFragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_friendlist:
                        getSupportFragmentManager().beginTransaction().replace(R.id.friend_framelayout,friendListFragment).commit();
                        return true;
                    case R.id.action_friendplus:
                        getSupportFragmentManager().beginTransaction().replace(R.id.friend_framelayout,followingFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
}
