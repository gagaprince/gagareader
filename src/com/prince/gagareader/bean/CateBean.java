package com.prince.gagareader.bean;

public class CateBean {
	private String cateName;
	private String cate;
	private int cateType;
	private String img;
	
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getCateName() {
		return cateName;
	}
	public void setCateName(String cateName) {
		this.cateName = cateName;
	}
	public String getCate() {
		return cate;
	}
	public void setCate(String cate) {
		this.cate = cate;
	}
	public CateBean(String cateName, String cate) {
		super();
		this.cateName = cateName;
		this.cate = cate;
		cateType = 1;
	}
	public CateBean(String cateName) {
		super();
		this.cateName = cateName;
		this.cate = cateName;
		cateType=0;
	}
	public CateBean(String cateName,String cate,String img) {
		super();
		this.cateName = cateName;
		this.cate = cateName;
		this.img=img;
		cateType=0;
	}
	public int getCateType() {
		return cateType;
	}
	public void setCateType(int cateType) {
		this.cateType = cateType;
	}
	
}
