package com.glocks.pojo;

public class ManagementTable {
	
	private String name;
	private String audName;
	private String audSequenceName;
	
	public ManagementTable() {

	}
	
	public ManagementTable(String name, String audName, String audSequenceName) {
		this.name = name;
		this.audName = audName;
		this.audSequenceName = audSequenceName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAudName() {
		return audName;
	}
	public void setAudName(String audName) {
		this.audName = audName;
	}

	public String getAudSequenceName() {
		return audSequenceName;
	}

	public void setAudSequenceName(String audSequenceName) {
		this.audSequenceName = audSequenceName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ManagementTable [name=");
		builder.append(name);
		builder.append(", audName=");
		builder.append(audName);
		builder.append(", audSequenceName=");
		builder.append(audSequenceName);
		builder.append("]");
		return builder.toString();
	}
	
}
