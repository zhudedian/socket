package com.ider.socket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.FileUtil;
import com.ider.socket.util.ListSort;
import com.ider.socket.util.MyData;
import com.ider.socket.util.UploadUtil;
import com.ider.socket.view.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static com.ider.socket.util.SocketClient.mHandler;

public class FileSelectActivity extends Activity {

    private ListView listView;
    private ProgressDialog progressDialog;
    private RelativeLayout menu;
    private TextView fileSelect;
    private ImageView delete,close,upload,createDir;
    private CheckBox allSelect;
    private FileAdapter adapter;
    private List<BoxFile> uploadFiles= new ArrayList<>();
    private List<BoxFile> overWriteFiles= new ArrayList<>();
    private List<BoxFile> uploadSelectFiles = new ArrayList<>();
    private boolean isOverDiaShow= false;
    private int uploadSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        menu = (RelativeLayout)findViewById(R.id.menu);
        delete = (ImageView) findViewById(R.id.delete);
        allSelect = (CheckBox)findViewById(R.id.all_select);
        fileSelect = (TextView)findViewById(R.id.select_path);
        close = (ImageView)findViewById(R.id.close);
        upload = (ImageView) findViewById(R.id.upload);
        createDir = (ImageView)findViewById(R.id.create_dir);
        listView = (ListView)findViewById(R.id.file_list);
        adapter = new FileAdapter(FileSelectActivity.this,R.layout.file_list_item,uploadFiles,uploadSelectFiles);
        listView.setAdapter(adapter);
        fileSelect.setText(MyData.fileSelect.getPath());
        File[] files = MyData.fileSelect.listFiles();
        if (files != null){
            for(File f:files){
                addFile(f);
            }
        }
        ListSort.sort(uploadFiles);
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
                        MyData.fileSelect = new File(boxFile.getFilePath());
                        fileSelect.setText(MyData.fileSelect.getPath());
                        File[] files = MyData.fileSelect.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                addFile(f);
                            }
                        }
                        ListSort.sort(uploadFiles);
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
                        delete.setVisibility(View.VISIBLE);
                    }else {
                        upload.setVisibility(View.GONE);
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
                uploadSelectFiles.clear();
                createDir.setVisibility(View.GONE);
                menu.setVisibility(View.VISIBLE);
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
        allSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allSelect.isChecked()) {
                    uploadSelectFiles.clear();
                    uploadSelectFiles.addAll(uploadFiles);
                    upload.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }else {
                    uploadSelectFiles.clear();
                    upload.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        createDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDirDialog();
            }
        });

    }
    private void addFile(File f){
        if (f.isDirectory()) {
            uploadFiles.add(new BoxFile(1, f.getName(), FileUtil.getSize(f), f.getPath()));
        } else if (FileUtil.getFileType(f).equals(FileUtil.str_video_type)) {
            uploadFiles.add(new BoxFile(2, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_audio_type)){
            uploadFiles.add(new BoxFile(3, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_image_type)){
            uploadFiles.add(new BoxFile(4, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_apk_type)){
            uploadFiles.add(new BoxFile(5, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_zip_type)){
            uploadFiles.add(new BoxFile(6, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_pdf_type)){
            uploadFiles.add(new BoxFile(7, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else if (FileUtil.getFileType(f).equals(FileUtil.str_txt_type)){
            uploadFiles.add(new BoxFile(8, f.getName(), FileUtil.getSize(f), f.getPath()));
        }else {
            uploadFiles.add(new BoxFile(9, f.getName(), FileUtil.getSize(f), f.getPath()));
        }
    }
    private void showProgressDialog(){
        progressDialog = new ProgressDialog(FileSelectActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setTitle("请稍后……");
        progressDialog.setMax(uploadSize);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyData.uploadingFiles.clear();
                        UploadUtil.isStart = false;
                        mHandler.sendEmptyMessage(2);
                    }
                });
        progressDialog.setMessage(MyData.uploadingFiles.get(0).getFileName());
        progressDialog.show();
    }
    private void showCreateDirDialog() {
        View view = View.inflate(FileSelectActivity.this, R.layout.create_dir, null);
        Popus popup = new Popus();
        popup.setvWidth(-1);
        popup.setvHeight(-1);
        popup.setClickable(true);
        popup.setAnimFadeInOut(R.style.PopupWindowAnimation);
        popup.setCustomView(view);
        popup.setContentView(R.layout.activity_file_select);
        PopupDialog popupDialog = PopuUtils.createPopupDialog(FileSelectActivity.this, popup);
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
                String makePath = MyData.fileSelect.getPath();
                makePath = makePath.endsWith("/")?(makePath+dir):(makePath+"/"+dir);
                File newDir=new File(makePath);
                if (!newDir.exists()){
                    newDir.mkdirs();
                    uploadFiles.add(new BoxFile(1, newDir.getName(), FileUtil.getSize(newDir), newDir.getPath()));
                    adapter.notifyDataSetChanged();
                }
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
                    if (file.isDirectory()&&file.exists()){
                        dirDelete(file);
                        uploadFiles.remove(uploadSelectFiles.get(i));
                    }else {
                        if (file.exists()){
                            file.delete();
                            uploadFiles.remove(uploadSelectFiles.get(i));
                        }
                    }


                }
                uploadSelectFiles.clear();
                PopuUtils.dismissPopupDialog();
                upload.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
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
                MyData.uploadingFiles.clear();
                PopuUtils.dismissPopupDialog();
                for (int i =0;i<uploadSelectFiles.size();i++){
                    BoxFile boxFile = uploadSelectFiles.get(i);
                    if (!MyData.boxFiles.contains(boxFile)) {
                        if (!MyData.uploadingFiles.contains(boxFile)) {
                            boxFile.setSavePath(MyData.boxFilePath);
                            MyData.uploadingFiles.add(boxFile);
                        }
                    }else {
                        overWriteFiles.add(boxFile);
                    }
                }
                if (overWriteFiles.size()>0) {
                    showOverWriteDialog();
                }else {
                    uploadSize = MyData.uploadingFiles.size();
                    uploading();
                }
            }
        });
    }
    private void dirDelete(File dir){
        File[] files = dir.listFiles();
        for (File file:files){
            if (file.isDirectory()){
                dirDelete(file);
            }else {
                file.delete();
            }
        }
        dir.delete();
    }
    private void showOverWriteDialog(){
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
        LinearLayout allSelect = (LinearLayout)view.findViewById(R.id.all_select);
        final CheckBox allcheck = (CheckBox)view.findViewById(R.id.all_select_check);
        TextView fileName = (TextView)view.findViewById(R.id.file_name);
        Button cancel = (Button)view.findViewById(R.id.cancel_action);
        Button ok = (Button)view.findViewById(R.id.ok_action);
        if (overWriteFiles.size()==1){
            allSelect.setVisibility(View.GONE);
        }else {
            allSelect.setVisibility(View.VISIBLE);
        }
        title.setText("该文件已存在！");
        fileName.setText(overWriteFiles.get(0).getFileName());
        cancel.setText("跳过");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allcheck.isChecked()){
                    overWriteFiles.clear();
                    PopuUtils.dismissPopupDialog();
                    uploadSize = MyData.uploadingFiles.size();
                    uploading();
                }else {
                    overWriteFiles.remove(0);
                    PopuUtils.dismissPopupDialog();
                    if (overWriteFiles.size() > 0) {
                        showOverWriteDialog();
                    } else {
                        uploadSize = MyData.uploadingFiles.size();
                        uploading();
                    }
                }
            }
        });
        ok.setText("覆盖");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allcheck.isChecked()){
                    for (BoxFile boxFile:overWriteFiles){
                        boxFile.setSavePath(MyData.boxFilePath);
                        MyData.uploadingFiles.add(boxFile);
                    }
                    overWriteFiles.clear();
                    uploadSize = MyData.uploadingFiles.size();
                    uploading();
                    PopuUtils.dismissPopupDialog();
                }else {
                    BoxFile boxFile = overWriteFiles.get(0);
                    boxFile.setSavePath(MyData.boxFilePath);
                    MyData.uploadingFiles.add(boxFile);
                    PopuUtils.dismissPopupDialog();
                    overWriteFiles.remove(0);
                    if (overWriteFiles.size()>0){
                        showOverWriteDialog();
                    }else {
                        uploading();
                        uploadSize = MyData.uploadingFiles.size();
                    }
                }

            }
        });
        allcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    if (MyData.uploadingFiles.size()>0) {
                        progressDialog.setMessage(MyData.uploadingFiles.get(0).getFileName());
                    }
                    progressDialog.setMax(uploadSize);
                    progressDialog.incrementProgressBy(1);
                    break;
                case 1:
                    progressDialog.dismiss();
                    break;
                case 2:
                    progressDialog.setTitle("正在取消……");
                    break;

            }
        }
    };
    private void uploading(){
        if (!MyData.isUploading) {
            showProgressDialog();
            new Thread() {
                @Override
                public void run() {
                    MyData.isUploading = true;
                    while (MyData.uploadingFiles.size() > 0) {
                        BoxFile boxFile = MyData.uploadingFiles.get(0);
                        if (boxFile.getFileType()==1){
                            File file = new File(boxFile.getFilePath());
                            File[] files = file.listFiles();
                            if (files!=null) {
                                for (File f : files) {
                                    BoxFile boxFile2;
                                    if (f.isDirectory()) {
                                        boxFile2= new BoxFile(1, f.getName(), FileUtil.getSize(f), f.getPath());
                                        boxFile2.setSavePath(boxFile.getSavePath()+"/"+boxFile.getFileName());
                                        MyData.uploadingFiles.add(boxFile2);
                                    } else if (FileUtil.getFileType(f).equals(FileUtil.str_video_type)) {
                                        boxFile2= new BoxFile(2, f.getName(), FileUtil.getSize(f), f.getPath());
                                        boxFile2.setSavePath(boxFile.getSavePath()+"/"+boxFile.getFileName());
                                        MyData.uploadingFiles.add(boxFile2);
                                    } else {
                                        boxFile2= new BoxFile(3, f.getName(), FileUtil.getSize(f), f.getPath());
                                        boxFile2.setSavePath(boxFile.getSavePath()+"/"+boxFile.getFileName());
                                        MyData.uploadingFiles.add(boxFile2);
                                    }
                                    uploadSize++;
                                }
                            }
                            MyData.uploadingFiles.remove(0);
                            mHandler.sendEmptyMessage(0);
                        }else {
                            File file = new File(boxFile.getFilePath());
                            Log.i("boxFile.getFilePath()", boxFile.getFilePath());
                            int result = UploadUtil.uploadFile(file, MyData.uploadUrl, boxFile.getSavePath());
                            Log.i("result", result+"");
                            if (result!=200){
                                continue;
                            }else {
                                if (MyData.uploadingFiles.size()>0) {
                                    MyData.uploadingFiles.remove(0);
                                }
                                mHandler.sendEmptyMessage(0);
                            }
                        }
                    }
                    MyData.isUploading = false;
                    mHandler.sendEmptyMessage(1);
                }
            }.start();
        }
    }
    @Override
    public void onBackPressed() {
//        Log.i("path",uploadFile.getPath());
//        Log.i("path",Environment.getExternalStorageDirectory().getPath());
        if (!MyData.isShowCheck) {
            if (MyData.fileSelect.equals(Environment.getExternalStorageDirectory())) {
                finish();
            } else {
                uploadFiles.clear();
                MyData.fileSelect = MyData.fileSelect.getParentFile();
                fileSelect.setText(MyData.fileSelect.getPath());
                File[] files = MyData.fileSelect.listFiles();
                if (files != null) {
                    for (File f : files) {
                        addFile(f);
                    }
                }
                ListSort.sort(uploadFiles);
                adapter.notifyDataSetChanged();
            }
        }else {
            MyData.isShowCheck = false;
            uploadSelectFiles.clear();
            upload.setVisibility(View.GONE);
            createDir.setVisibility(View.VISIBLE);
            allSelect.setChecked(false);
            menu.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
}
