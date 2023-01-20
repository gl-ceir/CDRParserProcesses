package com.glocks.pojo;

public class HttpResponse {
	
	private String response;
	private Integer statusCode;
	private Integer errorCode;
	private String user;
	private String tag;
	
	public Integer getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HttpResponse [response=");
		builder.append(response);
		builder.append(", statusCode=");
		builder.append(statusCode);
		builder.append(", user=");
		builder.append(user);
		builder.append(", tag=");
		builder.append(tag);
		builder.append("]");
		return builder.toString();
	}
	
}
