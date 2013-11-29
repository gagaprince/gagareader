package com.prince.gagareader.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;

import com.prince.gagareader.bean.Const;

public class ImageCacheUtil {
	private static ImageCacheUtil self;
	public static ImageCacheUtil getInstance(){
		if(self==null){
			self = new ImageCacheUtil();
		}
		return self;
	}
	private Map<String, SoftReference<Drawable>> caches;
	private ImageCacheUtil(){
		caches = new HashMap<String, SoftReference<Drawable>>();
	}
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
				Drawable bitmap = null;
				if(caches.containsKey(imgPath)){
					bitmap = caches.get(imgPath).get();
					if(bitmap!=null){
						listenner.onPrepared(bitmap);
						return;
					}
				}
				File imgFile = new File(imgPath);
				bitmap = Drawable.createFromPath(imgPath);
				if(imgFile.exists()&&bitmap!=null){
					caches.put(imgPath, new SoftReference<Drawable>(bitmap));
					listenner.onPrepared(bitmap);
				}else{
					try {
						fu.saveUrlContentToFile(imageUrl, imgPath);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bitmap = Drawable.createFromPath(imgPath);
					caches.put(imgPath, new SoftReference<Drawable>(bitmap));
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
