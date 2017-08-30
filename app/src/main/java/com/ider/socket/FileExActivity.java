package com.ider.socket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ider.socket.util.BackHandled;
import com.ider.socket.view.BoxFileFragment;

public class FileExActivity extends AppCompatActivity {
    private BoxFileFragment boxFileFragment;
    private BackHandled backHandled;
    private ImageView back;
    private TextView upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_ex);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        boxFileFragment= new BoxFileFragment();
        backHandled = boxFileFragment;
        back = (ImageView)findViewById(R.id.back_press);
        upload = (TextView)findViewById(R.id.upload_file);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileExActivity.this,FileSelectActivity.class);
                startActivity(intent);
            }
        });

        replaceFragment(boxFileFragment);
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.file_list,fragment);
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        backHandled.onBackPressed();
    }
}
