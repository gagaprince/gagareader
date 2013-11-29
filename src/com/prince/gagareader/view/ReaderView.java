package com.prince.gagareader.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.prince.gagareader.R;
import com.prince.gagareader.bean.ChapterBean;
import com.prince.gagareader.bean.PrepageBean;
import com.prince.gagareader.bean.StringRulerBean;

public class ReaderView extends View{
	private Bitmap bgBitmap;
	
	private Bitmap topBitmap;	//��ҳʱ�ϲ�ͼƬ
	private Bitmap bottomBitmap;	//��ҳʱ�²�ͼƬ
	private Bitmap realBitmap;		//��ʽ���Ƶ�ͼƬ
	private Canvas topCanvas;
	private Canvas bottomCanvas;
	private Canvas realCanvas;
	
	private Paint textPaint;
	private int viewWidth;
	private int viewHeight;
	private boolean hasMeasured = false;
	private boolean pageClock = false;	//��ҳ��
	private int textSize =24;
	private int marginTop = 5;
	private int marginBottom=15;
	private int marginLeft = 15;
	private int marginRight = 10;
	
	private int currentBegin=0;
	
	private String textData="";
	private ChapterBean currentChapter;
	
	public static final int TRANSFORMLEFT=-1;
	public static final int TRANSFORMINIT=0;
	public static final int TRANSFORMRIGHT=1;
	private float touchStartX=-1;	//��¼touchdownʱ��ָλ��
	private float touchStartY=-1;
	private int transFormDir = TRANSFORMINIT;	//������ҳʱ 
	
	private List<NeedLoadDataListenner> needLoadDataListenners;
	
	
	public ReaderView(Context context) {  
        super(context);  
    }
	
