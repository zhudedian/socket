package com.ider.socket.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ider.socket.R;
import com.ider.socket.db.ApkFile;

import com.ider.socket.util.FileUtil;
import com.ider.socket.util.UploadUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ider.socket.util.FileUtil.isApk;

/**
 * Created by Eric on 2017/8/28.
 */

public class UninstallFragment extends Fragment {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ListView apkList;
    private ProgressBar progressBar;
    private ApkAdapter adapter;
    private List<ApkFile> apks;
    private String apkPath,installResult;
    private File[] files;
    private List<String> dataList = new ArrayList<>();
    private String requestURL = "http://192.168.2.15:8080/upload";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("tag","onCreateView");
        View view = inflater.inflate(R.layout.uninstall_apk,container,false);
        apkList = (ListView) view.findViewById(R.id.apk_list);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar) ;
//        adapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1, dataList);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i("tag","onActivityCreated");
        preferences = getContext().getSharedPreferences("socket", Context.MODE_PRIVATE);
        editor = preferences.edit();
        showApk();
        apkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApkFile apkFile = apks.get(position);
                apkPath = apkFile.getFilePath();
                new Thread(){
                    @Override
                    public void run(){
                        File file = new File(apkPath);
                        if(file!=null)
                        {
                            installResult = UploadUtil.uploadFile( file, requestURL);
                            Log.i("tag","request="+installResult);
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                }.start();

            }
        });


    }
    private void showApk() {
        boolean isDataSave = preferences.getBoolean("data_save", false);
        if (isDataSave) {
            if (apks==null){
                apks = new ArrayList<>();
                apks = DataSupport.findAll(ApkFile.class);
                for (int i = 0;i<apks.size();i++){
                    ApkFile apk = apks.get(i);
                    File file = new File(apk.getFilePath());
                    if (!file.exists()){
                        DataSupport.deleteAll(ApkFile.class,"filePath = ?",apk.getFilePath());
                    }else {
                        dataList.add(file.getName());
                    }
                }
                apks= DataSupport.findAll(ApkFile.class);
            }
            adapter = new ApkAdapter(getContext(),R.layout.apk_list_item, apks);
            apkList.setAdapter(adapter);
            Log.i("apks",apks.size()+"");
            File file = Environment.getExternalStorageDirectory(); //从SD的根目录开始
            files = file.listFiles();     //本方法返回该文件夹展开后的所有文件的数组
            if (files != null) {
                new Thread(){
                    @Override
                    public void run(){
                        findApk(files);
                        mHandler.sendEmptyMessage(0);
                    }
                }.start();
            }
        } else {
            adapter = new ApkAdapter(getContext(),R.layout.apk_list_item, apks);
            apkList.setAdapter(adapter);
            qurryApk();
            if (apks.size()>0){
                editor.putBoolean("data_save", true);
                editor.apply();
            }
            mHandler.sendEmptyMessage(0);
        }
    }
    public void qurryApk(){
        progressBar.setVisibility(View.VISIBLE);
        File file = Environment.getExternalStorageDirectory(); //从SD的根目录开始
        files = file.listFiles();     //本方法返回该文件夹展开后的所有文件的数组
        if (files != null) {
            new Thread(){
                @Override
                public void run(){
                    findApk(files);
                    mHandler.sendEmptyMessage(0);
                }
            }.start();
        }

    }
    private void findApk(File[] files) {
        for(File f:files){//遍历展开后的文件夹的文件
            if(f.isDirectory()){//如果是文件夹，继续展开
                File[] filess = f.listFiles();
                if (filess!=null)findApk(filess);//用递归递归
            }else if(FileUtil.isApk(f)){
                ApkFile apkFile = new ApkFile(f.getName(),f.getPath());
                if (!apks.contains(apkFile)){
                    apks.add(apkFile);
                    mHandler.sendEmptyMessage(0);
                    DataSupport.deleteAll(ApkFile.class,"filePath = ?",f.getPath());
                    apkFile.save();
                }
            }
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    progressBar.setVisibility(View.GONE);
                    apkList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case 1:

                    break;
                case 2:
                    Toast.makeText(getContext(),installResult,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };
}
