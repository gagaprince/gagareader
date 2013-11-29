package com.prince.gagareader.util;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.prince.gagareader.bean.Const;

public class ImageUtil {
	private static ImageUtil self;
	public static ImageUtil getInstance(){
		if(self==null){
			self = new ImageUtil();
		}
		return self;
	}
	private ImageUtil(){}
	public interface OnPreparedImageListenner{
		public void onPrepared(Drawable bitmap);
	}
	public void preparedImage(final String imageUrl,final OnPreparedImageListenner listenner){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String imgPathStr = md5(imageUrl);
				String imgFoldPath = Const.APP_PHOTO_CACHE+"/"+imgPathStr.substring(0,4)+"/"+imgPathStr.substring(4, 8);
				String imgPath = imgFoldPath+"/"+imgPathStr.substring(8,16);
				FileUtil fu = FileUtil.getInstance();
				fu.mkdir(imgFoldPath);
				File imgFile = new File(imgPath);
				Drawable bitmap = null;
				bitmap = Drawable.createFromPath(imgPath);
				if(imgFile.exists()&&bitmap!=null){
					listenner.onPrepared(bitmap);
				}else{
					try {
						fu.saveUrlContentToFile(imageUrl, imgPath);
						Log.e("imgpath+imgurl", imgPath+":"+imageUrl);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bitmap = Drawable.createFromPath(imgPath);
					listenner.onPrepared(bitmap);
				}
			}
		}).start();
	}
	private String md5(String plainText) { 
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			md.update(plainText.getBytes()); 
			byte b[] = md.digest(); 
			int i; 
			StringBuffer buf = new StringBuffer(""); 
			for (int offset = 0; offset < b.length; offset++) { 
				i = b[offset]; 
				if(i<0) i+= 256; 
				if(i<16) 
					buf.append("0"); 
				buf.append(Integer.toHexString(i)); 
			} 
			plainText = buf.toString().substring(8,24);
		} catch (NoSuchAlgorithmException e) { 
			e.printStackTrace(); 
		}
		return plainText; 
	} 

}
