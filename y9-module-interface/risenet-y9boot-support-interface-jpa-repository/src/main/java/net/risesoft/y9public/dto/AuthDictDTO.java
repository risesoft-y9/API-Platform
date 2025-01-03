package net.risesoft.y9public.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.AuthDict;
import net.risesoft.y9public.entity.BaseEntity;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class AuthDictDTO extends BaseEntity implements Serializable{

	private static final long serialVersionUID = 3121642144259272227L;

	private String id;

	private String interfaceId;

	private String parameterName;

	private String fieldName;

	private String fieldVal;

	private String showVal;

	private String parameterType;

	private String parameterId;

	private String isDelete;

	private String pid;

	private Integer sort;

	private String isTree;

	private String isPrimary;

	private String personId;

	private String personName;

	private Boolean hasChildren;

	public AuthDictDTO(AuthDict dto) {
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

