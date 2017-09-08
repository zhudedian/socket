package com.ider.socket.util;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.ider.socket.Main2Activity.info;

/**
 * Created by Eric on 2017/8/26.
 */

public class EditChangeListener implements TextWatcher {

    private Handler mHandler;
    private OkHttpClient okHttpClient = new OkHttpClient();

    public EditChangeListener(Handler handler){
        super();
        mHandler = handler;
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.i("Listener","beforeTextChanged---" + charSequence.toString());
    }

    /**
     * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
     * 我们可以在这里实时地 通过搜索匹配用户的输入
     */
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.i("Listener", "onTextChanged---" + charSequence.toString());
    }

    /**
     * 编辑框的内容改变以后,用户没有继续输入时 的回调方法
     */
    @Override
    public void afterTextChanged(Editable editable) {
        Log.i("Listener", "afterTextChanged---"+editable.toString());
        MyData.editInfo = changeToUnicode(editable.toString());
        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("info",MyData.editInfo )
                            .url(MyData.infoUrl).build();
                    Call call = okHttpClient.newCall(request);
                    call.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
}
