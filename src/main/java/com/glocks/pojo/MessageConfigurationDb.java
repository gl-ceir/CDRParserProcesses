package com.glocks.pojo;

import java.io.Serializable;
import java.util.Date;

public class MessageConfigurationDb implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Date createdOn;
	private Date modifiedOn;
	private String tag;
	private String value;
	private String description;
	private Integer channel;
	private String subject;
	
	public MessageConfigurationDb() {
		
	}

	public MessageConfigurationDb(Long id, String tag, String value, String description, Integer channel, 
			String subject) {
		this.id = id;
		this.tag = tag;
		this.value = value;
		this.description = description;
		this.channel = channel;
		this.subject = subject;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Date getModifiedOn() {
		return modifiedOn;
	}
	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getChannel() {
		return channel;
	}
	public void setChannel(Integer channel) {
		this.channel = channel;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageConfigurationDb [id=");
		builder.append(id);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", modifiedOn=");
		builder.append(modifiedOn);
		builder.append(", tag=");
		builder.append(tag);
		builder.append(", value=");
		builder.append(value);
		builder.append(", description=");
		builder.append(description);
		builder.append(", channel=");
		builder.append(channel);
		builder.append("]");
		return builder.toString();
	}
}
