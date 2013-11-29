package com.prince.gagareader.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 每页数据的封装
 * @author gagaprince
 *
 */
public class PrepageBean {
	/**
	 * 一页文字的开头结尾
	 */
	private StringRulerBean all;
	/**
	 * 一页文字 每行的开头结尾
	 */
	private List<StringRulerBean> lines;
	public StringRulerBean getAll() {
		return all;
	}
	public void setAll(StringRulerBean all) {
		this.all = all;
	}
	public List<StringRulerBean> getLines() {
		return lines;
	}
	public void setLines(List<StringRulerBean> lines) {
		this.lines = lines;
	}
	public PrepageBean(){
		lines = new ArrayList<StringRulerBean>();
	}
}
