package com.ider.socket;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ider.socket.db.ApkFile;
import com.ider.socket.util.FileControl;
import com.ider.socket.util.UploadUtil;
import com.ider.socket.view.ApkAdapter;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UploadActivity extends AppCompatActivity {

    private String newName = "htys.mp3";
    //要上传的本地文件路径
    private String uploadFile = "/data/data/com.xzq/htys.mp3";
    //上传到服务器的指定位置
    private String requestURL = "http://192.168.2.15:8080/upload";
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private List<String> fileList=new ArrayList<>();
    private ListView apkList;
    private ImageView fresh;
    private ApkAdapter adapter;
    private ProgressBar progressBar;
    private File[] files;
    private List<ApkFile> apks = new ArrayList<>();
    private String upload,request;
    private boolean isDataSave ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        preferences = getSharedPreferences("socket", Context.MODE_PRIVATE);
        editor = preferences.edit();

        progressBar = (ProgressBar)findViewById(R.id.progress_bar) ;
        apkList = (ListView) findViewById(R.id.apk_list);
        fresh = (ImageView) findViewById(R.id.fresh);
        fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qurryApk();
            }
        });
        if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(UploadActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else {
            showApk();
        }
    }

    private void findXFormat(File[] files) {


        for(File f:files){//遍历展开后的文件夹的文件
            if(f.isDirectory()){//如果是文件夹，继续展开
                File[] filess = f.listFiles();
                if (filess!=null)findXFormat(filess);//用递归递归
            }else if(FileControl.isApk(f)){
                fileList.add(f.getPath());//符合格式的添加入列
                ApkFile apkFile = new ApkFile(f.getName(),f.getPath());
                apks.add(apkFile);
                DataSupport.deleteAll(ApkFile.class,"filePath = ?",f.getPath());
                apkFile.save();
            }
        }




    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    apkList.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    Toast.makeText(UploadActivity.this,request,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };
    private void qurryApk(){
        fileList.clear();
        apks.clear();
        progressBar.setVisibility(View.VISIBLE);
        apkList.setVisibility(View.GONE);
        fileList = new ArrayList<String>();
        File file = Environment.getExternalStorageDirectory(); //从SD的根目录开始
        files = file.listFiles();     //本方法返回该文件夹展开后的所有文件的数组

        if (files != null) {
            new Thread(){
                @Override
                public void run(){
                    findXFormat(files);
                    mHandler.sendEmptyMessage(1);
                }
            }.start();

        }
        if (fileList.size()>0){
            editor.putBoolean("data_save", true);
            editor.apply();
        }
    }
    private void showApk(){
        isDataSave = preferences.getBoolean("data_save", false);
        if (isDataSave){
            apks = DataSupport.findAll(ApkFile.class);
        }else {
            qurryApk();
        }


       adapter = new ApkAdapter(UploadActivity.this,R.layout.apk_list_item, apks);


        apkList.setAdapter(adapter);
        apkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApkFile apkFile = apks.get(position);
                upload = apkFile.getFilePath();
                new Thread(){
                    @Override
                    public void run(){
                        File file = new File(upload);
                        if(file!=null)
                        {
                            request = UploadUtil.uploadFile( file, requestURL);
                            Log.i("tag","request="+request);
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                }.start();

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    showApk();
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

}
