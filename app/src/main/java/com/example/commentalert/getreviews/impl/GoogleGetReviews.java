package com.example.commentalert.getreviews.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.pojava.datetime.DateTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.example.commentalert.getreviews.GetReviews;
import com.example.commentalert.getreviews.pojo.BaseEntry;
import com.example.commentalert.util.Constants;


public class GoogleGetReviews implements GetReviews {

	
	private Properties androidParams = null;
	private String androidParamsPath = "properties/params.properties";
	private String postURL;
	private Context context;
	private String reviewType;
	private String fromPageNum;
	private String ID;
	private String reviewSortOrder;
	private String XHR;
	private String outputFilePath;
	HttpClient httpClient = new DefaultHttpClient();
	private String cookies;
	private final String USER_AGENT = "Mozilla/5.0";
	String loginUrl ;
	private boolean useProxy;
	private boolean enablePeriod;
	private SimpleDateFormat fmt;
	private String dateFormat;
	private Date startDate;
	private String lastEntryAuthorId;
	private int continueAttempts = 3;
	private int currentCount = 0;
	private int bufferCount;
	private int bufferShow;
	private boolean canReturn;
	private SharedPreferences prefs;
	public GoogleGetReviews(Context context) {
		super();
		this.context = context;
		this.androidParams = new Properties();
		try {
			AssetManager am = context.getAssets();
			InputStream inputStream = am.open(androidParamsPath);
//			FileInputStream temp = new FileInputStream(iphoneParamsPath);
			androidParams.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initParams();
		//loginAndSaveCookie();
		prefs = context.getSharedPreferences("com.example.commentalert", Context.MODE_PRIVATE);
	}

	private void initParams(){
		if(!androidParams.isEmpty()){
			postURL = androidParams.getProperty("GOOGLEPLAY_POST_URL");
			reviewType = androidParams.getProperty("GOOGLEPLAY_REVIEW_TYPE");
			fromPageNum = androidParams.getProperty("GOOGLEPLAY_FROM_PAGE_NUM");
			ID = androidParams.getProperty("GOOGLEPLAY_ID");
			reviewSortOrder = androidParams.getProperty("GOOGLEPLAY_REVIEW_SORT_ORDER");
			XHR = androidParams.getProperty("GOOGLEPLAY_XHR");
			outputFilePath = androidParams.getProperty("GOOGLEPLAY_OUTPUTFILE_PATH");
			bufferCount = Integer.valueOf(androidParams.getProperty("GOOGLEPLAY_BUFFER_COUNT"));
			bufferShow = Integer.valueOf(androidParams.getProperty("GOOGLEPLAY_BUFFER_SHOW"));
			enablePeriod = Boolean.valueOf(androidParams.getProperty("GOOGLEPLAY_ENABLE_PERIOD"));
			//if(this.enablePeriod){
				dateFormat = androidParams.getProperty("GOOGLEPLAY_DATE_FORMAT");
				String startDateStr = androidParams.getProperty("GOOGLEPLAY_START_DATE");
				String endDateStr = androidParams.getProperty("GOOGLEPLAY_END_DATE");
				if(!("".equals(dateFormat) || "".equals(startDateStr) || "".equals(endDateStr))){
					try {
						//fmt = new SimpleDateFormat(dateFormat);
						DateTime dt1=new DateTime(startDateStr);
						startDate = dt1.toDate();
					} catch (Exception e) {
						this.enablePeriod = false;
						e.printStackTrace();
					}
				}else{
					this.enablePeriod = false;
				}
				
			//}
			loginUrl = androidParams.getProperty("GOOGLEPLAY_LOGIN_URL");
		}
	}
	
	/*private void loginAndSaveCookie(){
		HttpPost loginpost = new HttpPost(loginUrl);
        List<NameValuePair> params = null;
		try {
			 String html = this.GetPageContent(loginUrl);
			params = getFormParams(html,"htsdeploy@gmail.com","hsbc123456");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
//        params.add(new BasicNameValuePair("Email", "select.zhenyu.from@gmail.com"));   
//        params.add(new BasicNameValuePair("Passwd", "scholes616"));   
//        params.add(new BasicNameValuePair("PersistentCookie", "yes"));
//        params.add(new BasicNameValuePair("service", "googleplay"));
//        params.add(new BasicNameValuePair("continue", "https://play.google.com/store"));
     // add header
		
        loginpost.setHeader("Host", "accounts.google.com");
        loginpost.setHeader("User-Agent", USER_AGENT);
        loginpost.setHeader("Accept", 
             "text/html,application/xhtml+xml,application/xml;q=0.9,*;q=0.8");
        loginpost.setHeader("Accept-Language", "en-US,en;q=0.5");
        loginpost.setHeader("Cookie", getCookies());
        loginpost.setHeader("Connection", "keep-alive");
        loginpost.setHeader("Referer", "https://accounts.google.com/ServiceLoginAuth");
        loginpost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {   
        	loginpost.setEntity(new UrlEncodedFormEntity(params));   
        	HttpResponse hr = httpClient.execute(loginpost);
        	System.out.println(hr);
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace();   
        } catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			loginpost.releaseConnection();
		}
	}*/

	private JSONArray convertTOJSONOBject(String str) throws JSONException{
		str = str.substring(str.indexOf("["));
		JSONArray array = new JSONArray(str);
		return array;
	}
	@Override
	public List exportOneCountryData(String ctyCode) throws Exception {
		// TODO Auto-generated method stub
		return exportAllData();
	}
	
	@Override
	public List exportAllData() throws Exception{
		canReturn = false;
		currentCount = 0;// init to zero
		String rawData = "";
		//ExcelExport<Entry> excelExport = new ExcelExport<Entry>(outputFilePath);
		List<Entry> allEntries = new ArrayList(0);
		int pageNum = Integer.valueOf(fromPageNum);
		String locale = "GooglePlay";
		String tempDate = prefs.getString(locale+"_"+Constants.LATESTDATE, "");
		String lastUpdateAuthorId = prefs.getString(locale+"_"+Constants.LATESTAUTHORID, "");
		Date lastUpdateDate = null;
		if(!"".equals(tempDate)){
			//lastUpdateDate = fmt.parse(tempDate);
			DateTime dt1 = null;
			try {
				dt1 = new DateTime(tempDate);
			} catch (Exception e) {
				System.err.println("Found Chinese date format : "+tempDate);
				dt1 = new DateTime(string2Unicode(tempDate));
			}
			lastUpdateDate = dt1.toDate();
		}
		while(!"".equals(rawData = rawReviewsData(pageNum)) && !canReturn){
			if(currentCount >= bufferCount) break;
			List<Entry> entries = readEntries(rawData,lastUpdateDate,lastUpdateAuthorId);
			if(entries.size() == 0){
				break;
			}
			if(pageNum == 0){//save the latest date for this country
				System.out.println("pageNum: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+pageNum);
				try {
					prefs.edit().putString(locale+"_"+Constants.LATESTDATE, entries.get(0).getReviewDate()).apply();
					prefs.edit().putString(locale+"_"+Constants.LATESTAUTHORID, entries.get(0).getAuthorId()).apply();
				} catch (Exception e) {
					prefs.edit().putString(locale+"_"+Constants.LATESTDATE, "").apply();
					e.printStackTrace();
				}
				
			}
			allEntries.addAll(entries);
			System.out.println(entries);
			System.out.println("Page : "+pageNum);
			pageNum++;
		}
		return allEntries;
	}
	
	
	@Override
	public String rawReviewsData(int pageNum) {
		HttpPost httppost = null;
		String responseBody = "";
		try {
			StringBuffer sb = new StringBuffer(postURL);
			sb.append("?reviewType="+reviewType)
			.append("&pageNum="+pageNum)
			.append("&id="+ID)
			.append("&reviewSortOrder="+reviewSortOrder)
			.append("&xhr="+XHR);
			
			ResponseHandler<String> responseHandler = new ResponseHandler<String>(){
				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if(status >= 200 && status <300){
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity,"UTF-8") : null;
					}else{
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			if(useProxy){
				/*String pacUrl = androidParams.getProperty("PROXY_PAC");
				//px = ProxyUtil.getProxyInfoFromPAC(pacUrl, sb.toString());
				String username = androidParams.getProperty("PROXY_USER");
				String password = androidParams.getProperty("PROXY_PASSWORD");
				String proxyIp = androidParams.getProperty("PROXY_IP");
				int proxyPort = Integer.valueOf(androidParams.getProperty("PROXY_PORT"));
				if(sb.toString().startsWith("http://")){
					sb = sb.delete(0, 7);
				}else if(sb.toString().startsWith("https://")){
					sb = sb.delete(0, 8);
				}
				HttpHost target = new HttpHost(sb.toString(), 80, "http");
		        HttpHost proxy = new HttpHost(proxyIp, proxyPort);
		        
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
		        credsProvider.setCredentials(
		                new AuthScope(proxyIp, proxyPort),
		                new UsernamePasswordCredentials(username, password));
		        CloseableHttpClient httpclient = HttpClients.custom()
		                .setDefaultCredentialsProvider(credsProvider).build();
		        
		        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
		        int startIndexOfRelativeAddress = sb.indexOf("/");
	            HttpGet httpget = new HttpGet(sb.substring(startIndexOfRelativeAddress));
	            httpget.setConfig(config);
//	            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	            System.out.println("Executing request with Proxy " + httpget.getRequestLine() + " to " + target + " via " + proxy);
	            responseBody = httpclient.execute(target, httpget, responseHandler);*/
			}else{
				HttpClient httpClient = new DefaultHttpClient();
				httppost = new HttpPost(sb.toString());
				System.out.println("executing request "+httppost.getURI());
				responseBody = httpClient.execute(httppost, responseHandler);
			}

			//handle json
			JSONArray jsonObj = convertTOJSONOBject(responseBody);
			if(jsonObj.getJSONArray(0).length() > 2){
				String obj = jsonObj.getJSONArray(0).getString(2);
				System.out.println("---------------------------------");
				System.out.println(obj);
				System.out.println("---------------------------------");
				return obj;
			}else{
				return "";
			}
		} catch (Exception e) {
			if(e instanceof ClientProtocolException){
				System.out.println(e.getMessage() +" : End Page");
			}else{
				e.printStackTrace();
			}
		}/*finally{
			if(httppost != null){
				httppost.releaseConnection();
			}
		}*/
		return "";
	}
	
	
	private List<Entry> readEntries(String obj, Date lastUpdateDate , String lastUpdateAuthorId) throws UnsupportedEncodingException{
		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();
		 
		// take default cleaner properties
		CleanerProperties props = cleaner.getProperties();
		 
		// customize cleaner's behaviour with property setters
		props.setOmitDoctypeDeclaration(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		 
		// Clean HTML taken from simple string, file, URL, input stream, 
		// input source or reader. Result is root node of created 
		// tree-like structure. Single cleaner instance may be safely used
		// multiple times.
		TagNode node = cleaner.clean(obj);
		List<Entry> entriesList = new ArrayList<Entry>(0);
		TagNode[]  entryNodes = node.getElementsByAttValue("class", "single-review", false, false);
		if(entryNodes != null && entryNodes.length > 0){
			
			for(TagNode singleReview : entryNodes){
				if(currentCount >= bufferCount) break;
				String authorId = "";
				String authorName;
				String reviewDate;
				String starRating;
				String reivewTitle;
				String reviewBody;
				TagNode[] authorNames = singleReview.getElementsByAttValue("class", "author-name", true, false);
				authorName = authorNames[0].getText().toString();
				
				try {
					authorId = (singleReview.getElementsHavingAttribute("data-userid", false))[0].getAttributeByName("data-userid");
				} catch (Exception e) {
					System.out.println("It is Google User , so no user id : "+authorName);
					//<a href="/store/people/details?id=100438274427121954519">subhamay sur</a>
					try {
						TagNode[] nodes = authorNames[0].getElementsHavingAttribute("href", true);
						String href = nodes[0].getAttributeByName("href");
						authorId = href.substring(href.indexOf("id=")+3);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				TagNode[] reviewDates = singleReview.getElementsByAttValue("class", "review-date", true, false);
				reviewDate = reviewDates[0].getText().toString();
				//check periods
				if(!"".equals(reviewDate)){
					try {//orginal July 25, 2014 , to 25Jul2014
						String dateStr = reviewDate.trim();
						//String temp = string2Unicode(dateStr);
						//Date reviewDate2 = fmt.parse(temp);
						DateTime dt1 = null;
						try {
							dt1 = new DateTime(dateStr);
						} catch (Exception e) {
							System.err.println("Found Chinese date format : "+dateStr);
							dt1 = new DateTime(string2Unicode(dateStr));
						}
						Date reviewDate2 = dt1.toDate();
						if(lastUpdateDate !=null && reviewDate2.before(lastUpdateDate)){
							canReturn = true;
							break;
						}else if(lastUpdateDate !=null && !reviewDate2.after(lastUpdateDate)){
							if(authorId.equalsIgnoreCase(lastUpdateAuthorId)){
								canReturn = true;
								break;
							}
						}else if(enablePeriod && reviewDate2.before(startDate)){
							canReturn = true;
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				
				
				//check periods
				/*if(!"".equals(reviewDate)){
					try {
						String dateStr = reviewDate.substring(0, this.dateFormat.length());
						Date reviewDate2 = fmt.parse(dateStr);
						if(lastUpdatedDate !=null && !reviewDate2.after(lastUpdatedDate)){
							break;
						}else if(enablePeriod && reviewDate2.before(this.startDate)){
							break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
				}*/
				
				
				TagNode[] starRatings = singleReview.getElementsByAttValue("class", "current-rating", true, false);
				starRating = starRatings[0].getAttributeByName("style");
				starRating = convertStarRatings(starRating);
				TagNode[] reviewBodys = singleReview.getElementsByAttValue("class", "review-body", true, false);
				reviewBody = reviewBodys[0].getText().toString();
				
				reivewTitle = reviewBodys[0].getElementsByAttValue("class", "review-title", false, false)[0].getText().toString();
				entriesList.add(new Entry(authorId,authorName,reviewDate,starRating,reivewTitle,reviewBody));
				currentCount++;
				System.out.println("Current Count : "+currentCount);
			}
		}
		return entriesList;
	}
	
	/*private List<Entry> readEntries(String obj) throws UnsupportedEncodingException{
		// create an instance of HtmlCleaner
		HtmlCleaner cleaner = new HtmlCleaner();
		 
		// take default cleaner properties
		CleanerProperties props = cleaner.getProperties();
		 
		// customize cleaner's behaviour with property setters
		props.setOmitDoctypeDeclaration(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitXmlDeclaration(true);
		 
		// Clean HTML taken from simple string, file, URL, input stream, 
		// input source or reader. Result is root node of created 
		// tree-like structure. Single cleaner instance may be safely used
		// multiple times.
		TagNode node = cleaner.clean(obj);
		List<Entry> entriesList = new ArrayList<Entry>(0);
		TagNode[]  entryNodes = node.getElementsByAttValue("class", "single-review", false, false);
		if(entryNodes != null && entryNodes.length > 0){
			for(TagNode singleReview : entryNodes){
				String authorId = "";
				String authorName;
				String reviewDate;
				String starRating;
				String reivewTitle;
				String reviewBody;
				TagNode[] authorNames = singleReview.getElementsByAttValue("class", "author-name", true, false);
				authorName = authorNames[0].getText().toString();
				
				try {
					authorId = (singleReview.getElementsHavingAttribute("data-userid", false))[0].getAttributeByName("data-userid");
				} catch (Exception e) {
					System.out.println("It is Google User , so no user id : "+authorName);
					//<a href="/store/people/details?id=100438274427121954519">subhamay sur</a>
					try {
						TagNode[] nodes = authorNames[0].getElementsHavingAttribute("href", true);
						String href = nodes[0].getAttributeByName("href");
						authorId = href.substring(href.indexOf("id=")+3);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				TagNode[] reviewDates = singleReview.getElementsByAttValue("class", "review-date", true, false);
				reviewDate = reviewDates[0].getText().toString();
				//check periods
				if(this.enablePeriod && !"".equals(reviewDate)){
					try {//orginal July 25, 2014 , to 25Jul2014
						String dateStr = reviewDate.trim();
						
						
						
						String temp = string2Unicode(dateStr);
						Date reviewDate2 = fmt.parse(temp);
						if(reviewDate2.before(startDate)){
							break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
				}
				
				TagNode[] starRatings = singleReview.getElementsByAttValue("class", "current-rating", true, false);
				starRating = starRatings[0].getAttributeByName("style");
				starRating = convertStarRatings(starRating);
				TagNode[] reviewBodys = singleReview.getElementsByAttValue("class", "review-body", true, false);
				reviewBody = reviewBodys[0].getText().toString();
				
				reivewTitle = reviewBodys[0].getElementsByAttValue("class", "review-title", false, false)[0].getText().toString();
				entriesList.add(new Entry(authorId,authorName,reviewDate,starRating,reivewTitle,reviewBody));
			}
		}
		return entriesList;
	}*/
	
	private String convertStarRatings(String str){
		if(str == null) return "";
		String ret = "";
		if(str.contains("100%")){
			ret = "5";
		}else if(str.contains("80%")){
			ret = "4";
		}else if(str.contains("60%")){
			ret = "3";
		}else if(str.contains("40%")){
			ret = "2";
		}else if(str.contains("20%")){
			ret = "1";
		}
		
		return ret;
	}
	
	private String string2Unicode(String s) {  
	   return s.replace("年", "-").replace("月", "-").replace("日", "");
	  }   
	
	public static class Entry extends BaseEntry{
		public Entry(String authorId, String authorName, String reviewDate, String starRating,
				String reivewTitle, String reviewBody) {
			super();
			this.authorId = authorId;
			this.authorName = authorName;
			this.reviewDate = reviewDate;
			this.starRating = starRating;
			this.reivewTitle = reivewTitle;
			this.reviewBody = reviewBody;
		}
		@Override
		public String toString() {
			return this.authorId+" <> "+this.authorName+" <> "+this.reviewDate+" <> "+this.starRating+" <> "+this.reivewTitle+" <> "+this.reviewBody+"\r\n";
		}
		
		public static String[] getHeaders(){
			return new String[]{"Author-Id","Author","Review-Date","Rating","Review-Title","Review-Body"};
		}
		
	}



	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	/* public List<NameValuePair> getFormParams(
             String html, String username, String password)
                        throws UnsupportedEncodingException {
 
        System.out.println("Extracting form's data...");
 
        Document doc = Jsoup.parse(html);
        // Google form id
        Element loginform = doc.getElementById("gaia_loginform");
        Elements inputElements = loginform.getElementsByTag("input");
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
        for (Element inputElement : inputElements) {
                String key = inputElement.attr("name");
                String value = inputElement.attr("value");
 
                if (key.equals("Email"))
                        value = username;
                else if (key.equals("Passwd"))
                        value = password;
 
                paramList.add(new BasicNameValuePair(key, value));
 
        }
 
        return paramList;
  }*/
	
	 
	 /**
	  * 
	  * 
	  * */
private String dateFormatConvert(String orinDate){
	//orginal July 25, 2014 , to 25Jul2014
	String month = "";
	String date = "";
	String year = "";
	if(orinDate.indexOf("年") != -1){
		year = orinDate.substring(0, 4);
		int yearIndex = orinDate.indexOf("年");
		int monthIndex = orinDate.indexOf("月");
		month = orinDate.substring(yearIndex+1, monthIndex);
		date = orinDate.substring(monthIndex+1, orinDate.length()-1);
	}else{
		month = orinDate.substring(0,3);
		int spaceIndex = orinDate.indexOf(" ");
		int commaIndex = orinDate.indexOf(",");
		date = orinDate.substring(spaceIndex+1,commaIndex);
		if(date.length() == 1) date = "0"+date;
		year = orinDate.substring(orinDate.length()-4);
		
	}
	return date+month+year;
}
	 
 private String GetPageContent(String url) throws Exception {
	 
        HttpGet request = new HttpGet(url);
 
//        request.setHeader("User-Agent", USER_AGENT);
//        request.setHeader("Accept",
//                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        request.setHeader("Accept-Language", "en-US,en;q=0.5");
 
        HttpResponse response = httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
 
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
 
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
 
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
                result.append(line);
        }
 
        // set cookies
        setCookies(response.getFirstHeader("Set-Cookie") == null ? "" : 
                     response.getFirstHeader("Set-Cookie").toString());
 
        return result.toString();
 
  }

 	public int getBufferCount() {
		return bufferCount;
	}

	public int getBufferShow() {
		return bufferShow;
	}
	



}
