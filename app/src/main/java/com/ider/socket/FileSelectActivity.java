package com.ider.socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ider.socket.db.BoxFile;
import com.ider.socket.util.FileUtil;
import com.ider.socket.view.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSelectActivity extends Activity {

    private ListView listView;
    private ImageView close;
    private FileAdapter adapter;
    private File uploadFile;
    private List<BoxFile> uploadFiles= new ArrayList<>();
    private List<BoxFile> uploadSelectFiles = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        close = (ImageView)findViewById(R.id.close);
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BoxFile boxFile = uploadFiles.get(position);
                if (boxFile.getFileType()==1){
                    uploadFiles.clear();
                    uploadFile = new File(boxFile.getFilePath());
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
                    adapter.notifyDataSetChanged();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        Log.i("path",uploadFile.getPath());
        Log.i("path",Environment.getExternalStorageDirectory().getPath());
        if (uploadFile.equals(Environment.getExternalStorageDirectory())){
            finish();
        }else {
            uploadFiles.clear();
            uploadFile = uploadFile.getParentFile();
            File[] files = uploadFile.listFiles();
            if (files != null){
                for(File f:files) {
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
    }
}
