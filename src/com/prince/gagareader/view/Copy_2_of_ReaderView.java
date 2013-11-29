package com.prince.gagareader.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.prince.gagareader.R;
import com.prince.gagareader.bean.ChapterBean;
import com.prince.gagareader.bean.PrepageBean;
import com.prince.gagareader.bean.StringRulerBean;
import com.prince.gagareader.util.SharedPreferencesUtil;

public class Copy_2_of_ReaderView extends View{
	private Context context;
	public boolean hasLoadData = false;
	
	private int[] imgs={R.drawable.spring,R.drawable.nightstyle,R.drawable.sky,R.drawable.fenhong,R.drawable.yellow,R.drawable.yanpi,R.drawable.huyan,R.drawable.jiushu};
	private Bitmap bgBitmap;
	
	private Bitmap topBitmap;	//翻页时上层图片
	private Bitmap bottomBitmap;	//翻页时下层图片
	private Bitmap realBitmap;		//正式绘制的图片
	private Canvas topCanvas;
	private Canvas bottomCanvas;
	private Canvas realCanvas;
	
	private Paint textPaint;
	private Paint batterPaint;
	private Paint titlePaint;
	
	private int viewWidth;
	private int viewHeight;
	private boolean hasMeasured = false;
	private boolean pageClock = false;	//翻页动画锁
	private boolean touchMoveClock=false;//手指滑动锁，在按下屏幕时解锁 在需要加载下一张时加锁 并停止move操作
	private boolean loadDataClock = false;	//加载数据锁，在加载数据时 不再触发 excuteNext pre方法 加载完毕后解锁
	private int textSize =24;
	private int marginTop = 25;
	private int marginBottom=25;
	private int marginLeft = 25;
	private int marginRight = 15;
	
	private int currentBegin=0;
	
	private String textData="";
	private ChapterBean currentChapter;
	
	public static final int TRANSFORMLEFT=-1;
	public static final int TRANSFORMINIT=0;
	public static final int TRANSFORMRIGHT=1;
	private float touchStartX=-1;	//记录touchdown时手指位置
	private float touchStartY=-1;
	private int transFormDir = TRANSFORMINIT;	//滑动翻页时 
	
	private List<NeedLoadDataListenner> needLoadDataListenners;
	private OnLoadDataCompleteImpl onLoadDataComplete;
	
	private int yuChapterCount=-1;
	private float batterLevel=-1f;
	private String chapterName;
	
	private boolean drawing = false;
	
	private OnMiddleClick middleClickListenner;
	
	
	
	public Copy_2_of_ReaderView(Context context) {  
        super(context);  
    }
	
