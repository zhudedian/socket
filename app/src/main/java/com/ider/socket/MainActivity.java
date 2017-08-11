package com.ider.socket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ider.socket.util.NetTool;
import com.ider.socket.util.SocketClient;
import com.ider.socket.util.SocketServer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button clientBt, server, fresh, send;
    private SocketClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Intent intent = new Intent(MainActivity.this,Main2Activity.class);
        startActivity(intent);
        clientBt = (Button) findViewById(R.id.client);
        server = (Button) findViewById(R.id.server);
        fresh = (Button) findViewById(R.id.fresh);
        send = (Button) findViewById(R.id.send);
        client = new SocketClient();

        clientBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketClientActivity.class);
                startActivity(intent);
            }
        });
        SocketServer.ServerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();

            }
        };
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketServerActivity.class);
                startActivity(intent);
            }
        });
        NetTool netTool = new NetTool(MainActivity.this);
        final List<String> listIp = new ArrayList<>();
        netTool.scan(listIp);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listIp);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ip = listIp.get(position);
                Log.i("MainActivity", ip + "");
                client.clintValue(MainActivity.this, ip, 7777);
                client.openClientThread();

            }
        });
        fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyDataSetChanged();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.sendMsg("nihao !!");
            }
        });

    }




}
