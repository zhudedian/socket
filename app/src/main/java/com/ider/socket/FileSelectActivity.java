package com.ider.socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.FileUtil;
import com.ider.socket.util.MyData;
import com.ider.socket.util.UploadUtil;
import com.ider.socket.view.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

public class FileSelectActivity extends Activity {

    private ListView listView;
    private RelativeLayout menu;
    private TextView delete;
    private ImageView close,upload;
    private FileAdapter adapter;
    private File uploadFile;
    private List<BoxFile> uploadFiles= new ArrayList<>();
    private List<BoxFile> overWriteFiles= new ArrayList<>();
    private List<BoxFile> uploadSelectFiles = new ArrayList<>();
    private boolean isOverDiaShow= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        menu = (RelativeLayout)findViewById(R.id.menu);
        delete = (TextView)findViewById(R.id.delete);
        close = (ImageView)findViewById(R.id.close);
        upload = (ImageView) findViewById(R.id.upload);
        listView = (ListView)findViewById(R.id.file_list);
        if (uploadFile == null){
            adapter = new FileAdapter(FileSelectActivity.this,R.layout.file_list_item,uploadFiles,uploadSelectFiles);
            listView.setAdapter(adapter);
            uploadFile = Environment.getExternalStorageDirectory();
            File[] files = uploadFile.listFiles();
            if (files != null){
                for(File f:files){
                    if (f.isDirectory()){
                        uploadFiles.add(new BoxFile(1,f.getName(), FileUtil.getSize(f),f.getPath()));
                    }else if (FileUtil.getFileType(f).equals(FileUtil.str_video_type)){
                        uploadFiles.add(new BoxFile(2,f.getName(), FileUtil.getSize(f),f.getPath()));
                    }else {
                        uploadFiles.add(new BoxFile(3,f.getName(), FileUtil.getSize(f),f.getPath()));
                    }

                }
            }
        }
        adapter.notifyDataSetChanged();
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BoxFile boxFile = uploadFiles.get(position);
                if (!MyData.isShowCheck) {
                    if (boxFile.getFileType() == 1) {
                        uploadFiles.clear();
                        uploadFile = new File(boxFile.getFilePath());
                        File[] files = uploadFile.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (f.isDirectory()) {
                                    uploadFiles.add(new BoxFile(1, f.getName(), FileUtil.getSize(f), f.getPath()));
                                } else if (FileUtil.getFileType(f).equals(FileUtil.str_video_type)) {
                                    uploadFiles.add(new BoxFile(2, f.getName(), FileUtil.getSize(f), f.getPath()));
                                } else {
                                    uploadFiles.add(new BoxFile(3, f.getName(), FileUtil.getSize(f), f.getPath()));
                                }

                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        uploadSelectFiles.clear();
                        uploadSelectFiles.add(boxFile);
                        showConfirmDialog();
                    }
                }else {
                    if (uploadSelectFiles.contains(boxFile)){
                        uploadSelectFiles.remove(boxFile);
                    }else {
                        uploadSelectFiles.add(boxFile);
                    }
                    if (uploadSelectFiles.size()>0){
                        upload.setVisibility(View.VISIBLE);
                        menu.setVisibility(View.VISIBLE);
                    }else {
                        upload.setVisibility(View.GONE);
                        menu.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MyData.isShowCheck = true;
                uploadSelectFiles.clear();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.isShowCheck = false;
                finish();
            }
        });

    }
    private void showDeleteDialog(){
        isOverDiaShow = true;
        View view = View.inflate(FileSelectActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(FileSelectActivity.this, popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        title.setText("删除警告！");
        if (uploadSelectFiles.size()==1){
            fileName.setText(uploadSelectFiles.get(0).getFileName());
        }else {
            fileName.setText("已选择"+uploadSelectFiles.size()+"文件");
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
                for (int i=0;i<uploadSelectFiles.size();i++){
                    File file = new File(uploadSelectFiles.get(i).getFilePath());
                    file.delete();
                    uploadFiles.remove(uploadSelectFiles.get(i));
                }
                uploadSelectFiles.clear();
                PopuUtils.dismissPopupDialog();
                upload.setVisibility(View.GONE);
                menu.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void showConfirmDialog() {
        View view = View.inflate(FileSelectActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(FileSelectActivity.this, popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        title.setText("确认上传？");
        if (uploadSelectFiles.size()==1){
            fileName.setText(uploadSelectFiles.get(0).getFileName());
        }else {
            fileName.setText("已选择"+uploadSelectFiles.size()+"文件");
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSelectFiles.clear();
                PopuUtils.dismissPopupDialog();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopuUtils.dismissPopupDialog();
                for (int i =0;i<uploadSelectFiles.size();i++){
                    BoxFile boxFile = uploadSelectFiles.get(i);
                    if (!MyData.boxFiles.contains(boxFile)) {
                        if (!MyData.uploadingFiles.contains(boxFile)) {
                            MyData.uploadingFiles.add(boxFile);
                        }
                    }else {
                        overWriteFiles.add(boxFile);
                        if (!isOverDiaShow) {
                            showOverWriteDialog();
                        }
                    }
                }
                uploading();
            }
        });
    }
    private void showOverWriteDialog(){
        isOverDiaShow = true;
        View view = View.inflate(FileSelectActivity.this, R.layout.confirm_upload, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(FileSelectActivity.this, popup);
        popupDialog.showAtLocation(listView, Gravity.CENTER, 0, 0);
        TextView title = (TextView)view.findViewById(R.id.title);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        title.setText("该文件已存在！");
        fileName.setText(overWriteFiles.get(0).getFileName());
        cancel.setText("跳过");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overWriteFiles.remove(0);
                PopuUtils.dismissPopupDialog();
                if (overWriteFiles.size()>0){
                    showOverWriteDialog();
                }else {
                    isOverDiaShow = false;
                }
            }
        });
        ok.setText("覆盖");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.uploadingFiles.add(overWriteFiles.get(0));
                uploading();
                PopuUtils.dismissPopupDialog();
                overWriteFiles.remove(0);
                if (overWriteFiles.size()>0){
                    showOverWriteDialog();
                }else {
                    isOverDiaShow = false;
                }
            }
        });
    }
    private void uploading(){
        if (!MyData.isUploading) {
            new Thread() {
                @Override
                public void run() {
                    MyData.isUploading = true;
                    while (MyData.uploadingFiles.size() > 0) {
                        BoxFile boxFile = MyData.uploadingFiles.get(0);
                        File file = new File(boxFile.getFilePath());
                        String result = UploadUtil.uploadFile(file, "http://192.168.2.15:8080/upload");
                        Log.i("result", result);
                        MyData.boxFiles.add(MyData.uploadingFiles.get(0));
                        MyData.uploadingFiles.remove(0);
                    }
                    MyData.isUploading = false;
                }
            }.start();
        }
    }
    @Override
    public void onBackPressed() {
//        Log.i("path",uploadFile.getPath());
//        Log.i("path",Environment.getExternalStorageDirectory().getPath());
        if (!MyData.isShowCheck) {
            if (uploadFile.equals(Environment.getExternalStorageDirectory())) {
                finish();
            } else {
                uploadFiles.clear();
                uploadFile = uploadFile.getParentFile();
                File[] files = uploadFile.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            uploadFiles.add(new BoxFile(1, f.getName(), FileUtil.getSize(f), f.getPath()));
                        } else if (FileUtil.getFileType(f).equals(FileUtil.str_video_type)) {
                            uploadFiles.add(new BoxFile(2, f.getName(), FileUtil.getSize(f), f.getPath()));
                        } else {
                            uploadFiles.add(new BoxFile(3, f.getName(), FileUtil.getSize(f), f.getPath()));
                        }

                    }
                }
                adapter.notifyDataSetChanged();
            }
        }else {
            MyData.isShowCheck = false;
            uploadSelectFiles.clear();
            upload.setVisibility(View.GONE);
            menu.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
}
