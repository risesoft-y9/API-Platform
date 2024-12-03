package net.risesoft.controller.rest;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.InterfaceApplyService;
import net.risesoft.service.InterfaceManageService;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.vo.ApplyVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口申请信息
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/apply")
public class RestInterfaceApplyController {

    @Autowired
    private InterfaceApplyService interfaceApplyService;
    @Autowired
    private InterfaceManageService interfaceManageService;


    //根据接口id获取申请信息
    @RequestMapping("/getApplyInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口调用申请-获取申请信息")
    public Map<String, Object> getInterfaceById(String id) {
        Map<String, Object> map = new HashMap<>();
        InterfaceApply interfaceApply = interfaceApplyService.getApplyInfoById(id);
        if (interfaceApply != null) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
        }
        map.put("code", "0");
        if (StringUtils.isNotBlank(interfaceApply.getUserSecret())) {
            String secret = interfaceApply.getUserSecret().substring(0, 4) + "******" + interfaceApply.getUserSecret().substring(interfaceApply.getUserSecret().length() - 5, interfaceApply.getUserSecret().length());
            interfaceApply.setUserSecret(secret);
        }
        map.put("data", interfaceApply);
        return map;
    }

    //根据接口id获取申请列表
    @RequestMapping("/getApplyListById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口调用申请-获取每个接口个人的申请列表信息")
    public Map<String, Object> getApplyListById(String id, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        Page<ApplyVo> page1 = interfaceApplyService.getApplyListById(id, page, limit);
        if (page1 != null) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
        }
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        return map;
    }

    //下载密钥
    @RequestMapping("/downLoadSecret")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口调用申请-下载令牌对应的密钥")
    public void downLoadSecret(String id, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        InterfaceApply info = interfaceApplyService.getApplyInfoById(id);
        InterfaceManageDTO interfaceInfo = interfaceManageService.getInterfaceInfoById(info.getInterfaceId());
        response.addHeader("Content-Disposition", "attachment;filename=" + interfaceInfo.getInterfaceName() + "_密钥.txt");
        response.setContentType("application/octet-stream");
        try {
            OutputStream out = new BufferedOutputStream(response.getOutputStream());
            if (StringUtils.isNotBlank(info.getUserSecret())) {
                out.write(info.getUserSecret().getBytes());
                out.flush();
                out.close();
            } else {
                String str = "无密钥，请联系管理员";
                out.write(str.getBytes());
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断当前登录人是否已通过接口调用申请
    @RequestMapping("/findIsPass")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口调用申请-获取个人当前接口的调用是否通过")
    public Map<String, Object> findIsPass(String id, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("isPass", interfaceApplyService.findIsPass(id));
        map.put("status", "success");
        return map;
    }
}
