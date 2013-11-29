package com.prince.gagareader.bean;

import java.util.List;

public class BookBean {
	private String nid;				//小说nid
	private String novelName;		//小说名
	private String fengmianUrl;		//小说的封面图片 url 
	private String author;			//作者
	private String des;				//描述
	private String lastChapter;		//最新章节
	private int state;				//连载 完结状态
	private int allChapterCount;	//更新到最新多少话
	private int currentCount;		//当前阅读到 
	private String sourceSite;		//来源
	private List<String> indexList;	//目录中文
	private boolean isOnline;		//是否是在线阅读，在线阅读 和 离线阅读，离线阅读是将当前书籍的文本保存在手机中，在线阅读则是每次从网络获取书籍内容，保存至本地
	
	public String getFengmianUrl() {
		return fengmianUrl;
	}
	public void setFengmianUrl(String fengmianUrl) {
		this.fengmianUrl = fengmianUrl;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public String getNovelName() {
		return novelName;
	}
	public void setNovelName(String novelName) {
		this.novelName = novelName;
	}
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getAllChapterCount() {
		return allChapterCount;
	}
	public void setAllChapterCount(int allChapterCount) {
		this.allChapterCount = allChapterCount;
	}
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public List<String> getIndexList() {
		return indexList;
	}
	public void setIndexList(List<String> indexList) {
		this.indexList = indexList;
	}
	public BookBean(){
		nid="14595f0eef119432facf9c8ea24d594d";
		novelName="武动乾坤";
		fengmianUrl="http://cover.yicha.cn/img/fengmian/20130201/40/93/bd1fdacd1d37a04b8b5b4d5f1d62.jpg";
		author="天蚕土豆";
	}
	public BookBean(String nid){
		this.nid=nid;
	}
	public BookBean(String nid, String novelName, String fengmianUrl,
			String author,String des) {
		super();
		this.nid = nid;
		this.novelName = novelName;
		this.fengmianUrl = fengmianUrl;
		this.author = author;
		this.des=des;
	}
	
	public BookBean(String nid, String novelName, String fengmianUrl,
			String author, String des, String lastChapter, int state,
			int allChapterCount) {
		super();
		this.nid = nid;
		this.novelName = novelName;
		this.fengmianUrl = fengmianUrl;
		this.author = author;
		this.des = des;
		this.lastChapter = lastChapter;
		this.state = state;
		this.allChapterCount = allChapterCount;
		this.currentCount=1;
		this.isOnline=false;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public String getLastChapter() {
		return lastChapter;
	}
	public void setLastChapter(String lastChapter) {
		this.lastChapter = lastChapter;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	public String getSourceSite() {
		return sourceSite;
	}
	public void setSourceSite(String sourceSite) {
		this.sourceSite = sourceSite;
	}
	public String parseToString(String split){
		String beanStr="";
		beanStr+=nid+split+author+split+des+split+fengmianUrl+split+allChapterCount+split
				+currentCount+split+lastChapter+split+state+split+novelName+split+isOnline;
		return beanStr;
	}
	public BookBean(String beanStr,String split){
		String[] beanStrs = beanStr.split(split);
		nid = beanStrs[0];
		author=beanStrs[1];
		des = beanStrs[2];
		fengmianUrl = beanStrs[3];
		allChapterCount = Integer.parseInt(beanStrs[4]);
		currentCount = Integer.parseInt(beanStrs[5]);
		lastChapter = beanStrs[6];
		state = Integer.parseInt(beanStrs[7]);
		novelName = beanStrs[8];
		isOnline = beanStrs[9].equals("true");
	}
	public BookBean(String nid,String name,String image,String author){
		this.fengmianUrl = image;
		this.author =author;
		this.novelName = name;
		this.nid = nid;
	}
	public BookBean(String nid,String name,String image,String author,String des,String lastChapter,String sourceSite,int state){
		this.fengmianUrl = image;
		this.author =author;
		this.novelName = name;
		this.nid = nid;
		this.des =des;
		this.lastChapter = lastChapter;
		this.sourceSite = sourceSite;
		this.state = state;
	}
	public BookBean(String imgurl,String name,String author,String des,int type){
		this.fengmianUrl = imgurl;
		this.author =author;
		this.novelName = name;
		this.des = des;
	}
	
}
