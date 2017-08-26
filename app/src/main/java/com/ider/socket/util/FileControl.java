/*
 * Copyright (C) 2009 The Rockchip Android MID Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ider.socket.util;


import android.content.Context;
import android.content.res.Resources;

import android.util.Log;
import android.widget.ListView;


import java.io.File;


public class FileControl {	
	final String TAG = "FileControl.java";
	final boolean DEBUG = true;	//true;
	private void LOG(String str)
	{
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}
	Resources resources;
	static Context context_by;
	ListView main_ListView;
	ListView mDeviceList;
	public String currently_parent = null;	
	public String currently_path = null;
	
	int currently_state;
	final int AZ_COMPOSITOR = 0;	/* sort by name*/
	final int TIME_COMPOSITOR = 1;	/* sort by time*/
	final int SIZE_COMPOSITOR = 2;	/* sort by size */ 
	final int TYPE_COMPOSITOR = 3;	/* sort by type */
	
	String[] music_postfix = {".mp3", ".ogg", ".wma", ".wav", ".ape", 
								".mid", ".flac", ".mp3PRO", ".au", ".avi"};
	int size_postfix[] = new int[music_postfix.length];
	int pit_postfix[] = new int[music_postfix.length];
	
	public static String str_audio_type = "audio/*";
	public static String str_video_type = "video/*";
	public static String str_image_type = "image/*";
	public static String str_txt_type = "text/plain";
	public static String str_pdf_type = "application/pdf";
	public static String str_epub_type = "application/epub+zip";
	public static String str_apk_type = "application/vnd.android.package-archive";
	
	static boolean is_enable_fill = true;
	static boolean is_finish_fill = false;
	
	static boolean is_first_path = true;

	public static String str_last_path = null;
	public static int last_item;

	public static boolean is_enable_del = true;
	public static boolean is_finish_del = true;
	public FileControl(){

	}
    public FileControl(Context context, String path, ListView tmp_main_listview) {
                
        currently_parent = path;
        currently_path = path;
        currently_state = SIZE_COMPOSITOR;    		
        main_ListView = tmp_main_listview;
        context_by = context;

		
    }
       

    

    


    
    
    public String get_currently_parent(){
    	return currently_parent;
    }
    
    public String get_currently_path(){
    	return currently_path;
    }
    
    public void set_main_ListView(ListView tmp_listview){
    	main_ListView = tmp_listview;
    	if(main_ListView == null){
    		LOG("in set_main_ListView,------------main_ListView = null");
    	}
    }

	public static boolean isApk(File file){
		if (getMIMEType(file).equals(str_apk_type)){
			return true;
		}
		return false;
	}
    

    
    public static String getMIMEType(File f)
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
	public static boolean isAdd(File f){
		String type = getMIMEType(f);
		if (type.equals(str_audio_type)||type.equals(str_video_type)||type.equals(str_image_type)||type.equals(str_txt_type)||type.equals(str_pdf_type)
				||type.equals(str_apk_type)){
			return true;
		}
		return false;
	}
    

    private String fname;

	
    public boolean deleteDirectory(File dir){
	boolean ret = true;
	if(!is_enable_del)
		return false;
    	File[] file = dir.listFiles();
    	for (int i = 0; i < file.length; i++) {
    		if (file[i].isFile()){
			if(!is_enable_del)
				return false;
    			if(!file[i].delete()){
				Log.e(TAG, "  ------- :    Delete file " + file[i].getPath() + " fail~~");
			}
			if(!file[i].canWrite())
				ret = false;
    		}else{
			if(!is_enable_del)
				return false;
    			deleteDirectory(file[i]);
		}
    	}
    	dir.delete();
	return ret;
    }
    
    public void deleteFile(File file){
    	if(file == null)
    		return;
    	LOG(" delete file:%s" + file.getPath());
    	File tmp_file = null;
    	if((tmp_file = new File(currently_path+File.separator+file.getName())).exists()){
    		tmp_file.delete();
    	}
    }
    

    

    


    



    

}
