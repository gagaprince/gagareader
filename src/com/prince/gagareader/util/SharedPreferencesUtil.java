package com.prince.gagareader.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.BookMarkBean;

/**
 * 主要记忆  书架中的书籍 基本信息  以及每本书阅读记忆  阅读记忆包含 阅读到哪一章  精确到上次离开时
 * 阅读页 风格 包含 背景 文字 亮度 开关灯 等标志  暂时不提供加书签的功能
 * 
 * @author gagaprince
 *
 */
public class SharedPreferencesUtil {
	private static SharedPreferencesUtil self;
	public static SharedPreferencesUtil getInstance(){
		if(self==null){
			self = new SharedPreferencesUtil();
		}
		return self;
	}
	private SharedPreferencesUtil(){}	
	private SharedPreferences sp;
	public final String DB_NAME = "gaga_reader";
	public final String SPLIT_FLAG="~=~";
	public final String BOOK_SPLIT_FLAG="~@~";
	public final String BOOK_LIST_KEY = "book_list";
	public final String BG_KEY="reader_bg";
	public final String FONT_SIZE_KEY="reader_size";
	public final String LIGHT_KEY="reader_light";
	public final String BOOKMARK_KEY="bookmark";
	public final String LASTUPDATETIME="reader_update";
	
	
	private void initSp(Context c){
		if(sp==null){
			sp = c.getSharedPreferences(DB_NAME, 0);
		}
	}
	
	public void removeBookMark(String nid,Context c){
		remove(nid+BOOKMARK_KEY, c);
	}
	
	public void setBookMark(String nid,int currentCount,int currentPage,Context c){
		setString(nid+BOOKMARK_KEY, currentCount+SPLIT_FLAG+currentPage, c);
	}
	
	public void setBookMark(String nid,BookMarkBean bookMark,Context c){
		setString(nid+BOOKMARK_KEY, bookMark.getCurrentCount()+SPLIT_FLAG+bookMark.getCurrentPage(), c);
	}
	
	public int getContextBg(Context c){
		return getInt(BG_KEY, c);
	}
	
	public void setContextBg(int bgindex,Context c){
		setInt(BG_KEY, bgindex, c);
	}
	
	public int getContextLight(Context c){
		return getInt(LIGHT_KEY, c);
	}
	
	public void setContextLight(int bgindex,Context c){
		setInt(LIGHT_KEY, bgindex, c);
	}
	
	public int getContextFontSize(Context c){
		String fontSize = getString(FONT_SIZE_KEY, c);
		if("".equals(fontSize))return 24;
		return Integer.parseInt(fontSize);
	}
	
	public void setContextFontSize(int fontSize,Context c){
		setString(FONT_SIZE_KEY, fontSize+"", c);
	}
	
	public boolean isBookExist(BookBean bookBean ,Context c){
		String nid = bookBean.getNid();
		String bookStr = getString(nid, c);
		if(!"".equals(bookStr)){
			return true;
		}
		return false;
	}
	
	public BookMarkBean getBookMarkByNid(String nid ,Context c){
		String bookMarkStr = getString(nid+BOOKMARK_KEY, c);
		if("".equals(bookMarkStr)){
			return null;
		}
		String[] bookmarkStrs = bookMarkStr.split(SPLIT_FLAG);
		int currentCount = Integer.parseInt(bookmarkStrs[0]);
		int currentPage = Integer.parseInt(bookmarkStrs[1]);
		return new BookMarkBean(currentCount, currentPage);
	}
	
	public void addBookBeanIfExist(BookBean bookBean,Context c){
		if(isBookExist(bookBean, c)){
			addBookBean(bookBean, c);
		}
	}
	
	public void removeBookBean(BookBean bookBean,Context c){
		if(isBookExist(bookBean, c)){
			String nid = bookBean.getNid();
			removeBookFromList(c, nid);
			remove(nid, c);
			BookMarkBean bookMark = getBookMarkByNid(nid, c);
			if(bookMark==null){
				removeBookMark(nid, c);
			}
		}
		
	}
	
