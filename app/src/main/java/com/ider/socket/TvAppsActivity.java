package com.ider.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ider.socket.db.BoxFile;
import com.ider.socket.db.TvApp;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.HTTPFileDownloadTask;
import com.ider.socket.util.MyApplication;
import com.ider.socket.util.MyData;
import com.ider.socket.util.SocketClient;
import com.ider.socket.view.AppsAdapter;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.name;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.ider.socket.R.id.clean_data;
import static com.ider.socket.R.id.uninstall_app;

public class TvAppsActivity extends AppCompatActivity {


    private SocketClient client;
    private GridView gridView;
    private ImageView fresh;
    private ProgressBar progressBar;
    private AppsAdapter adapter;
    private OkHttpClient okHttpClient;
    private TvAppsActivity.HTTPdownloadHandler mHttpDownloadHandler;
    private TvAppsActivity.ScreenshotHandler mScreenshotHandler;
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private URI mHttpUri;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private List<TvApp> apps = new ArrayList<>();
    private String picDownPath;
    private List<String> downApps = new ArrayList<>();
    private boolean isDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_apps);
        gridView = (GridView)findViewById(R.id.app_grid);
        fresh = (ImageView)findViewById(R.id.fresh);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mHttpClient = CustomerHttpClient.getHttpClient();
        mHttpDownloadHandler = new TvAppsActivity.HTTPdownloadHandler();
        mScreenshotHandler = new TvAppsActivity.ScreenshotHandler();
        adapter = new AppsAdapter(TvAppsActivity.this,R.layout.applist_item,apps);
        gridView.setAdapter(adapter);
        okHttpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
        init();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TvApp app = apps.get(position);
                showMenuDialog(app);
            }
        });
        fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        try {
            mHttpUri = new URI(MyData.downUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        MyData.picIconSavePath = rootPath.endsWith(File.separator)?rootPath+"Ider/icon":rootPath+File.separator+"Ider/icon";
        MyData.screenshotSavePath = rootPath.endsWith(File.separator)?rootPath+"Ider/Screenshot":rootPath+File.separator+"Ider/Screenshot";


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Box_Message");
        intentFilter.addAction("connect_failed");
        registerReceiver(myReceiver,intentFilter);

    }
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Box_Message")){
                String info = intent.getStringExtra("info");
                if (info.contains("InUnCp")){
                    init();
                }else if (info.contains("InDiaDis")){
                    PopuUtils.dismissPopupDialog();

                }
            }else if (intent.getAction().equals("connect_failed")){
                finish();
            }

        }
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }
    private void init(){
        apps.clear();
        final String comment = changeToUnicode("\"RequestAllApps\"");
        progressBar.setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("comment",comment )
                            .url(MyData.downUrl).build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String result = response.body().string();
                    Log.i("result",result);
                    handResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void downloadPic(){
        isDownload = true;
        String name = downApps.get(0)+".jpg";
        String path = picDownPath+File.separator+name;
        File dir = new File(MyData.picIconSavePath);
        dir.mkdirs();
        BoxFile boxFile = new BoxFile();
        boxFile.setFilePath(path);
        mHttpTask = new HTTPFileDownloadTask(boxFile, mHttpClient, mHttpUri, MyData.picIconSavePath, name, 1);
        mHttpTask.setProgressHandler(mHttpDownloadHandler);
        mHttpTask.start();
    }

    private void handResult(String result){
        if (result.equals("null")) {
            mHandler.sendEmptyMessage(0);
            return;
        }
        String[] files = result.split("\"type=\"");
        picDownPath = files[0];
        for (int i =1 ;i<files.length;i++){
            String[] fil = files[i].split("\"label=\"");
            String type = fil[0];
            String[] fi = fil[1].split("\"pckn=\"");
            String label = fi[0];
            String pckn = fi[1];
            TvApp app = new TvApp(type,label,pckn);
            apps.add(app);
            File file = new File(MyData.picIconSavePath+File.separator+pckn+".jpg");
            if (!downApps.contains(app)&&!file.exists()) {
                downApps.add(app.getPackageName());
            }
        }
        if (!isDownload&&downApps.size()>0) {
            downloadPic();
        }
        mHandler.sendEmptyMessage(0);
    }
    private String changeToUnicode(String str){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0, length = str.length(); i < length; i++) {
            char c = str.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                stringBuffer.append(String.format("\\u%04x", (int) c));
            } else {
                stringBuffer.append(c);
            }
        }
        String unicode = stringBuffer.toString();
       return unicode;
    }
    private void showUninstallDialog(TvApp app){

        String name = app.getLabel();
        View view = View.inflate(TvAppsActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_tv_apps);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(TvAppsActivity.this, popup);
        popupDialog.showAtLocation(gridView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        LinearLayout allSelect = (LinearLayout)view.findViewById(R.id.all_select);
        final CheckBox allcheck = (CheckBox)view.findViewById(R.id.all_select_check);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        allSelect.setVisibility(View.GONE);
        title.setText("确认卸载");
        fileName.setText(name);
        cancel.setText("取消");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request.Builder().header("comment","\"uninstall\""+"cancel" )
                                    .url(MyData.downUrl).build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
//                            final String result = response.body().string();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setText("卸载");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request.Builder().header("comment","\"uninstall\""+"ok" )
                                    .url(MyData.downUrl).build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
//                            final String result = response.body().string();

                        } catch (Exception e) {
                            e.printStackTrace();
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
    private void showUnableUninstallDialog(String name){

        View view = View.inflate(TvAppsActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_tv_apps);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(TvAppsActivity.this, popup);
        popupDialog.showAtLocation(gridView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        LinearLayout allSelect = (LinearLayout)view.findViewById(R.id.all_select);
        final CheckBox allcheck = (CheckBox)view.findViewById(R.id.all_select_check);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        allSelect.setVisibility(View.GONE);
        title.setText("系统应用无法卸载");
        fileName.setText(name);
        cancel.setVisibility(View.GONE);
        ok.setText("关闭");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        allcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    private void showDetailMessage(String titleStr,String messageStr){
        View view = View.inflate(TvAppsActivity.this, R.layout.detail_message, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_tv_apps);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(TvAppsActivity.this, popup);
        popupDialog.showAtLocation(gridView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView message = (TextView)view.findViewById(R.id.message);
        title.setText(titleStr);
        message.setText(messageStr);

    }
    private void showMenuDialog(final TvApp app) {
        final String packageName = app.getPackageName();
        final String label = app.getLabel();
        final String comment = changeToUnicode(packageName);
        View view = View.inflate(TvAppsActivity.this, R.layout.app_menu, null);
        TextView uninstall = (TextView)view.findViewById(R.id.uninstall_app);
        TextView cleanData = (TextView)view.findViewById(R.id.clean_data);
        TextView forceStop = (TextView)view.findViewById(R.id.force_stop);
        TextView permission = (TextView)view.findViewById(R.id.permission);
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            final Request request = new Request.Builder().header("comment","\"requestAppInfo=\""+packageName )
                                    .url(MyData.downUrl).build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
                            final String result = response.body().string();
                            Log.i("result",result);
                            if (!result.equals("")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDetailMessage(label,result);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        cleanData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request.Builder().header("comment","\"cleanData=\""+comment )
                                    .url(MyData.downUrl).build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
                            final String result = response.body().string();
                            Log.i("result",result);
                            if (!result.equals("")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TvAppsActivity.this,result,Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                PopuUtils.dismissPopupDialog();
            }
        });
        forceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request.Builder().header("comment","\"forceStop=\""+comment )
                                    .url(MyData.downUrl).build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
                            final String result = response.body().string();
                            Log.i("result",result);
                            if (!result.equals("")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TvAppsActivity.this,result,Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                PopuUtils.dismissPopupDialog();
            }
        });
        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getType().equals("1")) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder().header("comment", "\"uninstall=\"" + comment)
                                        .url(MyData.downUrl).build();
                                Call call = okHttpClient.newCall(request);
                                Response response = call.execute();
                                final String result = response.body().string();
                                Log.i("result", result);
                                if (!result.equals("")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TvAppsActivity.this, result, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    PopuUtils.dismissPopupDialog();
                    showUninstallDialog(app);
                }else {
                    showUnableUninstallDialog(label);
                }
            }
        });

        Popus popup = new Popus();
        popup .setvWidth(-1);
        popup .setvHeight(-1);
        popup .setClickable( true );
        popup .setAnimFadeInOut(R.style.PopupWindowAnimation );
        popup.setCustomView(view);
        popup .setContentView(R.layout.activity_tv_apps );
        PopupDialog popupDialog = PopuUtils.createPopupDialog (TvAppsActivity.this, popup );
        popupDialog.showAtLocation(gridView, Gravity.CENTER,0,0);
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

        }
    };
    private class HTTPdownloadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE : {
                    mHandler.sendEmptyMessage(0);
                    downApps.remove(0);
                    if (downApps.size()>0) {
                        downloadPic();
                    }else {
                        isDownload = false;
                    }
                }
                break;
                default:
                    break;
            }
        }
    }
    private class ScreenshotHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE : {
                    Log.i("result","complete!");
                    Toast.makeText(TvAppsActivity.this,"截图保存在手机目录"+MyData.screenshotSavePath,Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    break;
            }
        }
    }
}
