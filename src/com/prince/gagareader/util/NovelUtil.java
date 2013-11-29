package com.prince.gagareader.util;

import java.io.File;
import java.io.IOException;

import com.prince.gagareader.bean.Const;

public class NovelUtil {
	private static NovelUtil self;
	public static NovelUtil getInstance(){
		if(self==null){
			self = new NovelUtil();
		}
		return self;
	}
	private NovelUtil(){}
	
	public String preparedNovelByNidCid(String nid,String cid) throws IOException{
		String url = Const.NOVEL_CONTENT_URL+"&nid="+nid+"&cid="+cid;
		String txtFoldPath = Const.APP_TEXT_CACHE+"/"+nid;
		String filePath = txtFoldPath+"/"+cid;
		FileUtil fu = FileUtil.getInstance();
		fu.mkdir(txtFoldPath);
		File txtFile = new File(filePath);
		if(!txtFile.exists()){
			fu.saveUrlContentToFile(url, filePath);
		}
		String novel = fu.getFileContent(txtFile);
		return novel;
	}
	public boolean downloadNovelByNidCid(String nid,String cid) throws IOException{
		String url = Const.NOVEL_CONTENT_URL+"&nid="+nid+"&cid="+cid;
		String txtFoldPath = Const.APP_TEXT_CACHE+"/"+nid;
		String filePath = txtFoldPath+"/"+cid;
		FileUtil fu = FileUtil.getInstance();
		fu.mkdir(txtFoldPath);
		File txtFile = new File(filePath);
		if(!txtFile.exists()){
			fu.saveUrlContentToFile(url, filePath);
			return true;
		}
		return false;
	}
	
	public void removeNovelByNid(String nid){
		String txtFoldPath = Const.APP_TEXT_CACHE+"/"+nid;
		File f = new File(txtFoldPath);
		FileUtil fu = FileUtil.getInstance();
		fu.deleteFile(f);
	}
}
