package com.ider.socket.view;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ider.socket.DirSelectActivity;
import com.ider.socket.FileSelectActivity;
import com.ider.socket.R;
import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.BackHandled;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.MyData;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;


/**
 * Created by Eric on 2017/8/29.
 */

public class BoxFileFragment extends Fragment implements BackHandled{

    private URI mHttpUri ;
    private HttpClient mHttpClient;
    private OkHttpClient okHttpClient;
    private ListView listView;
    private RelativeLayout menuRel;
    private ImageView delete,back,menu;
    private TextView upload,filePath;
    private CheckBox allSelect;
    private ProgressBar progressBar;
    private FileAdapter adapter;
    private String fileName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("tag","onCreateView");
        View view = inflater.inflate(R.layout.file_list,container,false);
        listView = (ListView)view.findViewById(R.id.list_view);
        back = (ImageView)view.findViewById(R.id.back_press);
        filePath = (TextView)view.findViewById(R.id.file_path);
        delete = (ImageView) view.findViewById(R.id.delete);
        allSelect = (CheckBox)view.findViewById(R.id.all_select);
        menuRel = (RelativeLayout)view.findViewById(R.id.menu);
        upload = (TextView)view.findViewById(R.id.upload_file);
        menu = (ImageView)view.findViewById(R.id.menu_bt);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar) ;
        return view;
    }
    @Override
    public void onActivityCreated(final Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mHttpClient = CustomerHttpClient.getHttpClient();
        okHttpClient = new OkHttpClient();
        try {
            mHttpUri = new URI("http://192.168.2.15:8080/down");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        adapter = new FileAdapter(getContext(),R.layout.file_list_item,MyData.boxFiles,MyData.selectBoxFiles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MyData.isShowCheck) {
                    BoxFile boxFile = MyData.boxFiles.get(position);
                    if (boxFile.getFileType()==1) {
                        fileName = boxFile.getFileName();
                        if (MyData.boxFilePath.equals("/")){
                            MyData.boxFilePath = MyData.boxFilePath +fileName;
                        }else {
                            MyData.boxFilePath = MyData.boxFilePath +"/"+fileName;
                        }
                        init();
                    }else {
                        MyData.selectBoxFiles.clear();
                        MyData.selectBoxFiles.add(boxFile);
                        showMenuDialog();
                    }
                }else {
                    BoxFile boxFile = MyData.boxFiles.get(position);
                    if (MyData.selectBoxFiles.contains(boxFile)){
                        MyData.selectBoxFiles.remove(boxFile);
                    }else {
                        MyData.selectBoxFiles.add(boxFile);
                    }
                    if (MyData.selectBoxFiles.size()>0){
                        delete.setVisibility(View.VISIBLE);
                    }else {
                        delete.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MyData.isShowCheck = true;
                upload.setVisibility(View.GONE);
                menuRel.setVisibility(View.VISIBLE);
                menu.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.isShowCheck = false;
                MyData.selectBoxFiles.clear();
                getActivity().finish();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),FileSelectActivity.class);
                startActivity(intent);
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });
        allSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allSelect.isChecked()) {
                    MyData.selectBoxFiles.clear();
                    MyData.selectBoxFiles.addAll(MyData.boxFiles);
                    delete.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }else {
                    MyData.selectBoxFiles.clear();
                    delete.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        init();
    }
    private void init(){
        final String comment = changeToUnicode(MyData.boxFilePath);
        progressBar.setVisibility(View.VISIBLE);
        MyData.boxFiles.clear();
        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("comment",comment )
                            .url(MyData.downUrl).build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String result = response.body().string();
                    Log.i("result",result);
                    if (result.equals("null")) {
                        mHandler.sendEmptyMessage(0);
                        return;
                    }
                    String[] files = result.split("type=");
                    MyData.boxFilePath = files[0];
                    for (int i =1 ;i<files.length;i++){
                        String[] fils = files[i].split("name=");
                        int type = Integer.parseInt(fils[0]);
                        String[] fis = fils[1].split("size=");
                        MyData.boxFiles.add(new BoxFile(type,fis[0],fis[1],MyData.boxFilePath+"/"+fis[0]));
                    }
                    mHandler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void delete(){
        fileName = "\"delete=\""+MyData.boxFilePath;
        for (int i=0;i<MyData.selectBoxFiles.size();i++){
            fileName = fileName + "name="+MyData.selectBoxFiles.get(i).getFileName();
        }
        final String comment = changeToUnicode(fileName);
        progressBar.setVisibility(View.VISIBLE);
        MyData.boxFiles.clear();
        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("comment",comment )
                            .url(MyData.downUrl).build();
                    Call call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    String result = response.body().string();
                    Log.i("result",result);
                    if (result.equals("null")) {
                        mHandler.sendEmptyMessage(0);
                        return;
                    }
                    String[] files = result.split("type=");
                    MyData.boxFilePath = files[0];
                    for (int i =1 ;i<files.length;i++){
                        String[] fils = files[i].split("name=");
                        int type = Integer.parseInt(fils[0]);
                        String[] fis = fils[1].split("size=");
                        MyData.boxFiles.add(new BoxFile(type,fis[0],fis[1],MyData.boxFilePath+"/"+fis[0]));
                    }
                    mHandler.sendEmptyMessage(0);
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

    private void showDeleteDialog(){
        View view = View.inflate(getContext(), R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(getContext(), popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView fileText = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        title.setText("删除警告！");
        if (MyData.selectBoxFiles.size()==1){
            fileText.setText(MyData.selectBoxFiles.get(0).getFileName());
        }else {
            fileText.setText("已选择"+MyData.selectBoxFiles.size()+"文件");
        }
        cancel.setText("取消");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setText("删除");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
                PopuUtils.dismissPopupDialog();
                progressBar.setVisibility(View.VISIBLE);
                MyData.selectBoxFiles.clear();
                upload.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
            }
        });
    }

    private void showConfirmDialog() {
        View view = View.inflate(getContext(), R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_ex);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(getContext(), popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        title.setText("确认下载？");
        if (MyData.selectBoxFiles.size()==1){
            fileName.setText(MyData.selectBoxFiles.get(0).getFileName());
        }else {
            fileName.setText("已选择"+MyData.selectBoxFiles.size()+"文件");
        }
        cancel.setText("取消");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setText("下载");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
                MyData.downLoadingFiles.addAll(MyData.selectBoxFiles);
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    Intent intent = new Intent(getContext(), DirSelectActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&& grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(getContext(), DirSelectActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(),"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }
    }

    private void showMenuDialog() {
        View view = View.inflate(getContext(), R.layout.menu_list, null);
        TextView delete = (TextView)view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        TextView downLoad = (TextView)view.findViewById(R.id.down_load);
        downLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    Intent intent = new Intent(getContext(), DirSelectActivity.class);
                    startActivity(intent);
                }
            }
        });

        Popus popup = new Popus();
        popup .setvWidth(-1);
        popup .setvHeight(-1);
        popup .setClickable( true );
        popup .setAnimFadeInOut(R.style.PopupWindowAnimation );
        popup.setCustomView(view);
        popup .setContentView(R.layout.activity_file_ex );
        PopupDialog popupDialog = PopuUtils.createPopupDialog (getContext(), popup );
        popupDialog.showAtLocation(listView, Gravity.CENTER,0,0);
    }

    private void showFileControlDialog() {
        View view = View.inflate(getContext(), R.layout.file_control, null);
        TextView delete = (TextView)view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.boxFiles.clear();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request.Builder().header("comment","\"delete=\""+"name="+fileName )
                                    .url("http://192.168.2.15:8080/down").build();
                            Call call = okHttpClient.newCall(request);
                            Response response = call.execute();
                            String result = response.body().string();
                            Log.i("result",result);
                            String[] files = result.split("type=");
                            for (int i =1 ;i<files.length;i++){
                                String[] fils = files[i].split("name=");
                                int type = Integer.parseInt(fils[0]);
                                String[] fis = fils[1].split("size=");
                                MyData.boxFiles.add(new BoxFile(type,fis[0],fis[1]));
                            }
                            mHandler.sendEmptyMessage(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                PopuUtils.dismissPopupDialog();
            }
        });
        TextView moreSelect = (TextView)view.findViewById(R.id.more_select);
        moreSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.isShowCheck = true;
                upload.setVisibility(View.GONE);
                menu.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                PopuUtils.dismissPopupDialog();
            }
        });
        Popus popup = new Popus();
        popup .setvWidth(-1);
        popup .setvHeight(-1);
        popup .setClickable( true );
        popup .setAnimFadeInOut(R.style.PopupWindowAnimation );
        popup.setCustomView(view);
        popup .setContentView(R.layout.activity_file_ex );
        PopupDialog popupDialog = PopuUtils.createPopupDialog (getContext(), popup );
        popupDialog.showAtLocation(listView, Gravity.CENTER,0,0);
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    filePath.setText(MyData.boxFilePath);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    break;
                case 1:
                    getActivity().finish();
                    break;
                case 2:

                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onBackPressed(){
        if (!MyData.isShowCheck) {
            if (MyData.boxFilePath.equals("/")||MyData.boxFilePath.equals("")){
                getActivity().finish();
                return;
            }
            Log.i("MyData.boxFilePath",MyData.boxFilePath);
            MyData.boxFilePath = MyData.boxFilePath.substring(0,MyData.boxFilePath.lastIndexOf("/"));

            init();
        }else {
            MyData.isShowCheck = false;
            MyData.selectBoxFiles.clear();
            upload.setVisibility(View.VISIBLE);
            allSelect.setChecked(false);
            menuRel.setVisibility(View.GONE);
            menu.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

}