	public ReaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBean();
		initTextPaint();
		initPreDrawListenner();
	}
	private void initBean(){
		needLoadDataListenners = new ArrayList<ReaderView.NeedLoadDataListenner>();
	}
	private void initBackPhoto(){
		bgBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.y1);
		topBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);//������Ļ��С�Ļ�����
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
	 * ��ʼ������
	 */
	private void initTextPaint(){
		textPaint = new Paint();
		textPaint.setTextSize(textSize);
		textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setColor(Color.BLACK);
	}
	/**
	 * ��ʼ��view ��ߵȲ���
	 */
	private void initPreDrawListenner(){
		ViewTreeObserver vto = this.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){  
            public boolean onPreDraw(){
                if (hasMeasured == false){  
                	hasMeasured = true;
                	viewWidth = ReaderView.this.getWidth();
            		viewHeight = ReaderView.this.getHeight();
            		initBackPhoto();
            		Log.e("predraw", textData);
            		if(textData!=null&&!"".equals(textData)){
            			initData();
            		}
                }  
                return true;  
            }  
        });
	}
	@Override
    protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       canvas.drawBitmap(realBitmap,null, new Rect(0, 0, viewWidth, viewHeight), textPaint);
    }
	private void drawBgOnCanvas(Canvas canvas){
		canvas.drawBitmap(bgBitmap,null, new Rect(0, 0, viewWidth, viewHeight), textPaint);
	}
	/**
	 * ���������ڻ����� ������ʼλ��
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
     * ��ȡ����߶�
     */
    private int getFontHeight() {
        FontMetrics fm = textPaint.getFontMetrics();
        return (int)Math.ceil(fm.descent - fm.top) + 2;
    }
	
	public void setTextData(String textData){
		this.textData=textData;
	}
	private void repaint(){
		if(realCanvas!=null)
			realCanvas.drawBitmap(bottomBitmap, 0, 0, textPaint);
		postInvalidate();
	}
	private void preparedTextBitmap(Canvas canvas,int begin){
		if(canvas!=null){
			drawBgOnCanvas(canvas);
			drawTextOnCanvas(canvas, begin);
		}
	}
	public void initData(){
		if(textData==null||"".equals(textData))return;
		if(!hasMeasured)return;
		currentChapter = new ChapterBean(textData, viewWidth-marginLeft-marginRight, viewHeight-marginTop-marginBottom, textSize, getFontHeight(), textPaint);
		preparedTextBitmap(bottomCanvas,currentBegin);
		repaint();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!hasMeasured)return super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStartX = event.getX();
			touchStartY = event.getY();
			return true;
		case MotionEvent.ACTION_MOVE:
			if(touchStartX==-1||touchStartY==-1){
				touchStartX = event.getX();
				touchStartY = event.getY();
			}
			float nowX = event.getX();
			float moveX = nowX-touchStartX;
			if(transFormDir==TRANSFORMINIT){
				if(moveX>10){
					if(currentBegin==0){
						excuteNeedPre();
						return false;
					}
					transFormDir=TRANSFORMRIGHT;
					preparedTextBitmap(topCanvas, currentBegin-1);
					preparedTextBitmap(bottomCanvas, currentBegin);
				}else if(moveX<-10){
					if(currentBegin==currentChapter.getPages().size()-1){
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
					drawAllBitmap((int)moveX-viewHeight);
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
			float upX = event.getX();
			if(transFormDir==TRANSFORMINIT){//ֻ�ǵ�� û���ֶ�����������ҳ
				if(upX<viewWidth/3){
					goPrePage(0);
				}else if(upX>viewWidth-viewWidth/3){
					goNextPage(-viewWidth);
				}else{
					// �����Ļ����
				}
			}else{//�ɻ��������ʹӴ�ʱ���������������
				float upMoveX = upX-touchStartX;
				if(transFormDir==TRANSFORMLEFT){
					if(upMoveX<0){
						goNextPage((int)upMoveX);
					}
				}else{
					if(upMoveX>0){
						goPrePage((int)upMoveX-viewWidth);
					}
				}
				transFormDir = TRANSFORMINIT;
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	/**
	 * �·�ҳ
	 */
	private void goNextPage(final int leftx){
		if(pageClock)return;
		if(currentBegin == currentChapter.getPages().size()-1){
			excuteNeedNext();
			return;
		}
		pageClock = true; //����
		preparedTextBitmap(topCanvas, currentBegin);
		preparedTextBitmap(bottomCanvas, currentBegin+1);
		new Thread(new Runnable() {
			int left=leftx;
			int v=30;
			public void run() {
				while(left>-viewWidth){
					drawAllBitmap(left);
					left-=v;
					v+=2;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						pageClock = false;
						e.printStackTrace();
					}
					if(left<=-viewWidth){
						left=-viewWidth;
						drawAllBitmap(left);
					}
				}
				currentBegin++;
				pageClock = false;
			}
		}).start();
	}
	
	/**
	 * �Ϸ�ҳ
	 */
	private void goPrePage(final int leftx){
		if(pageClock)return;
		if(currentBegin ==0){
			excuteNeedPre();
			return;
		}
		pageClock = true; //����
		preparedTextBitmap(topCanvas, currentBegin-1);
		preparedTextBitmap(bottomCanvas, currentBegin);
		new Thread(new Runnable() {
			int left=leftx;
			int v=30;
			public void run() {
				while(left<0){
					drawAllBitmap(left);
					left+=v;
					if(left>0)left=0;
					v+=2;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						pageClock = false;
						e.printStackTrace();
					}
					if(left>=0){
						left=0;
						drawAllBitmap(left);
					}
				}
				currentBegin--;
				pageClock = false;
			}
		}).start();
	}
	
	private void excuteNeedNext(){
		if(needLoadDataListenners!=null){
			preparedTextBitmap(topCanvas, currentBegin);
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.needNext();
			}
		}
	}
	private void excuteNeedPre(){
		if(needLoadDataListenners!=null){
			preparedTextBitmap(bottomCanvas, currentBegin);
			int size = needLoadDataListenners.size();
			for(int i=0;i<size;i++){
				NeedLoadDataListenner nl = needLoadDataListenners.get(i);
				nl.needPre();
			}
		}
	}
	
	/**
	 * ��������ͼ
	 */
	private void drawAllBitmap(int left){
		realCanvas.drawBitmap(bottomBitmap, 0, 0, textPaint);
		realCanvas.drawBitmap(topBitmap, left, 0, textPaint);
		postInvalidate();
	}
	/**
	 * �������
	 */
	public void addNeedLoadDataListenner(NeedLoadDataListenner nl){
		needLoadDataListenners.add(nl);
	}
	/**
	 * 
	 * ��Ҫ�ⲿʵ�ֵĽӿ�
	 * ���ز�����  ��Ҫ���ö�Ӧ��oncomplete 
	 *
	 */
	public abstract class NeedLoadDataListenner{
		public abstract void needNext();
		public abstract void needPre();
		public final void onNextComplete(){
			preparedTextBitmap(bottomCanvas, currentBegin);
			new Thread(new Runnable() {
				int left=0;
				int v=30;
				public void run() {
					while(left>-viewWidth){
						drawAllBitmap(left);
						left-=v;
						v+=2;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							pageClock = false;
							e.printStackTrace();
						}
						if(left<=-viewWidth){
							left=-viewWidth;
							drawAllBitmap(left);
						}
					}
					pageClock = false;
				}
			}).start();
		}
		public final void onPreComplete(){
			preparedTextBitmap(topCanvas, currentBegin);
			new Thread(new Runnable() {
				int left=-viewWidth;
				int v=30;
				public void run() {
					while(left<0){
						drawAllBitmap(left);
						left+=v;
						v+=2;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							pageClock = false;
							e.printStackTrace();
						}
						if(left>=0){
							left=0;
							drawAllBitmap(left);
						}
					}
					pageClock = false;
				}
			}).start();
		}
	}
}
