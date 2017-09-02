package com.ider.socket;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ider.socket.db.BoxFile;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.HTTPFileDownloadTask;
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

import static android.R.string.cancel;
import static com.ider.socket.util.MyData.myApkFragment;
import static com.ider.socket.util.SocketClient.mHandler;

public class DirSelectActivity extends Activity {

    private String TAG = "DirSelectActivity";
    private ListView listView;
    private ImageView close,done;
    private TextView selectPath;
    private OkHttpClient okHttpClient;
    private DirAdapter adapter;
    private List<BoxFile> dirs = new ArrayList<>();
    private DirSelectActivity.HTTPdownloadHandler mHttpDownloadHandler;
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private URI mHttpUri;
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

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<MyData.selectBoxFiles.size();i++){
                    BoxFile boxFile=MyData.selectBoxFiles.get(i);
                    if (boxFile.getSavePath()==null){
                        boxFile.setSavePath(MyData.dirSelect.getPath());
                        MyData.downLoadingFiles.add(boxFile);
                    }
                }
                if (!MyData.isDownloading) {
                    MyData.isDownloading = true;
                    startDownLoad();
                }
                finish();
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
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        init();


    }
    private void startDownLoad(){
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
                        String[] files = result.split("type=");
                        String filePath = files[0];
                        for (int i = 1; i < files.length; i++) {
                            String[] fils = files[i].split("name=");
                            int type = Integer.parseInt(fils[0]);
                            String[] fis = fils[1].split("size=");
                            BoxFile boxFile2 = new BoxFile(type, fis[0], fis[1]);
                            boxFile2.setFilePath(filePath+"/"+fis[0]);
                            boxFile2.setSavePath(boxFile.getSavePath()+"/"+boxFile.getFileName());
                            MyData.downLoadingFiles.add(boxFile2);
                        }
                    }
                    MyData.downLoadingFiles.remove(0);
                    startDownLoad();
                } catch (Exception e) {
                    e.printStackTrace();
                    startDownLoad();
                }
            }
        }.start();
    }

    private void init(){
        try {
            mHttpUri = new URI("http://192.168.2.15:8080/down");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mHttpClient = CustomerHttpClient.getHttpClient();
        mHttpDownloadHandler = new DirSelectActivity.HTTPdownloadHandler();
        selectPath.setText(MyData.dirSelect.getPath());
        File[] files = MyData.dirSelect.listFiles();
        if (files != null){
            for(File f:files){
                if (f.isDirectory()){
                    dirs.add(new BoxFile(f.getName(),f.getPath()));
                }
            }
        }
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
                    }
                }
                adapter.notifyDataSetChanged();
            }
    }

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
