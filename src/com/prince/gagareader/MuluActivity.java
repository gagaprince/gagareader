package com.prince.gagareader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prince.gagareader.bean.MuluBean;
import com.prince.gagareader.util.DateProvider;

public class MuluActivity extends Activity{
	private Handler handler;
	private String nid;
	private int cid;
	private String novelName;
	
	private ListView muluListView;
	private List<MuluBean> muluBeanList;
	private Button backButton;
	private TextView titleView;
	private LinearLayout loading;
	private MuluAdapter muluAdapter;
	private boolean isreading;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mulu);
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
	                case 1:
	                	loading.setVisibility(View.GONE);
	                	muluListView.setSelection(cid-1);
	                	muluAdapter.setSelectItem(cid-1);
	                	muluAdapter.notifyDataSetChanged();
	                	break;
	                case 2:
	                	break;
	                default:  
	                    break;  
	                }  
			}
		};
	}
	private void initData(){
		Intent intent = this.getIntent();
		nid = intent.getStringExtra("nid");
		novelName = intent.getStringExtra("novelName");
		isreading = intent.getBooleanExtra("isreading", false);
		if(novelName==null){
			novelName = "Ŀ¼";
		}
		cid = intent.getIntExtra("cid", 1);
		
		muluBeanList = new ArrayList<MuluBean>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				String netState = getNetWorkCate();
				boolean flag = false;
				if(!"none".equals(netState)){
					flag = true;
				}
				try {
					muluBeanList = DateProvider.getInstance().getMuluBeanListByNid(nid, flag);
					sendMessage(1);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	private void initView(){
		backButton = (Button)findViewById(R.id.btn_leftTop);
		titleView = (TextView)findViewById(R.id.tv_head);
		loading = (LinearLayout)findViewById(R.id.bottom_linear);
		titleView = (TextView)findViewById(R.id.tv_head);
		titleView.setText(novelName);
		muluListView = (ListView)findViewById(R.id.indexLv);
		muluAdapter = new MuluAdapter();
		muluListView.setAdapter(muluAdapter);
	}
	private void addListenner(){
		muluListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				muluAdapter.setSelectItem(index);
            	muluAdapter.notifyDataSetChanged();
            	MuluBean muluBean = muluBeanList.get(index);
            	String nid = muluBean.getNid();
            	int cid = muluBean.getCid();
            	Intent intent = new Intent(MuluActivity.this,
						ReaderActivity.class);
				intent.putExtra("nid", nid);
				intent.putExtra("currentChapter", cid+"");
            	if(isreading){
            		setResult(RESULT_OK, intent);
            		MuluActivity.this.finish();
            	}else{
            		startActivity(intent);
            	}
			}
		});
	}
	class MuluAdapter extends BaseAdapter{
		private int selectIndex=0;
		@Override
		public int getCount() {
			return muluBeanList.size();
		}
		@Override
		public Object getItem(int arg0) {
			return muluBeanList.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int index, View conventView, ViewGroup arg2) {
			if(conventView == null){
				conventView = LayoutInflater.from(MuluActivity.this).inflate(R.layout.mulu_item, null);
			}
			LinearLayout selectLiner = (LinearLayout)conventView.findViewById(R.id.select_item);
			if(index==selectIndex){
				selectLiner.setBackgroundColor(Color.RED);
			}else{
				selectLiner.setBackgroundColor(Color.WHITE);
			}
			MuluBean muluBean = muluBeanList.get(index);
			String title = muluBean.getTitle();
			TextView titleTv = (TextView)conventView.findViewById(R.id.muluName);
			titleTv.setText(title);
			return conventView;
		}
		public void setSelectItem(int index){
			selectIndex = index;
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
	
	private void sendMessage(int msgwhat){
		Message message = new Message();  
        message.what = msgwhat;
        handler.sendMessage(message);
	}
}
