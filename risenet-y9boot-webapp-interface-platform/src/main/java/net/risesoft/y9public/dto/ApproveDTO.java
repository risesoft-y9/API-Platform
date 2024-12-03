package net.risesoft.y9public.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.y9public.entity.Approve;
import net.risesoft.y9public.entity.BaseEntity;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ApproveDTO extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121652146259274217L;

    private String id;

    private String interfaceId;

    private String applyId;

    private String illustrate;

    private String approveStatus;

    private String notes;

    private String personName;

    private String personId;

    private String interfaceStatus;

    private String isOver;

    private String isNew;

    private String interfaceName;

    private String version;

    private String approveOption;

    private String flowNode;

    private String alreadyApproveUser;

    private String currentUserId;

    private String currentNode;

    public ApproveDTO(Approve dto) {
        this.id = dto.getId();
        this.interfaceId = dto.getInterfaceId();
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

