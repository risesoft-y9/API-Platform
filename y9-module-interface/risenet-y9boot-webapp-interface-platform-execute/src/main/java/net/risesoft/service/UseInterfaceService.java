package net.risesoft.service;

import net.risesoft.y9public.dto.InterfaceManageDTO;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UseInterfaceService {
    //接口转发
    ResponseEntity forward(String id, HttpServletRequest request,HttpServletResponse responseRt);

    //接口测试
    ResponseEntity testForward(InterfaceManageDTO interfaceManageDTO, HttpServletRequest request, HttpServletResponse responseRt);
}
