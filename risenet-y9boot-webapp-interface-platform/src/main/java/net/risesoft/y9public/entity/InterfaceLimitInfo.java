package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_LIMIT")
@org.hibernate.annotations.Table(comment = "接口限流信息表", appliesTo = "Y9_INTERFACE_LIMIT")
public class InterfaceLimitInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
    private String id;

    @Column(name = "INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '接口id'")
    @Comment(value = "接口ID")
    private String interfaceId;

    @Column(name = "EFFECT", columnDefinition = "varchar(500) default '' comment '流控效果：快速失败（1）;Warm Up（2）;排队等候（3）;'")
    @Comment(value = "流控效果")
    private String effect;

    @Column(name = "WARM_TIME", columnDefinition = "varchar(500) default '' comment '预热时长（秒）'")
    @Comment(value = "预热时长")
    private String warmTime;

    @Column(name = "THRESHOLD_TYPE", columnDefinition = "varchar(500) default '' comment '阈值类型'")
    @Comment(value = "阈值类型")
    private String thresholdType;

    @Column(name = "THRESHOLD_VAL", columnDefinition = "varchar(500) default '' comment '阈值'")
    @Comment(value = "阈值")
    private String thresholdVal;

    @Column(name = "WAIT_TIME", columnDefinition = "varchar(500) default '' comment '超时时间（毫秒）'")
    @Comment(value = "超时时间")
    private String waitTime;

    @Column(name = "IS_COLONY", columnDefinition = "varchar(500) default '' comment '是否集群'")
    @Comment(value = "是否集群")
    private String isColony;

    @Column(name = "LIMIT_TIME", columnDefinition = "varchar(20) default '' comment '限定时间，单位：秒'")
    @Comment(value = "限定时间")
    private String limitTime;

    @Column(name = "LiMIT_COUNT", columnDefinition = "varchar(10) default '' comment '限定时间内总访问量'")
    @Comment(value = "限定时间内总访问量")
    private String limitCount;

    @Column(name = "PERSON_ID", columnDefinition = "varchar(36) default '' comment '填写人ID'")
    @Comment(value = "填写人ID")
    private String personId;

    @Column(name = "PERSON_NAME", columnDefinition = "varchar(36) default '' comment '填写人名称'")
    @Comment(value = "填写人名称")
    private String personName;
}

