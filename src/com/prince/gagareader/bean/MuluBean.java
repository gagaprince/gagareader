package com.prince.gagareader.bean;

public class MuluBean {
	private String nid;
	private int cid;
	private String title;
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public MuluBean(String nid, int cid, String title) {
		super();
		this.nid = nid;
		this.cid = cid;
		this.title = title;
	}
	
}
