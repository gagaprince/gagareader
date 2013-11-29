package com.prince.gagareader.bean;

import java.util.List;

public class ShowItemBean {
	public static final int TITLE_TAG=0;
	public static final int SINGEL_BOOK_TAG=1;
	public static final int LIST_BOOK_TAG=2;
	public static final int CATE_TAG=3;
	private int type;//showItem����
	private BookBean bookBean;	//������listview�õ�
	private String title;		//title listView �õ�
	private List<BookBean> bookBeans;	//������ listview �õ�
	private List<CateBean> cateTitles;		//����չʾ �õ�
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public BookBean getBookBean() {
		return bookBean;
	}
	public void setBookBean(BookBean bookBean) {
		this.bookBean = bookBean;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<BookBean> getBookBeans() {
		return bookBeans;
	}
	public void setBookBeans(List<BookBean> bookBeans) {
		this.bookBeans = bookBeans;
	}
	public List<CateBean> getCateTitles() {
		return cateTitles;
	}
	public void setCateTitles(List<CateBean> cateTitles) {
		this.cateTitles = cateTitles;
	}
	
	public ShowItemBean(String title){
		type = TITLE_TAG;
		this.title=title;
	}
	public ShowItemBean(BookBean bookBean){
		type=SINGEL_BOOK_TAG;
		this.bookBean = bookBean;
	}
	public ShowItemBean(List<BookBean> bookBeans){
		type=LIST_BOOK_TAG;
		this.bookBeans = bookBeans;
	}
	public ShowItemBean(List<CateBean> cates,int tag){
		type=CATE_TAG;
		this.cateTitles = cates;
	}
	
}
