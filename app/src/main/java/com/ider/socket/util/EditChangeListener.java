package com.ider.socket.util;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.ider.socket.Main2Activity;

/**
 * Created by Eric on 2017/8/26.
 */

public class EditChangeListener implements TextWatcher {

    private Handler mHandler;

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
        Main2Activity.longinfo = editable.toString();
        mHandler.sendEmptyMessage(1);
    }
}
