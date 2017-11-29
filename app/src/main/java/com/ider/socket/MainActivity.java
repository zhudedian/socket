package com.ider.socket;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ider.socket.net.Connect;
import com.ider.socket.util.HTTPFileDownloadTask;
import com.ider.socket.util.MyApplication;
import com.ider.socket.util.MyData;
import com.ider.socket.util.SocketClient;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button install, server,download,apps,more,remote;
    private ProgressBar progressBar;
    private BluetoothAdapter mBluetoothAdapter;
    private int endCount;
    private boolean isEnd =false;



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
        apps = (Button) findViewById(R.id.apps);
        more = (Button) findViewById(R.id.more_set);
        remote = (Button)findViewById(R.id.remote);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar) ;
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
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else {
                    Intent intent = new Intent(MainActivity.this,FileExActivity.class);
                    startActivity(intent);
                }
            }
        });
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
                }else {
                    Intent intent = new Intent(MainActivity.this, TvAppsActivity.class);
                    startActivity(intent);
                }
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},3);
                }else {
                    Intent intent = new Intent(MainActivity.this, ToolActivity.class);
                    startActivity(intent);
                }
            }
        });
        remote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] sigin = { 9000,4500,
                        600,1600,600,1600,600,1600,600,600,
                        600,600,600,1600,600,1600,600,1600,
                        600,600,600,600,600,600,600,1600,
                        600,1600,600,600,600,600,600,600,
                        600,1600,600,1600,600,1600,600,1600,
                        600,1600,600,1600,600,1600,600,600,
                        600,600,600,600,600,600,600,600,
                        600,600,600,600,600,600,600,1600,
                        600,1600};
                ConsumerIrManager IR=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
                IR.transmit(38000,sigin);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 获取所有已经绑定的蓝牙设备
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice bluetoothDevice : devices) {
                Log.i("device.getName()",bluetoothDevice.getName());
            }
        }
        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, mFilter);
        // 注册搜索完时的receiver
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);
        mBluetoothAdapter.startDiscovery();


    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.i("device.getName()","............");
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName()!=null) {
                    Log.i("device.getName()", device.getName());
                }
                // 搜索到的不是已经绑定的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 显示在TextView上
                    if (device.getName()!=null) {
                        Log.i("device.getName()", device.getName());
                    }
                }

                // 搜索完成
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("搜索蓝牙设备");
            }
        }
    };
    @Override
    protected void onResume(){
        super.onResume();
        Connect.onBrodacastSend(mHandler);
        init();
    }
    private void init(){
        server.setVisibility(View.VISIBLE);
        install.setVisibility(View.VISIBLE);
        download.setVisibility(View.VISIBLE);
        apps.setVisibility(View.VISIBLE);
        more.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        if (MyData.isConnect){
            server.setVisibility(View.VISIBLE);
            install.setVisibility(View.VISIBLE);
            download.setVisibility(View.VISIBLE);
            apps.setVisibility(View.VISIBLE);
            more.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            if (MyData.client==null){
                MyData.client = new SocketClient();
                MyData.client.clintValue(MyData.boxIP, 7777);
                MyData.client.openClientThread();
                SocketClient.mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String pos = msg.obj.toString();
                        Log.i("msg",pos);
                        if (pos.contains("In")) {
                            Intent intent = new Intent("Box_Message");
                            intent.putExtra("info", pos);
                            MyApplication.getContext().sendBroadcast(intent);
                            endCount = 0;
                        }else {
                            if (endCount >= 4){
                                if (MyData.client!=null) {
                                    MyData.client.close();
                                    MyData.client = null;
                                    Connect.onBrodacastSend(mHandler);
                                    init();
                                    Intent intent = new Intent("connect_failed");
                                    MyApplication.getContext().sendBroadcast(intent);
                                    endCount = 0;
                                }
                            }else {
                                endCount++;
                                Log.i("count",endCount+"");
                            }
                        }

                    }
                };
            }

        }else {
            progressBar.setVisibility(View.VISIBLE);
            server.setVisibility(View.GONE);
            install.setVisibility(View.GONE);
            download.setVisibility(View.GONE);
            apps.setVisibility(View.GONE);
            more.setVisibility(View.GONE);
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
            case 2:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this, TvAppsActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this,ToolActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        MyData.isConnect = false;
        unregisterReceiver(mReceiver);
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    init();
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
