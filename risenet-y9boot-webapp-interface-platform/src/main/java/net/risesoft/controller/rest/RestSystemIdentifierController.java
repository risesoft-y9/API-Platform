package net.risesoft.controller.rest;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.SystemIdentifierService;
import net.risesoft.y9public.entity.SystemIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统标识录入
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/identifier")
public class RestSystemIdentifierController {


    @Autowired
    private SystemIdentifierService systemIdentifierService;

    //获取系统标识列表信息
    @RequestMapping("/getPage")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "系统标识管理-获取系统标识管理列表信息")
    public Map<String, Object> getPage(String name, String parameterType, int page, int limit) {
        SystemIdentifier systemIdentifier = new SystemIdentifier();
        systemIdentifier.setName(name);
        systemIdentifier.setParameterType(parameterType);
        Map<String, Object> map = new HashMap<>();
        Page<SystemIdentifier> page1 = systemIdentifierService.getSystemIdentifierPage(systemIdentifier, page, limit);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //获取系统标识列表信息
    @RequestMapping("/getListByType")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "系统标识管理-根据类型获取系统标识列表信息")
    public Map<String, Object> getListByType(String parameterType) {
        Map<String, Object> map = new HashMap<>();
        List<SystemIdentifier> list = systemIdentifierService.getSystemIdentifierList(parameterType);
        map.put("data", list);
        map.put("code", "0");
        return map;
    }

    //获取系统标识列表信息
    @RequestMapping("/getListByPid")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "系统标识管理-根据父级id获取系统标识列表信息")
    public Map<String, Object> getListByPid(String pid) {
        Map<String, Object> map = new HashMap<>();
        List<SystemIdentifier> list = systemIdentifierService.getSystemIdentifierListByPid(pid);
        map.put("data", list);
        map.put("code", "0");
        return map;
    }

    //保存信息
    @PostMapping("/saveInfo")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "系统标识管理-保存系统标识信息")
    public Map<String, Object> saveInterfaceInfo(SystemIdentifier systemIdentifier) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = systemIdentifierService.saveInfo(systemIdentifier);
        map.putAll(dataMap);
        map.put("code", "0");
        return map;
    }

    //根据id获取信息
    @RequestMapping("/getInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "系统标识管理-获取系统标识详细信息")
    public Map<String, Object> getAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        SystemIdentifier identifier = systemIdentifierService.getInfoById(id);
        map.put("data", identifier);
        map.put("code", "0");
        return map;
    }

    //根据id删除权限信息
    @RequestMapping("/delInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "系统标识管理-删除系统标识信息")
    public Map<String, Object> delAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = systemIdentifierService.delInfo(id);
        map.putAll(isOk);
        map.put("code", "0");
        return map;
    }


}
