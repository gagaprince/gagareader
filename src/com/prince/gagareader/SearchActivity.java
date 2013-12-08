package com.prince.gagareader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.HotwordBean;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;
import com.umeng.analytics.MobclickAgent;

public class SearchActivity extends Activity{
private Handler handler;
	
	private ListView resultListView;
	private ListView hotwordListView;
	private Button backButton;
	private Button searchButton;
	private TextView headTv;
	private EditText searchText;
	private LinearLayout loadingLinear;
	private LinearLayout noresultLinear;
	
	private List<BookBean> resultBeanList;
	private List<HotwordBean> hotwords;
	
	private ResultAdapter resultAdapter;
	private HotwordAdapter hotAdapter;
	private String key;
	private int lastVisibleIndex;
	private int pno=0;
	private boolean loadMoreLock=false;
	
	private LinearLayout resultLinear;
	private LinearLayout hotLinear;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
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
	                case 1:  //public
	                	MsgBean msgBean = (MsgBean)msg.obj;
	                	String tag = msgBean.getTag();
	                	Drawable bitmap = msgBean.getBitmap();
	                	ImageView iv = (ImageView)resultListView.findViewWithTag(tag);
						if(iv!=null){
							iv.setImageDrawable(bitmap);
						}
	                    break;  
	                case 2://数据加载完成 显示
	                	noresultLinear.setVisibility(View.GONE);
	                	hotLinear.setVisibility(View.GONE);
	                	loadingLinear.setVisibility(View.GONE);
	                	resultLinear.setVisibility(View.VISIBLE);
	                	resultAdapter.notifyDataSetChanged();
	                	break;
	                case 3://没有网络
	                	break;
	                case 4:
	                	noresultLinear.setVisibility(View.VISIBLE);
	                	loadingLinear.setVisibility(View.GONE);
	                	resultLinear.setVisibility(View.GONE);
	                	break;
	                case 5:
	                	loadingLinear.setVisibility(View.GONE);
	                	hotLinear.setVisibility(View.VISIBLE);
	                	hotAdapter.notifyDataSetChanged();
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
	}
	private void initData(){
		Intent intent = this.getIntent();
		key = intent.getStringExtra("key");
		resultBeanList = new ArrayList<BookBean>();
		hotwords = new ArrayList<HotwordBean>();
		if(key==null){
			new Thread(new Runnable() {
				@Override
				public void run() {
					loadHotWord();
				}
			}).start();
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
					loadMore();
				}
			}).start();
		}
		resultAdapter = new ResultAdapter();
		hotAdapter = new HotwordAdapter();
	}
	private void initView(){
		backButton = (Button)findViewById(R.id.btn_leftTop);
		resultListView = (ListView)findViewById(R.id.cateLv);
		headTv = (TextView)findViewById(R.id.tv_head);
		loadingLinear = (LinearLayout)findViewById(R.id.bottom_linear);
		headTv.setText("搜索");
		searchText = (EditText)findViewById(R.id.searchText);
		if(key!=null)searchText.setText(key);
		searchButton = (Button)findViewById(R.id.searchButton);
		
		hotwordListView = (ListView)findViewById(R.id.hotLv);
		resultLinear = (LinearLayout)findViewById(R.id.resultLinear);
		hotLinear = (LinearLayout)findViewById(R.id.hotLinear);
		noresultLinear = (LinearLayout)findViewById(R.id.noresultLinear);
		resultLinear.setVisibility(View.GONE);
		hotLinear.setVisibility(View.GONE);
		noresultLinear.setVisibility(View.GONE);
	}
	private void addListenner(){
		resultListView.setAdapter(resultAdapter);
		resultListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int location,
					long arg3) {
				BookBean bookBean = resultBeanList.get(location);
				Intent intent = new Intent(SearchActivity.this,
						BookDetailActivity.class);
				intent.putExtra("nid", bookBean.getNid());
            	startActivity(intent);
			}
		});
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchKey = searchText.getText().toString();
				if(searchKey!=null&&!"".equals(searchKey)){
					key = searchKey;
					resultBeanList.clear();
					resultAdapter.notifyDataSetChanged();
					loadingLinear.setVisibility(View.VISIBLE);
					pno=0;
					new Thread(new Runnable() {
						@Override
						public void run() {
							loadMore();
						}
					}).start();
				}else{
					resultLinear.setVisibility(View.GONE);
					if(hotwords!=null&&hotwords.size()>0){
						hotLinear.setVisibility(View.VISIBLE);
					}else{
						new Thread(new Runnable() {
							@Override
							public void run() {
								loadHotWord();
							}
						}).start();
					}
				}
			}
		});
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
			}
		});
		hotwordListView.setAdapter(hotAdapter);
		//resultListView.setOnScrollListener(this);
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
	private void sendMsgBean(String tag,Drawable bitmap,int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        MsgBean msgBean = new MsgBean(bitmap,tag);
        message.obj = msgBean;
        handler.sendMessage(message);
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
	
	private void loadMore(){
		if(loadMoreLock)return;
		loadMoreLock = true;
		String netState = getNetWorkCate();
		if(!"none".equals(netState)){
			try {
				List<BookBean> resultBeanListMore = DateProvider.getInstance().getResultJson(key, pno++);
				if(resultBeanListMore!=null&&resultBeanListMore.size()>0){
					resultBeanList.addAll(resultBeanListMore);
					sendMsgBean(null, null, 2);
				}
				if(resultBeanListMore==null){
					if(hotwords!=null&&hotwords.size()>0){
						hotLinear.setVisibility(View.VISIBLE);
					}else{
						loadHotWord();
					}
					sendMsgBean(null, null, 4);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			sendMsgBean(null, null, 3);
		}
		loadMoreLock = false;
	}
	private void loadHotWord(){
		try {
			hotwords = DateProvider.getInstance().getHotwordBeanList();
			sendMsgBean(null, null, 5);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		};
	}
	
	class HotwordAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return hotwords.size();
		}
		@Override
		public Object getItem(int arg0) {
			return hotwords.get(arg0);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HotwordBean hotwordBean = hotwords.get(position);
			return preparedHotwordView(hotwordBean, convertView);
		}
		public View preparedHotwordView(HotwordBean hotwordBean,View convertView){
			if(convertView==null)
				convertView=LayoutInflater.from(SearchActivity.this).inflate(R.layout.hotword_item, null);
			TextView leftTv = (TextView)convertView.findViewById(R.id.leftword);
			TextView rightTv = (TextView)convertView.findViewById(R.id.rightword);
			final String leftWord = hotwordBean.getLeftWord();
			final String rightWord = hotwordBean.getRigthWord();
			leftTv.setText(leftWord);
			rightTv.setText(rightWord);
			leftTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					key = leftWord;
					searchText.setText(key);
					resultBeanList.clear();
					resultAdapter.notifyDataSetChanged();
					loadingLinear.setVisibility(View.VISIBLE);
					hotLinear.setVisibility(View.GONE);
					pno=0;
					loadMore();
				}
			});
			rightTv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					key = rightWord;
					searchText.setText(key);
					resultBeanList.clear();
					resultAdapter.notifyDataSetChanged();
					loadingLinear.setVisibility(View.VISIBLE);
					hotLinear.setVisibility(View.GONE);
					pno=0;
					loadMore();
				}
			});
			return convertView;
		}
	}
	class ResultAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return resultBeanList.size();
		}
		@Override
		public Object getItem(int arg0) {
			return resultBeanList.get(arg0);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BookBean bookBean = resultBeanList.get(position);
			return preparedSingleBookView(bookBean, convertView);
		}
		public View preparedSingleBookView(BookBean bookBean,View convertView){
			if(convertView==null)
				convertView=LayoutInflater.from(SearchActivity.this).inflate(R.layout.single_book_item, null);
			ImageView iv = (ImageView)convertView.findViewById(R.id.bookshow);
			iv.setImageResource(R.drawable.bg);
			TextView name_tv = (TextView)convertView.findViewById(R.id.novel_name);
			TextView author_tv = (TextView)convertView.findViewById(R.id.novel_author);
			TextView des_tv = (TextView)convertView.findViewById(R.id.novel_des);
			TextView lastChapter_tv = (TextView)convertView.findViewById(R.id.novel_lastChapter);
			TextView site_tv = (TextView)convertView.findViewById(R.id.novel_site);
			name_tv.setText(bookBean.getNovelName());
			author_tv.setText("作者:"+bookBean.getAuthor());
			des_tv.setText("类别:"+bookBean.getDes()+" 状态:"+(bookBean.getState()==0?"连载中":"完结"));
			lastChapter_tv.setText("更新至:"+bookBean.getLastChapter());
			site_tv.setText("来源:"+bookBean.getSourceSite());
			final String imgurl = bookBean.getFengmianUrl();
			final String nid = bookBean.getNid();
			iv.setTag(imgurl+nid);
			ImageUtil iu = ImageUtil.getInstance();
			iu.preparedImage(imgurl, new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl+nid, bitmap, 1);
				}
			});
			return convertView;
		}
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
