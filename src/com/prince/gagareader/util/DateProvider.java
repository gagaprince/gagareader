package com.prince.gagareader.util;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.prince.gagareader.bean.BookBean;
import com.prince.gagareader.bean.CateBean;
import com.prince.gagareader.bean.Const;
import com.prince.gagareader.bean.HotwordBean;
import com.prince.gagareader.bean.MuluBean;
import com.prince.gagareader.bean.ShowItemBean;

public class DateProvider {
	private static DateProvider self;
	public static DateProvider getInstance(){
		if(self==null){
			self = new DateProvider();
		}
		return self;
	}
	private DateProvider(){}
	
	public BookBean getBookBeanByNid(String nid) throws IOException, JSONException{
		FileUtil fu = FileUtil.getInstance();
		String bookUrl = Const.NOVEL_INFO_URL+nid;
		String bookStr = fu.getUrlContent(bookUrl);
		JSONObject resultJson = new JSONObject(bookStr);
		JSONObject bookJson = resultJson.getJSONArray("result").getJSONObject(0);
		String author = bookJson.getString("author");
		int chapterCount = bookJson.getInt("chapterCount");
		String description = bookJson.getString("description");
		String image = bookJson.getString("image");
		String name = bookJson.getString("name");
		String lastChapter = bookJson.getString("lastChapter");
		String type = bookJson.getString("type");
		if("".equals(image)){
			image = "http://tbook.yicha.cn/timg.html?name="+URLEncoder.encode(name)+"&author="+URLEncoder.encode(author)+"&type="+URLEncoder.encode(type);
		}
		int state = bookJson.getInt("state");
		BookBean bookBean = new BookBean(nid, name, image, author, description, lastChapter, state, chapterCount);
		return bookBean;
	}
	
