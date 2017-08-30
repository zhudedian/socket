package com.ider.socket;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ider.socket.db.ApkFile;
import com.ider.socket.util.MyData;
import com.ider.socket.view.ApkAdapter;
import com.ider.socket.view.MyApkFragment;
import com.ider.socket.view.UninstallFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.fragment;


public class UploadActivity extends AppCompatActivity {


    private ImageView fresh;
    private Button myApk,unstApk;
    private UninstallFragment unfragment;
    private MyApkFragment myApkFragment;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (MyData.myApkFragment==null){
            unfragment = new UninstallFragment();
            myApkFragment = new MyApkFragment();
            MyData.myApkFragment = myApkFragment;
            MyData.uninstallFragment= unfragment;
        }else {
            unfragment = MyData.uninstallFragment;
            myApkFragment = MyData.myApkFragment;
        }


        myApk = (Button) findViewById(R.id.my_apk);
        unstApk = (Button) findViewById(R.id.uninstall_apk);
        fresh = (ImageView) findViewById(R.id.fresh);
        myApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page==1){
                    page = 0;
                    replaceFragment(myApkFragment);
                }

            }
        });
        unstApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page == 0 ) {
                    page = 1;
                    replaceFragment(unfragment);
                }
            }
        });
        fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfragment.qurryApk();
            }
        });
        if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(UploadActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else {
            replaceFragment(myApkFragment);
        }
//        packageManager = getPackageManager();
//        List<PackageInfo> mAllPackages;
//        mAllPackages = packageManager.getInstalledPackages(0);
//        for(int i = 0; i < mAllPackages.size(); i ++)
//        {
//            PackageInfo packageInfo = mAllPackages.get(i);
//            Log.i("package path", packageInfo.applicationInfo.sourceDir);
//            Log.i("apk name", packageInfo.applicationInfo.loadLabel(packageManager)+"");
//        }

    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.install_apk,fragment);
        transaction.commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    replaceFragment(myApkFragment);
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle bundle){
        Log.i("onSaveInstanceState","onSaveInstanceState");
    }

}
