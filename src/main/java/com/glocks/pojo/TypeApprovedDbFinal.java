package com.glocks.pojo;

import java.time.LocalDateTime;

public class TypeApprovedDbFinal {

	private long id;
	private String tac;
	private Integer approveStatus;
	private Integer adminApproveStatus;
	private Long userId;
	private String userType;
	private Long adminUserId;
	private String adminUserType;
	private LocalDateTime modifiedOn;
	private String txnId;

	public TypeApprovedDbFinal() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTac() {
		return tac;
	}
	public void setTac(String tac) {
		this.tac = tac;
	}
	public Integer getApproveStatus() {
		return approveStatus;
	}
	public void setApproveStatus(Integer approveStatus) {
		this.approveStatus = approveStatus;
	}
	public Integer getAdminApproveStatus() {
		return adminApproveStatus;
	}
	public void setAdminApproveStatus(Integer adminApproveStatus) {
		this.adminApproveStatus = adminApproveStatus;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public Long getAdminUserId() {
		return adminUserId;
	}
	public void setAdminUserId(Long adminUserId) {
		this.adminUserId = adminUserId;
	}
	public String getAdminUserType() {
		return adminUserType;
	}
	public void setAdminUserType(String adminUserType) {
		this.adminUserType = adminUserType;
	}
	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}
	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TypeApprovedDb [id=");
		builder.append(id);
		builder.append(", tac=");
		builder.append(tac);
		builder.append(", approveStatus=");
		builder.append(approveStatus);
		builder.append(", adminApproveStatus=");
		builder.append(adminApproveStatus);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", userType=");
		builder.append(userType);
		builder.append(", adminUserId=");
		builder.append(adminUserId);
		builder.append(", adminUserType=");
		builder.append(adminUserType);
		builder.append(", modifiedOn=");
		builder.append(modifiedOn);
		builder.append(", txnId=");
		builder.append(txnId);
		builder.append("]");
		return builder.toString();
	}
}