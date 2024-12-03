package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.dto.ParameterDTO;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_PARAMETER")
@org.hibernate.annotations.Table(comment = "接口参数表", appliesTo = "Y9_INTERFACE_PARAMETER")
public class Parameter extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
    private String id;

    @Column(name = "INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '接口id'")
    @Comment(value = "接口ID")
    private String interfaceId;

    @Column(name = "PARAMETER_KEY", columnDefinition = "varchar(500) default '' comment '参数key'")
    @Comment(value = "参数key")
    private String parameterKey;

    @Column(name = "VAL", columnDefinition = "varchar(500) default '' comment '参数值'")
    @Comment(value = "参数值")
    private String val;

    @Column(name = "DEFAULT_VAL", columnDefinition = "varchar(500) default '' comment '默认参数值'")
    @Comment(value = "默认参数值")
    private String defaultVal;

    @Column(name = "PARAMETER_TYPE", columnDefinition = "varchar(20) default '' comment '参数类型'")
    @Comment(value = "参数类型")
    private String parameterType;

    @Column(name = "REQUIRED", columnDefinition = "varchar(10) default '' comment '是否必填Y是，N否'")
    @Comment(value = "是否必填")
    private String required;

    @Column(name = "NOTES", columnDefinition = "varchar(254) default '' comment '参数说明'")
    @Comment(value = "参数说明")
    private String notes;

    @Column(name = "PERSON_ID", columnDefinition = "varchar(36) default '' comment '填写人ID'")
    @Comment(value = "填写人ID")
    private String personId;

    @Column(name = "PARAMETER_STATUS", columnDefinition = "varchar(10) default '' comment '参数状态（1请求头参数，2请求参数，3返回参数）'")
    @Comment(value = "参数状态")
    private String parameterStatus;

    @Column(name = "PID", columnDefinition = "varchar(36) default '' comment '父级id'")
    @Comment(value = "父级id")
    private String pid;

    @Column(name = "LEVEL", columnDefinition = "int  comment '节点层级，根节点层级为1'")
    @Comment(value = "节点层级")
    private Integer level;

    @Column(name = "IS_ITEMS", columnDefinition = "varchar(10) default '' comment '是否数组'")
    @Comment(value = "是否数组")
    private String isItems;

    @Column(name = "SORT", columnDefinition = "int  comment '序号'")
    @Comment(value = "序号")
    private Integer sort;

    public Parameter(ParameterDTO dto) {
        this.id = dto.getId();
        this.interfaceId = dto.getInterfaceId();
        this.parameterKey = dto.getParameterKey();
        this.val = dto.getVal();
        this.defaultVal = dto.getDefaultVal();
        this.parameterType = dto.getParameterType();
        this.required = dto.getRequired();
        this.notes = dto.getNotes();
        this.personId = dto.getPersonId();
        this.parameterStatus = dto.getParameterStatus();
        this.pid = dto.getPid();
        this.level = dto.getLevel();
        this.isItems = dto.getIsItems();
        this.sort = dto.getSort();
    }
}