	public void getSBKKdata() throws IOException{
		FileUtil fileUtil = FileUtil.getInstance();
		String cachePath = Const.APP_TEXT_CACHE+"/sbkk";
		try {
			fileUtil.saveUrlContentToFile(Const.NOVEL_BD_URL, cachePath);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * 获取随便看看内容json
	 * @param getFromNet
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<ShowItemBean> getShowItemBeanList(boolean getFromNet) throws IOException, JSONException{
		String cachePath = Const.APP_TEXT_CACHE+"/sbkk";
		FileUtil fileUtil = FileUtil.getInstance();
		File cacheFile = new File(cachePath);
		boolean updateFlag = true;
		if(!cacheFile.exists()){
			getSBKKdata();
			updateFlag = false;
		}
		if(updateFlag&&getFromNet){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						getSBKKdata();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		String bd_str = fileUtil.getFileContent(cacheFile);
		return parseIndexDate(bd_str);
	}
	
	public List<ShowItemBean> parseIndexDate(String bd_str) throws JSONException{
		List<ShowItemBean> itemDateList = new ArrayList<ShowItemBean>();
		JSONObject bd_obj = new JSONObject(bd_str);
		JSONObject bout_obj = bd_obj.getJSONObject("bout");
		JSONObject boy_obj = bd_obj.getJSONObject("boy");
		JSONObject girl_obj = bd_obj.getJSONObject("girl");
		JSONObject new_obj = bd_obj.getJSONObject("new");
		JSONObject hot_obj = bd_obj.getJSONObject("hot");
		JSONObject cbout_obj = bd_obj.getJSONObject("cbout");
	
		itemDateList.add(new ShowItemBean("精品推荐"));
		JSONArray bout_array = bout_obj.getJSONArray("items");
		parseSingelArray(bout_array, itemDateList);
		
		itemDateList.add(new ShowItemBean("男生最爱"));
		List<CateBean> cates = new ArrayList<CateBean>();
		cates.add(new CateBean("都市"));
		cates.add(new CateBean("玄幻"));
		cates.add(new CateBean("仙侠"));
		cates.add(new CateBean("武侠"));
		cates.add(new CateBean("游戏"));
		itemDateList.add(new ShowItemBean(cates, 0));
		JSONArray boy_array = boy_obj.getJSONArray("items");
		parseSingelArray(boy_array, itemDateList);
		
		itemDateList.add(new ShowItemBean("女生最爱"));
		cates = new ArrayList<CateBean>();
		cates.add(new CateBean("言情"));
		cates.add(new CateBean("穿越"));
		cates.add(new CateBean("青春"));
		cates.add(new CateBean("历史"));
		cates.add(new CateBean("奇幻"));
		itemDateList.add(new ShowItemBean(cates, 0));
		JSONArray girl_array = girl_obj.getJSONArray("items");
		parseSingelArray(girl_array, itemDateList);
		
		itemDateList.add(new ShowItemBean("传统文学"));
		JSONArray cbout_array = cbout_obj.getJSONArray("items");
		parseSingelArray(cbout_array, itemDateList);
		
		itemDateList.add(new ShowItemBean("新书速递"));
		cates = new ArrayList<CateBean>();
		cates.add(new CateBean("都市","现代都市"));
		cates.add(new CateBean("青春","青春文学"));
		cates.add(new CateBean("传记","传记纪实"));
		cates.add(new CateBean("爱情","浪漫爱情"));
		cates.add(new CateBean("随笔","随笔散文"));
		itemDateList.add(new ShowItemBean(cates, 0));
		JSONArray new_array = new_obj.getJSONArray("items");
		parseSingelArray(new_array, itemDateList);
		
		itemDateList.add(new ShowItemBean("畅销图书"));
		cates = new ArrayList<CateBean>();
		cates.add(new CateBean("经典","古代经典"));
		cates.add(new CateBean("恐怖","恐怖灵异"));
		cates.add(new CateBean("影视","影视名著"));
		cates.add(new CateBean("励志","职场励志"));
		cates.add(new CateBean("历史","历史军事"));
		itemDateList.add(new ShowItemBean(cates, 0));
		JSONArray hot_array = hot_obj.getJSONArray("items");
		parseSingelArray(hot_array, itemDateList);
		return itemDateList;
	}
	
	private void parseSingelArray(JSONArray array,List<ShowItemBean> itemDateList) throws JSONException{
		int i=0;
		List<BookBean> bookList = new ArrayList<BookBean>();
		for(i=0;i<3&&i<array.length();i++){
			JSONObject item = array.getJSONObject(i);
			String nid = item.getString("nid");
			String name = item.getString("name");
			String author = item.getString("author");
			String image = item.getString("image");
			BookBean bookBean = new BookBean(nid, name, image, author);
			bookList.add(bookBean);
		}
		itemDateList.add(new ShowItemBean(bookList));
		for(;i<array.length();i++){
			JSONObject item = array.getJSONObject(i);
			String nid = item.getString("nid");
			String name = item.getString("name");
			String author = item.getString("author");
			String image = item.getString("image");
			String des = item.getString("showName");
			BookBean bookBean = new BookBean(nid, name, image, author,des);
			itemDateList.add(new ShowItemBean(bookBean));
		}
	}
	public List<MuluBean> getMuluBeanListByNid(String nid,boolean getFromNet) throws IOException, JSONException{
		final FileUtil fu = FileUtil.getInstance();
		final String muluUrl = Const.NOVEL_INDEX_URL+"&nid="+nid+"&pno=0&psize=100000";
		final String filePath = Const.APP_TEXT_CACHE+"/"+nid+"/mulu";
		File muluFile = new File(filePath);
		if(getFromNet){
			fu.saveUrlContentToFile(muluUrl, filePath);
		}
		if(muluFile.exists()){
			List<MuluBean> muluBeanList = new ArrayList<MuluBean>();
			String muluStr = fu.getFileContent(muluFile);
			JSONObject muluObj = new JSONObject(muluStr);
			JSONArray muluArray = muluObj.getJSONArray("result");
			int length = muluArray.length();
			for(int i=0;i<length;i++){
				JSONObject muluOne = muluArray.getJSONObject(i);
				String title = muluOne.getString("chapterTitle");
				String nidstr = muluOne.getString("nid");
				int cid = Integer.parseInt(muluOne.getString("cid"));
				MuluBean muluBean = new MuluBean(nidstr, cid, title);
				muluBeanList.add(muluBean);
			}
			return muluBeanList;
		}else{
			return null;
		}
	}
	public List<CateBean> getAllPhbBeanList(Context context){
		FileUtil fu = FileUtil.getInstance();
		List<String> phbKeyList = fu.getAssetFileContentList(context,"phb.key");
		List<CateBean> cateBeanList = new ArrayList<CateBean>();
		int size = phbKeyList.size();
		for(int i=0;i<size;i++){
			cateBeanList.add(new CateBean(phbKeyList.get(i)));
		}
		return cateBeanList;
	}
	/**
	 * 获取标签
	 * @param url
	 * @param param
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<CateBean> getAllTagBeanList(String url,String param) throws IOException, JSONException{
		FileUtil fu = FileUtil.getInstance();
		String cateBeanListResult = fu.getUrlContent(url);
		List<CateBean> cateBeanList = new ArrayList<CateBean>();
		JSONObject cateBean = new JSONObject(cateBeanListResult);
		JSONArray cateItems = cateBean.getJSONArray("items");
		int length = cateItems.length();
		JSONObject cateTagItem=null;
		for(int i=0;i<length;i++){
			JSONObject cateItem = cateItems.getJSONObject(i);
			String tagname = cateItem.getString("tagName");
			if(param.equals(tagname)){
				cateTagItem = cateItem;
			}
		}
		if(cateTagItem==null){
			return null;
		}
		JSONArray cateArray = cateTagItem.getJSONArray("items");
		length = cateArray.length();
		for(int i=0;i<length;i++){
			String tagCate = cateArray.getString(i);
			CateBean cateBeanTemp = new CateBean(tagCate);
			cateBeanList.add(cateBeanTemp);
		}
		return cateBeanList;
	}
	
	/**
	 * 获取所有分类
	 * @param url
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public List<CateBean> getAllCateBeanList(String url) throws IOException, JSONException{
		FileUtil fu = FileUtil.getInstance();
		String cateBeanListResult = fu.getUrlContent(url);
		List<CateBean> cateBeanList = new ArrayList<CateBean>();
		JSONArray cateArray = new JSONArray(cateBeanListResult);
		int length = cateArray.length();
		for(int i=0;i<length;i++){
			JSONObject cateBeanJson = cateArray.getJSONObject(i);
			String cateName = cateBeanJson.getString("name");
			String img = cateBeanJson.getString("imgUrl");
			if(img!=null){
				img = "http://tbook.yicha.cn/cmsclient/data/img/"+img;
			}
			CateBean cateBean = new CateBean(cateName,cateName,img);
			cateBeanList.add(cateBean);
		}
		return cateBeanList;
	}
	
	/**
	 * 获取一个分类下的书籍列表
	 * @param cate
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public List<BookBean> getCateListJson(String cate,int pno,int cateType) throws IOException, JSONException{
		String bdUrl = "";
		if(cateType==0){
			bdUrl = Const.NOVEL_BD_CATE+"&cate="+URLEncoder.encode(cate)+"&pno="+pno;
		}else if(cateType==1){
			bdUrl = Const.NOVEL_TBD_CATE+"&cate="+URLEncoder.encode(cate)+"&pno="+pno;
		}else if(cateType==2){
			bdUrl = Const.NOVEL_TAG_CATE+"&cate="+URLEncoder.encode(cate)+"&pno="+pno;
		}else{
			return getCateListFromPhb(cate,pno,cateType);
		}
		FileUtil fu = FileUtil.getInstance();
		String bdResult = fu.getUrlContent(bdUrl);
		List<BookBean> bookBeanList=new ArrayList<BookBean>();
		if(bdResult.equals("[\"no result\"]"))return bookBeanList;
		JSONObject bdObj = new JSONObject(bdResult);
		JSONArray bdArray = bdObj.getJSONArray("record");
		int length = bdArray.length();
		for(int i=0;i<length;i++){
			JSONObject oneBook = bdArray.getJSONObject(i);
			String author = oneBook.has("author")?oneBook.getString("author"):"未知";
			String imgurl = oneBook.has("imgUrl")?oneBook.getString("imgUrl"):"";
			String resource_type = oneBook.has("resource_type")?oneBook.getString("resource_type"):"暂无分类";
			String novelName = oneBook.has("resource_name")?oneBook.getString("resource_name"):"暂无名字";
			if("".equals(imgurl)){
				imgurl = "http://tbook.yicha.cn/timg.html?name="+URLEncoder.encode(novelName)+"&author="+URLEncoder.encode(author)+"&type="+URLEncoder.encode(resource_type);
			}
			BookBean bookBean = new BookBean(imgurl, novelName, author, resource_type,1);
			bookBeanList.add(bookBean);
		}
		return bookBeanList;
	}
	/**
	 * 获取风云榜数据
	 * @param cate
	 * @param pno
	 * @param cateType
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<BookBean> getCateListFromPhb(String cate,int pno,int cateType) throws IOException, JSONException{
		String bdUrl = Const.NOVEL_PHB_CATE+"&cate="+URLEncoder.encode(cate)+"&pno="+pno;
		FileUtil fu = FileUtil.getInstance();
		String bdResult = fu.getUrlContent(bdUrl);
		List<BookBean> bookBeanList=new ArrayList<BookBean>();
		if(bdResult.equals("[\"no result\"]"))return bookBeanList;
		JSONObject bdObj = new JSONObject(bdResult);
		JSONArray bdArray = bdObj.getJSONArray("records");
		int length = bdArray.length();
		for(int i=0;i<length;i++){
			JSONObject oneBook = bdArray.getJSONObject(i);
			String author = oneBook.has("author")?oneBook.getString("author"):"未知";
			String imgurl = oneBook.has("img")?oneBook.getString("img"):"";
			String resource_type = oneBook.has("type")?oneBook.getString("type"):"暂无分类";
			String novelName = oneBook.has("name")?oneBook.getString("name"):"暂无名字";
			if("".equals(imgurl)){
				imgurl = "http://tbook.yicha.cn/timg.html?name="+URLEncoder.encode(novelName)+"&author="+URLEncoder.encode(author)+"&type="+URLEncoder.encode(resource_type);
			}
			BookBean bookBean = new BookBean(imgurl, novelName, author, resource_type,1);
			bookBeanList.add(bookBean);
		}
		return bookBeanList;
	}
	public List<BookBean> getResultJson(String key,int pno) throws IOException, JSONException{
		String searchUrl = Const.NOVEL_SEARCH_URL+"key="+URLEncoder.encode(key)+"&pno="+pno;
		FileUtil fu = FileUtil.getInstance();
		String searchResult = fu.getUrlContent(searchUrl);
		JSONObject resultJson = new JSONObject(searchResult);
		boolean iscontain = searchResult.contains("haveResult");
		if(!iscontain){
			List<BookBean> resultList = new ArrayList<BookBean>();
			JSONArray resultJsonArray = resultJson.getJSONArray("result");
			int length = resultJsonArray.length();
			for(int i=0;i<length;i++){
				JSONObject oneBookJson = resultJsonArray.getJSONObject(i);
				String author = oneBookJson.getString("author");
				String cate = oneBookJson.getString("type");
				String novelName = oneBookJson.getString("name");
				String imgUrl = oneBookJson.getString("imgUrl");
				if(imgUrl==null||"".equals(imgUrl)){
					imgUrl = "http://tbook.yicha.cn/timg.html?name="+URLEncoder.encode(novelName)+"&author="+URLEncoder.encode(author)+"&type="+URLEncoder.encode(cate);
				}
				JSONArray oneBooks = oneBookJson.getJSONArray("tSources");
				int bookLength = oneBooks.length();
				for(int j=0;j<bookLength;j++){
					JSONObject oneBookDetail = oneBooks.getJSONObject(j);
					int state = oneBookDetail.getInt("state");
					String nid = oneBookDetail.getString("nid");
					String sourceSite = oneBookDetail.getString("sourceSite");
					String lastChapter = oneBookDetail.getString("lastChapter");
					BookBean bookBean = new BookBean(nid,novelName,imgUrl,author,cate,lastChapter,sourceSite,state);
					resultList.add(bookBean);
				}
			}
			return resultList;
		}else{
			return null;
		}
	}
	public List<HotwordBean> getHotwordBeanList() throws IOException, JSONException{
		String hotwordUrl = Const.NOVEL_HOT_URL;
		FileUtil fu = FileUtil.getInstance();
		String hotResult = fu.getUrlContent(hotwordUrl);
		JSONObject hotJson = new JSONObject(hotResult);
		List<HotwordBean> hotBeanList = new ArrayList<HotwordBean>();
		JSONArray hotJsonArray = hotJson.getJSONArray("record");
		int length = hotJsonArray.length();
		for(int i=0;i<length;i++){
			String leftWord = hotJsonArray.getString(i++);
			if(i>=length){
				break;
			}
			String rigthWord = hotJsonArray.getString(i);
			HotwordBean hotwordBean = new HotwordBean(leftWord, rigthWord);
			hotBeanList.add(hotwordBean);
		}
		return hotBeanList;
	}
}
