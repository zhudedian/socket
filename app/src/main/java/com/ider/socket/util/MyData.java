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

    public static boolean isConnect ;
    public static String boxIP;
    public static String infoUrl;
    public static String downUrl;
    public static String uploadUrl;
    public static String installUrl;
    public static String appIconUrl;
    public static String editInfo;
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
    public static List<BoxFile> copingFiles = new ArrayList<>();
    public static String boxFilePath="";
    public static String picIconSavePath;
    public static String screenshotSavePath;
    public static SocketClient client;

}
