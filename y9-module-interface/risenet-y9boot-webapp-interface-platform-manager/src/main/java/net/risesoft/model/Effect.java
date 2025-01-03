package net.risesoft.model;

/**
 * 流控效果枚举类：快速失败（1）;Warm Up（2）;排队等候（3）
 */
public enum Effect {
    //快速失败
    FAIL_FAST("1", "快速失败"),
    //Warm Up
    WARM_UP("2", "Warm Up"),
    //排队等候
    WAIT("3", "排队等候");

    private final String enName;
    private final String name;

    private Effect(String enName, String name) {
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