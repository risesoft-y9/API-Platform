package net.risesoft.model;

/**
 * 限流类型枚举类
 */
public enum ThresholdType {
	//自定义
	TYPE_ZDY("0", "自定义"),
	//QPS
	QPS("1", "QPS");

	private final String enName;
	private final String name;

	private ThresholdType(String enName, String name) {
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