package com.ider.socket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ider.socket.util.SocketClient;
import com.ider.socket.util.SocketServer;

import static android.R.attr.x;
import static android.R.attr.y;

public class Main2Activity extends AppCompatActivity implements OnTouchListener,OnGestureListener {

    private GestureDetector gestureDetector;
    private float mPosX, mPosY, mCurPosX, mCurPosY;
    private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离
    private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度
    private GestureDetector mygesture = new GestureDetector(this);
    private RelativeLayout view;
    private SocketClient client;
    private boolean sendSuccess = true;

    private boolean twoTouch = false;

    private float lastTwoY;

    private String msg;

    private int twoTouchTimes;

    private float lastX2;

    private float lastY2;

    private float lastX1;

    private float lastY1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        gestureDetector = new GestureDetector(Main2Activity.this,onGestureListener);
        view = (RelativeLayout) findViewById(R.id.activity_main2);
        view.setOnTouchListener(this);
        //允许长按
        view.setLongClickable(true);
        client = new SocketClient();
        client.clintValue(Main2Activity.this, "192.168.2.50", 7777);
        client.openClientThread();
        SocketServer.ServerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(Main2Activity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                sendSuccess = true;
            }
        };

    }
//    private GestureDetector.OnGestureListener onGestureListener =
//            new GestureDetector.SimpleOnGestureListener() {
//                @Override
//                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                                       float velocityY) {
//                    float x = e2.getX() - e1.getX();
//                    float y = e2.getY() - e1.getY();
//                    Log.i("MainActivity", "e2.getX()="+e2.getX()+"e1.getX()="+e1.getX()+"e2.getY()="+e2.getY()+"e1.getY()="+e1.getY());
//
//                    return true;
//                }
//            };
//    public boolean onTouchEvent(MotionEvent event) {
//        return gestureDetector.onTouchEvent(event);
//    }







    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onTouch");
        System.out.println("获得两点的坐标");

        if (event.getPointerCount() == 2) {
            twoTouchTimes++;
            twoTouch = true;
            System.out.println("坐标A：X = " + event.getX(0) + "，Y = "

                    + event.getY(0));

            System.out.println("坐标B：X = " + event.getX(1) + "，Y = "

                    + event.getY(1));
            if (twoTouchTimes%3==0){
                if (lastTwoY != -1) {
                    int y = (int)(event.getY(1) - lastTwoY)*2;
                    msg = "scolor "+y+" "+"o"+" ";
                    client.sendMsg(msg);

                }
                lastTwoY = event.getY(1);
            }


        }

        return mygesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onDown"+e.getPointerCount());

        client.sendMsg("nexttouch ");
        return false;
    }


    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onShowPress");

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onSingleTapUp");
        client.sendMsg("onSingleTapUp ");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        Log.i("MainActivity,onScroll", "e2.getX()="+e2.getX()+"e1.getX()="+e1.getX()+"e2.getY()="+e2.getY()+"e1.getY()="+e1.getY()+"velocityX="+distanceX+"velocityY="+distanceY);
//        Message msg = new Message();
//        Bundle bundle = new Bundle();
//        bundle.putString("e2x",e2.getX()+"");
//        bundle.putString("e2y",e2.getY()+"");
//        msg.setData(bundle);
        String msg;
        if (lastX2!=e1.getX()&&lastY2!=e1.getY()){
            lastX2 =e1.getX();
            lastY2 = e1.getY();
            int x = (int)e2.getX();
            int x2 = (int)e1.getX();
            int y = (int)e2.getY();
            int y2 = (int)e1.getY();
            msg = (x-x2)+" "+(y-y2)+" ";
            lastX1 = e2.getX();
            lastY1 = e2.getY();
        }else {
            msg = (int)(e2.getX()-lastX1)+" "+(int)(e2.getY()-lastY1)+" ";
        }

        Log.i("mejklj",msg);
        if (!twoTouch){
            client.sendMsg(msg);
        }


        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onLongPress");

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // TODO Auto-generated method stub
        // e1：第1个ACTION_DOWN MotionEvent
        // e2：最后一个ACTION_MOVE MotionEvent
        // velocityX：X轴上的移动速度（像素/秒）
        // velocityY：Y轴上的移动速度（像素/秒）
        twoTouch = false;
        twoTouchTimes =0 ;
        lastTwoY = -1;
        Log.i("MainActivity", "e2.getX()="+e2.getX()+"e1.getX()="+e1.getX()+"e2.getY()="+e2.getY()+"e1.getY()="+e1.getY()+"velocityX="+velocityX+"velocityY="+velocityY);
        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
        //向
        if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE){
//                     && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

        }
        //向上
        if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

        }
        return false;
    }
}
