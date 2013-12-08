package com.prince.gagareader;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.services.DownLoadService;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;
import com.prince.gagareader.util.SharedPreferencesUtil;
import com.umeng.analytics.MobclickAgent;

public class BookDetailActivity extends Activity{
	private Handler handler;
	
	private String nid;
	private BookBean bookBean;
	
	private LinearLayout bottomLinear;
	
	private ImageView bookshow;
	private TextView novel_name;
	private TextView novel_author;
	private TextView novel_state;
	private Button readbtn;
	private Button indexbtn;
	private Button downLoadbtn;
	private Button putShelfbtn;
	private Button backButton;
	private TextView description;
	private TextView novel_lastnew;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        initHandler();
        initData();
        initView();
        addListenner();
    }
	
	private void initHandler(){
		handler = new Handler(){
			@Override  
            public void handleMessage(Message msg) {
				 super.handleMessage(msg);  
	                switch (msg.what) {  
	                case 1://数据加载完成 显示
	                	bottomLinear.setVisibility(View.GONE);
	                	preparedPage();
	                	break;
	                case 2:
	                	Drawable bitmap = (Drawable)msg.obj;
	                	bookshow.setBackgroundDrawable(bitmap);
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
	}
	private void preparedPage(){
		novel_name.setText(bookBean.getNovelName());
		novel_author.setText("作者："+bookBean.getAuthor());
		novel_state.setText("状态："+(bookBean.getState()==0?"连载":"完结"));
		description.setText(bookBean.getDes());
		novel_lastnew.setText("最新章节："+bookBean.getLastChapter());
		ImageUtil iu = ImageUtil.getInstance();
		final String imgurl = bookBean.getFengmianUrl();
		iu.preparedImage(imgurl,new OnPreparedImageListenner() {
			@Override
			public void onPrepared(Drawable bitmap) {
				sendMsgBean(2, bitmap);
			}
		});
	}
	private void initData(){
		Intent intent=getIntent();
		nid = intent.getStringExtra("nid");
		if(nid==null)nid="0dbe15224f0324a86d527972466e3adc";
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
	}
	private void initView(){
		bottomLinear = (LinearLayout)findViewById(R.id.bottom_linear);
		bookshow = (ImageView)findViewById(R.id.bookshow);
		readbtn = (Button)findViewById(R.id.online_read);
		indexbtn = (Button)findViewById(R.id.index_btn);
		downLoadbtn = (Button)findViewById(R.id.download_btn);
		putShelfbtn = (Button)findViewById(R.id.put_shelf_btn);
		novel_name = (TextView)findViewById(R.id.novel_name);
		novel_author = (TextView)findViewById(R.id.novel_author);
		novel_state = (TextView)findViewById(R.id.novel_state);
		description = (TextView)findViewById(R.id.des);
		novel_lastnew = (TextView)findViewById(R.id.novel_lastnew);
		backButton = (Button)findViewById(R.id.btn_leftTop);
	}
	private void addListenner(){
		putShelfbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				BookBean bookBeanDB = su.getBookBeanByNid(nid, BookDetailActivity.this);
				if(bookBeanDB==null){
					su.addBookBean(bookBean, BookDetailActivity.this);
				}else{
					su.addBookBean(bookBeanDB, BookDetailActivity.this);
				}
			}
		});
		indexbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BookDetailActivity.this,
						MuluActivity.class);
				String nid = bookBean.getNid();
				String name = bookBean.getNovelName();
				intent.putExtra("nid", nid);
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				BookBean bookDB = su.getBookBeanByNid(nid, BookDetailActivity.this);
				if(bookDB!=null){
					int cid = bookDB.getCurrentCount();
					intent.putExtra("cid", cid);
				}
				intent.putExtra("novelName", name);
            	startActivity(intent);
			}
		});
		downLoadbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BookDetailActivity.this, DownLoadService.class);
				intent.putExtra("nid", nid);
				intent.putExtra("novelName", bookBean.getNovelName());
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				BookBean bookBeanDB = su.getBookBeanByNid(nid, BookDetailActivity.this);
				if(bookBeanDB==null){
					su.addBookBean(bookBean, BookDetailActivity.this);
				}else{
					su.addBookBean(bookBeanDB, BookDetailActivity.this);
				}
				startService(intent);
			}
		});
		readbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BookDetailActivity.this,
						ReaderActivity.class);
				intent.putExtra("nid", nid);
				SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
				BookBean bookDB = su.getBookBeanByNid(nid, BookDetailActivity.this);
				if(bookDB!=null){
					int cid = bookDB.getCurrentCount();
					intent.putExtra("currentChapter", ""+cid);
				}else{
					intent.putExtra("currentChapter", "1");
				}
            	startActivity(intent);
			}
		});
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BookDetailActivity.this.finish();
			}
		});
	}
	private void sendMsgBean(int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        handler.sendMessage(message);
	}
	private void sendMsgBean(int msgWhat,Drawable bitmap){
		Message message = new Message();  
        message.what = msgWhat;
        message.obj=bitmap;
        handler.sendMessage(message);
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
