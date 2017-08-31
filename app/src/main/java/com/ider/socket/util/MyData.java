package com.ider.socket.util;

import com.ider.socket.db.BoxFile;
import com.ider.socket.view.MyApkFragment;
import com.ider.socket.view.UninstallFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2017/8/28.
 */

public class MyData {
    public static UninstallFragment uninstallFragment;
    public static MyApkFragment myApkFragment;
    public static boolean isShowCheck = false;
    public static boolean isUploading = false;
    public static List<BoxFile> boxFiles = new ArrayList<>();
    public static List<BoxFile> uploadingFiles = new ArrayList<>();
    public static List<BoxFile> downLoadingFiles = new ArrayList<>();

}
