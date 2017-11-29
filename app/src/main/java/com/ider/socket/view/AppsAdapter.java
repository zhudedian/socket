package com.ider.socket.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ider.socket.R;
import com.ider.socket.db.TvApp;
import com.ider.socket.util.MyData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Eric on 2017/10/16.
 */

public class AppsAdapter extends ArrayAdapter<TvApp> {
    private int resourceId;
    private Context context;
    public AppsAdapter(Context context, int textViewResourceId, List<TvApp> objects){
        super(context,textViewResourceId,objects);
        this.context = context;
        resourceId = textViewResourceId;

    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        TvApp app = getItem(posetion);
        String label = app.getLabel();
        String packageName = app.getPackageName();
        View view;
        AppsAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder =new AppsAdapter.ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.app_item_text);
            viewHolder.draw = (ImageView)view.findViewById(R.id.app_item_image);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (AppsAdapter.ViewHolder) view.getTag();
        }
        viewHolder.name.setText(label);
        Bitmap bitmap = getLoacalBitmap(MyData.picIconSavePath+ File.separator+packageName+".jpg");
        viewHolder.draw.setImageBitmap(bitmap);
        return view;
    }
    class ViewHolder{
        ImageView draw;
        TextView name,path;
    }
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
