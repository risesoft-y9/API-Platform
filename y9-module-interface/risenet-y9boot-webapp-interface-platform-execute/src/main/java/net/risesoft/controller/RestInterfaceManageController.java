package net.risesoft.controller;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.UseInterfaceService;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口管理
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/interface")
public class RestInterfaceManageController {

    @Autowired
    private UseInterfaceService useInterfaceService;

    //测试接口
    @PostMapping(value = "/testInterface")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "接口测试")
    public ResponseEntity testInterface(@RequestBody InterfaceManageDTO interfaceManageDTO, HttpServletRequest request,HttpServletResponse responseRt) {
        Map<String, Object> map = new HashMap<>();
        return useInterfaceService.testForward(interfaceManageDTO,request,responseRt);
    }
}
