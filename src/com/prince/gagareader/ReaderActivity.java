package com.prince.gagareader;

import java.io.IOException;
import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.BookMarkBean;
import com.prince.gagareader.bean.ContentBean;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.NovelUtil;
import com.prince.gagareader.util.SharedPreferencesUtil;
import com.prince.gagareader.view.ReaderView;
import com.prince.gagareader.view.ReaderView.NeedLoadDataListenner;
import com.prince.gagareader.view.ReaderView.OnMiddleClick;
import com.prince.gagareader.view.ReaderView.OnloadDataComplete;
import com.umeng.analytics.MobclickAgent;

import dalvik.system.TemporaryDirectory;

public class ReaderActivity extends Activity {
	private ReaderView readerView;
	private Handler handler;
	
	private String nid;
	private BookBean bookBean;
	private BookMarkBean bookMark;
	
	private LinearLayout dengLinear;
	private LinearLayout bottemButtonLinear;
	private LinearLayout muluButtonLinear;
	private LinearLayout bgiconLinear;
	private LinearLayout bgbuttonLinear;
	private LinearLayout lightseekBarLinear;
	private LinearLayout lightbuttonlinear;
	private SeekBar lightSeekBar;
	private LinearLayout jinduseekBarLinear;
	private SeekBar jinduSeekBar;
	private LinearLayout jinduButtonLinear;
	private SeekBar fontsizeSeekBar;
	private LinearLayout fontsizeButtonLinear;
	private LinearLayout fontsizeBarLinear;
	private LinearLayout nightlinear;
	
