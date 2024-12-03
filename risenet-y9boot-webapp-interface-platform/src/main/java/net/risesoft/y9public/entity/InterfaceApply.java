package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.dto.InterfaceApplyDTO;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_APPLY")
@org.hibernate.annotations.Table(comment = "接口申请表", appliesTo = "Y9_INTERFACE_APPLY")
public class InterfaceApply extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
    private String id;

    @Column(name = "INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '接口id'")
    @Comment(value = "接口ID")
    private String interfaceId;

    @Column(name = "APPLY_PERSON_NAME", columnDefinition = "varchar(50) default '' comment '接口申请人'")
    @Comment(value = "接口申请人")
    private String applyPersonName;

    @Column(name = "APPLY_PERSON_ID", columnDefinition = "varchar(36) default '' comment '接口申请人ID'")
    @Comment(value = "接口申请人ID")
    private String applyPersonId;

    @Column(name = "SYSTEM_IDENTIFIER", columnDefinition = "varchar(254) default '' comment '系统标识'")
    @Comment(value = "申请系统标识")
    private String systemIdentifier;

    @Column(name = "APPLY_SYSTEM_NAME", columnDefinition = "varchar(254) default '' comment '申请系统名称'")
    @Comment(value = "申请系统名称")
    private String applySystemName;

    @Column(name = "AUTH", columnDefinition = "text comment '权限信息'")
    @Comment(value = "权限信息")
    private String auth;

    @Column(name = "APPLY_PERSON_DEPT_NAME", columnDefinition = "varchar(254) default '' comment '接口申请人单位名称'")
    @Comment(value = "接口申请人单位名称")
    private String applyPersonDeptName;

    @Column(name = "APPLY_PERSON_DEPT_ID", columnDefinition = "varchar(254) default '' comment '接口申请人单位id'")
    @Comment(value = "接口申请人单位id")
    private String applyPersonDeptId;

    @Column(name = "USE_PERSON_RESPONSIBLE", columnDefinition = "varchar(50) default '' comment '接口调用责任人'")
    @Comment(value = "接口调用责任人")
    private String usePersonResponsible;

    @Column(name = "USE_PERSON_RESPONSIBLE_PHONE", columnDefinition = "varchar(20) default '' comment '接口调用责任人联系方式'")
    @Comment(value = "接口调用责任人联系方式")
    private String usePersonResponsiblePhone;

    @Column(name = "NOTES", columnDefinition = "varchar(254) default '' comment '申请备注'")
    @Comment(value = "申请备注")
    private String notes;

    @Column(name = "APPLY_TIME", columnDefinition = "varchar(20) default '' comment '申请日期'")
    @Comment(value = "申请日期")
    private String applyTime;

    @Column(name = "APPLY_REASON", columnDefinition = "varchar(500) default '' comment '申请事由'")
    @Comment(value = "申请事由")
    private String applyReason;

    @Column(name = "APPLY_TYPE", columnDefinition = "varchar(10) default '' comment '申请类型'")
    @Comment(value = "申请类型")
    private String applyType;

    @Column(name = "USER_KEY", columnDefinition = "varchar(100) default '' comment '用户令牌'")
    @Comment(value = "用户令牌")
    private String userKey;

    @Column(name = "USER_SECRET", columnDefinition = "varchar(100) default '' comment '用户密钥'")
    @Comment(value = "用户密钥")
    private String userSecret;

    @Column(name = "IP_WHITELIST", columnDefinition = "varchar(100) default '' comment 'IP白名单'")
    @Comment(value = "IP白名单")
    private String ipWhitelist;

    @Column(name = "APPLY_STOP_TIME", columnDefinition = "varchar(20) default '' comment '申请停用时间'")
    @Comment(value = "申请停用时间")
    private String applyStopTime;

    @Column(name = "OLD_ID", columnDefinition = "varchar(36) default '' comment '关联回显之前的申请信息,目前只有接口调用申请会存'")
    @Comment(value = "变更前旧id")
    private String oldId;

    @Column(name = "SAME_ID", columnDefinition = "varchar(36) default '' comment '同一个申请变更id'")
    @Comment(value = "同一个申请变更id")
    private String sameId;

    @Column(name = "DATA_TYPE", columnDefinition = "varchar(10) default '新增' comment '数据类型-新增-变更'")
    @Comment(value = "数据类型")
    private String dataType;

    @Column(name = "IS_EFFECTIVE", columnDefinition = "varchar(2) default 'Y' comment '数据是否有效：Y有效-N无效'")
    @Comment(value = "数据是否有效")
    private String isEffective;

    public InterfaceApply(InterfaceApplyDTO dto) {
        this.id = dto.getId();
        this.interfaceId = dto.getInterfaceId();
        this.applyPersonName = dto.getApplyPersonName();
        this.applyPersonId = dto.getApplyPersonId();
        this.systemIdentifier = dto.getSystemIdentifier();
        this.applySystemName = dto.getApplySystemName();
        this.auth = dto.getAuth();
        this.applyPersonDeptName = dto.getApplyPersonDeptName();
        this.applyPersonDeptId = dto.getApplyPersonDeptId();
        this.usePersonResponsible = dto.getUsePersonResponsible();
        this.usePersonResponsiblePhone = dto.getUsePersonResponsiblePhone();
        this.notes = dto.getNotes();
        this.applyTime = dto.getApplyTime();
        this.applyReason = dto.getApplyReason();
        this.applyType = dto.getApplyType();
        this.userKey = dto.getUserKey();
        this.userSecret = dto.getUserSecret();
        this.ipWhitelist = dto.getIpWhitelist();
        this.applyStopTime = dto.getApplyStopTime();
        this.oldId = dto.getOldId();
        this.sameId = dto.getSameId();
        this.dataType = dto.getDataType();
        this.isEffective = dto.getIsEffective();
    }
}

