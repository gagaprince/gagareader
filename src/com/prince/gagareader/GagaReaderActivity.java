package com.prince.gagareader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.BookShelfBean;
import com.prince.gagareader.bean.Const;
import com.prince.gagareader.bean.TabHostMsgBean;
import com.prince.gagareader.services.DownLoadService;
import com.prince.gagareader.util.FileUtil;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;
import com.prince.gagareader.util.NovelUtil;
import com.prince.gagareader.util.SharedPreferencesUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class GagaReaderActivity extends ActivityGroup {
	private ListView sj_listView;
	private List<BookShelfBean> bookshelfBeanList;
	private Handler handler;
	private Button menuButton;
	private Button tjButton;
	private LinearLayout menuLayout;
    private LinearLayout appLayout;
    private ListView menuLv;
    
    private List<String> menuNameList;
    private List<TabHostMsgBean> tabHostMsgBeanList;
    
    private BookShelfListViewAdapter bookShelfLvAdapter;
    private int[] muneImgs = {R.drawable.search,R.drawable.xihuan,R.drawable.jinrituijian,R.drawable.boy,R.drawable.girl,R.drawable.paihang,R.drawable.chuantong};
	
	private boolean sliderOut=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initHandler();
        initData();
        initView();
        UmengUpdateAgent.update(this);
    }
    private void initData(){
    	try {
    		initConst();
    		initBookshelf();
			initTabHostList();
			initMenuNameList();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    private void initConst(){
    	if(Const.APP_PHOTO_CACHE==null){
    		mkAppDirs();
    	}
    }
    private void initBookshelf(){
    	bookshelfBeanList = new ArrayList<BookShelfBean>();
    	SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
    	List<BookBean> shelfBooks = su.getBookBeanList(this);
    	shelfBooks.add(new BookBean("jia"));
    	for(int i=0;i<shelfBooks.size();){
    		List<BookBean> addBooks = new ArrayList<BookBean>();
    		int j;
    		for(j=i;j<shelfBooks.size()&&j<i+3;j++){
    			addBooks.add(shelfBooks.get(j));
    		}
    		if(addBooks.size()>0){
    			BookShelfBean bookshlefBean = new BookShelfBean(addBooks);
        		bookshelfBeanList.add(bookshlefBean);
    		}
    		i=j;
    	}
    	for(int i=bookshelfBeanList.size();i<4;i++){
    		bookshelfBeanList.add(new BookShelfBean());
    	}
    }
    private void initHandler(){
    	handler = new Handler(){
			@Override  
            public void handleMessage(Message msg) {
				 super.handleMessage(msg);  
	                switch (msg.what) {  
	                case 1:  //public
	                	MsgBean msgBean = (MsgBean)msg.obj;
	                	String tag = msgBean.getTag();
	                	Drawable bitmap = msgBean.getBitmap();
	                	ImageButton btn = (ImageButton)sj_listView.findViewWithTag(tag);
						if(btn!=null){
							btn.setBackgroundDrawable(bitmap);
						}
	                    break;  
	                case 2://数据加载完成 显示
	                	
	                	break;
	                case 3://移除
	                	String nid = (String)msg.obj;
	                	removeBook(nid);
	                	break;
	                case 4://移除完毕
	                	initBookshelf();
	                	bookShelfLvAdapter.notifyDataSetChanged();
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
    }
    private void initView(){
    	bookShelfLvAdapter= new BookShelfListViewAdapter();
    	sj_listView = (ListView) findViewById(R.id.sj_listview);
    	sj_listView.setAdapter(bookShelfLvAdapter);
    	
    	menuButton = (Button)findViewById(R.id.btn_leftTop);
    	menuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeMenuState();
			}
		});
    	
    	tjButton = (Button)findViewById(R.id.btn_rightTop);
    	tjButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GagaReaderActivity.this, IndexActivity.class);
				intent.putExtra("param","");
				startActivity(intent);
			}
		});
    	
    	menuLayout = (LinearLayout)findViewById(R.id.menuLayout);
    	appLayout = (LinearLayout)findViewById(R.id.appshowLayout);
    	
    	menuLv = (ListView)findViewById(R.id.menuLv);
    	menuLv.setAdapter(new MenuListViewAdapter(menuNameList));
    	menuLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TabHostMsgBean tabMsgBean = tabHostMsgBeanList.get(arg2);
				final Class<Activity> className = tabMsgBean.getClassName();
				final String param = tabMsgBean.getParam();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(GagaReaderActivity.this, className);
						intent.putExtra("param",param);
						startActivity(intent);
					}
				},400);
				changeMenuState();
			}
		});
    }
    
    private void removeBook(final String nid){
    	final SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
    	final BookBean bookBean = su.getBookBeanByNid(nid, this);
    	if(bookBean!=null){
    		new Thread(new Runnable() {
				@Override
				public void run() {
					NovelUtil nu = NovelUtil.getInstance();
		    		nu.removeNovelByNid(nid);
		    		su.removeBookBean(bookBean, GagaReaderActivity.this);
		    		sendMsgBean(null, 4);
				}
			}).start();
    	}
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
    	if(sliderOut){
    		int menuLayoutW = menuLayout.getMeasuredWidth(); 
    		if(me.getX()>menuLayoutW){
    			changeMenuState();
        		return true;
    		}
    	}
    	return super.dispatchTouchEvent(me);
    	
    }
    
    private void changeMenuState(){
    	final int menuLayoutW = menuLayout.getMeasuredWidth(); 
		final int appLayoutW = appLayout.getMeasuredWidth(); 
	    final int appLayoutH = appLayout.getMeasuredHeight();
	    Animation animation = null; 
	    if (sliderOut) { 
	        // hide 
	        animation = new TranslateAnimation(0, -menuLayoutW, 0, 0); 
	    } else { 
	        // show 
	        animation = new TranslateAnimation(0,menuLayoutW, 0, 0); 
	    } 
	    animation.setFillAfter(true); 
	    animation.setDuration(300); 
	    appLayout.startAnimation(animation); 
	    animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				if(!sliderOut){
					appLayout.layout((int) menuLayoutW, 0, (int) (menuLayoutW+ appLayoutW), appLayoutH);
				}else{
					appLayout.layout(0, 0, appLayoutW, appLayoutH); 
				}
				appLayout.clearAnimation();
				sliderOut=!sliderOut;
			}
		});
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN){
			if(sliderOut){
				showIsBgPlayDialog();
			}else{
				changeMenuState();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
    
    /**
	 * 显示退出提示
	 */
	private void showIsBgPlayDialog(){
		new AlertDialog.Builder(GagaReaderActivity.this)
		.setTitle("确定退出嘎嘎读书？")
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						GagaReaderActivity.this.finish();
					}
				})
		.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}
				}).create().show();
	}
    
    class BookShelfListViewAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return bookshelfBeanList.size();
		}
		@Override
		public Object getItem(int arg0) {
			return bookshelfBeanList.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(int arg0, View conventView, ViewGroup parent) {
			Log.e("getView", arg0+"");
			conventView=LayoutInflater.from(GagaReaderActivity.this).inflate(R.layout.bookshelf_item, null);
			LinearLayout layout1 = (LinearLayout)conventView.findViewById(R.id.book_item1);
			LinearLayout layout2 = (LinearLayout)conventView.findViewById(R.id.book_item2);
			LinearLayout layout3 = (LinearLayout)conventView.findViewById(R.id.book_item3);
			List<LinearLayout> layouts = new ArrayList<LinearLayout>();
			layouts.add(layout1);
			layouts.add(layout2);
			layouts.add(layout3);
			BookShelfBean bookShelfBean = bookshelfBeanList.get(arg0);
			List<BookBean> bookBeans = bookShelfBean.getBookBeans();
			if(bookBeans!=null){
				int size = bookBeans.size();
				for(int i=0;i<size&&i<3;i++){
					BookBean bookBean = bookBeans.get(i);
					if(bookBean!=null){
						LinearLayout layoutTemp = layouts.get(i);
						layoutTemp.setVisibility(View.VISIBLE);
						if(bookBean.getNid().equals("jia")){
							ImageButton imgBtn = (ImageButton)layoutTemp.findViewById(R.id.bookImage);
							imgBtn.setBackgroundResource(R.drawable.jia);
							imgBtn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(GagaReaderActivity.this,
											IndexActivity.class);
				                	startActivity(intent);
								}
							});
						}else{
							ImageButton imgBtn = (ImageButton)layoutTemp.findViewById(R.id.bookImage);
							ImageUtil iu = ImageUtil.getInstance();
							final String imgurl = bookBean.getFengmianUrl();
							final String nid = bookBean.getNid();
							final String novelName = bookBean.getNovelName();
							imgBtn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(GagaReaderActivity.this,
											ReaderActivity.class);
									intent.putExtra("nid", nid);
				                	startActivity(intent);
									/*Intent intent = new Intent(GagaReaderActivity.this, DownLoadService.class);
									intent.putExtra("nid", nid);
									intent.putExtra("novelName", novelName);
									startService(intent);*/
								}
							});
							imgBtn.setOnLongClickListener(new OnLongClickListener() {
								@Override
								public boolean onLongClick(View v) {
									new AlertDialog.Builder(GagaReaderActivity.this).setTitle("您要的操作").setSingleChoiceItems(
									     new String[] { "下载更新", "移除书架" }, 0,
									     	new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													switch (which) {
													case 0:
														downloadUpdate(nid, novelName);
														break;
													case 1:
														removeBookDialog(nid);
														break;
													default:
														break;
													}
													dialog.dismiss();
												}
											})
									     .setNegativeButton("取消", null).show();
									return true;
								}
							});
							imgBtn.setTag(imgurl);
							iu.preparedImage(imgurl,new OnPreparedImageListenner() {
								@Override
								public void onPrepared(Drawable bitmap) {
									sendMsgBean(imgurl, bitmap, 1);
								}
							});
							
							boolean updateFlag = bookBean.isOnline();
							LinearLayout updatelinear = (LinearLayout)layoutTemp.findViewById(R.id.updateflag);
							if(updateFlag){
								updatelinear.setVisibility(View.VISIBLE);
							}else{
								updatelinear.setVisibility(View.GONE);
							}
						}
					}
				}
			}
			return conventView;
		}
    	
    }
    
    private void downloadUpdate(String nid,String novelname){
    	Intent intent = new Intent(GagaReaderActivity.this, DownLoadService.class);
		intent.putExtra("nid", nid);
		intent.putExtra("novelName", novelname);
		startService(intent);
    }
    
    private void removeBookDialog(final String nid){
    	new AlertDialog.Builder(GagaReaderActivity.this)
			.setTitle("将这本书移出书架")
			.setMessage("移除会同时移除已经下载和阅读的所有章节以及进度信息")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							sendMsgBean(nid, 3);
						}
					})
			.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).create().show();
    }
    
    private void sendMsgBean(String tag,Drawable bitmap,int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        MsgBean msgBean = new MsgBean(bitmap,tag);
        message.obj = msgBean;
        handler.sendMessage(message);
	}
    
    private void sendMsgBean(String nid,int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        message.obj = nid;
        handler.sendMessage(message);
	}
    
	@SuppressWarnings("unchecked")
	private void initTabHostList() throws JSONException{
    	FileUtil fileUtil = FileUtil.getInstance();
    	String assetStr = fileUtil.getAssetFileContent(this, "frame.json");
    	JSONArray hostMsgs = new JSONArray(assetStr);
    	int length = hostMsgs.length();
    	tabHostMsgBeanList = new ArrayList<TabHostMsgBean>();
    	for(int i=0;i<length;i++){
    		JSONObject hostMsg = hostMsgs.getJSONObject(i);
    		String tagName = hostMsg.getString("name");
    		String className = hostMsg.getString("className");
    		String param = hostMsg.getString("param");
    		TabHostMsgBean thmb = new TabHostMsgBean();
    		thmb.setTag(tagName);
    		thmb.setParam(param);
        	try {
				thmb.setClassName((Class<Activity>) Class.forName(className));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			tabHostMsgBeanList.add(thmb);
    	}
    }
	private void initMenuNameList(){
	    int size = tabHostMsgBeanList.size();
	    menuNameList = new ArrayList<String>();
	    for(int i=0;i<size;i++){
	    	menuNameList.add(tabHostMsgBeanList.get(i).getTag());
	    }
	}
	
	class MenuListViewAdapter extends BaseAdapter{
    	private List<String> menuNameList;
    	public MenuListViewAdapter(List<String> menuNameList){
    		this.menuNameList = menuNameList;
    	}
		@Override
		public int getCount() {
			return menuNameList.size();
		}
		@Override
		public Object getItem(int arg0) {
			return menuNameList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView=LayoutInflater.from(GagaReaderActivity.this).inflate(R.layout.menu_item, null);
			ImageView iv = (ImageView)convertView.findViewById(R.id.menuimg);
			iv.setBackgroundResource(muneImgs[position]);
			TextView menuNameTV = (TextView)convertView.findViewById(R.id.menuName);
			String menuName = menuNameList.get(position);
			menuNameTV.setText(menuName);
			return convertView;
		}
	}
	
	class MsgBean{
		private String tag;
		private Drawable bitmap;
		public MsgBean(Drawable bitmap,String tag){
			this.bitmap = bitmap;
			this.tag = tag;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public Drawable getBitmap() {
			return bitmap;
		}
		public void setBitmap(Drawable bitmap) {
			this.bitmap = bitmap;
		}
	}
	
	@Override  
    protected void onResume() {
		initBookshelf();
		bookShelfLvAdapter.notifyDataSetChanged();
		if (sliderOut) { 
			final int appLayoutW = appLayout.getMeasuredWidth(); 
		    final int appLayoutH = appLayout.getMeasuredHeight();
		    appLayout.layout(0, 0, appLayoutW, appLayoutH); 
		    sliderOut=!sliderOut;
		}
        super.onResume();
        MobclickAgent.onResume(this);
    }
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	private boolean mkAppDirs(){
		FileUtil fileUtil = FileUtil.getInstance();
		Log.e("mkAppDirs", ""+fileUtil.isHaveSize());
		if(fileUtil.isHaveSize()){//有剩余空间 就建立文件夹
			String rootPath = Environment.getExternalStorageDirectory().getPath();
			String appRootPath = rootPath+"/gagaReader";
			Const.APP_PHOTO_CACHE= appRootPath+"/photocache";
			Const.APP_TEXT_CACHE= appRootPath+"/textcache";
			
			fileUtil.mkdir(appRootPath);
			fileUtil.mkdir(Const.APP_PHOTO_CACHE);
			fileUtil.mkdir(Const.APP_TEXT_CACHE);
		}else{						//没有剩余空间  设置状态为 直接从网络播放 不缓存
			// 显示dialog  提示没有sd卡 不能使用
			return false;
		}
		return true;
	}
}