package com.prince.gagareader.bean;

import android.app.Activity;

public class TabHostMsgBean {
	private String tag;
	private Class<Activity> className;
	private String param;
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Class<Activity> getClassName() {
		return className;
	}
	public void setClassName(Class<Activity> className) {
		this.className = className;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	
}
