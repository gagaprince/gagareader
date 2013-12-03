package com.prince.gagareader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.CateBean;
import com.prince.gagareader.bean.Const;
import com.prince.gagareader.util.DateProvider;
import com.prince.gagareader.util.ImageUtil;
import com.prince.gagareader.util.ImageUtil.OnPreparedImageListenner;

public class CateListActivity extends Activity{
	private Handler handler;
	private List<CateBean> cateBeanList;
	
	private ListView cateListView;
	private Button backButton;
	private TextView headTv;
	
	private CateAdapter cateAdapter;
	
	private String cateUrl;
	private String param;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catelist_list);
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
		if(param!=null&&"1".equals(param)){
			cateUrl = Const.NOVEL_CATE_URL;
		}else{
			cateUrl = Const.NOVEL_CATE_TRURL;
		}
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
		backButton = (Button)findViewById(R.id.btn_leftTop);
		cateListView = (ListView)findViewById(R.id.cateLv);
		headTv = (TextView)findViewById(R.id.tv_head);
		headTv.setText("1".equals(param)?"热门分类":"传统文学");
	}
	private void addListenner(){
		cateListView.setAdapter(cateAdapter);
		cateListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int location,
					long arg3) {
				CateBean cateBean = cateBeanList.get(location);
				Intent intent = new Intent(CateListActivity.this,
						CateActivity.class);
				String cate = cateBean.getCate();
				intent.putExtra("cate",cate);
				if(param!=null&&"2".equals(param)){
					intent.putExtra("cateType",1);
				}
            	startActivity(intent);
			}
		});
	}
	
	private void loadMore(){
		try {
			List<CateBean> cateBeanListMore = DateProvider.getInstance().getAllCateBeanList(cateUrl);
			if(cateBeanListMore!=null&&cateBeanListMore.size()>0){
				cateBeanList=cateBeanListMore;
				sendMsgBean(null, null, 2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
				convertView=LayoutInflater.from(CateListActivity.this).inflate(R.layout.single_book_item, null);
			ImageView iv = (ImageView)convertView.findViewById(R.id.bookshow);
			iv.setImageResource(R.drawable.bg);
			TextView name_tv = (TextView)convertView.findViewById(R.id.novel_name);
			name_tv.setText(cateBean.getCateName());
			
			TextView author_tv = (TextView)convertView.findViewById(R.id.novel_author);
			TextView des_tv = (TextView)convertView.findViewById(R.id.novel_des);
			author_tv.setVisibility(View.GONE);
			des_tv.setVisibility(View.GONE);
			final String imgurl = cateBean.getImg();
			if(imgurl!=null){
				iv.setTag(imgurl);
				ImageUtil iu = ImageUtil.getInstance();
				iu.preparedImage(imgurl, new OnPreparedImageListenner() {
					@Override
					public void onPrepared(Drawable bitmap) {
						sendMsgBean(imgurl, bitmap, 1);
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
}
