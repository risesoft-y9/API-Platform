package net.risesoft.y9public.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.y9public.entity.InterfaceApply;


import java.io.Serializable;

@Data
@NoArgsConstructor
public class InterfaceApplyDTO implements Serializable {

    private static final long serialVersionUID = 3121642146259274227L;

    private String id;

    private String interfaceId;

    private String applyPersonName;

    private String applyPersonId;

    private String systemIdentifier;

    private String applySystemName;

    private String auth;

    private String applyPersonDeptName;

    private String applyPersonDeptId;

    private String usePersonResponsible;

    private String usePersonResponsiblePhone;

    private String notes;

    private String applyTime;

    private String applyReason;

    private String applyType;

    private String userKey;

    private String userSecret;

    private String ipWhitelist;

    private String applyStopTime;

    private String oldId;

    private String sameId;

    private String dataType;

    private String isEffective;

    private String flowId;

    private String userData;


    public InterfaceApplyDTO(InterfaceApply dto) {
        this.id = dto.getId();
        this.interfaceId = dto.getInterfaceId();
        this.applyPersonName = dto.getApplyPersonName();
        this.applyPersonId = dto.getApplyPersonId();
        this.systemIdentifier = dto.getSystemIdentifier();
        this.auth = dto.getAuth();
        this.applyPersonDeptName = dto.getApplyPersonDeptName();
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
        this.applySystemName = dto.getApplySystemName();
        this.applyPersonDeptId = dto.getApplyPersonDeptId();
    }
}

