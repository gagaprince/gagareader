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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;

public class CateActivity extends Activity implements OnScrollListener{
	private Handler handler;
	
	private ListView cateListView;
	private Button backButton;
	private TextView headTv;
	private LinearLayout loadingLinear;
	
	private List<BookBean> cateBeanList;
	private String cate;
	private int cateType; //0网络 1传统 2标签3排行榜
	private CateAdapter cateAdapter;
	
	private int lastVisibleIndex;
	private int pno=0;
	private boolean loadMoreLock=false;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catelist);
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
	                	ImageView iv = (ImageView)cateListView.findViewWithTag(tag);
						if(iv!=null){
							iv.setImageDrawable(bitmap);
						}
	                    break;  
	                case 2://数据加载完成 显示
	                	loadingLinear.setVisibility(View.GONE);
	                	cateAdapter.notifyDataSetChanged();
	                	break;
	                case 3://没有网络
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
	}
	private void initData(){
		Intent intent = this.getIntent();
		cate = intent.getStringExtra("cate");
		if(cate==null)cate = "都市";
		cateType = intent.getIntExtra("cateType", 0);
		cateBeanList = new ArrayList<BookBean>();
		new Thread(new Runnable() {
			@Override
			public void run() {
		    	loadMore();
			}
		}).start();
		cateAdapter = new CateAdapter();
	}
	private void initView(){
		backButton = (Button)findViewById(R.id.btn_leftTop);
		cateListView = (ListView)findViewById(R.id.cateLv);
		headTv = (TextView)findViewById(R.id.tv_head);
		loadingLinear = (LinearLayout)findViewById(R.id.bottom_linear);
		headTv.setText(cate);
	}
	private void addListenner(){
		cateListView.setAdapter(cateAdapter);
		cateListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int location,
					long arg3) {
				BookBean bookBean = cateBeanList.get(location);
				Intent intent = new Intent(CateActivity.this,
						SearchActivity.class);
				intent.putExtra("key", bookBean.getNovelName());
            	startActivity(intent);
			}
		});
		cateListView.setOnScrollListener(this);
		
	}
	
	class CateAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return cateBeanList.size();
		}

		@Override
		public Object getItem(int position) {
			return cateBeanList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BookBean bookBean = cateBeanList.get(position);
			return preparedSingleBookView(bookBean, convertView);
		}
		public View preparedSingleBookView(BookBean bookBean,View convertView){
			if(convertView==null)
				convertView=LayoutInflater.from(CateActivity.this).inflate(R.layout.single_book_item, null);
			ImageView iv = (ImageView)convertView.findViewById(R.id.bookshow);
			iv.setImageResource(R.drawable.bg);
			TextView name_tv = (TextView)convertView.findViewById(R.id.novel_name);
			TextView author_tv = (TextView)convertView.findViewById(R.id.novel_author);
			TextView des_tv = (TextView)convertView.findViewById(R.id.novel_des);
			name_tv.setText(bookBean.getNovelName());
			author_tv.setText("作者:"+bookBean.getAuthor());
			des_tv.setText("类别:"+bookBean.getDes());
			final String imgurl = bookBean.getFengmianUrl();
			iv.setTag(imgurl);
			ImageUtil iu = ImageUtil.getInstance();
			iu.preparedImage(imgurl, new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl, bitmap, 1);
				}
			});
			return convertView;
		}
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
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastVisibleIndex = firstVisibleItem + visibleItemCount;
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && lastVisibleIndex == cateAdapter.getCount()) {
             loadingLinear.setVisibility(View.VISIBLE);
             handler.postDelayed(new Runnable() {
	             @Override
	             public void run() {
	            	 loadMore();
	             }
	         }, 2000);

        }
	}
	private void loadMore(){
		if(loadMoreLock)return;
		loadMoreLock = true;
		String netState = getNetWorkCate();
		if(!"none".equals(netState)){
			try {
				List<BookBean> cateBeanListMore = DateProvider.getInstance().getCateListJson(cate,pno++,cateType);
				if(cateBeanListMore!=null&&cateBeanListMore.size()>0){
					cateBeanList.addAll(cateBeanListMore);
					sendMsgBean(null, null, 2);
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
}
