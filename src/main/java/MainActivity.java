package com.example.dnjsr.bakingcat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.drm.DrmStore;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.dnjsr.bakingcat.Fragment.ChatFragment;
import com.example.dnjsr.bakingcat.Fragment.FriendListFragment;
import com.example.dnjsr.bakingcat.Fragment.NewsFeedFragment;
import com.example.dnjsr.bakingcat.Fragment.ProfileFragment;
import com.example.dnjsr.bakingcat.Fragment.SearchFragment;
import com.example.dnjsr.bakingcat.currentUserInfo.CurrentUserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    NewsFeedFragment newsFeedFragment;
    ChatFragment chatFragment;
    SearchFragment searchFragment;
    ProfileFragment profileFragment;    //fragment객체
    BottomNavigationView bottomNavigationView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu,menu);
        Drawable drawable = menu.getItem(0).getIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_logout){
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
            adBuilder.setTitle("알림").setMessage("로그아웃 하시겠습니까?").setNegativeButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences appData = getSharedPreferences("appData",MODE_PRIVATE);
                    SharedPreferences.Editor editor= appData.edit();
                    editor.remove("autoLogin");
                    editor.commit();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    finish();
                }
            }).setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("currentuser","userId :"+CurrentUserInfo.getCurrentUserInfo().getUserId()+" Id :"+CurrentUserInfo.getCurrentUserInfo().getId()+"" +
                " nic :"+CurrentUserInfo.getCurrentUserInfo().getUserNicname()+
                " image :"+CurrentUserInfo.getCurrentUserInfo().getUserProfileImageUri()+" friendlist ; "+CurrentUserInfo.getCurrentUserInfo().getUserFriendList());
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("뉴스피드");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#2f2f30"));
        }


        bottomNavigationView = findViewById(R.id.main_bottomnavigationview);

        String userId = getIntent().getStringExtra("id");
        passPushTokenToServer(); //서버에 토큰 받아서 푸시.
        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

        newsFeedFragment = new NewsFeedFragment();
        chatFragment = new ChatFragment();
        searchFragment = new SearchFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,newsFeedFragment).commit();



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_newsfeed:
                        actionBar.setTitle("뉴스피드");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,newsFeedFragment).commit();
                        return true;
                    case R.id.action_chat:
                        actionBar.setTitle("채팅");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,chatFragment).commit();
                        return true;
                    case R.id.action_search:
                        actionBar.setTitle("친구추가");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,searchFragment).commit();
                        return true;
                    case R.id.action_profile:
                        actionBar.setTitle("프로필");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout,profileFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

    public void  passPushTokenToServer(){
        //String token = FirebaseInstanceId.getInstance().getToken(); //토큰을 얻어옴.
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "token못받아옴.", Toast.LENGTH_SHORT).show();
                }
                String token = task.getResult().getToken();
                Map<String,Object> map = new HashMap<>();
                map.put("userPushToken",token);//토큰 푸시
                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUserInfo.getCurrentUserInfo().getId()).updateChildren(map);
            }
        });
    }
}
