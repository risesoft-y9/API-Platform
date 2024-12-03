package net.risesoft.y9public.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "VIEW_APPROVE_LIST")
@org.hibernate.annotations.Table(comment = "接口审批视图表", appliesTo = "VIEW_APPROVE_LIST")
public class ViewApprove implements Serializable {

    private static final long serialVersionUID = 3121642145259274427L;

    @Id
    @Column(name = "APPROVE_ID")
    private String approveId;

    @Column(name = "INTERFACE_ID")
    @Comment(value = "接口ID")
    private String interfaceId;

    @Column(name = "APPLY_ID")
    @Comment(value = "申请ID")
    private String applyId;

    @Column(name = "APPROVE_STATUS")
    @Comment(value = "审批意见")
    private String approveStatus;

    @Column(name = "ILLUSTRATE")
    @Comment(value = "审批说明")
    private String illustrate;

    @Column(name = "APPLY_TYPE")
    @Comment(value = "申请类型")
    private String applyType;

    @Column(name = "NOTES")
    @Comment(value = "备注")
    private String notes;

    @Column(name = "IS_OVER")
    @Comment(value = "是否结束")
    private String isOver;

    @Column(name = "APPROVE_INTERFACE_STATUS")
    @Comment(value = "审批接口状态")
    private String approveInterfaceStatus;

    @Column(name = "APPROVE_PERSON_NAME")
    @Comment(value = "审批人")
    private String approvePersonName;

    @Column(name = "APPROVE_PERSON_ID")
    @Comment(value = "审批人ID")
    private String approvePersonId;

    @Column(name = "APPLY_PERSON_NAME")
    @Comment(value = "申请人名称")
    private String applyPersonName;

    @Column(name = "APPLY_PERSON_ID")
    @Comment(value = "申请人ID")
    private String applyPersonId;

    @Column(name = "INTERFACE_NAME")
    @Comment(value = "接口名称")
    private String interfaceName;

    @Column(name = "INTERFACE_STATUS")
    @Comment(value = "接口状态")
    private String interfaceStatus;

    @Column(name = "VERSION")
    @Comment(value = "接口版本")
    private String version;

    @Column(name = "IS_LIMIT_DATA")
    @Comment(value = "是否控制数据权限信息")
    private String isLimitData;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    @Column(name = "CREATETIME")
    private Date createTime;

    @Column(name = "ALREADY_APPROVE_USER", columnDefinition = "varchar(1000) default '' comment '已审批人员id'")
    @Comment(value = "已审批人员id")
    private String alreadyApproveUser;

    @Column(name = "CURRENT_USER_ID", columnDefinition = "varchar(100) default '' comment '当前审批人员'")
    @Comment(value = "当前审批人员")
    private String currentUserId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    @Column(name = "APPLY_TIME")
    private Date applyTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    @Column(name = "UPDATETIME")
    private Date updateTime;

    @Column(name = "STATUS_SORT")
    @Comment(value = "状态排序")
    private String statusSort;
}

