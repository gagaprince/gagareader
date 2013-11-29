package com.prince.gagareader.bean;

public class BookMarkBean {
	private int currentCount;
	private int currentPage;
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public BookMarkBean(int currentCount, int currentPage) {
		super();
		this.currentCount = currentCount;
		this.currentPage = currentPage;
	}
	
}