	public Copy_2_of_ReaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initBean();
		initTextPaint();
		initPreDrawListenner();
	}
	private void initBean(){
		needLoadDataListenners = new ArrayList<Copy_2_of_ReaderView.NeedLoadDataListenner>();
		onLoadDataComplete = new OnLoadDataCompleteImpl();
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		textSize = su.getContextFontSize(context);
		bgBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), imgs[su.getContextBg(context)]);
	}
	/**
	 * 更新背景
	 * @param bgindex
	 */
	public void updateBg(int bgindex){
		bgBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), imgs[bgindex]);
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		int currentIndex = su.getContextBg(context);
		su.setContextBg(bgindex, context);
		int color = Color.BLACK;
		if(bgindex==1){
			color = Color.WHITE;
			su.setContextLight(currentIndex, context);
		}
		textPaint.setColor(color);
		batterPaint.setColor(color);
		titlePaint.setColor(color);
		preparedTextBitmap(topCanvas, currentBegin);
		drawAllBitmap(0);
		postInvalidate();
	}
	
	public int getJindu(){
		return currentBegin+1;
	}
	public int getAllPage(){
		int allPageSize = currentChapter.getPages().size();
		return allPageSize;
	}
	public void setJindu(int despage){
		if(despage==getAllPage()){
			despage--;
		}
		if(despage<currentBegin){
			goPrePage(-viewWidth,despage);
		}else if(despage==currentBegin){
			
		}else{
			goNextPage(0, despage);
		}
	}
	
	private void initBackPhoto(){
		//bgBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.y1);
		topBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);//创建屏幕大小的缓冲区
		bottomBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		realBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		topCanvas = new Canvas();
		bottomCanvas = new Canvas();
		realCanvas = new Canvas();
		topCanvas.setBitmap(topBitmap);
		bottomCanvas.setBitmap(bottomBitmap);
		realCanvas.setBitmap(realBitmap);
	}
	/**
	 * 初始化画笔
	 */
	private void initTextPaint(){
		SharedPreferencesUtil su = SharedPreferencesUtil.getInstance();
		int color = Color.BLACK;
		if(su.getContextBg(context)==1){
			color = Color.WHITE;
		}
		textPaint = new Paint();
		textPaint.setTextSize(textSize);
		textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setColor(color);
		
		batterPaint = new Paint();
		batterPaint.setTextSize(12);
		batterPaint.setColor(color);
		
		titlePaint = new Paint();
		titlePaint.setTextSize(16);
		titlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setTextAlign(Align.LEFT);
		titlePaint.setColor(color);
	}
	/**
	 * 初始化view 宽高等参数
	 */
	private void initPreDrawListenner(){
		ViewTreeObserver vto = this.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){  
            public boolean onPreDraw(){
                if (hasMeasured == false){  
                	hasMeasured = true;
                	viewWidth = Copy_2_of_ReaderView.this.getWidth();
            		viewHeight = Copy_2_of_ReaderView.this.getHeight();
            		initBackPhoto();
            		excuteNeedReloadCurrent();
            		preparedTextBitmap(topCanvas, currentBegin);
            		drawAllBitmap(0);
                }  
                return true;  
            }  
        });
	}
	@Override
    protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       synchronized (realBitmap) {
    	   canvas.drawBitmap(realBitmap,null, new Rect(0, 0, viewWidth, viewHeight), textPaint);
       }
       if(drawing){
    	   invalidate();
       }
    }
	private void drawBgOnCanvas(Canvas canvas){
		canvas.drawBitmap(bgBitmap,null, new Rect(0, 0, viewWidth, viewHeight), textPaint);
	}
	/**
	 * 绘制文字在画布上 给出开始位置
	 * @param canvas
	 * @param begin
	 */
	private void drawTextOnCanvas(Canvas canvas,int index){
		if(currentChapter!=null){
			PrepageBean page = currentChapter.getPages().get(index);
			String content = currentChapter.getChapterContent();
			List<StringRulerBean> lines = page.getLines();
			int lineHeight = getFontHeight();
			for(int i=0;i<lines.size();i++){
				StringRulerBean line = lines.get(i);
				String text = content.substring(line.getStart(),line.getEnd());
				canvas.drawText(text, marginLeft, marginTop+(i+1)*lineHeight, textPaint);
			}
		}
	}
    /*
     * 获取字体高度
     */
    private int getFontHeight() {
        FontMetrics fm = textPaint.getFontMetrics();
        return (int)Math.ceil(fm.descent - fm.top) + 2;
    }
	
	public void setTextData(String textData){
		this.textData=textData;
	}
	private void preparedTextBitmap(Canvas canvas,int begin){
		if(canvas!=null){
			drawBgOnCanvas(canvas);
			drawTextOnCanvas(canvas, begin);
		}
	}
	/*public void initData(){
		if(textData==null||"".equals(textData))return;
		if(!hasMeasured)return;
		currentChapter = new ChapterBean(textData, viewWidth-marginLeft-marginRight, viewHeight-marginTop-marginBottom, textSize, getFontHeight(), textPaint);
		preparedTextBitmap(bottomCanvas,currentBegin);
		repaint();
	}*/
	
	public void reload(){
		currentChapter = new ChapterBean(textData, viewWidth-marginLeft-marginRight, viewHeight-marginTop-marginBottom, textSize, getFontHeight(), textPaint);
	}
	
	private void beginDraw(){
		drawing=true;
		postInvalidate();
	}
	private void stopDraw(){
		drawing = false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!hasMeasured)return super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStartX = event.getX();
			touchStartY = event.getY();
			touchMoveClock = false;//解锁
			beginDraw();
			return true;
		case MotionEvent.ACTION_MOVE:
			if(touchStartX==-1||touchStartY==-1){
				touchStartX = event.getX();
				touchStartY = event.getY();
			}
			if(touchMoveClock){
				return false;
			}
			float nowX = event.getX();
			float moveX = nowX-touchStartX;
			if(transFormDir==TRANSFORMINIT){
				if(moveX>10){
					if(currentBegin==0){
						touchMoveClock = true;
						excuteNeedPre();
						return false;
					}
					transFormDir=TRANSFORMRIGHT;
					preparedTextBitmap(topCanvas, currentBegin-1);
					preparedTextBitmap(bottomCanvas, currentBegin);
				}else if(moveX<-10){
					if(currentBegin==currentChapter.getPages().size()-1){
						touchMoveClock = true;
						excuteNeedNext();
						return false;
					}
					transFormDir=TRANSFORMLEFT;
					preparedTextBitmap(topCanvas, currentBegin);
					preparedTextBitmap(bottomCanvas, currentBegin+1);
				}else{
					return true;
				}
			}
			if(moveX>0){
				if(transFormDir==TRANSFORMLEFT){
					drawAllBitmap(0);
				}else{
					drawAllBitmap((int)moveX-viewWidth);
				}
			}else{
				if(transFormDir==TRANSFORMLEFT){
					drawAllBitmap((int)moveX);
				}else{
					drawAllBitmap(-viewWidth);
				}
			}
			return true;
		case MotionEvent.ACTION_UP:
			if(!pageClock){
				stopDraw();
			}
			if(touchMoveClock){
				return false;
			}
			float upX = event.getX();
			if(transFormDir==TRANSFORMINIT){//只是点击 没有手动滑动操作翻页
				if(upX<viewWidth/3){
					goPrePage(-viewWidth,-1);
				}else if(upX>viewWidth-viewWidth/3){
					goNextPage(0,-1);
				}else{
					// 点击屏幕中央
					if(middleClickListenner!=null){
						middleClickListenner.onClick();
					}
				}
			}else{//由滑动操作就从此时坐标继续滑动操作
				float upMoveX = upX-touchStartX;
				if(transFormDir==TRANSFORMLEFT){
					if(upMoveX<0){
						goNextPage((int)upMoveX,-1);
					}
				}else{
					if(upMoveX>0){
						goPrePage((int)upMoveX-viewWidth,-1);
					}
				}
				transFormDir = TRANSFORMINIT;
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	/**
	 * 下翻页
	 */
	private void goNextPage(final int leftx,final int despage){
		if(pageClock)return;
		if(currentBegin == currentChapter.getPages().size()-1){
			excuteNeedNext();
			return;
		}
		pageClock = true; //加锁
		preparedTextBitmap(topCanvas, currentBegin);
		if(despage!=-1){
			preparedTextBitmap(bottomCanvas, despage);
		}else{
			preparedTextBitmap(bottomCanvas, currentBegin+1);
		}
		beginDraw();
		new Thread(new Runnable() {
			int left=leftx;
			int v=15;
			public void run() {
				//left-=viewWidth/3;
				if(left<=50-viewWidth){
					left=-viewWidth;
				}
				drawAllBitmap(left);
				while(left>-viewWidth){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						pageClock = false;
						e.printStackTrace();
					}
					left-=v;
					v+=1;
					if(left<=-viewWidth){
						left=-viewWidth;
					}
					drawAllBitmap(left);
				}
				if(despage!=-1){
					currentBegin = despage;
				}else{
					currentBegin++;
				}
				excuteNeedChangeBookMark();
				pageClock = false;
				stopDraw();
			}
		}).start();
	}
	
	/**
	 * 上翻页
	 */
	private void goPrePage(final int leftx,final int despage){
		if(pageClock)return;
		if(currentBegin ==0){
			excuteNeedPre();
			return;
		}
		pageClock = true; //加锁
		if(despage!=-1){
			preparedTextBitmap(topCanvas, despage);
		}else{
			preparedTextBitmap(topCanvas, currentBegin-1);
		}
		preparedTextBitmap(bottomCanvas, currentBegin);
		beginDraw();
		new Thread(new Runnable() {
			int left=leftx;
			int v=15;
			public void run() {
				//left+=viewWidth/2;
				if(left>=0){
					left=0;
				}
				drawAllBitmap(left);
				while(left<0){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						pageClock = false;
						e.printStackTrace();
					}
					left+=v;
					v+=1;
					if(left>=0){
						left=0;
					}
					drawAllBitmap(left);
				}
				if(despage!=-1){
					currentBegin=despage;
				}else{
					currentBegin--;
				}
				excuteNeedChangeBookMark();
				pageClock = false;
				stopDraw();
			}
		}).start();
	}
	
	public void excuteNeedReloadCurrent(){
		if(loadDataClock)return;
		if(needLoadDataListenners!=null){
			loadDataClock = true;
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.reloadCurrent(onLoadDataComplete);
			}
			if(size>0){
				hasLoadData = true;
			}else{
				loadDataClock = false;
			}
		}
	}
	
	private void excuteNeedNext(){
		if(loadDataClock)return;
		if(needLoadDataListenners!=null){
			loadDataClock = true;
			preparedTextBitmap(topCanvas, currentBegin);
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.needNext(onLoadDataComplete);
			}
		}
	}
	private void excuteNeedPre(){
		if(loadDataClock)return;
		if(needLoadDataListenners!=null){
			loadDataClock = true;
			preparedTextBitmap(bottomCanvas, currentBegin);
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.needPre(onLoadDataComplete);
			}
		}
	}
	
	private void excuteNeedChangeBookMark(){
		if(needLoadDataListenners!=null){
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.needChangeBookMark(currentBegin);
			}
		}
	}
	
	/**
	 * 绘制整体图
	 */
	private void drawAllBitmap(int left){
		synchronized (realBitmap) {
			realCanvas.drawBitmap(bottomBitmap, 0, 0, textPaint);
			realCanvas.drawBitmap(topBitmap, left, 0, textPaint);
			drawInfo(23, 12, 30, viewHeight-30);
		}
	}
	
	private void drawInfo(float batterWidth,float batterHeight,float leftBegin,float topBegin){
		drawTitle();
		drawBatter(batterWidth, batterHeight, leftBegin, topBegin);
		drawYuChapter(topBegin+batterHeight);
	}
	private void drawTitle(){
		if(chapterName==null)return;
		realCanvas.drawText(chapterName, 30, 25, titlePaint);
	}
	/**
	 * 绘制电池
	 */
	private void drawBatter(float batterWidth,float batterHeight,float leftBegin,float topBegin){
		batteryLevel();
		if(batterLevel==-1)return;
		batterPaint.setStyle(Paint.Style.STROKE);
		batterPaint.setStrokeWidth(1.5f);
		realCanvas.drawRect(leftBegin, topBegin, leftBegin+batterWidth, topBegin+batterHeight, batterPaint);
		batterPaint.setStyle(Paint.Style.FILL);
		float touleft = leftBegin+batterWidth;
		float toutop = topBegin+batterHeight/4;
		float touright = touleft+4;
		float toubottem = toutop+batterHeight/2;
		realCanvas.drawRect(touleft, toutop, touright, toubottem, batterPaint);
		float battertop = topBegin+1.6f;
		float batterleft = leftBegin+1.6f;
		float batterbottem = topBegin+batterHeight-1.6f;
		float allBatterWidth = batterWidth-3.2f;
		float batterright = batterleft+allBatterWidth*batterLevel;
		realCanvas.drawRect(batterleft, battertop, batterright, batterbottem, batterPaint);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String time = sdf.format(new Date());
		realCanvas.drawText(time, touright+10, topBegin+batterHeight, titlePaint);
	}
	private void drawYuChapter(float top){
		if(yuChapterCount==-1)return;
		String text = "还剩"+yuChapterCount+"章";
		float textwidth = titlePaint.measureText(text);
		realCanvas.drawText(text, viewWidth-textwidth-50, top, titlePaint);
	}
	
	/**
	 * 添加侦听
	 */
	public void addNeedLoadDataListenner(NeedLoadDataListenner nl){
		needLoadDataListenners.add(nl);
	}
	
	public interface OnloadDataComplete{
		public void onNextComplete();
		public void onPreComplete();
		public void onError();
		public void onInitComplete();
	}
	
	/**
	 * 
	 * 需要外部实现的接口
	 * 加载操作后  需要调用对应的oncomplete 
	 *
	 */
	public interface NeedLoadDataListenner{
		public void needNext(OnloadDataComplete call);
		public void reloadCurrent(OnloadDataComplete call);
		public void needPre(OnloadDataComplete call);
		public void needChangeBookMark(int currentBegin);
	}
	
	public class OnLoadDataCompleteImpl implements OnloadDataComplete{
		public final void onNextComplete(){
			currentBegin= 0;
			excuteNeedChangeBookMark();
			goNext();
		}
		public final void onPreComplete(){
			reload();
			loadDataClock = false;
			currentBegin = currentChapter.getPages().size()-1;
			excuteNeedChangeBookMark();
			pageClock = true;
			preparedTextBitmap(topCanvas, currentBegin);
			beginDraw();
			new Thread(new Runnable() {
				int left=-viewWidth;
				int v=15;
				public void run() {
					//left+=viewWidth/2;
					if(left>=0){
						left=0;
						stopDraw();
					}
					drawAllBitmap(left);
					while(left<0){
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							pageClock = false;
							e.printStackTrace();
						}
						left+=v;
						v+=1;
						if(left>=0){
							left=0;
						}
						drawAllBitmap(left);
					}
					pageClock = false;
					stopDraw();
				}
			}).start();
		}
		@Override
		public void onError() {
			loadDataClock = false;
		}
		@Override
		public void onInitComplete() {
			goNext();
		}
		private void goNext(){
			reload();
			loadDataClock = false;
			pageClock = true;
			preparedTextBitmap(bottomCanvas, currentBegin);
			beginDraw();
			new Thread(new Runnable() {
				int left=0;
				int v=15;
				public void run() {
					//left-=viewWidth/2;
					if(left<=-viewWidth){
						left=-viewWidth;
						stopDraw();
					}
					drawAllBitmap(left);
					while(left>-viewWidth){
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							pageClock = false;
							e.printStackTrace();
						}
						left-=v;
						v+=1;
						if(left<=50-viewWidth){
							left=-viewWidth;
						}
						drawAllBitmap(left);
					}
					pageClock = false;
					stopDraw();
				}
			}).start();
		}
	}

	public int getCurrentBegin() {
		return currentBegin;
	}

	public void setCurrentBegin(int currentBegin) {
		this.currentBegin = currentBegin;
	}

	public int getYuChapterCount() {
		return yuChapterCount;
	}

	public void setYuChapterCount(int yuChapterCount) {
		this.yuChapterCount = yuChapterCount;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}
	
	private void batteryLevel() { 
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() { 
            public void onReceive(Context context, Intent intent) { 
                context.unregisterReceiver(this); 
                int rawlevel = intent.getIntExtra("level", -1);//获得当前电量 
                int scale = intent.getIntExtra("scale", -1); 
                float level = -1; 
                if (rawlevel >= 0 && scale > 0) { 
                    level = (float)rawlevel/ scale; 
                    batterLevel = level;
                } 
            } 
        }; 
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); 
        context.registerReceiver(batteryLevelReceiver, batteryLevelFilter); 
    }
	
	public void resizeFontSize(int fontsize){
		textSize = fontsize;
		textPaint.setTextSize(textSize);
		int allpageCount = getAllPage();
		int currentPage = currentBegin;
		reload();
		int nowAllpageCount = getAllPage();
		currentBegin = currentPage*nowAllpageCount/allpageCount;
		loadDataClock = false;
		excuteNeedChangeBookMark();
		pageClock = true;
		preparedTextBitmap(bottomCanvas, currentBegin);
		beginDraw();
		new Thread(new Runnable() {
			int left=0;
			int v=15;
			public void run() {
				//left-=viewWidth/2;
				if(left<=-viewWidth){
					left=-viewWidth;
					stopDraw();
				}
				drawAllBitmap(left);
				while(left>-viewWidth){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						pageClock = false;
						e.printStackTrace();
					}
					left-=v;
					v+=1;
					if(left<=50-viewWidth){
						left=-viewWidth;
					}
					drawAllBitmap(left);
				}
				pageClock = false;
				stopDraw();
			}
		}).start();
	}
	
	public interface OnMiddleClick{
		public void onClick();
	}
	public void setOnMiddleClick(OnMiddleClick middleClickListenner){
		this.middleClickListenner = middleClickListenner;
	}
}
