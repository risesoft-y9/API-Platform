package net.risesoft.y9public.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.BaseEntity;
import net.risesoft.y9public.entity.InterfaceLimitInfo;
import net.risesoft.y9public.entity.InterfaceManage;
import net.risesoft.y9public.entity.Parameter;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class InterfaceManageDTO extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3226632145259274287L;


    private String id;

    private String interfaceName;

    private String sameInterfaceId;

    private String interfaceUrl;

    private String parameterIds;

    private String isAuth;

    private String isLimit;

    private String isBack;

    private String illustrate;

    private String interfaceType;

    private String interfaceStatus;

    private String interfaceMethod;

    private String version;

    private String notes;

    private String personName;

    private String personId;

    private String deptInfo;

    private String deptId;

    private String systemName;

    private String systemId;

    private String head;

    private String isOverwrite;

    private String interfaceFileUrl;

    private String interfaceFileName;

    private String isDelete;

    private String isTest;

    private String networkAgreement;

    private String headPhone;

    private String overwriteInterfaceId;

    private String nameSpace;

    private String method;

    private String webSpecification;

    private Integer page;

    private Integer limit;

    private String parameters;

    private String resParameters;

    private String reqParameters;

    private String limitInfo;

    private String approveStatus;

    private String mayApply;

    private String isLimitData;

    private List<Parameter> parameterList;

    private InterfaceLimitInfo limitInfoEntity;

    public InterfaceManageDTO(InterfaceManage dto) {
        this.id = dto.getId();
        this.interfaceName = dto.getInterfaceName();
        this.sameInterfaceId = dto.getSameInterfaceId();
        this.interfaceUrl = dto.getInterfaceUrl();
        this.parameterIds = dto.getParameterIds();
        this.isAuth = dto.getIsAuth();
        this.isLimit = dto.getIsLimit();
        this.illustrate = dto.getIllustrate();
        this.interfaceType = dto.getInterfaceType();
        this.interfaceStatus = dto.getInterfaceStatus();
        this.interfaceMethod = dto.getInterfaceMethod();
        this.version = dto.getVersion();
        this.notes = dto.getNotes();
        this.personName = dto.getPersonName();
        this.personId = dto.getPersonId();
        this.deptInfo = dto.getDeptInfo();
        this.head = dto.getHead();
        this.isOverwrite = dto.getIsOverwrite();
        this.interfaceFileUrl = dto.getInterfaceFileUrl();
        this.isDelete = dto.getIsDelete();
        this.isTest = dto.getIsTest();
        this.networkAgreement = dto.getNetworkAgreement();
        this.headPhone = dto.getHeadPhone();
        this.overwriteInterfaceId = dto.getOverwriteInterfaceId();
        this.nameSpace = dto.getNameSpace();
        this.method = dto.getMethod();
        this.webSpecification = dto.getWebSpecification();
        this.isLimitData = dto.getIsLimitData();
        this.deptId = dto.getDeptId();
        this.systemId = dto.getSystemId();
        this.systemName = dto.getSystemName();
    }
}

