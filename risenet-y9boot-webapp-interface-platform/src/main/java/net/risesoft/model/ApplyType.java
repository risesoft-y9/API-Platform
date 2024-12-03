package net.risesoft.model;

/**
 * 申请类型枚举类
 */
public enum ApplyType {
    //接口调用申请
    INVOKE("0", "调用"),
    //接口停用申请
    PUB_INTERFACE("2", "发布"),
    //接口发布申请
    STOP_INTERFACE("1", "停用");

    private final String enName;
    private final String name;

    private ApplyType(String enName, String name) {
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