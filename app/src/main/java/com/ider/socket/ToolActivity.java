package com.ider.socket;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ider.socket.db.BoxFile;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.HTTPFileDownloadTask;
import com.ider.socket.util.MyData;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ToolActivity extends AppCompatActivity {

    private TextView screenShot,cleanProgress;
    private OkHttpClient okHttpClient;
    private ToolActivity.ScreenshotHandler mScreenshotHandler;
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private URI mHttpUri;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private String picDownPath;
    private boolean isScreenshot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);
        screenShot = (TextView)findViewById(R.id.screen_shot);
        cleanProgress = (TextView)findViewById(R.id.clean_progress);

        mHttpClient = CustomerHttpClient.getHttpClient();
        mScreenshotHandler = new ToolActivity.ScreenshotHandler();
        okHttpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
        try {
            mHttpUri = new URI(MyData.downUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        MyData.screenshotSavePath = rootPath.endsWith(File.separator)?rootPath+"Ider/Screenshot":rootPath+File.separator+"Ider/Screenshot";
        screenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScreenshot) {
                    isScreenshot = true;
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder().header("comment", "screenshot")
                                        .url(MyData.downUrl).build();
                                Call call = okHttpClient.newCall(request);
                                Response response = call.execute();
                                String result = response.body().string();
                                Log.i("result", result);
                                handScreenshotResult(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }

            }
        });
        cleanProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder().header("comment", "\"cleanProgress\"")
                                        .url(MyData.downUrl).build();
                                Call call = okHttpClient.newCall(request);
                                Response response = call.execute();
                                String result = response.body().string();
                                Log.i("result", result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();


            }
        });

    }


    private void downloadScreenshot(String name){
        String path = picDownPath+ File.separator+name;
        File dir = new File(MyData.screenshotSavePath);
        dir.mkdirs();
        BoxFile boxFile = new BoxFile();
        boxFile.setFilePath(path);
        mHttpTask = new HTTPFileDownloadTask(boxFile, mHttpClient, mHttpUri, MyData.screenshotSavePath, name, 1);
        mHttpTask.setProgressHandler(mScreenshotHandler);
        mHttpTask.start();
    }
    private void handScreenshotResult(String result){
        if (result.equals("failed")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    Toast.makeText(ToolActivity.this,"截图失败！",Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        String[] files = result.split("\"name=\"");
        picDownPath = files[0];
        final String name = files[1];
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                downloadScreenshot(name);
            }
        }.start();

    }
    private class ScreenshotHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE : {
                    Log.i("result","complete!");
                    isScreenshot = false;
                    Toast.makeText(ToolActivity.this,"截图保存在手机目录"+MyData.screenshotSavePath,Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    break;
            }
        }
    }
}
