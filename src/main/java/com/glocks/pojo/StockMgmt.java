package com.glocks.pojo;

import java.io.Serializable;
import java.sql.Date;

public class StockMgmt implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Date createdOn;
	private Date modifiedOn;
	private String suplierName;
	private String supplierId;
	private String invoiceNumber;
	private String txnId;
	private String fileName;
	private Long userId;
	private String roleType;
	private int quantity;
	private int stockStatus;
	private int previousStockStatus;
	private int currency;
	private String userType;
	private Double totalPrice;
	private String remarks;
	private Long assignerId;
	private Integer deleteFlag;
	private String deleteFlagInterp;
	private int deviceQuantity;
	private Long ceirAdminId;
	
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
	public String getSuplierName() {
		return suplierName;
	}
	public void setSuplierName(String suplierName) {
		this.suplierName = suplierName;
	}
	public String getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getRoleType() {
		return roleType;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getStockStatus() {
		return stockStatus;
	}
	public void setStockStatus(int stockStatus) {
		this.stockStatus = stockStatus;
	}
	public int getPreviousStockStatus() {
		return previousStockStatus;
	}
	public void setPreviousStockStatus(int previousStockStatus) {
		this.previousStockStatus = previousStockStatus;
	}
	public int getCurrency() {
		return currency;
	}
	public void setCurrency(int currency) {
		this.currency = currency;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Long getAssignerId() {
		return assignerId;
	}
	public void setAssignerId(Long assignerId) {
		this.assignerId = assignerId;
	}
	public Integer getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(Integer deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public String getDeleteFlagInterp() {
		return deleteFlagInterp;
	}
	public void setDeleteFlagInterp(String deleteFlagInterp) {
		this.deleteFlagInterp = deleteFlagInterp;
	}
	public int getDeviceQuantity() {
		return deviceQuantity;
	}
	public void setDeviceQuantity(int deviceQuantity) {
		this.deviceQuantity = deviceQuantity;
	}
	public Long getCeirAdminId() {
		return ceirAdminId;
	}
	public void setCeirAdminId(Long ceirAdminId) {
		this.ceirAdminId = ceirAdminId;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StockMgmt [id=");
		builder.append(id);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", modifiedOn=");
		builder.append(modifiedOn);
		builder.append(", suplierName=");
		builder.append(suplierName);
		builder.append(", supplierId=");
		builder.append(supplierId);
		builder.append(", invoiceNumber=");
		builder.append(invoiceNumber);
		builder.append(", txnId=");
		builder.append(txnId);
		builder.append(", fileName=");
		builder.append(fileName);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", roleType=");
		builder.append(roleType);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", stockStatus=");
		builder.append(stockStatus);
		builder.append(", previousStockStatus=");
		builder.append(previousStockStatus);
		builder.append(", currency=");
		builder.append(currency);
		builder.append(", userType=");
		builder.append(userType);
		builder.append(", totalPrice=");
		builder.append(totalPrice);
		builder.append(", remarks=");
		builder.append(remarks);
		builder.append(", assignerId=");
		builder.append(assignerId);
		builder.append(", deleteFlag=");
		builder.append(deleteFlag);
		builder.append(", deleteFlagInterp=");
		builder.append(deleteFlagInterp);
		builder.append(", deviceQuantity=");
		builder.append(deviceQuantity);
		builder.append(", ceirAdminId=");
		builder.append(ceirAdminId);
		builder.append("]");
		return builder.toString();
	}
	
}