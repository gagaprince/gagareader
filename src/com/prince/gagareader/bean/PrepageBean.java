package com.prince.gagareader.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * ÿҳ���ݵķ�װ
 * @author gagaprince
 *
 */
public class PrepageBean {
	/**
	 * һҳ���ֵĿ�ͷ��β
	 */
	private StringRulerBean all;
	/**
	 * һҳ���� ÿ�еĿ�ͷ��β
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
