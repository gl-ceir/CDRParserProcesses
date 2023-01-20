package com.glocks.pojo;

import java.io.Serializable;

public class DeviceCustomDb  implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long rev;
	private Integer revtype;
	private String createdOn;
	private String modifiedOn;

	private String deviceType;
	private String deviceIdType;
	private String multipleSimStatus;
	private String snOfDevice;
	private String imeiEsnMeid;
	private String deviceLaunchDate;
	private String deviceStatus;

	private Long userId;
	private String txnId;
	private String period;
	private String featureName;
	
	public DeviceCustomDb() {
		
	}
	
	public DeviceCustomDb(Long rev, int revtype, String createdOn, String modifiedOn,
			String deviceType, String deviceIdType, String multipleSimStatus, String snOfDevice, String imeiEsnMeid,
			String deviceLaunchDate, String deviceStatus, Long userId, String txnId, String period, String featureName) {
		this.rev = rev;
		this.revtype = revtype;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
		this.deviceType = deviceType;
		this.deviceIdType = deviceIdType;
		this.multipleSimStatus = multipleSimStatus;
		this.snOfDevice = snOfDevice;
		this.imeiEsnMeid = imeiEsnMeid;
		this.deviceLaunchDate = deviceLaunchDate;
		this.deviceStatus = deviceStatus;
		this.userId = userId;
		this.txnId = txnId;
		this.period = period;
		this.featureName = featureName;
	}
	
	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public Long getRev() {
		return rev;
	}

	public void setRev(Long rev) {
		this.rev = rev;
	}

	public Integer getRevtype() {
		return revtype;
	}

	public void setRevtype(Integer revtype) {
		this.revtype = revtype;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(String modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceIdType() {
		return deviceIdType;
	}

	public void setDeviceIdType(String deviceIdType) {
		this.deviceIdType = deviceIdType;
	}

	public String getMultipleSimStatus() {
		return multipleSimStatus;
	}

	public void setMultipleSimStatus(String multipleSimStatus) {
		this.multipleSimStatus = multipleSimStatus;
	}

	public String getSnOfDevice() {
		return snOfDevice;
	}

	public void setSnOfDevice(String snOfDevice) {
		this.snOfDevice = snOfDevice;
	}

	public String getImeiEsnMeid() {
		return imeiEsnMeid;
	}

	public void setImeiEsnMeid(String imeiEsnMeid) {
		this.imeiEsnMeid = imeiEsnMeid;
	}

	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getDeviceLaunchDate() {
		return deviceLaunchDate;
	}

	public void setDeviceLaunchDate(String deviceLaunchDate) {
		this.deviceLaunchDate = deviceLaunchDate;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeviceCustomDb [id=");
		builder.append(id);
		builder.append(", rev=");
		builder.append(rev);
		builder.append(", revtype=");
		builder.append(revtype);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", modifiedOn=");
		builder.append(modifiedOn);
		builder.append(", deviceType=");
		builder.append(deviceType);
		builder.append(", deviceIdType=");
		builder.append(deviceIdType);
		builder.append(", multipleSimStatus=");
		builder.append(multipleSimStatus);
		builder.append(", snOfDevice=");
		builder.append(snOfDevice);
		builder.append(", imeiEsnMeid=");
		builder.append(imeiEsnMeid);
		builder.append(", deviceLaunchDate=");
		builder.append(deviceLaunchDate);
		builder.append(", deviceStatus=");
		builder.append(deviceStatus);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", txnId=");
		builder.append(txnId);
		builder.append(", period=");
		builder.append(period);
		builder.append(", featureName=");
		builder.append(featureName);
		builder.append("]");
		return builder.toString();
	}
}