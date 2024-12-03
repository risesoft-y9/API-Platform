package net.risesoft.y9public.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.Parameter;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class ParameterDTO implements Serializable {

    private static final long serialVersionUID = 3821646146259274267L;

    private String id;

    private String interfaceId;

    private String parameterKey;

    private String val;

    private String defaultVal;

    private String parameterType;

    private String required;

    private String notes;

    private String personId;

    private String parameterStatus;

    private String pid;

    private Integer level;

    private Integer sort;

    private String isItems;

    private List<ParameterDTO> children;

    public ParameterDTO(Parameter dto) {
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

