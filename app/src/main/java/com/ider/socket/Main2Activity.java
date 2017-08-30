package com.ider.socket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ider.socket.util.EditChangeListener;
import com.ider.socket.util.SocketClient;
import com.ider.socket.util.SocketServer;

import static com.ider.socket.R.id.server;

public class Main2Activity extends AppCompatActivity implements OnTouchListener,OnGestureListener ,View.OnClickListener{

    private GestureDetector gestureDetector;
    private float mPosX, mPosY, mCurPosX, mCurPosY;
    private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离
    private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度
    private GestureDetector mygesture = new GestureDetector(this);
    private LinearLayout view;
    private EditText editText;
    private Button back,ok;
    private static SocketClient client;
    private int lastUpX,lastUpY;

    private boolean twoTouch = false;

    private float lastTwoY,lastTwoX;

    private String msg;

    public static String info,longinfo,lenth;

    private InputMethodManager imm;

    private int twoTouchTimes;

    private float lastX2;

    private float lastY2;

    private float lastX1;

    private float lastY1;
    private float lastYa,lastYb,lastXa,lastXb;
    private Button up,down,left,right,center;
    private boolean isEnd =false;
    private int endCount,goneTimes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        gestureDetector = new GestureDetector(Main2Activity.this,onGestureListener);
        view = (LinearLayout) findViewById(R.id.mouse_move);
        back = (Button) findViewById(R.id.back);
        ok = (Button) findViewById(R.id.conform);
        editText = (EditText) findViewById(R.id.edit_view);
        editText.addTextChangedListener(new EditChangeListener(handler));
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        up = (Button)findViewById(R.id.up);
        down = (Button)findViewById(R.id.down);
        left = (Button)findViewById(R.id.left);
        right = (Button)findViewById(R.id.right);
        center = (Button) findViewById(R.id.center);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        center.setOnClickListener(this);

