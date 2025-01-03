package net.risesoft.y9public.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.dto.AuthDictDTO;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_INTERFACE_AUTH_DICT")
@org.hibernate.annotations.Table(comment = "接口鉴权字典", appliesTo = "Y9_INTERFACE_AUTH_DICT")
public class AuthDict extends BaseEntity implements Serializable{

	private static final long serialVersionUID = 3121642144259272227L;

	@Id
	@Column(name = "ID", columnDefinition = "varchar(36) default '' comment 'ID'")
	private String id;

	@Column(name = "INTERFACE_ID", columnDefinition = "varchar(36) default '' comment '接口id'")
	@Comment(value = "接口ID")
	private String interfaceId;

	@Column(name = "PARAMETER_NAME", columnDefinition = "varchar(100) default '' comment '参数名'")
	@Comment(value = "参数名")
	private String parameterName;

	@Column(name = "FIELD_NAME", columnDefinition = "varchar(36) default '' comment '字段名'")
	@Comment(value = "字段名")
	private String fieldName;

	@Column(name = "FIELD_VAL", columnDefinition = "varchar(100) default '' comment '字段值'")
	@Comment(value = "字段值")
	private String fieldVal;

	@Column(name = "SHOW_VAL", columnDefinition = "varchar(100) default '' comment '显示值'")
	@Comment(value = "显示值")
	private String showVal;

	@Column(name = "PARAMETER_TYPE", columnDefinition = "varchar(100) default '' comment '参数类型（公有；私有；）'")
	@Comment(value = "参数类型")
	private String parameterType;

	@Column(name = "PARAMETER_ID", columnDefinition = "varchar(100) default '' comment '参数ID'")
	@Comment(value = "参数ID")
	private String parameterId;

	@Column(name = "IS_DELETE", columnDefinition = "varchar(100) default '' comment '参数类型（Y无效；N有效）'")
	@Comment(value = "是否有效")
	private String isDelete;

	@Column(name = "PID", columnDefinition = "varchar(36) default '' comment '父级id'")
	@Comment(value = "父级id")
	private String pid;

	@Column(name = "SORT", columnDefinition = "int   comment '序号'")
	@Comment(value = "序号")
	private Integer sort;

	@Column(name = "IS_TREE", columnDefinition = "varchar(10) default '' comment '是否树形数据（是；否；）'")
	@Comment(value = "是否树形")
	private String isTree;

	@Column(name = "IS_PRIMARY", columnDefinition = "varchar(10) default '' comment '是否主表数据'")
	@Comment(value = "是否主表数据")
	private String isPrimary;

	@Column(name = "PERSON_ID", columnDefinition = "varchar(36) default '' comment '填写人ID'")
	@Comment(value = "填写人ID")
	private String personId;

	@Column(name = "PERSON_NAME", columnDefinition = "varchar(100) default '' comment '填写人名称'")
	@Comment(value = "填写人名称")
	private String personName;

	public AuthDict(AuthDictDTO dto) {
		this.id = dto.getId();
		this.interfaceId = dto.getInterfaceId();
		this.parameterName = dto.getParameterName();
		this.fieldName = dto.getFieldName();
		this.fieldVal = dto.getFieldVal();
		this.showVal = dto.getShowVal();
		this.parameterType = dto.getParameterType();
		this.parameterId = dto.getParameterId();
		this.isDelete = dto.getIsDelete();
		this.pid = dto.getPid();
		this.sort = dto.getSort();
		this.isTree = dto.getIsTree();
		this.isPrimary = dto.getIsPrimary();
		this.personId = dto.getPersonId();
		this.personName = dto.getPersonName();
	}
}

