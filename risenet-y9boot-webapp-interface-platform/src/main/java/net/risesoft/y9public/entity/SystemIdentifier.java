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
@Table(name = "Y9_INTERFACE_SYSTEM_IDENTIFIER")
@org.hibernate.annotations.Table(comment = "系统标识录入", appliesTo = "Y9_INTERFACE_SYSTEM_IDENTIFIER")
public class SystemIdentifier extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
    private String id;

    @Column(name = "name", columnDefinition = "varchar(100) default '' comment 'name'")
    @Comment(value = "名称")
    private String name;

    @Column(name = "PARAMETER_TYPE", columnDefinition = "varchar(20) default '' comment '数据类型0是单位，1是系统'")
    @Comment(value = "数据类型")
    private String parameterType;

    @Column(name = "PID", columnDefinition = "varchar(36) default '' comment '父级id'")
    @Comment(value = "父级id")
    private String pid;

    @Column(name = "PNAME", columnDefinition = "varchar(100) default '' comment '父级名称'")
    @Comment(value = "父级名称")
    private String pname;

    @Column(name = "SORT", columnDefinition = "int comment '排序'")
    @Comment(value = "排序")
    private Integer sort;

    @Column(name = "IS_DELETE", columnDefinition = "varchar(10) default 'N' comment '是否删除Y删除，N未删除'")
    @Comment(value = "是否删除")
    private String isDelete;

}

