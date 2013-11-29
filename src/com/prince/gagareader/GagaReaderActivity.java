package com.prince.gagareader;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.prince.gagareader.util.FileUtil;
import com.prince.gagareader.view.ReaderView;

public class GagaReaderActivity extends Activity {
	private ReaderView readerView;
	private Handler handler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initHandler();
        initView();
        beginReader();
    }
    private void initHandler(){
    	handler = new Handler();
    }
    private void initView(){
    	readerView = (ReaderView) findViewById(R.id.myReaderView);
    }
    private void beginReader(){
    	handler.post(new Runnable() {
			@Override
			public void run() {
				String textContent;
				try {
					textContent = FileUtil.getInstance().getUrlContent("http://tbook.yicha.cn/tb/con.y?at=new_read&nid=1c60587eb2e04298c2695508f268b109&cid=9&type=json");
					Log.e("result", textContent);
					JSONObject textJson = new JSONObject(textContent).getJSONObject("result");
					readerView.setTextData(textJson.getString("content").replaceAll("B#R", "\n    "));
					readerView.initData();
				} catch (IOException e) {
					//ÍøÂçÒì³£
					e.printStackTrace();
				} catch (JSONException e) {
					//½âÎö³ö´í
					e.printStackTrace();
				}
			}
		});
    }
}