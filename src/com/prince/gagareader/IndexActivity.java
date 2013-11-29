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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.CateBean;
import com.prince.gagareader.bean.ShowItemBean;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;

public class IndexActivity extends Activity{
	private Handler handler;
	private ListView indexLv;
	private LinearLayout bottomLinear;
	private IndexLvAdapter indexLvAdapter;
	
	private List<ShowItemBean> itemDateList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        initHandler();
        initData();
        initView();
        addListenner();
    }
    private void initData(){
    	itemDateList = new ArrayList<ShowItemBean>();
    	new Thread(new Runnable() {
			@Override
			public void run() {
		    	try {
		    		String netState = getNetWorkCate();
		    		if(!"none".equals(netState)){
		    			itemDateList = DateProvider.getInstance().getShowItemBeanList(true);
		    		}else{
		    			itemDateList = DateProvider.getInstance().getShowItemBeanList(false);
		    		}
					sendMsgBean(null, null, 2);
				} catch (IOException e) {
					//网络异常
					e.printStackTrace();
				} catch (JSONException e) {
					// 
					e.printStackTrace();
				}
			}
		}).start();
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
	                	ImageView iv = (ImageView)indexLv.findViewWithTag(tag);
						if(iv!=null){
							iv.setImageDrawable(bitmap);
						}
	                    break;  
	                case 2://数据加载完成 显示
	                	bottomLinear.setVisibility(View.GONE);
	                	indexLvAdapter.notifyDataSetChanged();
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
    }
    private void initView(){
    	indexLv = (ListView)findViewById(R.id.indexLv);
    	bottomLinear = (LinearLayout)findViewById(R.id.bottom_linear);
    	indexLvAdapter = new IndexLvAdapter();
    }
    private void addListenner(){
    	indexLv.setAdapter(indexLvAdapter);
    }
    
    class IndexLvAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return itemDateList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return itemDateList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShowItemBean showItemBean = itemDateList.get(position);
			int beanType = showItemBean.getType();
			switch (beanType) {
			case ShowItemBean.TITLE_TAG:
				convertView = preparedTitleView(showItemBean);
				break;
			case ShowItemBean.CATE_TAG:
				convertView = preparedCateView(showItemBean);
				break;
			case ShowItemBean.LIST_BOOK_TAG:
				convertView = preparedListBookView(showItemBean);
				break;
			case ShowItemBean.SINGEL_BOOK_TAG:
				convertView = preparedSingleBookView(showItemBean);
				break;
			}
			return convertView;
		}
    	
		public View preparedTitleView(ShowItemBean showItemBean){
			View convertView=LayoutInflater.from(IndexActivity.this).inflate(R.layout.index_title_item, null);
			TextView tv = (TextView)convertView.findViewById(R.id.index_title);
			tv.setText(showItemBean.getTitle());
			return convertView;
		}
		public View preparedSingleBookView(ShowItemBean showItemBean){
			View convertView=LayoutInflater.from(IndexActivity.this).inflate(R.layout.single_book_item, null);
			ImageView iv = (ImageView)convertView.findViewById(R.id.bookshow);
			TextView name_tv = (TextView)convertView.findViewById(R.id.novel_name);
			TextView author_tv = (TextView)convertView.findViewById(R.id.novel_author);
			TextView des_tv = (TextView)convertView.findViewById(R.id.novel_des);
			BookBean bookBean = showItemBean.getBookBean();
			name_tv.setText(bookBean.getNovelName());
			author_tv.setText(bookBean.getAuthor());
			des_tv.setText(bookBean.getDes());
			final String imgurl = bookBean.getFengmianUrl();
			iv.setTag(imgurl);
			ImageUtil iu = ImageUtil.getInstance();
			iu.preparedImage(imgurl, new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl, bitmap, 1);
				}
			});
			final String nid = bookBean.getNid();
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(IndexActivity.this,
							BookDetailActivity.class);
					intent.putExtra("nid", nid);
                	startActivity(intent);
				}
			});
			return convertView;
		}
		public View preparedListBookView(ShowItemBean showItemBean){
			View convertView=LayoutInflater.from(IndexActivity.this).inflate(R.layout.book_list_item, null);
			ImageView iv1 = (ImageView)convertView.findViewById(R.id.bookshow1);
			ImageView iv2 = (ImageView)convertView.findViewById(R.id.bookshow2);
			ImageView iv3 = (ImageView)convertView.findViewById(R.id.bookshow3);
			List<BookBean> beans = showItemBean.getBookBeans();
			final String imgurl1 = beans.get(0).getFengmianUrl();
			final String imgurl2 = beans.get(1).getFengmianUrl();
			final String imgurl3 = beans.get(2).getFengmianUrl();
			iv1.setTag(imgurl1);
			iv2.setTag(imgurl2);
			iv3.setTag(imgurl3);
			ImageUtil iu = ImageUtil.getInstance();
			iu.preparedImage(imgurl1,new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl1, bitmap, 1);
				}
			});
			iu.preparedImage(imgurl2,new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl2, bitmap, 1);
				}
			});
			iu.preparedImage(imgurl3,new OnPreparedImageListenner() {
				@Override
				public void onPrepared(Drawable bitmap) {
					sendMsgBean(imgurl3, bitmap, 1);
				}
			});
			final String iv1Nid=beans.get(0).getNid();
			iv1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(IndexActivity.this,
							BookDetailActivity.class);
					intent.putExtra("nid", iv1Nid);
                	startActivity(intent);
				}
			});
			final String iv2Nid=beans.get(1).getNid();
			iv2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(IndexActivity.this,
							BookDetailActivity.class);
					intent.putExtra("nid", iv2Nid);
                	startActivity(intent);
				}
			});
			final String iv3Nid=beans.get(2).getNid();
			iv3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(IndexActivity.this,
							BookDetailActivity.class);
					intent.putExtra("nid", iv3Nid);
                	startActivity(intent);
				}
			});
			return convertView;
		}
		public View preparedCateView(ShowItemBean showItemBean){
			View convertView=LayoutInflater.from(IndexActivity.this).inflate(R.layout.cate_item, null);
			TextView tv1 = (TextView)convertView.findViewById(R.id.cate_title1);
			TextView tv2 = (TextView)convertView.findViewById(R.id.cate_title2);
			TextView tv3 = (TextView)convertView.findViewById(R.id.cate_title3);
			TextView tv4 = (TextView)convertView.findViewById(R.id.cate_title4);
			TextView tv5 = (TextView)convertView.findViewById(R.id.cate_title5);
			List<TextView> tvList= new ArrayList<TextView>();
			tvList.add(tv1);
			tvList.add(tv2);
			tvList.add(tv3);
			tvList.add(tv4);
			tvList.add(tv5);
			List<CateBean> cates = showItemBean.getCateTitles();
			for(int i=0;i<tvList.size();i++){
				TextView tv = tvList.get(i);
				CateBean cateBean =  cates.get(i);
				String cateName = cateBean.getCateName();
				tv.setText(cateName);
				final String cate = cateBean.getCate();
				final int cateType = cateBean.getCateType();
				tv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(IndexActivity.this,
								CateActivity.class);
						intent.putExtra("cate",cate);
						intent.putExtra("cateType", cateType);
	                	startActivity(intent);
					}
				});
			}
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
}