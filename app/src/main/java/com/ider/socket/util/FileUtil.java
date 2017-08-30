package com.ider.socket.util;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Eric on 2017/8/29.
 */

public class FileUtil {


    public static String str_audio_type = "audio/*";
    public static String str_video_type = "video/*";
    public static String str_image_type = "image/*";
    public static String str_txt_type = "text/plain";
    public static String str_pdf_type = "application/pdf";
    public static String str_epub_type = "application/epub+zip";
    public static String str_apk_type = "application/vnd.android.package-archive";
    public static String getFileType(File f)
    {
        String type="";
        String fName=f.getName();
        String end=fName.substring(fName.lastIndexOf(".")+1,
                fName.length()).toLowerCase();

      /* get type name  by MimeType */
        if(end.equalsIgnoreCase("mp3")||end.equalsIgnoreCase("wma")
                ||end.equalsIgnoreCase("mp1")||end.equalsIgnoreCase("mp2")
                ||end.equalsIgnoreCase("ogg")||end.equalsIgnoreCase("oga")
                ||end.equalsIgnoreCase("flac")||end.equalsIgnoreCase("ape")
                ||end.equalsIgnoreCase("wav")||end.equalsIgnoreCase("aac")
                ||end.equalsIgnoreCase("m4a")||end.equalsIgnoreCase("m4r")
                ||end.equalsIgnoreCase("amr")||end.equalsIgnoreCase("mid")
                ||end.equalsIgnoreCase("asx"))
        {
            type = str_audio_type;
        }
        else if(end.equalsIgnoreCase("3gp")||end.equalsIgnoreCase("mp4")
                ||end.equalsIgnoreCase("rmvb")||end.equalsIgnoreCase("3gpp")
                ||end.equalsIgnoreCase("avi")||end.equalsIgnoreCase("rm")
                ||end.equalsIgnoreCase("mov")||end.equalsIgnoreCase("flv")
                ||end.equalsIgnoreCase("mkv")||end.equalsIgnoreCase("wmv"))
//		  ||end.equalsIgnoreCase("divx")||end.equalsIgnoreCase("bob")
//		  ||end.equalsIgnoreCase("mpg") || end.equalsIgnoreCase("mpeg")
//		  ||end.equalsIgnoreCase("ts") || end.equalsIgnoreCase("dat")
//		  ||end.equalsIgnoreCase("m2ts")||end.equalsIgnoreCase("vob")
//		  ||end.equalsIgnoreCase("asf")||end.equalsIgnoreCase("evo")
//		  ||end.equalsIgnoreCase("iso"))
        {
            type = str_video_type;
//        if(end.equalsIgnoreCase("3gpp")){
//        	if(isVideoFile(f)){
//        		type = str_video_type;
//        	}else{
//        		type = str_audio_type;
//        	}
//        }
        }
        else if(end.equalsIgnoreCase("jpg")||end.equalsIgnoreCase("gif")
                ||end.equalsIgnoreCase("png")||end.equalsIgnoreCase("jpeg")
                ||end.equalsIgnoreCase("bmp"))
        {
            type = str_image_type;
        }
        else if(end.equalsIgnoreCase("txt"))
        {
            type = str_txt_type;
        }
        else if(end.equalsIgnoreCase("epub") || end.equalsIgnoreCase("pdb") || end.equalsIgnoreCase("fb2") || end.equalsIgnoreCase("rtf") || end.equalsIgnoreCase("txt"))
        {
            type = str_epub_type;
        }
        else if(end.equalsIgnoreCase("pdf"))
        {
            type = str_pdf_type;
        }
        else if(end.equalsIgnoreCase("apk"))
        {
            type = str_apk_type;
        }
        else
        {
            type="*/*";
        }

        return type;
    }
    public static boolean isApk(File file){
        if (getFileType(file).equals(str_apk_type)){
            return true;
        }
        return false;
    }
    public static String getSize(File file){
        if (file.isDirectory()){
            double size = file.getTotalSpace();
            if (size<1024){
                return size+"Byte";
            }else if (size<1024*1024){
                int si = (int) (size/1024*100);
                double s = si;
                return s/100+"K";
            }else if (size<1024*1024*1024){
                int si = (int) (size/1024/1024*100);
                double s = si;
                return s/100+"M";
            }else if (size/1024<1024*1024*1024){
                int si = (int) (size/1024/1024/1024*100);
                double s = si;
                return s/100+"G";
            }else {
                int si = (int) (size/1024/1024/1024/1024*100);
                double s = si;
                return s/100+"T";
            }
        }else {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                double size = fileInputStream.available();
                if (size<1024){
                    return size+"Byte";
                }else if (size<1024*1024){
                    int si = (int) (size/1024*100);
                    double s = si;
                    return s/100+"K";
                }else if (size<1024*1024*1024){
                    int si = (int) (size/1024/1024*100);
                    double s = si;
                    return s/100+"M";
                }else if (size/1024<1024*1024*1024){
                    int si = (int) (size/1024/1024/1024*100);
                    double s = si;
                    return s/100+"G";
                }else {
                    int si = (int) (size/1024/1024/1024/1024*100);
                    double s = si;
                    return s/100+"T";
                }
            } catch (Exception e) {
				e.printStackTrace();
                return "0B";
            }
        }


    }
}
