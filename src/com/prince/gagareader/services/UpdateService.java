package com.prince.gagareader.services;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import com.prince.gagareader.GagaReaderActivity;
import com.prince.gagareader.R;
import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.Const;
import com.prince.gagareader.util.FileUtil;
import com.prince.gagareader.util.SharedPreferencesUtil;

public class UpdateService extends Service{
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
	                	MsgBean msgBean = (MsgBean)msg.obj;
	                	int updateChapterNum = msgBean.updateChapterNum;
	                	BookBean bookBean = msgBean.bookBean;
	                	showNotification(updateChapterNum,bookBean);
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
		Date now = new Date();
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		long lastUpdate = su.getLastUpdateTime(this);
		long nowTime = now.getTime();
		long timeDistance = nowTime-lastUpdate;
		if(timeDistance>3600000&&!"none".equals(getNetWorkCate())){
			checkBookUpdate();
			su.addUpdateTime(nowTime, this);
		}
	}
	
	private void showNotification(int updateChapterNum,BookBean bookBean){
		String nid = bookBean.getNid();
		Integer notificationId = nid_id.get(nid);
		if(notificationId!=null){
			
		}else{
			notificationId = this.notificationId++;
			nid_id.put(nid, notificationId);
		}
		String novelName = bookBean.getNovelName();
		Notification notification = initNotification(notificationId, novelName,updateChapterNum);
		manager.notify(notificationId,notification);
	}
	
	private void checkBookUpdate(){
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		List<BookBean> books = su.getBookBeanList(this);
		int size = books.size();
		StringBuffer nidsb = new StringBuffer("");
		for(int i=0;i<size;i++){
			BookBean book = books.get(i);
			nidsb.append(book.getNid()).append(",");
		}
		getBooksInfo(nidsb.toString());
	}
	
	private void getBooksInfo(final String nids){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = Const.NOVEL_INFO_URL+nids;
				FileUtil fu = FileUtil.getInstance();
				try {
					String result = fu.getUrlContent(url);
					JSONObject updateJson = new JSONObject(result);
					JSONArray updateBooksJson = updateJson.getJSONArray("result");
					int size = updateBooksJson.length();
					for(int i=0;i<size;i++){
						JSONObject updateBookJson = updateBooksJson.getJSONObject(i);
						int chapterCount = updateBookJson.getInt("chapterCount");
						String nid = updateBookJson.getString("nid");
						SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
						BookBean bookBean = su.getBookBeanByNid(nid, UpdateService.this);
						if(bookBean!=null){
							int chapterCountInShelf = bookBean.getAllChapterCount();
							int updateChapterCount = chapterCount-chapterCountInShelf;
							if(updateChapterCount>0){
								String lastChapter = updateBookJson.getString("lastChapter");
								bookBean.setAllChapterCount(chapterCount);
								bookBean.setLastChapter(lastChapter);
								bookBean.setOnline(true);
								su.addBookBean(bookBean, UpdateService.this);
								MsgBean msgBean= new MsgBean(updateChapterCount,bookBean);
								sendMessage(1, msgBean);//有书更新
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private Notification initNotification(int notificationId,String novelName,int updateChapterCount){
		Notification notification =new Notification(notificationId,novelName+"  更新了"+updateChapterCount+"章", System.currentTimeMillis());
		notification.icon = R.drawable.icon;
		notification.contentView = new RemoteViews(getPackageName(), R.layout.updatenofitication); 
		notification.contentView.setTextViewText(R.id.novelName, novelName+"  更新了"+updateChapterCount+"章");
		Intent notificationIntent =new Intent(UpdateService.this, GagaReaderActivity.class); // 点击该通知后要跳转的Activity  
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);  
		notification.contentIntent = contentItent;
		return notification;
	}
	
	private void sendMessage(int msgwhat, MsgBean msgBean){
		Message message = new Message();  
        message.what = msgwhat;
        message.obj = msgBean;
        handler.sendMessage(message);
	}
	
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
	class MsgBean{
		public int updateChapterNum;
		public BookBean bookBean;
		public MsgBean(int updateChapterNum,BookBean bookBean){
			this.updateChapterNum = updateChapterNum;
			this.bookBean = bookBean;
		}
	}
}
