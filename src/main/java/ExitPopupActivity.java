package com.example.dnjsr.bakingcat;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ExitPopupActivity extends AppCompatActivity {
    Button buttonYes ;
    Button buttonNo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_popup);
        buttonYes = findViewById(R.id.buttonyes);
        buttonNo = findViewById(R.id.buttonno);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setIcon(R.drawable.cat1);
        //actionBar.setTitle("식빵굽는 고양이");

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent(getApplicationContext(),LoginActivity.class);
                setResult(RESULT_OK,resultIntent);
                finish();
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("life","종료팝업 액티비티 start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("life","종료팝업 액티비티 resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("life","종료팝업 액티비티 pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("life","종료팝업 액티비티 stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("life","종료팝업 액티비티 destroy");
    }
}
