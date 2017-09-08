package com.ider.socket.view;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.ider.socket.R;
import com.ider.socket.db.ApkFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.FreshHanled;
import com.ider.socket.util.MyData;
import com.ider.socket.util.UploadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2017/8/28.
 */

public class MyApkFragment extends Fragment implements FreshHanled{
    private PackageManager packageManager;
    private ProgressDialog progressDialog;
    private ListView apkList;
    private ProgressBar progressBar;
    private ApkAdapter adapter;
    private String apkPath,installResult;
    private String requestURL = "http://192.168.2.15:8080/install";
    private List<ApkFile> apks ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("tag","onCreateView");
        View view = inflater.inflate(R.layout.uninstall_apk,container,false);
        apkList = (ListView) view.findViewById(R.id.apk_list);
        apkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApkFile apkFile = apks.get(position);
                showInstallDialog(apkFile);
            }
        });
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar) ;
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if (apks==null) {
            apks = new ArrayList<>();
            progressBar.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
                    qurryApk();
                }
            }.start();

        }
        adapter = new ApkAdapter(getContext(),R.layout.apk_list_item, apks);
        apkList.setAdapter(adapter);
    }
    @Override
    public void fresh(){
        apks.clear();
        progressBar.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        new Thread() {
            @Override
            public void run() {
                qurryApk();
            }
        }.start();
    }
    private void qurryApk(){

        packageManager = getContext().getPackageManager();
        List<PackageInfo> mAllPackages;
        mAllPackages = packageManager.getInstalledPackages(0);
        for(int i = 0; i < mAllPackages.size(); i ++)
        {
            PackageInfo packageInfo = mAllPackages.get(i);
            String path = packageInfo.applicationInfo.sourceDir;
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            Drawable drawable = packageInfo.applicationInfo.loadIcon(packageManager);
            if (path.contains("/data/app/")) {
                ApkFile apkFile = new ApkFile(name, path, drawable);
                if (!apks.contains(apkFile)){
                    apks.add(apkFile);
                }
//                if (apks.size()==7){
//                    mHandler.sendEmptyMessage(0);
//                }
            }

//            Log.i("package path", packageInfo.applicationInfo.sourceDir);
//            Log.i("apk name", packageInfo.applicationInfo.loadLabel(packageManager).toString());
        }
        mHandler.sendEmptyMessage(0);
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
