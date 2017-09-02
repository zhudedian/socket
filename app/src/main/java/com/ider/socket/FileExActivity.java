package com.ider.socket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.ider.socket.util.BackHandled;
import com.ider.socket.view.BoxFileFragment;

public class FileExActivity extends AppCompatActivity {
    private BoxFileFragment boxFileFragment;
    private BackHandled backHandled;
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
        replaceFragment(boxFileFragment);
    }
    @Override
    protected void onResume(){
        super.onResume();
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
