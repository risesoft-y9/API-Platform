package net.risesoft.controller.rest;


import net.risesoft.enums.platform.ManagerLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.AuthDictService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.AuthDict;
import net.risesoft.y9public.dto.AuthDictDTO;
import org.apache.commons.lang3.StringUtils;
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
 * 权限信息
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/auth")
public class RestAuthController {


    @Autowired
    private AuthDictService authDictService;

    //获取参数key
    @RequestMapping("/getAuthKey")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "已接入接口-获取审批进度信息")
    public Map<String, Object> getAuthKeyList(String interfaceId, String fieldName, String parameterName, String parameterType, int page, int limit) {
        AuthDict authDict = new AuthDict();
        authDict.setFieldName(fieldName != null ? fieldName : "");
        authDict.setParameterName(parameterName != null ? parameterName : "");
        authDict.setInterfaceId(interfaceId != null ? interfaceId : "");
        authDict.setParameterType(parameterType != null ? parameterType : "");
        authDict.setIsPrimary("Y");
        Map<String, Object> rtData = hasRoleRtPersonId();
        if (!(boolean) rtData.get("haveRole")) {
            authDict.setPersonId(rtData.get("personId").toString());
        }
        Map<String, Object> map = new HashMap<>();
        Page<AuthDict> page1 = authDictService.getAuthDictList(authDict, page, limit);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //获取参数key
    @RequestMapping("/getAuthKeyView")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "已接入接口-获取审批进度信息")
    public Map<String, Object> getAuthKeyViewList(String checkedIds, int page, int limit) {
        AuthDict authDict = new AuthDict();
        authDict.setIsPrimary("Y");
        authDict.setId(checkedIds);
        Map<String, Object> map = new HashMap<>();
        Page<AuthDict> page1 = authDictService.getAuthDictList(authDict, page, limit);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //获取参数key
    @RequestMapping("/getAuthVal")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "权限配置管理-获取权限值列表信息")
    public Map<String, Object> getAuthValList(String interfaceId, String parameterId, String showVal, String fieldVal, String pid, int page, int limit) {
        Map<String, Object> map = new HashMap<>();
        AuthDict authDict = new AuthDict();
        authDict.setFieldVal(fieldVal != null ? fieldVal : "");
        authDict.setShowVal(showVal != null ? showVal : "");
        if (StringUtils.isNotBlank(parameterId)) {
            authDict.setParameterId(parameterId);
        } else {
            map.put("code", "500");
            map.put("msg", "参数ID为空，请确认！");
            return map;
        }
        if (StringUtils.isNotBlank(interfaceId)) {
            authDict.setInterfaceId(interfaceId);
        }
        authDict.setIsPrimary("N");

        if (StringUtils.isNotBlank(pid) && !"-1".equals(pid)) {
            authDict.setPid(pid);
            List<AuthDictDTO> list = authDictService.getAuthDictTreeList(authDict);
            map.put("data", list);
        } else {
            Page<AuthDict> page1 = authDictService.getAuthDictList(authDict, page, limit);
            map.put("data", page1.getContent());
            map.put("count", page1.getTotalElements());
        }
        map.put("code", "0");
        return map;
    }

    //保存权限信息
    @PostMapping("/saveAuthDictInfo")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "权限配置管理-保存权限信息")
    public Map<String, Object> saveInterfaceInfo(AuthDict authDict) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = authDictService.saveAuthDictInfo(authDict);
        map.putAll(dataMap);
        map.put("code", "0");
        return map;
    }

    //根据id获取权限信息
    @RequestMapping("/getAuthInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "权限配置管理-获取权限详细信息")
    public Map<String, Object> getAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        AuthDict authDict = authDictService.getAuthDictInfoById(id);
        map.put("data", authDict);
        map.put("code", "0");
        return map;
    }

    //根据id删除权限信息
    @RequestMapping("/delAuthInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "权限配置管理-删除权限信息")
    public Map<String, Object> delAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = authDictService.delInfo(id);
        map.putAll(isOk);
        map.put("code", "0");
        return map;
    }

    //根据id删除权限信息
    @RequestMapping("/delAuthInfoByIdPersonal")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "权限配置管理-删除权限信息")
    public Map<String, Object> delAuthInfoByIdPersonal(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = authDictService.delInfoPerson(id);
        map.putAll(isOk);
        map.put("code", "0");
        return map;
    }

    private Map<String, Object> hasRoleRtPersonId() {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        //判断登录人是否有管理员权限
        if (person.getManagerLevel().getValue() == ManagerLevelEnum.SYSTEM_MANAGER.getValue()) {
            map.put("haveRole", true);
        } else {
            map.put("haveRole", false);
        }
        map.put("personId", person.getPersonId());
        return map;
    }
}
