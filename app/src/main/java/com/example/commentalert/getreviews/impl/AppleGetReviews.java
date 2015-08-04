package com.example.commentalert.getreviews.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.example.commentalert.getreviews.GetReviews;
import com.example.commentalert.getreviews.pojo.BaseEntry;
import com.example.commentalert.util.Constants;

public class AppleGetReviews implements GetReviews {
	private Properties iphoneParams = null;
	private Context context;
	private String iphoneParamsPath = "properties/params.properties";
	//private String continuousOneStarPath = "continuousOneStar.txt";
	private File oneStarFile;
	private String postURL;
	private String originalPostURL;
	private String fromPageNum;
	private Date startDate;
	private String dateFormat;
	private String ID;
	private File outputFileFile[] = null;
	private String outputFilePath;
	private String relative_URI;
	private String[] countries;
	private int continuousOneStar = 5;
	private boolean useProxy;
	private boolean enablePeriod;
	private SimpleDateFormat fmt;
	private Date savedStartDate;
	private int bufferCount;
	private int bufferShow;
	private int currentCount = 0;
	private SharedPreferences prefs;
	private Map countryMap = new HashMap();
	private DocumentBuilderFactory factory ;  
	private DocumentBuilder builder ;  
	private boolean canReturn;
	public AppleGetReviews(Context context) {
		super();
		this.context = context;
		this.iphoneParams = new Properties();
		try {
			AssetManager am = context.getAssets();
			InputStream inputStream = am.open(iphoneParamsPath);
//			FileInputStream temp = new FileInputStream(iphoneParamsPath);
			iphoneParams.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initParams();
		try {
			factory  = DocumentBuilderFactory.newInstance();  
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}  
		prefs = context.getSharedPreferences("com.example.commentalert", Context.MODE_PRIVATE);
	}
	
	@Override
	public List exportOneCountryData(String ctyCode) throws Exception{
		prefs.edit().putString("APPSTORE_"+Constants.LASTSELECTEDCOUNTRY, ctyCode).apply();
		currentCount = 0;// init to zero
		canReturn = false;
		String rawData = "";
		List<Entry> countryEntries = new ArrayList(0);
		int pageNum = Integer.valueOf(fromPageNum);
		String locale = iphoneParams.getProperty("country_"+ctyCode, "unknown");
		postURL = originalPostURL + ctyCode + "/"+ relative_URI;
		String tempDate = prefs.getString(locale+"_"+Constants.LATESTDATE, "");
		Date lastUpdateDate = null;
		if(!"".equals(tempDate)){
			lastUpdateDate = fmt.parse(tempDate);
		}
		while(!"".equals(rawData = rawReviewsData(pageNum)) && !canReturn){
			if(currentCount >= bufferCount) break;
			List<Entry> entries = readEntries(rawData,locale,lastUpdateDate);
			if(entries.size() == 0){
				break;
			}
			if(pageNum == 1){//save the latest date for this country
				try {
					prefs.edit().putString(locale+"_"+Constants.LATESTDATE, entries.get(0).getReviewDate()).apply();
				} catch (Exception e) {
					prefs.edit().putString(locale+"_"+Constants.LATESTDATE, "").apply();
					e.printStackTrace();
				}
				
				
			}
			countryEntries.addAll(entries);
			System.out.println(entries);
			System.out.println("Page : "+pageNum);
			pageNum++;
		}
		return countryEntries;
	}
	
	
	@Override
	public List exportAllData() throws Exception {
//		pageNum = 253;
		currentCount = 0;// init to zero
		String rawData = "";
		//ExcelExport<Entry> excelExport = new ExcelExport<Entry>(outputFilePath);
		List<Entry> countryEntries = new ArrayList(0);
		for(int i = 0 ; i < countries.length ; i ++){
			if(currentCount >= bufferCount) break;
			int pageNum = Integer.valueOf(fromPageNum);
			String locale = iphoneParams.getProperty("country_"+countries[i], "unknown");
			postURL = originalPostURL + countries[i] + "/"+ relative_URI;
			String tempDate = prefs.getString(locale+"_"+Constants.LATESTDATE, "");
			Date lastUpdateDate = null;
			if(!"".equals(tempDate)){
				lastUpdateDate = fmt.parse(tempDate);
			}
			while(!"".equals(rawData = rawReviewsData(pageNum))){
				if(currentCount >= bufferCount) break;
				List<Entry> entries = readEntries(rawData,locale,lastUpdateDate);
				if(entries.size() == 0){
					break;
				}
				if(pageNum == 1){//save the latest date for this country
					try {
						prefs.edit().putString(locale+"_"+Constants.LATESTDATE, entries.get(0).getReviewDate()).apply();
					} catch (Exception e) {
						prefs.edit().putString(locale+"_"+Constants.LATESTDATE, "").apply();
						e.printStackTrace();
					}
				}
				countryEntries.addAll(entries);
				System.out.println(entries);
				System.out.println("Page : "+pageNum);
				pageNum++;
			}
		}
		return countryEntries;
	}
	
	


	private void initParams(){
		if(!iphoneParams.isEmpty()){
			postURL = iphoneParams.getProperty("APPLESTORE_POST_URL");
			originalPostURL = postURL;
			fromPageNum = iphoneParams.getProperty("APPLESTORE_FROM_PAGE_NUM");
			ID = iphoneParams.getProperty("APPLESTORE_ID");
			relative_URI = iphoneParams.getProperty("APPLESTORE_RELATIVE_URI");
			outputFilePath = iphoneParams.getProperty("APPLESTORE_OUTPUTFILE_PATH");
			bufferCount = Integer.valueOf(iphoneParams.getProperty("APPLESTORE_BUFFER_COUNT"));
			bufferShow = Integer.valueOf(iphoneParams.getProperty("APPLESTORE_BUFFER_SHOW"));
			enablePeriod = Boolean.valueOf(iphoneParams.getProperty("APPLESTORE_ENABLE_PERIOD"));
			//if(this.enablePeriod){
				dateFormat = iphoneParams.getProperty("APPLESTORE_DATE_FORMAT");
				String startDateStr = iphoneParams.getProperty("APPLESTORE_START_DATE");
				String endDateStr = iphoneParams.getProperty("APPLESTORE_END_DATE");
				if(!("".equals(dateFormat) || "".equals(startDateStr) || "".equals(endDateStr))){
					try {
						fmt = new SimpleDateFormat(dateFormat);
						startDate = fmt.parse(startDateStr);
					} catch (ParseException e) {
						this.enablePeriod = false;
						e.printStackTrace();
					}
				}else{
					this.enablePeriod = false;
				}
				
			//}
			String countriesString = iphoneParams.getProperty("APPLESTORE_COUNTRIES");
			useProxy = Boolean.valueOf(iphoneParams.getProperty("USE_PROXY"));
			if(countriesString != null){
				countries = countriesString.split(",");
				for(String shortName : countries){
					if(shortName.trim().length() == 0){
						continue;
					}
					String fullName = iphoneParams.getProperty("country_"+shortName);
					countryMap.put(shortName, fullName);
				}
				
				
			}
		}
	}
	
	
/*	public static void main(String[] args){
		AppleGetReviews obj = new AppleGetReviews();
		try {
			obj.exportAllData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	@Override
	public String rawReviewsData(int pageNum) {
		HttpPost httppost = null;
		String responseBody = "";
		try {
			StringBuffer sb = new StringBuffer(postURL);
			sb.append("id="+ID)
			.append("/page="+pageNum)
			.append("/xml");
			
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
				/* commented for mobile
				String pacUrl = iphoneParams.getProperty("PROXY_PAC");
				//px = ProxyUtil.getProxyInfoFromPAC(pacUrl, sb.toString());
				String username = iphoneParams.getProperty("PROXY_USER");
				String password = iphoneParams.getProperty("PROXY_PASSWORD");
				String proxyIp = iphoneParams.getProperty("PROXY_IP");
				int proxyPort = Integer.valueOf(iphoneParams.getProperty("PROXY_PORT"));
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
	            responseBody = httpclient.execute(target, httpget, responseHandler);
	            */
			}else{
				HttpClient httpClient = new DefaultHttpClient();
				httppost = new HttpPost(sb.toString());
				System.out.println("executing request "+httppost.getURI());
				responseBody = httpClient.execute(httppost, responseHandler);
			}
			//handle json
//			System.out.println("---------------------------------");
//			System.out.println(responseBody);
//			System.out.println("---------------------------------");
			return responseBody;
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
	
