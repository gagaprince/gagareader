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
	 * ��ʼ��handler
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
	 * ���ò���xml
	 */
	protected abstract void setCurrentView();
	/**
	 * �ַ� ��Ϣ����
	 * @param msg
	 */
	protected abstract void childHandleMessage(Message msg);
	/**
	 * ��ʼ������
	 */
	protected abstract void initData();
	/**
	 * ��ʼ��view
	 */
	protected abstract void initView();
	/**
	 * ��ʼ��ʱ������
	 */
	protected abstract void addListenner();
	/**
	 * ������Ϣ
	 * @param msgWhat
	 */
	protected void sendMsgBean(int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        handler.sendMessage(message);
	}
	/**
	 * ������Ϣ
	 * @param msgWhat
	 * @param object ����obj
	 */
	protected void sendMsgBean(int msgWhat,Object object){
		Message message = new Message();  
        message.what = msgWhat;
        message.obj=object;
        handler.sendMessage(message);
	}
	/**
	 * ��ȡ�������
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