	public void addBookBean(BookBean bookBean,Context c){
		String nid = bookBean.getNid();
		String bookStr = bookBean.parseToString(BOOK_SPLIT_FLAG);
		setString(nid, bookStr, c);
		BookMarkBean bookMark = getBookMarkByNid(nid, c);
		if(bookMark==null){
			setBookMark(nid, bookBean.getCurrentCount(), 0, c);
		}
		addBookToList(c, nid);
	}
	
	private void removeBookFromList(Context c,String nid){
		String nowListStr = getString(BOOK_LIST_KEY, c);
		String[] nids = nowListStr.split(SPLIT_FLAG);
		int length = nids.length;
		StringBuffer sb = new StringBuffer("");
		for(int i=0;i<length;i++){
			if(!nid.equals(nids[i])){
				sb.append(nids[i]).append(SPLIT_FLAG);
			}
		}
		setString(BOOK_LIST_KEY, sb.toString(), c);
	}
	
	private void addBookToList(Context c,String value){
		String nowListStr = getString(BOOK_LIST_KEY, c);
		if(nowListStr.indexOf(value)!=-1){
			String[] nids = nowListStr.split(SPLIT_FLAG);
			int length = nids.length;
			int index=0;
			for(int i=0;i<length;i++){
				if(value.equals(nids[i])){
					index = i;
				}
			}
			String temp = nids[index];
			for(int i=index;i>0;i--){
				nids[i]=nids[i-1];
			}
			nids[0]=temp;
			StringBuffer sb = new StringBuffer("");
			for(int i=0;i<length;i++){
				sb.append(nids[i]).append(SPLIT_FLAG);
			}
			setString(BOOK_LIST_KEY, sb.toString(), c);
		}else{
			if("".equals(nowListStr)){
				nowListStr+=value;
			}else{
				nowListStr=value+SPLIT_FLAG+nowListStr;
			}
			setString(BOOK_LIST_KEY, nowListStr, c);
		}
	}
	public List<BookBean> getBookBeanList(Context c){
		List<BookBean> bookBeanList = new ArrayList<BookBean>();
		String[] nids = getBookNidList(c);
		for(int i=0;i<nids.length;i++){
			String nid = nids[i];
			if("".equals(nid)){
				continue;
			}
			BookBean bookBean = getBookBeanByNid(nid, c);
			bookBeanList.add(bookBean);
		}
		return bookBeanList;
	}
	
	public String[] getBookNidList(Context c){
		String nidStr = getString(BOOK_LIST_KEY, c);
		return nidStr.split(SPLIT_FLAG);
	}
	
	public BookBean getBookBeanByNid(String nid,Context c){
		String bookStr = getString(nid, c);
		BookBean bookBean = null;
		if(!"".equals(bookStr)){
			bookBean = new BookBean(bookStr, BOOK_SPLIT_FLAG);
		}
		return bookBean;
	}
	
	public void addUpdateTime(long time,Context c){
		setLong(LASTUPDATETIME, time, c);
	}
	public long getLastUpdateTime(Context c){
		return getLong(LASTUPDATETIME, c);
	}
	
	private void remove(String key,Context c){
		initSp(c);
		sp.edit().remove(key).commit();;
	}
	
	private void setString(String key,String value,Context c){
		initSp(c);
		sp.edit().putString(key, value).commit();
	}
	public void setInt(String key,int value,Context c){
		initSp(c);
		sp.edit().putInt(key, value).commit();
	}
	public int getInt(String key,Context c){
		initSp(c);
		return sp.getInt(key,0);
	}
	public void setLong(String key,long value,Context c){
		initSp(c);
		sp.edit().putLong(key, value).commit();
	}
	public long getLong(String key,Context c){
		initSp(c);
		return sp.getLong(key,0);
	}
	private String getString(String key,Context c){
		initSp(c);
		return sp.getString(key, "");
	}
	
}
