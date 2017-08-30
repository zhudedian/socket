package com.ider.socket.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.ider.socket.util.UploadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ider.socket.util.SocketClient.mHandler;

/**
 * Created by Eric on 2017/8/28.
 */

public class MyApkFragment extends Fragment {
    private PackageManager packageManager;
    private ListView apkList;
    private ProgressBar progressBar;
    private ApkAdapter adapter;
    private String apkPath,installResult;
    private String requestURL = "http://192.168.2.15:8080/upload";
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
                apks.add(new ApkFile(name, path, drawable));
                mHandler.sendEmptyMessage(0);
            }

            Log.i("package path", packageInfo.applicationInfo.sourceDir);
            Log.i("apk name", packageInfo.applicationInfo.loadLabel(packageManager).toString());
        }
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
                default:
                    break;
            }

        }
    };
}
