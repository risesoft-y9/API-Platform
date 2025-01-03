package net.risesoft.y9public.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.y9public.entity.AuthDict;

import java.util.List;

/**
 * 构建权限信息前端下拉框数据
 */
@Data
@NoArgsConstructor
public class AuthSelectVo {
    private String id;
    private String label;
    private String value;
    private String pid;
    private Boolean disabled;
    private List<AuthSelectVo> children;

    public AuthSelectVo(AuthDict dto) {
        this.id = dto.getId();
        this.label = dto.getShowVal();
        this.value = dto.getFieldVal();
        this.pid = dto.getPid();
    }
}
