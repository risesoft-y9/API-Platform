package net.risesoft.controller.rest;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.ApproveService;
import net.risesoft.service.InterfaceApplyService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.ViewApproveDTO;
import net.risesoft.y9public.entity.*;
import net.risesoft.y9public.vo.ViewApproveVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/approve")
public class RestApproveController {

    @Autowired
    private ApproveService approveService;

    @Autowired
    private InterfaceApplyService interfaceApplyService;

    //获取需要审批接口列表信息
    @RequestMapping("/getApproveList")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口审批主页-审批列表数据")
    public Map<String, Object> getApproveList(ViewApproveDTO viewApproveDTO) {
        Map<String, Object> map = new HashMap<>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        viewApproveDTO.setApprovePersonId(userInfo.getPersonId());
        Page<ViewApproveVo> page1 = approveService.getViewApproveList(viewApproveDTO);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //获取需要审批信息通过id
    @RequestMapping("/getApproveById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "接口审批主页-审批信息详细")
    public Map<String, Object> getApproveById(String id) {
        Map<String, Object> map = new HashMap<>();
        Approve approve = approveService.findApproveById(id);
        if (StringUtils.isNotBlank(approve.getApplyId())) {
            map.put("applyInfo", interfaceApplyService.getApplyInfoById(approve.getApplyId()));
        }
        map.put("data", approve);
        map.put("code", "0");
        return map;
    }


    //通过审批接口信息
    @RequestMapping("/submit")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "接口审批主页-修改审批信息")
    public Map<String, Object> submit(@RequestBody Approve approve) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = approveService.submitData(approve);
        if ("true".equals(isOk.get("status"))) {
            map.put("status", "success");
        } else {
            map.putAll(isOk);
            map.put("status", "error");
        }
        map.put("code", "0");
        return map;
    }

    //拒绝审批接口信息
    @RequestMapping("/refuse")
    @ResponseBody
    public Map<String, Object> refuse(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = approveService.refuseApproveInfo(approve);
        if ("true".equals(isOk.get("status"))) {
            map.put("status", "success");
        } else {
            map.putAll(isOk);
            map.put("status", "error");
        }
        map.put("code", "0");
        return map;
    }

    //保存接口信息
    @RequestMapping("/saveApproveInfo")
    @ResponseBody
    public Map<String, Object> saveApproveInfo(Approve approve) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = approveService.saveApproveInfo(approve);
        if ("true".equals(isOk.get("status"))) {
            map.put("status", "success");
        } else {
            map.putAll(isOk);
            map.put("status", "error");
        }
        map.put("code", "0");
        return map;
    }

    //获取审批进度信息
    @RequestMapping("/getApproveByInterfaceId")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "已接入接口-获取审批进度信息")
    public Map<String, Object> getApproveByInterfaceId(String id, Boolean applyType) {
        Map<String, Object> map = new HashMap<>();
        List<Approve> approve = approveService.getApproveByInterfaceId(id, applyType);
        if (approve != null && approve.size() != 0) {
            if (StringUtils.isNotBlank(approve.get(0).getApplyId())) {
                map.put("applyInfo", interfaceApplyService.getApplyInfoById(approve.get(0).getApplyId()));
            }
            map.put("data", approve.get(0));
        } else {
            map.put("data", "");
        }
        map.put("code", "0");
        return map;
    }
}
