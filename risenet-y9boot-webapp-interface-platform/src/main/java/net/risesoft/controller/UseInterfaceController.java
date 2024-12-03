package net.risesoft.controller;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.UseInterfaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 外部调用
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/openInterface")
public class UseInterfaceController {

    @Autowired
    private UseInterfaceService useInterfaceService;

    /**
     * 接口转发
     *
     * @param userKey
     * @return
     */
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "接口转发")
    @RequestMapping("/forward")
    public ResponseEntity personPage(HttpServletRequest request, String userKey) {
        if (StringUtils.isBlank(userKey)) {
            userKey = request.getHeader("userKey");
        }
        ResponseEntity responseEntity = ResponseEntity.ok(useInterfaceService.forward(userKey, request));
        return responseEntity;
    }


}
