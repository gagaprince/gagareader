package com.prince.gagareader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.prince.gagareader.bean.CateBean;
import com.prince.gagareader.bean.Const;
import com.prince.gagareader.util.DateProvider;
import com.umeng.analytics.MobclickAgent;

public class PhbActivity extends Activity{
	private Handler handler;
	private List<CateBean> cateBeanList;
	private GridView cateGridView;
	private String cateUrl;
	private String param;
	private CateAdapter cateAdapter;
	private TextView tv_head;
	private Button backButton;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phbgrid_grid);
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
	                    break;  
	                case 2://数据加载完成 显示
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
		param = intent.getStringExtra("param");
		cateUrl = Const.NOVEL_TAG_URL;
		cateBeanList = new ArrayList<CateBean>();
		new Thread(new Runnable() {
			@Override
			public void run() {
		    	loadMore();
			}
		}).start();
		cateAdapter = new CateAdapter();
	}
	private void initView(){
		cateGridView = (GridView)findViewById(R.id.myGridView);
		tv_head = (TextView)findViewById(R.id.tv_head);
		tv_head.setText(param);
		backButton = (Button)findViewById(R.id.btn_leftTop);
	}
	private void addListenner(){
		Log.e("addListenner",cateGridView+"");
		cateGridView.setAdapter(cateAdapter);
		cateGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int location,
					long arg3) {
				CateBean cateBean = cateBeanList.get(location);
				Intent intent = new Intent(PhbActivity.this,
						CateActivity.class);
				String cate = cateBean.getCate();
				intent.putExtra("cate",cate);
				intent.putExtra("cateType", 3);
            	startActivity(intent);
			}
		});
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhbActivity.this.finish();
			}
		});
	}
	
	private void loadMore(){
		List<CateBean> cateBeanListMore = DateProvider.getInstance().getAllPhbBeanList(PhbActivity.this);
		if(cateBeanListMore!=null&&cateBeanListMore.size()>0){
			cateBeanList=cateBeanListMore;
			sendMsgBean(null, null, 2);
		}
	}
	
	private void sendMsgBean(String tag,Drawable bitmap,int msgWhat){
		Message message = new Message();  
        message.what = msgWhat;
        MsgBean msgBean = new MsgBean(bitmap,tag);
        message.obj = msgBean;
        handler.sendMessage(message);
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
			CateBean cateBean = cateBeanList.get(position);
			return preparedSingleBookView(cateBean, convertView);
		}
		public View preparedSingleBookView(CateBean cateBean,View convertView){
			if(convertView==null)
				convertView=LayoutInflater.from(PhbActivity.this).inflate(R.layout.categrid_item, null);
			TextView cateText = (TextView)convertView.findViewById(R.id.catetv);
			String showText = cateBean.getCateName();
			cateText.setText(showText);
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
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
