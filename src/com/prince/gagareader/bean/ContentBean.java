package com.prince.gagareader.bean;

public class ContentBean {
	private String content;
	private String title;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ContentBean(String content, String title) {
		super();
		this.content = content;
		this.title = title;
	}
	
}
