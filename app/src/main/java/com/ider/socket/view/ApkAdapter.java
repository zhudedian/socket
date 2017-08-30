package com.ider.socket.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ider.socket.R;
import com.ider.socket.db.ApkFile;

import java.util.List;

/**
 * Created by Eric on 2017/8/26.
 */

public class ApkAdapter extends ArrayAdapter<ApkFile> {
    private int resourceId;
    public ApkAdapter(Context context, int textViewResourceId, List<ApkFile> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;

    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        Log.i("", "listView");
        ApkFile apkFile = getItem(posetion);
        View view;
        ApkAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder =new ApkAdapter.ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.file_name);
            viewHolder.path = (TextView)view.findViewById(R.id.file_path);
            viewHolder.draw = (ImageView)view.findViewById(R.id.apk_image);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ApkAdapter.ViewHolder) view.getTag();
        }
        viewHolder.name.setText(apkFile.getFileName());
        viewHolder.path.setText(apkFile.getFilePath());
        if (apkFile.getApkDraw()!=null){
            viewHolder.draw.setImageDrawable(apkFile.getApkDraw());
        }
        return view;
    }
    class ViewHolder{
        ImageView draw;
        TextView name,path;
    }
}
