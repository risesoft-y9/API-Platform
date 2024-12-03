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
@Table(name = "Y9_INTERFACE_BLACKLISTING")
@org.hibernate.annotations.Table(comment = "黑名单录入", appliesTo = "Y9_INTERFACE_BLACKLISTING")
public class Blacklisting extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121642146259214221L;

    @Id
    @Column(name = "ID", columnDefinition = "varchar(50) default '' comment 'ID'")
    private String id;

    @Column(name = "name", columnDefinition = "varchar(100) default '' comment '名称'")
    @Comment(value = "名称")
    private String name;

    @Column(name = "IP", columnDefinition = "varchar(300) default '' comment '黑名单IP'")
    @Comment(value = "黑名单IP")
    private String ip;

    @Column(name = "INTERFACE_IDS", columnDefinition = "varchar(1000) default '' comment '接口id'")
    @Comment(value = "接口id")
    private String interfaceIds;

    @Column(name = "notes", columnDefinition = "varchar(254) default '' comment '备注'")
    @Comment(value = "备注")
    private String notes;

    @Column(name = "SORT", columnDefinition = "int comment '排序'")
    @Comment(value = "排序")
    private Integer sort;

    @Column(name = "IS_DELETE", columnDefinition = "varchar(10) default 'N' comment '是否删除Y删除，N未删除'")
    @Comment(value = "是否删除")
    private String isDelete;

    @Column(name = "IS_ENABLE", columnDefinition = "varchar(10) default 'N' comment '参数类型（true启用；false停用）'")
    @Comment(value = "是否启用")
    private String isEnable;

}