	/*private List<Entry> readEntries(String obj){
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
		
		//review is from second entry
		TagNode[]  entryNodes = node.getElementsByName("entry", true);
		for(int i=1 ; i < entryNodes.length ; i++){
			TagNode one = entryNodes[i];
			String authorId = "";
			String authorName = "";
			String reviewDate = "";
			String starRating = "";
			String reivewTitle = "";
			String reviewBody = "";
			
			TagNode[] reviewDates = one.getElementsByName("updated", false);
			if(reviewDates != null && reviewDates.length > 0){
				reviewDate = reviewDates[0].getText().toString();
			}
			TagNode[] authorIds = one.getElementsByName("id", false);
			if(authorIds != null && authorIds.length > 0){
				authorId = authorIds[0].getText().toString();
			}
			TagNode[] reivewTitles = one.getElementsByName("title", false);
			if(reivewTitles != null && reivewTitles.length > 0){
				reivewTitle = convertHTMLSpecialChars(reivewTitles[0].getText().toString());
			}
			TagNode[] reviewBodys = one.getElementsByName("content", false);
			if(reviewBodys != null && reviewBodys.length >0){
				reviewBody = convertHTMLSpecialChars(reviewBodys[0].getText().toString());
			}
			TagNode[] starRatings = one.getElementsByName("im:rating", false);
			if(starRatings != null && starRatings.length >0){
				starRating = starRatings[0].getText().toString();
			}
			TagNode[] authorNames = one.getElementsByName("author", false);
			if(authorNames != null && authorNames.length >0){
				authorName = convertHTMLSpecialChars((authorNames[0].getElementsByName("name", false))[0].getText().toString());
			}
			entriesList.add(new Entry(authorId,authorName,reviewDate,starRating,reivewTitle,reviewBody));
		}
		return entriesList;
	}
	*/
	private List<Entry> readEntries(String obj , String locale, Date lastUpdatedDate){
		List<Entry> entriesList = new ArrayList<Entry>(0);
		
		try {
			Document document = builder.parse(new InputSource(new ByteArrayInputStream(obj.getBytes("UTF-8"))));  
			//review is from second entry
			NodeList  entryNodes = document.getElementsByTagName("entry");
			for(int i=1 ; i < entryNodes.getLength() ; i++){
				if(currentCount >= bufferCount) break;
				Element one = (Element)entryNodes.item(i);
				String authorId = "";
				String authorName = "";
				String reviewDate = "";
				String starRating = "";
				String reivewTitle = "";
				String reviewBody = "";
				
				NodeList reviewDates = one.getElementsByTagName("updated");
				if(reviewDates != null && reviewDates.getLength() > 0){
					reviewDate = ((Element)reviewDates.item(0)).getTextContent();
					//check periods
					if(!"".equals(reviewDate)){
						try {
							String dateStr = reviewDate.substring(0, this.dateFormat.length());
							Date reviewDate2 = fmt.parse(dateStr);
							if(lastUpdatedDate !=null && !reviewDate2.after(lastUpdatedDate)){
								canReturn = true;
								break;
							}else if(enablePeriod && reviewDate2.before(this.startDate)){
								canReturn = true;
								break;
							}
						} catch (ParseException e) {
							e.printStackTrace();
							continue;
						}
					}
					reviewDate = reviewDate.substring(0,19);
				}
				NodeList authorIds = one.getElementsByTagName("id");
				if(authorIds != null && authorIds.getLength() > 0){
					authorId = ((Element)authorIds.item(0)).getTextContent();
				}
				NodeList reivewTitles = one.getElementsByTagName("title");
				if(reivewTitles != null && reivewTitles.getLength() > 0){
					reivewTitle = convertHTMLSpecialChars( ((Element)reivewTitles.item(0)).getTextContent());
				}
				NodeList reviewBodys = one.getElementsByTagName("content");
				if(reviewBodys != null && reviewBodys.getLength() >0){
					reviewBody = convertHTMLSpecialChars(((Element)reviewBodys.item(0)).getTextContent());
				}
				NodeList starRatings = one.getElementsByTagName("im:rating");
				if(starRatings != null && starRatings.getLength() >0){
					starRating = ((Element)starRatings.item(0)).getTextContent();
				}
				NodeList authorNames = one.getElementsByTagName("author");
				if(authorNames != null && authorNames.getLength() >0){
					authorName = convertHTMLSpecialChars(((Element)authorNames.item(0).getFirstChild()).getTextContent());
				}
				entriesList.add(new Entry(authorId,authorName,reviewDate,starRating,reivewTitle,reviewBody,locale));
				currentCount++;
				System.out.println("Current Count : "+currentCount);
			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entriesList;
	}
	
	private String convertHTMLSpecialChars(String str){
		if(str != null){
			return str.replace("&#39;", "'");
		}
		return ""; 
	}
	
	
	public static class Entry extends BaseEntry{

		public Entry(String authorId, String authorName, String reviewDate, String starRating,
				String reivewTitle, String reviewBody, String locale) {
			super();
			this.authorId = authorId;
			this.authorName = authorName;
			this.reviewDate = reviewDate;
			this.starRating = starRating;
			this.reivewTitle = reivewTitle;
			this.reviewBody = reviewBody;
			this.locale = locale;
		}
		@Override
		public String toString() {
			return this.authorId+" <> "+this.authorName+" <> "+this.reviewDate+" <> "+this.starRating+" <> "+this.reivewTitle+" <> "+this.reviewBody+"\r\n";
		}
		public static String[] getHeaders(){
			return new String[]{"Author-Id","Author","Review-Date","Rating","Review-Title","Review-Body"};
		}
	}


	public void setSavedStartDate(Date savedStartDate) {
		this.savedStartDate = savedStartDate;
	}





	public int getBufferCount() {
		return bufferCount;
	}





	public int getBufferShow() {
		return bufferShow;
	}
	
	public Map getAllCountries(){
		return countryMap;
	}
}
