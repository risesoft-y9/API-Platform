package net.risesoft.model;

/**
 * 接口状态枚举类
 */
public enum InterfaceStatus {
	//申请发布
	SUBMIT_APPROVE("1", "待发布"),
	//发布
	APPROVE("2", "发布"),
	//停用
	UN_APPROVE("3", "停用");

	private final String enName;
	private final String name;

	private InterfaceStatus(String enName, String name) {
		this.enName = enName;
		this.name = name;
	}

	public String getEnName() {
		return enName;
	}

	public String getName() {
		return name;
	}
}