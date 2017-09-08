package com.ider.socket.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ider.socket.FileSelectActivity;
import com.ider.socket.R;
import com.ider.socket.db.ApkFile;
import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.FileUtil;
import com.ider.socket.util.FreshHanled;
import com.ider.socket.util.MyData;
import com.ider.socket.util.UploadUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Eric on 2017/8/28.
 */

public class UninstallFragment extends Fragment implements FreshHanled{

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private ListView apkList;
    private ProgressBar progressBar;
    private ApkAdapter adapter;
    private List<ApkFile> apks;
    private String apkPath,installResult;
    private File[] files;
    private List<String> dataList = new ArrayList<>();

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
                showInstallDialog(apkFile);
            }
        });


    }
    @Override
    public void fresh(){
        qurryApk();
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
            apks = new ArrayList<>();
            adapter = new ApkAdapter(getContext(),R.layout.apk_list_item, apks);
            apkList.setAdapter(adapter);
            qurryApk();
        }
    }
    public void qurryApk(){
        apks.clear();
        adapter.notifyDataSetChanged();
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
        if (apks.size()>0){
            editor.putBoolean("data_save", true);
            editor.apply();
        }
    }
    private void showInstallDialog(final ApkFile apkFile){

        View view = View.inflate(getContext(), R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(getContext(), popup);
        popupDialog.showAtLocation(apkList, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        LinearLayout allSelect = (LinearLayout)view.findViewById(R.id.all_select);
        final CheckBox allcheck = (CheckBox)view.findViewById(R.id.all_select_check);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        allSelect.setVisibility(View.GONE);
        title.setText("确认安装");
        fileName.setText(apkFile.getFileName());
        cancel.setText("取消");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setText("安装");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apkPath = apkFile.getFilePath();
                showProgressDialog(apkFile.getFileName());
                new Thread(){
                    @Override
                    public void run(){
                        File file = new File(apkPath);
                        if(file!=null)
                        {
                            int res = UploadUtil.uploadFile( file, MyData.installUrl,"");
                            Log.i("tag","request="+res);
                            if (res==200){
                                mHandler.sendEmptyMessage(3);
                            }else {
                                mHandler.sendEmptyMessage(4);
                            }

                        }
                    }
                }.start();
                PopuUtils.dismissPopupDialog();
            }
        });
        allcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void showProgressDialog(String name){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setTitle("正在安装，请稍后……");
        progressDialog.setMessage(name);
        progressDialog.show();
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
                case 3:
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),"安装成功",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),"安装失败",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };
}
