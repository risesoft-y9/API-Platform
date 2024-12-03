package net.risesoft.y9public.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.ViewApprove;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class ViewApproveVo implements Serializable {

    private static final long serialVersionUID = 3121644149259274427L;

    private String approveId;

    private String interfaceId;

    private String applyId;

    private String approveStatus;

    private String illustrate;

    private String applyType;

    private String notes;

    private String isOver;

    private String approveInterfaceStatus;

    private String approvePersonName;

    private String approvePersonId;

    private String applyPersonName;

    private String applyPersonId;

    private String interfaceName;

    private String interfaceStatus;

    private String version;
    private String statusSort;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;
    private String alreadyApproveUser;

    private String currentUserId;

    private String isNow;
    private String isLimitData;

    public ViewApproveVo(ViewApprove dto) {
        this.approveId = dto.getApproveId();
        this.interfaceId = dto.getInterfaceId();
        this.applyId = dto.getApplyId();
        this.approveStatus = dto.getApproveStatus();
        this.illustrate = dto.getIllustrate();
        this.applyType = dto.getApplyType();
        this.notes = dto.getNotes();
        this.isOver = dto.getIsOver();
        this.approveInterfaceStatus = dto.getApproveInterfaceStatus();
        this.approvePersonName = dto.getApprovePersonName();
        this.approvePersonId = dto.getApprovePersonId();
        this.applyPersonName = dto.getApplyPersonName();
        this.applyPersonId = dto.getApplyPersonId();
        this.interfaceName = dto.getInterfaceName();
        this.interfaceStatus = dto.getInterfaceStatus();
        this.version = dto.getVersion();
        this.createTime = dto.getCreateTime();
        this.alreadyApproveUser = dto.getAlreadyApproveUser();
        this.currentUserId = dto.getCurrentUserId();
        this.applyTime = dto.getApplyTime();
        this.isLimitData = dto.getIsLimitData();
        this.statusSort = dto.getStatusSort();
    }
}

