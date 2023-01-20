package com.glocks.pojo;

public class TacApproveRequest {
	
	private Integer adminApproveStatus;
	private String adminUserType = "CEIRSYSTEM";  //  SystemAdmin
	private String txnId;
	
	public TacApproveRequest(String txnId, Integer adminApproveStatus) {
		this.txnId = txnId;
		this.adminApproveStatus = adminApproveStatus;
	}
	
	public Integer getAdminApproveStatus() {
		return adminApproveStatus;
	}
	public void setAdminApproveStatus(Integer adminApproveStatus) {
		this.adminApproveStatus = adminApproveStatus;
	}
	public String getAdminUserType() {
		return adminUserType;
	}
	public void setAdminUserType(String adminUserType) {
		this.adminUserType = adminUserType;
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
		builder.append("TacApproveRequest [adminApproveStatus=");
		builder.append(adminApproveStatus);
		builder.append(", adminUserType=");
		builder.append(adminUserType);
		builder.append(", txnId=");
		builder.append(txnId);
		builder.append("]");
		return builder.toString();
	}

}
