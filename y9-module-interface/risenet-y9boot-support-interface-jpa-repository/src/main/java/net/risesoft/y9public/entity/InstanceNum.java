package net.risesoft.y9public.entity;

public class InstanceNum {
    private Integer num;
    private String instanceId;

    public InstanceNum() {
    }

    public InstanceNum(Long num, String instanceId) {
        this.num = num.intValue();
        this.instanceId = instanceId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
