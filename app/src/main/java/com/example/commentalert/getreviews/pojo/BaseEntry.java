package com.example.commentalert.getreviews.pojo;

public class BaseEntry {
	protected String authorId;
	protected String authorName;
	protected String reviewDate;
	protected String starRating;
	protected String reivewTitle;
	protected String reviewBody;
	protected String locale;
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getReviewDate() {
		return reviewDate;
	}
	public void setReviewDate(String reviewDate) {
		this.reviewDate = reviewDate;
	}
	public String getStarRating() {
		return starRating;
	}
	public void setStarRating(String starRating) {
		this.starRating = starRating;
	}
	public String getReivewTitle() {
		return reivewTitle;
	}
	public void setReivewTitle(String reivewTitle) {
		this.reivewTitle = reivewTitle;
	}
	public String getReviewBody() {
		return reviewBody;
	}
	public void setReviewBody(String reviewBody) {
		this.reviewBody = reviewBody;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	
}