	private RelativeLayout allView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);
        initView();
        initHandler();
        initData();
        addListenner();
        beginReader();
    }
    private void initData(){
    	Intent intent=getIntent();
		nid = intent.getStringExtra("nid");
		if(nid==null)nid="0dbe15224f0324a86d527972466e3adc";
		//先从书架初始化，在从网络初始化
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		bookBean = su.getBookBeanByNid(nid, this);
		if(bookBean==null){
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						bookBean = DateProvider.getInstance().getBookBeanByNid(nid);
						sendMsgBean(1);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			},20);
		}else{
			sendMsgBean(1);
		}
    }
    private void initHandler(){
    	handler = new Handler(){
			@Override  
            public void handleMessage(Message msg) {
				 super.handleMessage(msg);  
	                switch (msg.what) {  
	                case 1://数据加载完成 显示
	                	preparedPage();
	                	break;
	                case 2:
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
    }
    
    private void preparedPage(){
    	Intent intent=getIntent();
    	String currentChapterStr = intent.getStringExtra("currentChapter");
    	if(bookBean!=null){
    		bookBean.setOnline(false);
    	}
    	if(currentChapterStr!=null){
    		int currentChapter = Integer.parseInt(currentChapterStr);
    		if(bookBean!=null){
    			SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
    			int currentChapterDB = bookBean.getCurrentCount();
    			if(currentChapter==currentChapterDB){
    				bookMark = su.getBookMarkByNid(nid, this);
    				if (bookMark==null) {
						bookMark = new BookMarkBean(1, 0);
					}
    				su.addBookBean(bookBean, this);
    			}else{
    				bookMark = new BookMarkBean(currentChapter, 0);
    				bookBean.setCurrentCount(currentChapter);
    				su.addBookBeanIfExist(bookBean, this);
    				su.setBookMark(nid, currentChapter, 0, this);
    			}
    		}
    	}else{
    		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
    		su.addBookBean(bookBean, this);
    		bookMark = su.getBookMarkByNid(nid, this);
			if (bookMark==null) {
				bookMark = new BookMarkBean(1, 0);
			}
    	}
    	readerView.addNeedLoadDataListenner(new MyNeedLoadData());
    	if(!readerView.hasLoadData){
    		readerView.excuteNeedReloadCurrent();
    	}
    }
    
    private void initView(){
    	readerView = (ReaderView) findViewById(R.id.myReaderView);
    	dengLinear = (LinearLayout) findViewById(R.id.dengshengLinear);
    	bottemButtonLinear = (LinearLayout)findViewById(R.id.buttonBottemLinear);
    	muluButtonLinear = (LinearLayout)findViewById(R.id.muluButtonLinear);
    	bgiconLinear = (LinearLayout)findViewById(R.id.bgiconLinear);
    	bgbuttonLinear = (LinearLayout)findViewById(R.id.bgbuttonLinear);
    	lightseekBarLinear = (LinearLayout)findViewById(R.id.lightseekBarLinear);
    	lightbuttonlinear = (LinearLayout)findViewById(R.id.lightbuttonlinear);
    	lightSeekBar = (SeekBar)findViewById(R.id.lightseekBar);
    	jinduseekBarLinear = (LinearLayout)findViewById(R.id.jinduseekBarLinear);
    	jinduSeekBar = (SeekBar)findViewById(R.id.jinduseekBar);
    	jinduButtonLinear = (LinearLayout)findViewById(R.id.jinduButtonLinear);
    	fontsizeBarLinear = (LinearLayout)findViewById(R.id.fontsizeseekBarLinear);
    	fontsizeSeekBar = (SeekBar)findViewById(R.id.fontsizeseekBar);
    	fontsizeButtonLinear = (LinearLayout)findViewById(R.id.fontsizeButtonLinear);
    	nightlinear = (LinearLayout)findViewById(R.id.nightlinear);
    	allView = (RelativeLayout)findViewById(R.id.allView);
    }
    
    private void addListenner(){
    	readerView.setOnMiddleClick(new OnMiddleClick() {
			@Override
			public void onClick() {
				if(dengLinear.getVisibility()==View.INVISIBLE){
					showCtrl();
				}else{
					hideCtrl();
				}
			}
		});
    	muluButtonLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ReaderActivity.this,
						MuluActivity.class);
				String nid = bookBean.getNid();
				String name = bookBean.getNovelName();
				intent.putExtra("nid", nid);
				intent.putExtra("cid", bookBean.getCurrentCount());
				intent.putExtra("novelName", name);
				intent.putExtra("isreading", true);
				startActivityForResult(intent, 0);
				hideCtrl();
			}
		});
    	bgbuttonLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bottemButtonLinear.setVisibility(View.INVISIBLE);
				bgiconLinear.setVisibility(View.VISIBLE);
			}
		});
    	lightbuttonlinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bottemButtonLinear.setVisibility(View.INVISIBLE);
			 	lightSeekBar.setMax(255); 
		        int normal = Settings.System.getInt(getContentResolver(), 
		                Settings.System.SCREEN_BRIGHTNESS, 255); 
		        lightSeekBar.setProgress(normal); 
		        bottemButtonLinear.setVisibility(View.INVISIBLE);
				lightseekBarLinear.setVisibility(View.VISIBLE);
			}	
		});
    	jinduButtonLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int allPageSize = readerView.getAllPage();
				jinduSeekBar.setMax(allPageSize);
				int jindu = readerView.getJindu();
				jinduSeekBar.setProgress(jindu); 
				bottemButtonLinear.setVisibility(View.INVISIBLE);
				jinduseekBarLinear.setVisibility(View.VISIBLE);
			}
		});
    	fontsizeButtonLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				int fontsize = su.getContextFontSize(ReaderActivity.this);
				fontsizeSeekBar.setMax(20);
				fontsizeSeekBar.setProgress(fontsize-10); 
				bottemButtonLinear.setVisibility(View.INVISIBLE);
				fontsizeBarLinear.setVisibility(View.VISIBLE);
			}
		});
    	LinearLayout linearTemp = (LinearLayout)findViewById(R.id.topbgiconLinear);
    	int childsize = linearTemp.getChildCount();
    	for(int i=0;i<childsize;i++){
    		View view = linearTemp.getChildAt(i);
    		Object o = view.getTag();
    		if(o!=null&&((String)o).startsWith("bgicon")){
    			final int index = Integer.parseInt(((String)o).split("_")[1]);
    			view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						readerView.updateBg(index);
					}
				});
    		}
    	}
    	linearTemp = (LinearLayout)findViewById(R.id.bottembgiconLinear);
    	childsize = linearTemp.getChildCount();
    	for(int i=0;i<childsize;i++){
    		View view = linearTemp.getChildAt(i);
    		Object o = view.getTag();
    		if(o!=null&&((String)o).startsWith("bgicon")){
    			final int index = Integer.parseInt(((String)o).split("_")[1]);
    			view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						readerView.updateBg(index);
					}
				});
    		}
    	}
    	lightSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int tmpInt = seekBar.getProgress(); 
                if (tmpInt < 80) { 
                    tmpInt = 80; 
                } 
                // 根据当前进度改变亮度 
                Settings.System.putInt(getContentResolver(), 
                        Settings.System.SCREEN_BRIGHTNESS, tmpInt); 
                tmpInt = Settings.System.getInt(getContentResolver(), 
                        Settings.System.SCREEN_BRIGHTNESS, -1); 
                WindowManager.LayoutParams wl = getWindow().getAttributes(); 
                float tmpFloat = (float) tmpInt / 255; 
                if (tmpFloat > 0 && tmpFloat <= 1) { 
                    wl.screenBrightness = tmpFloat; 
                } 
                getWindow().setAttributes(wl);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {}
		});
    	jinduSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int tmpInt = seekBar.getProgress(); 
				readerView.setJindu(tmpInt);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {}
    	});
    	fontsizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int tmpInt = seekBar.getProgress(); 
				int fontsize = tmpInt+10;
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				int currentSize = su.getContextFontSize(ReaderActivity.this);
				if(currentSize!=fontsize){
					su.setContextFontSize(fontsize, ReaderActivity.this);
					readerView.resizeFontSize(fontsize);
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {}
    	});
    	dengLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				excuteNightButton();
			}
		});
    	nightlinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				excuteNightButton();
			}
		});
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode==0&&resultCode==RESULT_OK){
    		int cid = Integer.parseInt(data.getStringExtra("currentChapter"));
    		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
			int currentChapterDB = bookBean.getCurrentCount();
			if(cid!=currentChapterDB){
				bookMark = new BookMarkBean(cid, 0);
				bookBean.setCurrentCount(cid);
				su.addBookBeanIfExist(bookBean, this);
				su.setBookMark(nid, cid, 0, this);
				readerView.excuteNeedReloadCurrent();
				hideCtrl();
			}
    	}
    	super.onActivityResult(requestCode,resultCode, data);   
    }
    
    private void excuteNightButton(){
    	SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		int currentBg = su.getContextBg(ReaderActivity.this);
		if(currentBg==1){
			int newBg = su.getContextLight(ReaderActivity.this);
			readerView.updateBg(newBg);
		}else{
			readerView.updateBg(1);
		}
		hideCtrl();
    }
    
    /**
     * 显示控制面板
     */
    private void showCtrl(){
    	quitFullScreen();
    	allView.setPadding(0, -getStatusBarHeight(this), 0, 0);
    	dengLinear.setVisibility(View.VISIBLE);
    	bottemButtonLinear.setVisibility(View.VISIBLE);
    	SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
    	int currentBg = su.getContextBg(this);
    	ImageView nightimg = (ImageView)findViewById(R.id.nightimg);
    	TextView nighttv = (TextView)findViewById(R.id.nighttv);
    	if(currentBg==1){
    		nightimg.setBackgroundResource(R.drawable.dayicon);
    		nighttv.setText("白天");
    	}else{
    		nightimg.setBackgroundResource(R.drawable.nighticon);
    		nighttv.setText("黑夜");
    	}
    }
    /**
     * 隐藏控制面板
     */
    private void hideCtrl(){
    	setFullScreen();
    	allView.setPadding(0, 0, 0, 0);
    	dengLinear.setVisibility(View.INVISIBLE);
    	bottemButtonLinear.setVisibility(View.INVISIBLE);
    	bgiconLinear.setVisibility(View.INVISIBLE);
    	lightseekBarLinear.setVisibility(View.INVISIBLE);
    	jinduseekBarLinear.setVisibility(View.INVISIBLE);
    	fontsizeBarLinear.setVisibility(View.INVISIBLE);
    }
    private void beginReader(){
    	/*handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					String content = getContentBycid(""+chapter);
					readerView.setTextData(content);
					readerView.initData();
				} catch (IOException e) {
					//网络异常
					e.printStackTrace();
				} catch (JSONException e) {
					//解析出错
					e.printStackTrace();
				}
			}
		});*/
    }
    
    private void sendMsgBean(int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        handler.sendMessage(message);
	}
    
    private ContentBean getContentBycid(String cid) throws IOException, JSONException{
    	if(bookBean!=null){
    		String nid = bookBean.getNid();
    		String novelContent = NovelUtil.getInstance().preparedNovelByNidCid(nid, cid);
    		JSONObject textJson = new JSONObject(novelContent).getJSONObject("result");
    		String content = "        "+textJson.getString("content").trim().replaceAll("B#R", "\n        ");
    		String title = textJson.getString("ctitle");
    		return  new ContentBean(content, title);
    	}
		return null;
    }
    
    class MyNeedLoadData implements NeedLoadDataListenner{
		@Override
		public void needNext(final OnloadDataComplete call) {
			if(bookBean!=null){
				int allChapterCount = bookBean.getAllChapterCount();
				final int currentChapter = bookBean.getCurrentCount();
				if(currentChapter<allChapterCount){
					bookBean.setCurrentCount(currentChapter+1);
					SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
					su.addBookBeanIfExist(bookBean, ReaderActivity.this);
					preparedReaderData(call, currentChapter+1,1);
				}
			}
		}

		@Override
		public void needPre(final OnloadDataComplete call) {
			if(bookBean!=null){
				final int currentChapter = bookBean.getCurrentCount();
				if(currentChapter>1){
					bookBean.setCurrentCount(currentChapter-1);
					SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
					su.addBookBeanIfExist(bookBean, ReaderActivity.this);
					preparedReaderData(call, currentChapter-1,0);
				}
			}
		}

		@Override
		public void reloadCurrent(OnloadDataComplete call) {
			Log.e("reload",bookMark+"");
			if(bookBean!=null&&bookMark!=null){
				final int currentChapter = bookBean.getCurrentCount();
				Log.e("reloadcurrent", bookMark.getCurrentPage()+"");
				readerView.setCurrentBegin(bookMark.getCurrentPage());
				preparedReaderData(call, currentChapter,2);
			}
		}

		@Override
		public void needChangeBookMark(int currentBegin) {
			bookMark.setCurrentCount(bookBean.getCurrentCount());
			bookMark.setCurrentPage(currentBegin);
			Log.e("changeBookMark", currentBegin+"");
			SharedPreferencesUtil.getInstance().setBookMark(nid, bookMark, ReaderActivity.this);
		}
    }
    
    private void preparedReaderData(final OnloadDataComplete call,final int currentChapter,final int flag){
    	handler.post(new Runnable() {
			@Override
			public void run() {
				ContentBean contentBean;
				try {
					contentBean = getContentBycid(""+currentChapter);
					readerView.setTextData(contentBean.getContent());
					readerView.setChapterName(contentBean.getTitle());
					readerView.setYuChapterCount(bookBean.getAllChapterCount()-currentChapter);
					if(flag==1){
						call.onNextComplete();
					}else if(flag==2){
						call.onInitComplete();
					}else{
						call.onPreComplete();
					}
				} catch (IOException e) {
					call.onError();
					e.printStackTrace();
				} catch (JSONException e) {
					call.onError();
					e.printStackTrace();
				}
			}
		});
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN){
			SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
			if(!su.isBookExist(bookBean, this)){
				showIsAddDialog();
			}else{
				ReaderActivity.this.finish();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
    
    /**
	 * 显示退出提示
	 */
	private void showIsAddDialog(){
		new AlertDialog.Builder(ReaderActivity.this)
		.setTitle("温馨提示")
		.setMessage("是否将此书加入书架，方便以后阅读")
		.setPositiveButton("加入书架",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
						su.addBookBean(bookBean, ReaderActivity.this);
						ReaderActivity.this.finish();
					}
				})
		.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						ReaderActivity.this.finish();
					}
				}).create().show();
	}
	
	private void setFullScreen()
    {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    //[代码] 退出全屏函数：
    private void quitFullScreen()
    {
      final WindowManager.LayoutParams attrs = getWindow().getAttributes();
      attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
      getWindow().setAttributes(attrs);
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
    
    public static int getStatusBarHeight(Context context){    
        Class<?> c = null;    
        Object obj = null;    
        Field field = null;    
        int x = 0,
        statusBarHeight = 0;    
        try{    
            c= Class.forName("com.android.internal.R$dimen");    
            obj= c.newInstance();    
            field= c.getField("status_bar_height");    
            x= Integer.parseInt(field.get(obj).toString());    
            statusBarHeight= context.getResources().getDimensionPixelSize(x);    
        }catch(Exception e1) {    
            e1.printStackTrace();    
        }
        return statusBarHeight;   
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