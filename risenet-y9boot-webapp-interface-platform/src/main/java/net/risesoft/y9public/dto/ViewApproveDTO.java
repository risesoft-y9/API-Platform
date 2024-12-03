package net.risesoft.y9public.dto;


import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class ViewApproveDTO implements Serializable {

    private static final long serialVersionUID = 3121642145259274427L;


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

    private Integer page;

    private Integer limit;
    private String statusSort;

    private Date createTime;
    private Date applyTime;

    private String statusRole;
    private String alreadyApproveUser;
    private String currentUserId;
    private String startDate;
    private String endDate;
}

