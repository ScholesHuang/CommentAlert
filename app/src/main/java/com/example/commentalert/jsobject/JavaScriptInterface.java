package com.example.commentalert.jsobject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;

import com.example.commentalert.getreviews.GetReviews;
import com.example.commentalert.getreviews.impl.AppleGetReviews;
import com.example.commentalert.getreviews.impl.GoogleGetReviews;
import com.example.commentalert.getreviews.pojo.BaseEntry;
import com.example.commentalert.util.Constants;
import com.google.gson.Gson;

public class JavaScriptInterface {
	private Context context;
	private SharedPreferences prefs;
	private GetReviews reviews; 
	public JavaScriptInterface(Context context,int storeType) {
		super();
		this.context = context;
		prefs = context.getSharedPreferences("com.example.commentalert", Context.MODE_PRIVATE);
		switch(storeType){
			case Constants.STORE_TYPE_APPLE :
			reviews = new AppleGetReviews(context);
			break;
			case Constants.STORE_TYPE_GOOGLEPLAY :
			reviews = new GoogleGetReviews(context);
			break;
		}
	}


	@JavascriptInterface
	public String getAppStoreComment(String ctyCode, boolean hasRefresh){
		//check if record in prefs
		String savedRecord = prefs.getString(Constants.SAVERECORD+"_"+ctyCode, "");
		try {
			List<BaseEntry> list = reviews.exportOneCountryData(ctyCode);
			int buffShow = reviews.getBufferShow();
			Gson gson = new Gson();
			String retJson = gson.toJson(list.subList(0, buffShow<list.size()?buffShow : list.size()));//just return buff show count
			//save buff into pref
			if(savedRecord.length() > 0){//get back the previous
				LinkedList<BaseEntry> existingList = gson.fromJson(savedRecord, LinkedList.class);
				for(int i = 0;existingList.size() + list.size() > reviews.getBufferCount() && i < list.size() ; i++){//if exceed the buffer , remove the oldest record
					existingList.removeLast();
					existingList.addFirst(list.get(list.size()-1-i));
				}
				String latestSaveJson = gson.toJson(existingList);
				if(!hasRefresh && existingList.size() >0){
					retJson = latestSaveJson;
				}
				prefs.edit().putString(Constants.SAVERECORD+"_"+ctyCode, latestSaveJson).apply();
			}else{
				String allListJson = gson.toJson(list);
				prefs.edit().putString(Constants.SAVERECORD+"_"+ctyCode, allListJson).apply();
			}
			return retJson;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			
		}
		return "";
	}
	

	
	@JavascriptInterface
	public String getAppStoreCommentOffline(String ctyCode){
		//check if record in prefs
		String savedRecord = prefs.getString(Constants.SAVERECORD+"_"+ctyCode, "");
		return savedRecord;
	}
	
	
	@JavascriptInterface
	public String getAppStoreCommentContinue(String ctyCode,int fromIndex){
		try {
			//check if record in prefs
			String savedRecord = prefs.getString(Constants.SAVERECORD+"_"+ctyCode, "");
			if(savedRecord.length() > 0){//get back the previous
				Gson gson = new Gson();
				LinkedList<BaseEntry> existingList = gson.fromJson(savedRecord, LinkedList.class);
				List<BaseEntry> subList = null;
				if(existingList.size() < fromIndex + reviews.getBufferShow()){
					subList = existingList.subList(fromIndex,existingList.size());
				}else{
					subList = existingList.subList(fromIndex, fromIndex + reviews.getBufferShow());
				}
				return gson.toJson(subList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	
	@JavascriptInterface
	public String getCountries(){
		Map countryMap = ((AppleGetReviews)reviews).getAllCountries();
		if(countryMap.size() > 0){
			Gson gson = new Gson();
			return gson.toJson(countryMap);
		}
		return "";
	}
	
	@JavascriptInterface
	public String getLastSelectedCountry(){
		return prefs.getString("APPSTORE_"+Constants.LASTSELECTEDCOUNTRY, "gb");	
	}
	
	//For Google Play
	@JavascriptInterface
	public String getCurrentCountry(){
		try {
			String code = context.getResources().getConfiguration().locale.getCountry();
			return code;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return "";
	}
	
	//For Google Play
		@JavascriptInterface
		public String getCurrentDisplayCountry(){
			try {
				String display = context.getResources().getConfiguration().locale.getDisplayCountry();
				return display;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "";
		}
	
	
	
	@JavascriptInterface
	public String getCurrentTime(String ctyCode){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String currentTime = dateFormat.format(cal.getTime());
		prefs.edit().putString(ctyCode+"_"+Constants.UPDATETIME, currentTime).apply();
		return currentTime;
	}
	
	@JavascriptInterface
	public String getCountryUpdateTime(String ctyCode){
		return prefs.getString(ctyCode+"_"+Constants.UPDATETIME, "");
	}
	
	@JavascriptInterface
	public int getBufferCount(){
		return reviews.getBufferCount();
	}
	
}
