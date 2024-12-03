package net.risesoft.service;

import net.risesoft.y9public.dto.InterfaceManageDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UseInterfaceService {
    //接口转发
    Map<String, Object> forward(String id, HttpServletRequest request);

    //接口测试
    Map<String, Object> testForward(InterfaceManageDTO interfaceManageDTO, HttpServletRequest request);
}
