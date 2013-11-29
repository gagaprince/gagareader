package com.prince.gagareader.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.prince.gagareader.GagaReaderActivity;
import com.prince.gagareader.R;
import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.DownLoadNotificationBean;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.NovelUtil;

public class DownLoadService extends Service{
	private int notificationId=0;
	private Map<String,Integer> nid_id;
	private Handler handler;
	private NotificationManager manager;
	@Override
	public void onCreate() {
		nid_id=new HashMap<String, Integer>();
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		handler = new Handler(){
			@Override  
            public void handleMessage(Message msg) {
				 super.handleMessage(msg);  
	                switch (msg.what) {  
	                case 1:
	                	DownLoadNotificationBean downloadBean = (DownLoadNotificationBean)msg.obj;
	                	Notification notification = downloadBean.getNotification();
	                	int proccess = downloadBean.getProccess();
	                	notification.contentView.setTextViewText(R.id.novelName, downloadBean.getName());
	                	if(proccess==100){
	                		notification.contentView.setTextViewText(R.id.content_view_text1, "下载完毕");
	                	}else{
	                		notification.contentView.setTextViewText(R.id.content_view_text1, proccess+"%");
	                	}
	                	notification.contentView.setProgressBar(R.id.content_view_progress, 100, proccess, false); 
	                	manager.notify(downloadBean.getId(),notification);
	                	break;
	                case 2:
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onStart(Intent intent, int startId) {
		Log.e("onstart", "start");
		String nid = intent.getStringExtra("nid");
		String novelName = intent.getStringExtra("novelName");
		int begin = intent.getIntExtra("begin", 1);
		Integer notificationId = nid_id.get(nid);
		if(notificationId!=null){
			
		}else{
			notificationId = this.notificationId++;
			nid_id.put(nid, notificationId);
			Notification notification = initNotification(notificationId, novelName);
			DownLoadNotificationBean downloadBean = new DownLoadNotificationBean(notificationId, novelName, notification);
			manager.notify(downloadBean.getId(),notification);
			new DownLoadThread(nid, begin, downloadBean).start();
		}
	}
	
	private Notification initNotification(int notificationId,String novelName){
		Notification notification =new Notification(notificationId,novelName, System.currentTimeMillis());
		notification.icon = R.drawable.icon;
		//notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中  
		notification.contentView = new RemoteViews(getPackageName(), R.layout.nofitication); 
		notification.contentView.setTextViewText(R.id.novelName, novelName);
		notification.contentView.setTextViewText(R.id.content_view_text1, "0%");
		notification.contentView.setProgressBar(R.id.content_view_progress, 100, 0, false); 
		Intent notificationIntent =new Intent(DownLoadService.this, GagaReaderActivity.class); // 点击该通知后要跳转的Activity  
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);  
		notification.contentIntent = contentItent;
		return notification;
	}
	
	class DownLoadThread extends Thread{
		private String nid;
		private int begin;
		private BookBean bookBean;
		private DownLoadNotificationBean downloadBean;
		public DownLoadThread(String nid,int begin,DownLoadNotificationBean downloadBean){
			this.nid = nid;
			this.begin = begin;
			this.downloadBean = downloadBean;
		}
		public void run(){
			try {
				bookBean = DateProvider.getInstance().getBookBeanByNid(nid);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			int allChapter = bookBean.getAllChapterCount();
			if(begin>=allChapter)return;
			for(int i=begin;i<=allChapter;i++){
				try {
					boolean flag = NovelUtil.getInstance().downloadNovelByNidCid(nid, i+"");
					int proccess = i*100/allChapter;
					if(flag&&proccess!=downloadBean.getProccess()){
						downloadBean.setProccess(proccess);
						sendMessage(1, downloadBean);
						continue;
					}
					if(proccess==100){
						downloadBean.setProccess(proccess);
						sendMessage(1, downloadBean);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void sendMessage(int msgwhat,DownLoadNotificationBean downloadBean){
		Message message = new Message();  
        message.what = msgwhat;
        message.obj = downloadBean;
        handler.sendMessage(message);
	}
}
