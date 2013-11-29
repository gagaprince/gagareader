package com.prince.gagareader.bean;

public class HotwordBean {
	private String leftWord;
	private String rigthWord;
	public String getLeftWord() {
		return leftWord;
	}
	public void setLeftWord(String leftWord) {
		this.leftWord = leftWord;
	}
	public String getRigthWord() {
		return rigthWord;
	}
	public void setRigthWord(String rigthWord) {
		this.rigthWord = rigthWord;
	}
	public HotwordBean(String leftWord, String rigthWord) {
		super();
		this.leftWord = leftWord;
		this.rigthWord = rigthWord;
	}
	
}
