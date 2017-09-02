package com.ider.socket.util;

import android.os.Environment;

import com.ider.socket.db.BoxFile;
import com.ider.socket.view.MyApkFragment;
import com.ider.socket.view.UninstallFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2017/8/28.
 */

public class MyData {
    public static String downUrl="http://192.168.2.15:8080/down";
    public static String uploadUrl="http://192.168.2.15:8080/upload";
    public static UninstallFragment uninstallFragment;
    public static MyApkFragment myApkFragment;
    public static boolean isShowCheck = false;
    public static boolean isUploading = false;
    public static boolean isDownloading = false;
    public static File dirSelect = Environment.getExternalStorageDirectory();
    public static File fileSelect = Environment.getExternalStorageDirectory();
    public static List<BoxFile> boxFiles = new ArrayList<>();
    public static List<BoxFile> selectBoxFiles = new ArrayList<>();
    public static List<BoxFile> uploadingFiles = new ArrayList<>();
    public static List<BoxFile> downLoadingFiles = new ArrayList<>();
    public static String boxFilePath="";

}
