package com.ider.socket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.HTTPFileDownloadTask;
import com.ider.socket.util.SocketClient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button install, server,download;
    private HTTPFileDownloadTask mHttpTask;
    private HttpClient mHttpClient;
    private OkHttpClient client;
    private URI mHttpUri;
    private MainActivity.HTTPdownloadHandler mHttpDownloadHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        install = (Button) findViewById(R.id.install);
        server = (Button) findViewById(R.id.server);
        download = (Button) findViewById(R.id.down_load);
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UploadActivity.class);
                startActivity(intent);
            }
        });
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                startActivity(intent);
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mHttpTask = new HTTPFileDownloadTask(mHttpClient, mHttpUri, Environment.getExternalStorageDirectory().getPath(), "updata.zip", 1);
//                mHttpTask.setProgressHandler(mHttpDownloadHandler);
//                mHttpTask.start();
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else {
                    Intent intent = new Intent(MainActivity.this,FileExActivity.class);
                    startActivity(intent);
                }



//                new Thread() {
//                    @Override
//                    public void run() {
//                        HttpGet httpGet = new HttpGet(mHttpUri);
//                        try {
//
//
//                            Request request = new Request.Builder().header("comment","897889897" )
//                                    .url("http://192.168.2.15:8080/down").build();
//                            Call call = client.newCall(request);
//                            Response response = call.execute();
//
////                            String value = URLEncoder.encode("dakai","UTF-8");
////                            byte[] buf = ("key="+value).getBytes("UTF-8");
////                            String path = "http://192.168.2.15:8080/down";
////                            URL url = new URL(path);
////                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
////                            con.setRequestMethod("POST");
////                            con.setDoOutput(true);
////
////                            OutputStream out = con.getOutputStream();
////                            out.write(buf);
////                            int res = con.getResponseCode();
////                            HttpResponse response = mHttpClient.execute(httpGet);
//////                            String result = response.getStatusLine().getReasonPhrase();
////                            String result = EntityUtils.toString(response.getEntity());
////                            Log.i("result",result);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
            }
        });
        mHttpClient = CustomerHttpClient.getHttpClient();
        client = new OkHttpClient();
        try {
//            mHttpUri = new URI("http://192.168.2.20:8080/otaupdate/xml/download/up/1.zip");
            mHttpUri = new URI("http://192.168.2.15:8080/down");
        } catch (URISyntaxException e) {
            Toast.makeText(MainActivity.this,"网络异常，请重试！",Toast.LENGTH_LONG).show();
            finish();
            e.printStackTrace();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this,FileExActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

    private class HTTPdownloadHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int whatMassage = msg.what;
            switch(whatMassage) {
                case HTTPFileDownloadTask.PROGRESS_UPDATE :
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_COMPLETE :
                break;
                case HTTPFileDownloadTask.PROGRESS_DOWNLOAD_FAILED :
                break;
//                case HTTPFileDownloadTask.PROGRESS_START_COMPLETE : {
//                    //mTxtState.setText("");
//                    mState = STATE_STARTED;
//                    mBtnControl.setText(getString(R.string.pause));
//                    mBtnControl.setClickable(true);
//                    mBtnControl.setFocusable(true);
//                    mBtnCancel.setClickable(true);
//                    mBtnCancel.setFocusable(true);
//                    setNotificationStrat();
//                    showNotification();
//                    mWakeLock.acquire();
//                }
//                break;
                case HTTPFileDownloadTask.PROGRESS_STOP_COMPLETE :

                break;
                default:
                    break;
            }
        }
    }




}
