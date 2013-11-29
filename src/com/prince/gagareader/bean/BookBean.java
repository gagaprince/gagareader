package com.prince.gagareader.bean;

import java.util.List;

public class BookBean {
	private String nid;				//С˵nid
	private String novelName;		//С˵��
	private String fengmianUrl;		//С˵�ķ���ͼƬ url 
	private String author;			//����
	private String des;				//����
	private String lastChapter;		//�����½�
	private int state;				//���� ���״̬
	private int allChapterCount;	//���µ����¶��ٻ�
	private int currentCount;		//��ǰ�Ķ��� 
	private String sourceSite;		//��Դ
	private List<String> indexList;	//Ŀ¼����
	private boolean isOnline;		//�Ƿ��������Ķ��������Ķ� �� �����Ķ��������Ķ��ǽ���ǰ�鼮���ı��������ֻ��У������Ķ�����ÿ�δ������ȡ�鼮���ݣ�����������
	
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
		novelName="�䶯Ǭ��";
		fengmianUrl="http://cover.yicha.cn/img/fengmian/20130201/40/93/bd1fdacd1d37a04b8b5b4d5f1d62.jpg";
		author="�������";
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
