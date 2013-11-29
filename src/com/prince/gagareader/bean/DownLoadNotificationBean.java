package com.prince.gagareader.bean;

import android.app.Notification;

public class DownLoadNotificationBean {
	private int id;
	private String name;
	private int proccess;
	private Notification notification;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getProccess() {
		return proccess;
	}
	public void setProccess(int proccess) {
		this.proccess = proccess;
	}
	public Notification getNotification() {
		return notification;
	}
	public void setNotification(Notification notification) {
		this.notification = notification;
	}
	public DownLoadNotificationBean(int id, String name,Notification notification) {
		super();
		this.id = id;
		this.name = name;
		this.proccess = 0;
		this.notification = notification;
	}
	
}
