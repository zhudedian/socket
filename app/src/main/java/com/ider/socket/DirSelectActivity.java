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

import static android.R.string.cancel;
import static com.ider.socket.util.MyData.myApkFragment;

public class DirSelectActivity extends Activity {

    private String TAG = "DirSelectActivity";
    private ListView listView;
    private ImageView close;
    private TextView downLoad;
    private DirAdapter adapter;
    private File dir;
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
        downLoad = (TextView)findViewById(R.id.down_load);
        listView = (ListView)findViewById(R.id.dir_list);
        adapter = new DirAdapter(DirSelectActivity.this,R.layout.dir_list_item,dirs);
        listView.setAdapter(adapter);

        downLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i =0;i<MyData.downLoadingFiles.size();i++) {
                    BoxFile boxFile = MyData.downLoadingFiles.get(i);
                    try {
                        mHttpUri = new URI("http://192.168.2.15:8080/down");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    mHttpClient = CustomerHttpClient.getHttpClient();
                    mHttpDownloadHandler = new DirSelectActivity.HTTPdownloadHandler();
                    mHttpTask = new HTTPFileDownloadTask(boxFile, mHttpClient, mHttpUri, dir.getPath(), MyData.downLoadingFiles.get(i).getFileName(), 1);
                    mHttpTask.setProgressHandler(mHttpDownloadHandler);
                    mHttpTask.start();

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
                dir = new File(dirs.get(position).getFilePath());
                dirs.clear();
                File[] files = dir.listFiles();
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

    private void init(){
        dir = Environment.getExternalStorageDirectory();
        File[] files = dir.listFiles();
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
            if (dir.equals(Environment.getExternalStorageDirectory())) {
                finish();
            } else {
                dirs.clear();
                dir = dir.getParentFile();
                File[] files = dir.listFiles();
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
                    MyData.downLoadingFiles.clear();
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED");
                }
                break;
                case HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE : {
                    Log.i(TAG, "HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE");
                }
                break;
                default:
                    break;
            }
        }
    }
}
