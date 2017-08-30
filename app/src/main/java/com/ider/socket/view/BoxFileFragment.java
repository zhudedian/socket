package com.ider.socket.view;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ider.socket.R;
import com.ider.socket.db.BoxFile;
import com.ider.socket.popu.PopuUtils;
import com.ider.socket.popu.PopupDialog;
import com.ider.socket.popu.Popus;
import com.ider.socket.util.BackHandled;
import com.ider.socket.util.CustomerHttpClient;
import com.ider.socket.util.FileUtil;
import com.ider.socket.util.MyData;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Eric on 2017/8/29.
 */

public class BoxFileFragment extends Fragment implements BackHandled{

    private URI mHttpUri ;
    private HttpClient mHttpClient;
    private OkHttpClient okHttpClient;
    private ListView listView;
    private ProgressBar progressBar;
    private FileAdapter adapter;
    private String fileName;
    private File uploadFile;
    private List<BoxFile> uploadFiles= new ArrayList<>();
    private List<BoxFile> uploadSelectFiles = new ArrayList<>();
    private List<BoxFile> boxFiles = new ArrayList<>();
    private List<BoxFile> selectFiles = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i("tag","onCreateView");
        View view = inflater.inflate(R.layout.file_list,container,false);
        listView = (ListView)view.findViewById(R.id.list_view);
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
        progressBar.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder().header("comment","" )
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
                        boxFiles.add(new BoxFile(type,fis[0],fis[1]));


                    }
                    mHandler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        adapter = new FileAdapter(getContext(),R.layout.file_list_item,boxFiles,selectFiles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MyData.isShowCheck) {
                    fileName = boxFiles.get(position).getFileName();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0, length = fileName.length(); i < length; i++) {
                        char c = fileName.charAt(i);
                        if (c <= '\u001f' || c >= '\u007f') {
                            stringBuffer.append(String.format("\\u%04x", (int) c));
                        } else {
                            stringBuffer.append(c);
                        }
                    }
                    fileName = stringBuffer.toString();
                    boxFiles.clear();
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder().header("comment", fileName)
                                        .url("http://192.168.2.15:8080/down").build();
                                Call call = okHttpClient.newCall(request);
                                Response response = call.execute();
                                String result = response.body().string();
                                Log.i("result", result);
                                if (request.equals("null")) {
                                    return;
                                }
                                String[] files = result.split("type=");
                                for (int i = 1; i < files.length; i++) {
                                    String[] fils = files[i].split("name=");
                                    int type = Integer.parseInt(fils[0]);
                                    String[] fis = fils[1].split("size=");
                                    boxFiles.add(new BoxFile(type, fis[0], fis[1]));
                                }
                                mHandler.sendEmptyMessage(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }else {
                    BoxFile boxFile = boxFiles.get(position);
                    if (selectFiles.contains(boxFile)){
                        selectFiles.remove(boxFile);
                    }else {
                        selectFiles.add(boxFile);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                fileName = boxFiles.get(position).getFileName();
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0, length = fileName.length(); i < length; i++) {
                    char c = fileName.charAt(i);
                    if (c <= '\u001f' || c >= '\u007f') {
                        stringBuffer.append(String.format("\\u%04x", (int) c));
                    } else {
                        stringBuffer.append(c);
                    }
                }
                fileName = stringBuffer.toString();
                showFileControlDialog();
                return true;
            }
        });
    }
    private void showFileUploadDialog(){
        View view = View.inflate(getContext(), R.layout.upload_file_select, null);
        Popus popup = new Popus();
        popup .setvWidth(-1);
        popup .setvHeight(-1);
        popup .setClickable( true );
        popup .setAnimFadeInOut(R.style.PopupWindowAnimation );
        popup.setCustomView(view);
        popup .setContentView(R.layout.activity_file_ex );
        PopupDialog popupDialog = PopuUtils.createPopupDialog (getContext(), popup );
        popupDialog.showAtLocation(listView, Gravity.CENTER,0,0);
        ListView fileList = (ListView)view.findViewById(R.id.file_list);
        final FileAdapter adapter = new FileAdapter(getContext(),R.layout.file_list_item,uploadFiles,uploadSelectFiles);
        fileList.setAdapter(adapter);
        if (uploadFile == null){
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
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return true;
            }
        });
    }
    private void showFileControlDialog() {
        View view = View.inflate(getContext(), R.layout.file_control, null);
        TextView delete = (TextView)view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                boxFiles.clear();
//                new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Request request = new Request.Builder().header("comment","\"delete=\""+fileName )
//                                    .url("http://192.168.2.15:8080/down").build();
//                            Call call = okHttpClient.newCall(request);
//                            Response response = call.execute();
//                            String result = response.body().string();
//                            Log.i("result",result);
//                            String[] files = result.split("type=");
//                            for (int i =1 ;i<files.length;i++){
//                                String[] fils = files[i].split("name=");
//                                int type = Integer.parseInt(fils[0]);
//                                String[] fis = fils[1].split("size=");
//                                boxFiles.add(new BoxFile(type,fis[0],fis[1]));
//                            }
//                            mHandler.sendEmptyMessage(0);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//                PopuUtils.dismissPopupDialog();
            }
        });
        TextView moreSelect = (TextView)view.findViewById(R.id.more_select);
        moreSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyData.isShowCheck = true;
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
            boxFiles.clear();
            progressBar.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder().header("comment", "comment_back")
                                .url("http://192.168.2.15:8080/down").build();
                        Call call = okHttpClient.newCall(request);
                        Response response = call.execute();
                        String result = response.body().string();
                        Log.i("result", result);
                        if (result.equals("top")) {
                            mHandler.sendEmptyMessage(1);
                            return;
                        }
                        String[] files = result.split("type=");
                        for (int i = 1; i < files.length; i++) {
                            String[] fils = files[i].split("name=");
                            int type = Integer.parseInt(fils[0]);
                            String[] fis = fils[1].split("size=");
                            boxFiles.add(new BoxFile(type, fis[0], fis[1]));
                        }
                        mHandler.sendEmptyMessage(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }else {
            MyData.isShowCheck = false;
            selectFiles.clear();
            adapter.notifyDataSetChanged();
        }
    }

}
