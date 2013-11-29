package com.prince.gagareader.bean;

public class StringRulerBean {
	private int start;
	private int end;
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public StringRulerBean(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}
	
}
