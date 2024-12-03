package net.risesoft.y9public.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class ApplyVo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3121652146259274217L;

    private String id;

    private String applyPersonName;

    private String approveStatus;

    private String applyReason;

    private String interfaceId;

    private String sameId;

    private String approveId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    protected Date createTime;

    public ApplyVo(String id, String applyPersonName, String approveStatus, Date createTime, String applyReason, String interfaceId, String sameId, String approveId) {
        this.id = id;
        this.applyPersonName = applyPersonName;
        this.approveStatus = approveStatus;
        this.createTime = createTime;
        this.applyReason = applyReason;
        this.interfaceId = interfaceId;
        this.sameId = sameId;
        this.approveId = approveId;
    }
}

