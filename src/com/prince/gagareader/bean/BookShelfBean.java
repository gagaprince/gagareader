package com.prince.gagareader.bean;

import java.util.ArrayList;
import java.util.List;

public class BookShelfBean {
	private List<BookBean> bookBeans;

	public List<BookBean> getBookBeans() {
		return bookBeans;
	}

	public void setBookBeans(List<BookBean> bookBeans) {
		this.bookBeans = bookBeans;
	}
	public BookShelfBean(){
	}
	public BookShelfBean(List<BookBean> bookBeans){
		this.bookBeans = bookBeans;
	}
	public BookShelfBean(int size){
		bookBeans = new ArrayList<BookBean>();
		for(int i=0;i<size;i++){
			bookBeans.add(new BookBean());
		}
		bookBeans.add(new BookBean("jia"));
	}
}
