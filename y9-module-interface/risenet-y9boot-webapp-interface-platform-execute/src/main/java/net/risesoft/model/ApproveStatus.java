package net.risesoft.model;


/**
 * 审批状态枚举类
 */
public enum ApproveStatus {
	//未提交审批（初始状态）
	UN_SUBMIT_APPROVE("0", "未提交"),
	//未审批
	SUBMIT_APPROVE("1", "未审批"),
	//审批中
	NOW_APPROVE("2", "审批中"),
	//通过
	APPROVE("3", "通过"),
	//不通过
	UN_APPROVE("4", "不通过");

	private final String enName;
	private final String name;

	ApproveStatus(String enName, String name) {
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