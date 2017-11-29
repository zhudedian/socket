package com.ider.socket;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.HTTPFileDownloadTask;
import com.ider.socket.util.ListSort;
import com.ider.socket.util.MyData;
import com.ider.socket.view.DirAdapter;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirSelectActivity extends Activity {

    private String TAG = "DirSelectActivity";
    private ListView listView;
    private ImageView close,done;
    private TextView selectPath;
    private OkHttpClient okHttpClient;
    private DirAdapter adapter;
    private List<BoxFile> dirs = new ArrayList<>();
    private List<BoxFile> allFiles= new ArrayList<>();
    private List<BoxFile> overWriteFiles= new ArrayList<>();
    private ProgressDialog progressDialog;
    private DirSelectActivity.HTTPdownloadHandler mHttpDownloadHandler;
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private URI mHttpUri;
    private int downloadSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_select);
        close = (ImageView)findViewById(R.id.close);
        done = (ImageView)findViewById(R.id.done);
        selectPath = (TextView)findViewById(R.id.select_path);
        listView = (ListView)findViewById(R.id.dir_list);
        adapter = new DirAdapter(DirSelectActivity.this,R.layout.dir_list_item,dirs);
        listView.setAdapter(adapter);
        okHttpClient = new OkHttpClient();
        try {
            mHttpUri = new URI(MyData.downUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyData.dirSelect = new File(dirs.get(position).getFilePath());
                selectPath.setText(MyData.dirSelect.getPath());
                dirs.clear();
                File[] files = MyData.dirSelect.listFiles();
                if (files != null){
                    for(File f:files){
                        if (f.isDirectory()){
                            dirs.add(new BoxFile(f.getName(), f.getPath()));
                        }
                        allFiles.add(new BoxFile(f.getName(), f.getPath()));
                    }
                }
                ListSort.sort(dirs);
                adapter.notifyDataSetChanged();
            }
        });

        init();


    }
    private void startDownLoad(){
        MyData.isDownloading = true;
        if (MyData.downLoadingFiles.size()>0) {
            BoxFile boxFile = MyData.downLoadingFiles.get(0);
            if (boxFile.getFileType()==1){
                File file = new File(boxFile.getSavePath()+"/"+boxFile.getFileName());
                if (!file.exists()){
                    file.mkdir();
                }
                openDownloadDir(boxFile);
            }else {
                if (boxFile.getSavePath()!=null) {
                    mHttpTask = new HTTPFileDownloadTask(boxFile, mHttpClient, mHttpUri, boxFile.getSavePath(), MyData.downLoadingFiles.get(0).getFileName(), 1);
                    mHttpTask.setProgressHandler(mHttpDownloadHandler);
                    mHttpTask.start();
                }else {
                    MyData.downLoadingFiles.remove(0);
                    startDownLoad();
                }
            }

        }else {
            MyData.isDownloading = false;
            mHandler.sendEmptyMessage(1);
        }
    }
    private void openDownloadDir(final BoxFile boxFile){
        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("comment",boxFile.getFilePath() )
                            .url(MyData.downUrl).build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String result = response.body().string();
                    Log.i("result",result);
                    if (!request.equals("null")) {
                        String[] files = result.split("\"type=\"");
                        String filePath = files[0];
                        for (int i = 1; i < files.length; i++) {
                            String[] fils = files[i].split("\"name=\"");
                            int type = Integer.parseInt(fils[0]);
                            String[] fis = fils[1].split("\"size=\"");
                            BoxFile boxFile2 = new BoxFile(type, fis[0], fis[1]);
                            boxFile2.setFilePath(filePath+File.separator+fis[0]);
                            boxFile2.setSavePath(boxFile.getSavePath()+File.separator+boxFile.getFileName());
                            MyData.downLoadingFiles.add(boxFile2);
                            downloadSize++;
                        }
                    }
                    MyData.downLoadingFiles.remove(0);
                    mHandler.sendEmptyMessage(0);
                    startDownLoad();
                } catch (Exception e) {
                    e.printStackTrace();
                    startDownLoad();
                }
            }
        }.start();
    }
    private void showProgressDialog(){
        progressDialog = new ProgressDialog(DirSelectActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setTitle("请稍后……");
        progressDialog.setMax(downloadSize);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyData.downLoadingFiles.clear();
                        mHttpTask.stopDownload();
                        mHandler.sendEmptyMessage(2);
                    }
                });
        progressDialog.setMessage(MyData.downLoadingFiles.get(0).getFileName());
        progressDialog.show();
    }
    private void showConfirmDialog() {
        View view = View.inflate(DirSelectActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(DirSelectActivity.this, popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView fileName = (TextView) view.findViewById(R.id.file_name);
        Button cancel = (Button) view.findViewById(R.id.cancel_action);
        Button ok = (Button) view.findViewById(R.id.ok_action);
        title.setText("确认下载？");
        if (MyData.selectBoxFiles.size() == 1) {
            fileName.setText(MyData.selectBoxFiles.get(0).getFileName());
        } else {
            fileName.setText("已选择" + MyData.selectBoxFiles.size() + "文件");
        }
        cancel.setText("取消");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setText("下载");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.downLoadingFiles.clear();
                PopuUtils.dismissPopupDialog();
                for (int i=0;i<MyData.selectBoxFiles.size();i++){
                    BoxFile boxFile=MyData.selectBoxFiles.get(i);
                    if (!allFiles.contains(boxFile)) {
                        boxFile.setSavePath(MyData.dirSelect.getPath());
                        MyData.downLoadingFiles.add(boxFile);
                    }else {
                        overWriteFiles.add(boxFile);
                    }
                }
                if (overWriteFiles.size()>0) {
                    showOverWriteDialog();
                }else {
                    downloadSize = MyData.downLoadingFiles.size();
                    if (!MyData.isDownloading) {
                        startDownLoad();
                        showProgressDialog();
                    }
                }


            }
        });
    }
    private void showOverWriteDialog(){
        View view = View.inflate(DirSelectActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(DirSelectActivity.this, popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        LinearLayout allSelect = (LinearLayout)view.findViewById(R.id.all_select);
        final CheckBox allcheck = (CheckBox)view.findViewById(R.id.all_select_check);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        if (overWriteFiles.size()==1){
            allSelect.setVisibility(View.GONE);
        }else {
            allSelect.setVisibility(View.VISIBLE);
        }
        title.setText("该文件已存在！");
        fileName.setText(overWriteFiles.get(0).getFileName());
        cancel.setText("跳过");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allcheck.isChecked()){
                    overWriteFiles.clear();
                    PopuUtils.dismissPopupDialog();
                    downloadSize = MyData.downLoadingFiles.size();
                    startDownLoad();
                    showProgressDialog();
                }else {
                    overWriteFiles.remove(0);
                    PopuUtils.dismissPopupDialog();
                    if (overWriteFiles.size() > 0) {
                        showOverWriteDialog();
                    } else {
                        downloadSize = MyData.downLoadingFiles.size();
                        startDownLoad();
                        showProgressDialog();
                    }
                }
            }
        });
        ok.setText("覆盖");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allcheck.isChecked()){
                    for (BoxFile boxFile:overWriteFiles){
                        boxFile.setSavePath(MyData.boxFilePath);
                        MyData.downLoadingFiles.add(boxFile);
                    }
                    overWriteFiles.clear();
                    downloadSize = MyData.downLoadingFiles.size();
                    startDownLoad();
                    showProgressDialog();
                    PopuUtils.dismissPopupDialog();
                }else {
                    BoxFile boxFile = overWriteFiles.get(0);
                    boxFile.setSavePath(MyData.dirSelect.getPath());
                    MyData.downLoadingFiles.add(boxFile);

                    PopuUtils.dismissPopupDialog();

                    overWriteFiles.remove(0);
                    if (overWriteFiles.size()>0){
                        showOverWriteDialog();
                    }else {
                        startDownLoad();
                        showProgressDialog();
                        downloadSize = MyData.downLoadingFiles.size();
                    }
                }

            }
        });
        allcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void init(){
        mHttpClient = CustomerHttpClient.getHttpClient();
        mHttpDownloadHandler = new DirSelectActivity.HTTPdownloadHandler();
        selectPath.setText(MyData.dirSelect.getPath());
        File[] files = MyData.dirSelect.listFiles();
        if (files != null){
            for(File f:files){
                if (f.isDirectory()){
                    dirs.add(new BoxFile(f.getName(),f.getPath()));
                }
                allFiles.add(new BoxFile(f.getName(), f.getPath()));
            }
        }
        ListSort.sort(dirs);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onBackPressed() {
            if (MyData.dirSelect.equals(Environment.getExternalStorageDirectory())) {
                finish();
            } else {
                dirs.clear();
                MyData.dirSelect = MyData.dirSelect.getParentFile();
                selectPath.setText(MyData.dirSelect.getPath());
                File[] files = MyData.dirSelect.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            dirs.add(new BoxFile(f.getName(), f.getPath()));
                        }
                        allFiles.add(new BoxFile(f.getName(), f.getPath()));
                    }
                }
                adapter.notifyDataSetChanged();
            }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    if (MyData.downLoadingFiles.size()>0) {
                        progressDialog.setMessage(MyData.downLoadingFiles.get(0).getFileName());
                    }
                    progressDialog.setMax(downloadSize);
                    progressDialog.incrementProgressBy(1);
                    break;
                case 1:
                    progressDialog.dismiss();
                    finish();
                    break;
                case 2:
                    progressDialog.setTitle("正在取消……");
                    break;

            }
        }
    };

    private class HTTPdownloadHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_UPDATE : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_UPDATE");
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE");
//                    Bundle bundle = msg.getData();
//                    String fileName = bundle.getString("fileName");
//                    String fileSize = bundle.getString("fileSize");
//                    BoxFile boxFile = new BoxFile(1,fileName,fileSize);

                    MyData.downLoadingFiles.remove(0);
                    mHandler.sendEmptyMessage(0);
                    startDownLoad();
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED");
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE");
                    startDownLoad();
                }
                break;
                default:
                    break;
            }
        }
    }
}
