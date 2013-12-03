package com.prince.gagareader;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class BaseActivity extends Activity{
	protected Handler handler;
	
	public final static int NETWORK_ERROR=-1;
	public final static int SDCARD_ERROR=-2;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentView();
        initHandler();
        initData();
        initView();
        addListenner();
    }
	/**
	 * 初始化handler
	 */
	protected void initHandler(){
		handler = new Handler(){
			@Override  
            public void handleMessage(Message msg) {
				 super.handleMessage(msg);  
                 switch (msg.what) {  
                 case NETWORK_ERROR:
                	 break;
                 case SDCARD_ERROR:
                	 break;
                 }
                 childHandleMessage(msg);
			}
		};
	}
	/**
	 * 设置布局xml
	 */
	protected abstract void setCurrentView();
	/**
	 * 分发 消息请求
	 * @param msg
	 */
	protected abstract void childHandleMessage(Message msg);
	/**
	 * 初始化数据
	 */
	protected abstract void initData();
	/**
	 * 初始化view
	 */
	protected abstract void initView();
	/**
	 * 初始化时间侦听
	 */
	protected abstract void addListenner();
	/**
	 * 发送消息
	 * @param msgWhat
	 */
	protected void sendMsgBean(int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        handler.sendMessage(message);
	}
	/**
	 * 发送消息
	 * @param msgWhat
	 * @param object 发送obj
	 */
	protected void sendMsgBean(int msgWhat,Object object){
		Message message = new Message();  
        message.what = msgWhat;
        message.obj=object;
        handler.sendMessage(message);
	}
	/**
	 * 获取网络情况
	 * @return
	 */
	protected String getNetWorkCate(){
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
            return "3g";
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
            return "wifi";
        return "none";
	}
}
