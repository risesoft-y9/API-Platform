package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.dto.ApproveDTO;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_APPROVE")
@org.hibernate.annotations.Table(comment = "接口审批表", appliesTo = "Y9_INTERFACE_APPROVE")
public class Approve extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
    private String id;

    @Column(name = "INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '接口id'")
    @Comment(value = "接口ID")
    private String interfaceId;

    @Column(name = "APPLY_ID", columnDefinition = "varchar(36) default '' comment '申请ID'")
    @Comment(value = "申请ID")
    private String applyId;

    @Column(name = "PERSON_NAME", columnDefinition = "varchar(1000) default '' comment '审批人'")
    @Comment(value = "审批人")
    private String personName;

    @Column(name = "PERSON_ID", columnDefinition = "varchar(1000) default '' comment '审批人ID'")
    @Comment(value = "审批人ID")
    private String personId;

    @Column(name = "ILLUSTRATE", columnDefinition = "varchar(4000) default '' comment '审批说明'")
    @Comment(value = "审批说明")
    private String illustrate;

    @Column(name = "APPROVE_STATUS", columnDefinition = "varchar(100) default '' comment '审批意见'")
    @Comment(value = "审批意见")
    private String approveStatus;

    @Column(name = "NOTES", columnDefinition = "varchar(2000) default '' comment '备注'")
    @Comment(value = "备注")
    private String notes;

    @Column(name = "INTERFACE_STATUS", columnDefinition = "varchar(10) default '' comment '接口状态'")
    @Comment(value = "接口状态")
    private String interfaceStatus;

    @Column(name = "IS_OVER", columnDefinition = "varchar(10) default '' comment '是否结束(Y结束，N未结束)'")
    @Comment(value = "是否结束")
    private String isOver;

    @Column(name = "APPLY_TYPE", columnDefinition = "varchar(10) default '' comment '申请类型'")
    @Comment(value = "申请类型")
    private String applyType;

    @Column(name = "IS_NEW", columnDefinition = "varchar(10) default '' comment '是否最新数据'")
    @Comment(value = "是否最新数据")
    private String isNew;

    @Column(name = "FLOW_NODE", columnDefinition = "text  comment '流程节点信息'")
    @Comment(value = "流程节点信息")
    private String flowNode;

    @Column(name = "ALREADY_APPROVE_USER", columnDefinition = "varchar(1000) default '' comment '已审批人员id'")
    @Comment(value = "已审批人员id")
    private String alreadyApproveUser;

    @Column(name = "CURRENT_USER_ID", columnDefinition = "varchar(100) default '' comment '当前审批人员'")
    @Comment(value = "当前审批人员")
    private String currentUserId;

    @Column(name = "CURRENT_NODE", columnDefinition = "varchar(500) default '' comment '当前节点信息'")
    @Comment(value = "当前节点信息")
    private String currentNode;

    @Column(name = "BUS_ID_JSON", columnDefinition = "text  comment '业务关联id信息'")
    @Comment(value = "当前节点信息")
    private String busIdJson;

    public Approve(ApproveDTO dto) {
        this.id = dto.getId();
        this.interfaceId = dto.getInterfaceId();
        this.applyId = dto.getApplyId();
        this.illustrate = dto.getIllustrate();
        this.approveStatus = dto.getApproveStatus();
        this.notes = dto.getNotes();
        this.personName = dto.getPersonName();
        this.personId = dto.getPersonId();
        this.interfaceStatus = dto.getInterfaceStatus();
        this.isOver = dto.getIsOver();
        this.flowNode = dto.getFlowNode();
        this.alreadyApproveUser = dto.getAlreadyApproveUser();
        this.currentUserId = dto.getCurrentUserId();
        this.currentNode = dto.getCurrentNode();
    }
}

