package com.ider.socket.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ider.socket.R;
import com.ider.socket.db.BoxFile;
import com.ider.socket.util.MyData;

import java.util.List;

/**
 * Created by Eric on 2017/8/31.
 */

public class DirAdapter extends ArrayAdapter {
    private int resourceId;

    public DirAdapter(Context context, int textViewResourceId, List<BoxFile> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        Log.i("", "listView");
        BoxFile dir = (BoxFile) getItem(posetion);
        View view;
        DirAdapter.ViewHolder viewHolder;
        if (convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder =new DirAdapter.ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.dir_name);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (DirAdapter.ViewHolder) view.getTag();
        }
        viewHolder.name.setText(dir.getFileName());
        return view;
    }
    class ViewHolder{
        TextView name;
    }
}
