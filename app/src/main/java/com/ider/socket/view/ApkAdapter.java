package com.ider.socket.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
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
    private Context context;
    private List<Bitmap> bitmapList;
    public ApkAdapter(Context context, int textViewResourceId, List<ApkFile> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
        this.context = context;


    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        ApkFile apkFile = getItem(posetion);
        View view;
        ApkAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder =new ApkAdapter.ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.file_name);
            viewHolder.path = (TextView)view.findViewById(R.id.file_path);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ApkAdapter.ViewHolder) view.getTag();
        }
        viewHolder.name.setText(apkFile.getFileName());
        viewHolder.path.setText(apkFile.getFilePath());



        return view;
    }
    class ViewHolder{
        TextView name,path;
    }
}
