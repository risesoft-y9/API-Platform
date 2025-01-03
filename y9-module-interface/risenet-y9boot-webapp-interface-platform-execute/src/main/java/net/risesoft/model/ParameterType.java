package net.risesoft.model;

/**
 * 数据类型枚举类
 */
public enum ParameterType {
	//INT类型
	INTEGER("integer", "integer"),
	//BOOLEAN
	BOOLEAN("boolean", "boolean"),
	//INT类型
	STRING("String", "String"),
	//DOUBLE
	DOUBLE("double", "double");

	private final String enName;
	private final String name;

	ParameterType(String enName, String name) {
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