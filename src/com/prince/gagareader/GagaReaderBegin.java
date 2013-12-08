package com.prince.gagareader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prince.gagareader.bean.Const;
import com.prince.gagareader.util.FileUtil;
import com.umeng.analytics.MobclickAgent;

public class GagaReaderBegin extends Activity{
	private Handler handler ;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin);
        initHandler();
        boolean flag = initAppArgs();
        if(flag)
        	initAD();
    }
	/**
	 * ��ʼ����Ϣ�������
	 */
	private void initHandler(){
		handler = new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                switch (msg.what) {  
                case 1:  //��������
                	this.postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(GagaReaderBegin.this,
									GagaReaderActivity.class);
		                	startActivity(intent);
		                	GagaReaderBegin.this.finish();
						}
					}, 2000);
                    break;  
                default:  
                    break;  
                }  
            }  
        };
	}
	/**
	 * ��ʼ��app����
	 */
	private boolean initAppArgs(){
		loadBoardFile();	//���°��ļ�
		return mkAppDirs();		//���������ļ���
	}
	private boolean mkAppDirs(){
		FileUtil fileUtil = FileUtil.getInstance();
		Log.e("mkAppDirs", ""+fileUtil.isHaveSize());
		if(fileUtil.isHaveSize()){//��ʣ��ռ� �ͽ����ļ���
			String rootPath = Environment.getExternalStorageDirectory().getPath();
			String appRootPath = rootPath+"/gagaReader";
			Const.APP_PHOTO_CACHE= appRootPath+"/photocache";
			Const.APP_TEXT_CACHE= appRootPath+"/textcache";
			
			fileUtil.mkdir(appRootPath);
			fileUtil.mkdir(Const.APP_PHOTO_CACHE);
			fileUtil.mkdir(Const.APP_TEXT_CACHE);
		}else{						//û��ʣ��ռ�  ����״̬Ϊ ֱ�Ӵ����粥�� ������
			// ��ʾdialog  ��ʾû��sd�� ����ʹ��
			return false;
		}
		return true;
	}
	private void loadBoardFile(){
		
	}
	/**
	 * ��ʼ��������� �������� ������ҳ
	 */
	private void initAD(){
		String netWorkCate = getNetWorkCate();
		if("wifi".equals(netWorkCate)){//�����֮��
			sendMessage(1);
		}else{
			sendMessage(1);
		}
	}
	public void sendMessage(int what){
		Message message = new Message();  
        message.what = what; 
        handler.sendMessage(message);
	}
	/*private boolean isOpenNetwork() {  
	    ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);  
	    if(connManager.getActiveNetworkInfo() != null) {  
	        return connManager.getActiveNetworkInfo().isAvailable();  
	    }  
	    return false;  
	}*/
	private String getNetWorkCate(){
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
            return "3g";
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
            return "wifi";
        return "none";
	}
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
