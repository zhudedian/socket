package com.ider.socket.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Eric on 2017/8/26.
 */

public class ApkFile extends DataSupport {
    private String fileName;

    private String filePath;

    private int fileSize;

    public ApkFile(String fileName,String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void setFileName(String name){
        this.fileName = name;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFilePath(String path){
        this.filePath = path;
    }
    public String getFilePath(){
        return filePath;
    }

    public void setFileSize(int size){
        this.fileSize = size;
    }

    public int getFileSize(){
        return fileSize;
    }
}
