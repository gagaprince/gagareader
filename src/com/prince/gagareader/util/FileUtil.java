package com.prince.gagareader.util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FileUtil {
	private static FileUtil self;
	public static FileUtil getInstance(){
		if(self==null){
			self = new FileUtil();
		}
		return self;
	}
	private FileUtil(){}
	
	public void mkdir(String filepath){
		File fdir = new File(filepath);
		if(!fdir.exists()){
			Log.e("mkdir", filepath);
			fdir.mkdir();
			Log.e("mkdir", fdir.exists()+"");
		}
	}
	
	public String getFileContent(String path){
		return getFileContent(new File(path));
	}
	
	public String getFileContent(File f){
		StringBuffer sb = new StringBuffer("");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f),"utf-8"));
			String strBuffer = "";
			while((strBuffer=br.readLine())!=null){
				sb.append(strBuffer);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public String getAssetFileContent(Context context,String fileName){
		String res="";
		try {  
            InputStream in = context.getResources().getAssets().open(fileName);
            int length = in.available();  
            byte[] buffer = new byte[length];  
            in.read(buffer);  
            res = EncodingUtils.getString(buffer, "UTF-8");  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        return res;
	}
	
	public List<String> getAssetFileContentList(Context context,String fileName){
		List<String> strList=new ArrayList<String>();
		try {  
            InputStream in = context.getResources().getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String str = null;
            while((str=br.readLine())!=null){
            	strList.add(str);
            }
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        return strList;
	}
	
	public String getUrlContent(String url) throws IOException{
		StringBuffer sb = new StringBuffer("");
		HttpURLConnection conn = null;
		BufferedReader br = null;
		try {
			URL urlHttp = new URL(url);
			conn = (HttpURLConnection) urlHttp.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String str = null;
			while((str = br.readLine())!=null){
				sb.append(str);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}finally{
			if (conn!=null) {
				conn.disconnect();
			}
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public Bitmap getBitmapFromFile(File f){
		InputStream in = null;
		Bitmap bitmap = null;
		try {
			in = new FileInputStream(f);
			bitmap = BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}
	
	public void saveUrlContentToFile(String url,String filePath) throws IOException{
		if(url==null||!url.startsWith("http:"))return;
		checkFileExist(filePath);
		File saveFile = new File(filePath);
		HttpURLConnection conn = null;
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		try {
			URL urlHttp = new URL(url);
			conn = (HttpURLConnection) urlHttp.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			bis = new BufferedInputStream(conn.getInputStream());
			fos = new FileOutputStream(saveFile);
			byte[] bytes = new byte[1024];
			int length = 0;
			while((length=bis.read(bytes))!=-1){
				fos.write(bytes, 0, length);
				fos.flush();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}finally{
			if(conn!=null){
				conn.disconnect();
				conn = null;
			}
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void checkFileExist(String target) {
		File file = new File(target);
		if (!file.exists()) {
			// 判断文件�?��的路径是否存在，不存在则创建
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				Log.e("createFile", file.getPath());
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isFileExist(String target) {
		File file = new File(target);
		return file.exists();
	}

	public long getFreeSize() {
		try {
			// 取得SDCard当前的状�?
			String sDcString = android.os.Environment.getExternalStorageState();
			if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
				// 取得sdcard文件路径
				File pathFile = android.os.Environment
						.getExternalStorageDirectory();
				android.os.StatFs statfs = new android.os.StatFs(
						pathFile.getPath());
				// 获取SDCard上每个block的SIZE
				long nBlocSize = statfs.getBlockSize();
				// 获取可供程序使用的Block的数�?
				long nAvailaBlock = statfs.getAvailableBlocks();
				// 计算 SDCard 剩余大小Byte
				long nSDFreeSize = nAvailaBlock * nBlocSize;
				return nSDFreeSize;
			} else {
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean isHaveSize() {
		if (getFreeSize() < 10485670) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean checkFileSize(String filePath,int size){
		File f = new File(filePath);
		long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            try {
				fis = new FileInputStream(f);
				s= fis.available();
	           	fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return s==size;
	}
	
	public void copyFile(String yuan,String des){
		if(yuan==null||"".equals(yuan)||des==null||"".equals(des))return;
		File yuanFile = new File(yuan);
		File desFile = new File(des);
		
		if(!desFile.exists()){
			try {
				desFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(yuanFile));
			fos = new FileOutputStream(desFile);
			byte[] bytes = new byte[1024];
			int length = 0;
			while((length=bis.read(bytes))!=-1){
				fos.write(bytes, 0, length);
				fos.flush();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void  savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void deleteFile(File f){
		if(f.isDirectory()){
			File[] fs = f.listFiles();
			for(File ft:fs){
				deleteFile(ft);
			}
		}else{
			f.delete();
		}
	}
}
