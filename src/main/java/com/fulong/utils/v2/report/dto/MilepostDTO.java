package com.fulong.utils.v2.report.dto;
/**
 * 里程碑的传输实体
 * @see
 * @author jx
 * @date 2017年11月17日 下午5:04:39
 */
public class MilepostDTO {
	private String milepostId;
	private String milepostName;

	
	public MilepostDTO(String milepostId, String milepostName) {
		this.milepostId = milepostId;
		this.milepostName = milepostName;
	}
	public String getMilepostId() {
		return milepostId;
	}
	public void setMilepostId(String milepostId) {
		this.milepostId = milepostId;
	}
	public String getMilepostName() {
		return milepostName;
	}
	public void setMilepostName(String milepostName) {
		this.milepostName = milepostName;
	}
	
}
