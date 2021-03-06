package com.ider.socket.view;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
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
import com.ider.socket.util.ListSort;
import com.ider.socket.util.MyData;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Eric on 2017/8/29.
 */

public class BoxFileFragment extends Fragment implements BackHandled {

    private URI mHttpUri ;
    private HttpClient mHttpClient;
    private ProgressDialog progressDialog;
    private OkHttpClient okHttpClient;
    private ListView listView;
    private RelativeLayout menuRel;
    private ImageView back,menu,dirCreate;
    private TextView upload,filePath;
    private CheckBox allSelect;
    private ProgressBar progressBar;
    private FileAdapter adapter;
    private String fileName;
    private int copySize;
    private List<BoxFile> toCopyFiles = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("tag","onCreateView");
        View view = inflater.inflate(R.layout.file_list,container,false);
        listView = (ListView)view.findViewById(R.id.list_view);
        back = (ImageView)view.findViewById(R.id.back_press);
        filePath = (TextView)view.findViewById(R.id.file_path);
        allSelect = (CheckBox)view.findViewById(R.id.all_select);
        menuRel = (RelativeLayout)view.findViewById(R.id.menu);
        upload = (TextView)view.findViewById(R.id.upload_file);
        menu = (ImageView)view.findViewById(R.id.menu_bt);
        dirCreate = (ImageView)view.findViewById(R.id.create_dir);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar) ;
        return view;
    }
    @Override
    public void onActivityCreated(final Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mHttpClient = CustomerHttpClient.getHttpClient();
        okHttpClient = new OkHttpClient.Builder().connectTimeout(20,TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();

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
                    adapter.notifyDataSetChanged();
                }else {
                    MyData.selectBoxFiles.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        dirCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDirDialog();
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
        adapter.notifyDataSetChanged();
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
                    handResult(result);
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
        adapter.notifyDataSetChanged();
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
                    handResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void createDir(String dirName){
        fileName = "\"createDir=\""+MyData.boxFilePath;
        String filePath = fileName.endsWith("/")?(fileName+dirName):(fileName+"/"+dirName);
        final String comment = changeToUnicode(filePath);
        progressBar.setVisibility(View.VISIBLE);
        MyData.boxFiles.clear();
        adapter.notifyDataSetChanged();
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
                    handResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void rename(BoxFile boxFile,String newName){
        fileName = "\"reNameFile=\""+boxFile.getFilePath();
        String filePath = fileName+"\"newName=\""+newName;
        final String comment = changeToUnicode(filePath);
        progressBar.setVisibility(View.VISIBLE);
        MyData.boxFiles.clear();
        adapter.notifyDataSetChanged();
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
                    handResult(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void copy(){
        if (MyData.copingFiles.size()>0) {
            BoxFile boxFile = MyData.copingFiles.get(0);
            fileName = "\"copyFile=\""+boxFile.getFilePath()+"\"newPath=\""+boxFile.getSavePath()+File.separator+boxFile.getFileName();
            final String comment = changeToUnicode(fileName);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder().header("comment", comment)
                                .url(MyData.downUrl).build();
                        Call call = okHttpClient.newCall(request);
                        Response response = call.execute();
                        String result = response.body().string();
                        Log.i("result", result);
                        if (result.equals("success")) {
                            MyData.copingFiles.remove(0);
                            mHandler.sendEmptyMessage(2);
                            copy();
                        }else {
                            copy();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        copy();
                    }
                }
            }.start();
        }else {
            mHandler.sendEmptyMessage(3);
        }
    }
    private void move(){
        if (MyData.copingFiles.size()>0) {
            BoxFile boxFile = MyData.copingFiles.get(0);
            fileName = "\"moveFile=\""+boxFile.getFilePath()+"\"newPath=\""+boxFile.getSavePath()+File.separator+boxFile.getFileName();
            final String comment = changeToUnicode(fileName);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder().header("comment", comment)
                                .url(MyData.downUrl).build();
                        Call call = okHttpClient.newCall(request);
                        Response response = call.execute();
                        String result = response.body().string();
                        Log.i("result", result);
                        if (result.equals("success")) {
                            MyData.copingFiles.remove(0);
                            mHandler.sendEmptyMessage(2);
                            move();
                        }else {
                            move();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        move();
                    }
                }
            }.start();
        }else {
            mHandler.sendEmptyMessage(3);
        }
    }
    private void handResult(String result){
        if (result.equals("null")) {
            mHandler.sendEmptyMessage(0);
            return;
        }
        String[] files = result.split("\"type=\"");
        MyData.boxFilePath = files[0];
        for (int i =1 ;i<files.length;i++){
            String[] fils = files[i].split("\"name=\"");
            int type = Integer.parseInt(fils[0]);
            String[] fis = fils[1].split("\"size=\"");
            MyData.boxFiles.add(new BoxFile(type,fis[0],fis[1],MyData.boxFilePath+"/"+fis[0]));
        }
        ListSort.sort(MyData.boxFiles);
        mHandler.sendEmptyMessage(0);
    }
    private void showProgressDialog(){
        progressDialog = new ProgressDialog(getContext());
        copySize = MyData.copingFiles.size();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setTitle("请稍后……");
        progressDialog.setMax(copySize);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyData.copingFiles.clear();
                        mHandler.sendEmptyMessage(4);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Request request = new Request.Builder().header("comment", "\"stopCopyFile\"")
                                            .url(MyData.downUrl).build();
                                    Call call = okHttpClient.newCall(request);
                                    Response response = call.execute();
                                    String result = response.body().string();
                                    Log.i("result", result);
//                                    mHandler.sendEmptyMessage(3);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
        progressDialog.setMessage(MyData.copingFiles.get(0).getFileName());
        progressDialog.show();
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

    private void showCreateDirDialog() {
        View view = View.inflate(getContext(), R.layout.create_dir, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(getContext(), popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        final EditText dirName = (EditText) view.findViewById(R.id.dir_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
                String dir = dirName.getText().toString();
                createDir(dir);
            }
        });
    }

    private void showRenameDialog(final BoxFile boxFile) {
        View view = View.inflate(getContext(), R.layout.create_dir, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(getContext(), popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        final EditText dirName = (EditText) view.findViewById(R.id.dir_name);
        dirName.setText(boxFile.getFileName());
        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText("请编辑");
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
                String dir = dirName.getText().toString();
                rename(boxFile,dir);
                MyData.selectBoxFiles.clear();
                MyData.isShowCheck = false;
                allSelect.setChecked(false);
                menuRel.setVisibility(View.GONE);
            }
        });
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
        final TextView fileText = (TextView)view.findViewById(R.id.file_name);
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
                allSelect.setChecked(false);
                upload.setVisibility(View.GONE);
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
        TextView moreSelect = (TextView)view.findViewById(R.id.more_select);
        moreSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.isShowCheck = true;
                upload.setVisibility(View.GONE);
                menuRel.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                PopuUtils.dismissPopupDialog();
            }
        });
        TextView upload = (TextView)view.findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),FileSelectActivity.class);
                startActivity(intent);
                PopuUtils.dismissPopupDialog();
            }
        });
        TextView copy = (TextView)view.findViewById(R.id.copy);
        TextView rename = (TextView)view.findViewById(R.id.rename);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toCopyFiles.clear();
                toCopyFiles.addAll(MyData.selectBoxFiles);
                PopuUtils.dismissPopupDialog();
            }
        });
        TextView move = (TextView)view.findViewById(R.id.move);
        TextView paste = (TextView)view.findViewById(R.id.paste);

        if (toCopyFiles.size()==0){
            move.setVisibility(View.GONE);
            paste.setVisibility(View.GONE);
        }else {
            move.setVisibility(View.VISIBLE);
            paste.setVisibility(View.VISIBLE);
        }
        if (MyData.isShowCheck){
            moreSelect.setVisibility(View.GONE);
        }else {
            moreSelect.setVisibility(View.VISIBLE);
        }
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog(MyData.selectBoxFiles.get(0));
            }
        });
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0;i<toCopyFiles.size();i++){
                    BoxFile boxFile = toCopyFiles.get(i);
                    boxFile.setSavePath(MyData.boxFilePath);
                    MyData.copingFiles.add(boxFile);
                }
                move();
                toCopyFiles.clear();
                showProgressDialog();
                PopuUtils.dismissPopupDialog();
            }
        });
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0;i<toCopyFiles.size();i++){
                    BoxFile boxFile = toCopyFiles.get(i);
                    boxFile.setSavePath(MyData.boxFilePath);
                    MyData.copingFiles.add(boxFile);
                }
                copy();
                showProgressDialog();
                PopuUtils.dismissPopupDialog();
            }
        });
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
        if (MyData.selectBoxFiles.size()==0){
            downLoad.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);
        }else {
            if (MyData.selectBoxFiles.size()==1){
                rename.setVisibility(View.VISIBLE);
            }else {
                rename.setVisibility(View.GONE);
            }
            downLoad.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            copy.setVisibility(View.VISIBLE);
        }
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
                    if (MyData.boxFiles.size()==0){
                        menuRel.setVisibility(View.GONE);
                        MyData.isShowCheck = false;
                    }
                    break;
                case 1:
                    getActivity().finish();
                    break;
                case 2:
                    if (MyData.copingFiles.size()>0) {
                        progressDialog.setMessage(MyData.copingFiles.get(0).getFileName());
                    }
                    progressDialog.incrementProgressBy(1);
                    break;
                case 3:
                    progressDialog.dismiss();
                    init();
                    break;
                case 4:
                    progressDialog.setTitle("正在取消……");
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onBackPressed(){
        if (!MyData.isShowCheck) {
            if (MyData.boxFilePath.equals(File.separator)||MyData.boxFilePath.equals("")){
                getActivity().finish();
                return;
            }
            Log.i("MyData.boxFilePath",MyData.boxFilePath);
            if (MyData.boxFilePath.lastIndexOf(File.separator)==0){
                MyData.boxFilePath=File.separator;
            }else {
                MyData.boxFilePath = MyData.boxFilePath.substring(0,MyData.boxFilePath.lastIndexOf(File.separator));
            }
            init();
        }else {
            MyData.isShowCheck = false;
            MyData.selectBoxFiles.clear();
            upload.setVisibility(View.VISIBLE);
            allSelect.setChecked(false);
            menuRel.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }


}
