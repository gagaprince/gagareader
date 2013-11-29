package com.prince.gagareader.bean;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

public class ChapterBean {
	private String chapterContent;
	private List<PrepageBean> pages;
	public String getChapterContent() {
		return chapterContent;
	}
	public void setChapterContent(String chapterContent) {
		this.chapterContent = chapterContent;
	}
	public List<PrepageBean> getPages() {
		return pages;
	}
	public void setPages(List<PrepageBean> pages) {
		this.pages = pages;
	}
	public ChapterBean(String content,int frameWidth,int frameHeight,int textSize,int fontHeight,Paint p){
		this.chapterContent = content;
		pages = new ArrayList<PrepageBean>();
		parseContent(frameWidth, frameHeight, textSize, p);
	}
	
	public void parseContent(int frameWidth,int frameHeight,int textSize,Paint p){
		int startNum = 0;
		while(true){
			PrepageBean page = getOnePage(frameWidth, frameHeight, textSize, p, startNum);
			if(page==null)break;
			startNum = page.getAll().getEnd();
			pages.add(page);
		}
	}
	
	/**
     * 获取字符串宽
     * @param str
     * @return
     */
    private int getStringWidth(String str,Paint p) {
        return (int) p.measureText(str); 
    }
    /*
     * 获取字体高度
     */
    private int getFontHeight(Paint p) {
        FontMetrics fm = p.getFontMetrics();
        return (int)Math.ceil(fm.descent - fm.top) + 5;
    }
    
    /**
	 * 返回一行文字的字数
	 * @param canvas
	 * @param begin
	 * @return
	 */
	private StringRulerBean getStringRulerBeanByArgs(int frameWidth,int textSize,Paint p,int begin){
		int length = chapterContent.length();
		if(begin<length-1){
			int num = frameWidth/textSize+1;
			if(num>length-begin){
				num=length-begin;
			}else{
				String strTemp = chapterContent.substring(begin,begin+num);
				if(strTemp.charAt(1)==' '){
					num+=15;
				}
				if(num>length-begin){
					num=length-begin;
				}
			}
			String strTemp = chapterContent.substring(begin,begin+num);
			int numTemp = strTemp.indexOf("\n");
			if(numTemp!=-1){
				if(numTemp==0){
					begin++;
					num--;
				}else{
					num = numTemp;
				}
			}
			while(num>0){
				strTemp = chapterContent.substring(begin,begin+num);
				int lineWidth = getStringWidth(strTemp,p);
				if(lineWidth<=frameWidth){
					break;
				}
				num--;
			}
			return new StringRulerBean(begin, begin+num);
		}
		return null;
		
	}
	/**
	 *  返回一页数据bean
	 */
	private PrepageBean getOnePage(int frameWidth,int frameHeigth,int textSize,Paint p,int begin){
		int length = chapterContent.length();
		if(begin>=length-1)return null;
		PrepageBean returnPage = new PrepageBean();
		List<StringRulerBean> lines = returnPage.getLines();
		int lineHeight = getFontHeight(p);
		int lineNum = frameHeigth/lineHeight;
		int startTemp = begin;
		for(int i=0;i<lineNum;i++){
			StringRulerBean oneLine = getStringRulerBeanByArgs(frameWidth, textSize, p, startTemp);
			if(oneLine==null)break;
			int wordNum = oneLine.getEnd();
			startTemp=wordNum;
			lines.add(oneLine);
		}
		returnPage.setAll(new StringRulerBean(begin, startTemp));
		return returnPage;
	}
}
