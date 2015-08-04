package com.example.commentalert.getreviews;

import java.util.List;

public interface GetReviews {
	String appStoreCommentPath = "file:///android_asset/web_resource/appstorecomment.html";
	List exportAllData() throws Exception;
//	List exportAllData(String startDate) throws Exception {
	List exportOneCountryData(String ctyCode) throws Exception;
	String rawReviewsData(int pageNum);
	int getBufferCount();
	int getBufferShow();
}