        view.setOnTouchListener(this);
        //允许长按
        view.setLongClickable(true);
        if (client==null){
            client = new SocketClient();
            client.clintValue(Main2Activity.this, "192.168.2.15", 7777);
            client.openClientThread();
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = "inBeginBeginBeg";
                client.sendMsg(msg);
                view.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                longinfo = editText.getText().toString();
                lenth = longinfo.length()+"";
                Log.i("lenth",lenth+lenth.length());
                if (lenth.length()==1){
                    lenth = "0000"+lenth;
                }else if (lenth.length()==2){
                    lenth = "000"+lenth;
                }else if (lenth.length()==3){
                    lenth = "00"+lenth;
                }else if (lenth.length()==4){
                    lenth = "0"+lenth;
                }

//                msg = "inEndEndEndEndE";
//                client.sendMsg(msg);


            }
        });
        SocketClient.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                Toast.makeText(Main2Activity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                String pos = msg.obj.toString();
                Log.i("msg",pos);
                handCom(pos);

            }
        };
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.sendMsg("cb ,,,,,,,,,,,,");
            }
        });

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.up:
                client.sendMsg("coup ,,,,,,,,,,");
                break;
            case R.id.down:
                client.sendMsg("codown ,,,,,,,,");
                break;
            case R.id.left:
                client.sendMsg("coleft ,,,,,,,,");
                break;
            case R.id.right:
                client.sendMsg("coright ,,,,,,,");
                break;
            case R.id.center:
                client.sendMsg("cocenter ,,,,,,");
                break;
            default:
                break;
        }
    }
    private void handCom(String pos){
        if (pos.contains("InOp")){
            editText.setVisibility(View.VISIBLE);
            ok.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
//            Log.i("InOp",info);
//            editText.setText(info);
            imm.showSoftInput(editText,InputMethodManager.SHOW_FORCED);
            endCount = 0;
            return;
        }
        if (pos.contains("InCl")){
            editText.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            ok.setVisibility(View.GONE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            endCount = 0;
            return;
        }
        if (pos.contains("Info")){
            String[] infor = pos.split("R,T;Y.");
            info = infor[0].replace("Info","");
            Log.i("Info",info);
            editText.setText(info);
            endCount = 0;
            return;
        }
        if (pos.contains("sendSuccess")){
            msg = "inEndEndEndEndE";
            client.sendMsg(msg);
            endCount = 0;
            return;
        }
        if (pos.contains("recieveOready")){
            msg = "longinfo" + lenth + longinfo;
            client.sendMsg(msg);
            endCount = 0;
            return;
        }
        if (endCount >= 4){
            if (!isEnd){
                isEnd = true;
               client.close();
                client = null;
                finish();
            }
            endCount = 0;
        }else {
            endCount++;
            Log.i("count",endCount+"");
        }
    }








    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onTouch");
        System.out.println("获得两点的坐标");

        if (event.getPointerCount() == 2) {
            if (twoTouchTimes == 0){
                client.sendMsg("cd ,,,,,,,,,,,,");
                lastTwoX=event.getX(1);
                lastTwoY = event.getY(1);
                lastXa = lastTwoX;
                lastYa = lastTwoY;
            }else {
                lastYb =lastYa;
                lastXb = lastXa;
                lastYa = event.getY(1);
                lastXa = event.getX(1);
            }
            twoTouchTimes++;
            twoTouch = true;
            System.out.println("坐标A：X = " + event.getX(0) + "，Y = "

                    + event.getY(0));

            System.out.println("坐标B：X = " + event.getX(1) + "，Y = "

                    + event.getY(1));
            if (twoTouchTimes!=0){
                if (lastTwoY != -1) {
                    lastUpY = (int)(event.getY(1) - lastTwoY);
                    lastUpX = (int)(event.getX(1)-lastTwoX);
                    int x = lastUpX*2;
                    int y = lastUpY*2;
                    msg = "csP"+x+"P"+y+" ";
                    int length = msg.length();
                    if (length<15){
                        for (int i=0;i<15-length;i++){
                            msg= msg+",";
                        }
                    }
                    client.sendMsg(msg);

                }
//                lastTwoY = event.getY(0);
            }


        }

        return mygesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        Log.i("MainActivity", "onDown"+e.getPointerCount());
        twoTouch = false;
        twoTouchTimes =0 ;
        lastTwoY = -1;
        client.sendMsg("cn ,,,,,,,,,,,,");
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
        client.sendMsg("cc ,,,,,,,,,,,,");
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
            msg =  "cmP"+(x-x2)+"P"+(y-y2)+" ";
            lastX1 = e2.getX();
            lastY1 = e2.getY();
        }else {
            msg = "cmP"+(int)(e2.getX()-lastX1)+"P"+(int)(e2.getY()-lastY1)+" ";
        }
        int length = msg.length();
        if (length<15){
            for (int i=0;i<15-length;i++){
                msg= msg+",";
            }
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

        twoTouchTimes =0 ;
        lastTwoY = -1;
        if (twoTouch) {
            msg = "cuP" + (lastUpX + (int) (lastXa - lastXb)) * 2 + "P" + (lastUpY + (int) (lastYa - lastYb)) * 2 + " ";
            int length = msg.length();
            if (length < 15) {
                for (int i = 0; i < 15 - length; i++) {
                    msg = msg + ",";
                }
            }
            client.sendMsg(msg);
        }
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
        twoTouch = false;
        return false;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
//        client.close();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msgs) {
            switch (msgs.what) {
                case 1:
                    msg = "inBeginBeginBeg";
                    client.sendMsg(msg);
                    lenth = longinfo.length()+"";
                    Log.i("lenth",lenth+lenth.length());
                    if (lenth.length()==1){
                        lenth = "0000"+lenth;
                    }else if (lenth.length()==2){
                        lenth = "000"+lenth;
                    }else if (lenth.length()==3){
                        lenth = "00"+lenth;
                    }else if (lenth.length()==4){
                        lenth = "0"+lenth;
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
