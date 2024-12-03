package net.risesoft.controller.rest;

import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.InterfaceStatus;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.InterfaceManageService;
import net.risesoft.service.UseInterfaceService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.dto.InterfaceApplyDTO;
import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口管理
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/interface")
public class RestInterfaceManageController {


    @Autowired
    private InterfaceManageService interfaceManageService;

    @Autowired
    private UseInterfaceService useInterfaceService;


    //获取已接入接口列表信息
    @RequestMapping("/getInterfaceListByPersonId")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口管理-获取接口管理列表信息")
    public Map<String, Object> getServerInfoList(InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> map = new HashMap<>();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        interfaceManageDTO.setPersonId(person.getPersonId());
        Page<InterfaceManageDTO> page1 = interfaceManageService.getInterfaceList(interfaceManageDTO);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //保存接口信息
    @PostMapping(value = "/saveInterfaceInfo")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "已接入接口-接口信息录入保存")
    public Map<String, Object> saveInterfaceInfo(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = interfaceManageService.saveInterfaceInfo(interfaceManageDTO);
        map.putAll(dataMap);
        map.put("code", "0");
        return map;
    }

    //导出接口信息
    @RequestMapping(value = "/exportInterface")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "导出接口信息")
    public void exportInterface(String ids, HttpServletResponse response) {
        String[] split = ids.split(",");
        interfaceManageService.exportFile(Arrays.stream(split).collect(Collectors.toList()), response);
    }

    //导入接口信息
    @RequestMapping(value = "/uploadInterface")
    @ResponseBody
    @Transactional
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "导入接口信息")
    public Map<String, Object> uploadInterface(MultipartFile file, String isOverWrite) {
        Map<String, Object> map = interfaceManageService.uploadFile(file, Boolean.valueOf(isOverWrite));
        map.put("code", "0");
        return map;
    }

    //获取接口id
    @RequestMapping("/getInterfaceId")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口id")
    public Map<String, Object> getInterfaceId(String id) {
        Map<String, Object> map = new HashMap<>();
        String newId = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        map.put("code", "0");
        map.put("data", newId);
        return map;
    }

    //删除接口信息
    @RequestMapping("/delInterfaceById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除接口信息")
    public Map<String, Object> delInterfaceById(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = interfaceManageService.delInterfaceInfo(id);
        if ("true".equals(isOk.get("status"))) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg"));
        }
        map.put("code", "0");
        return map;
    }


    //获取接口信息
    @RequestMapping("/getInterfaceById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口详细信息")
    public Map<String, Object> getInterfaceById(String id) {
        Map<String, Object> map = new HashMap<>();
        InterfaceManageDTO interfaceManage = interfaceManageService.getInterfaceInfoById(id);
        if (interfaceManage != null) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
        }
        map.put("code", "0");
        map.put("data", interfaceManage);
        return map;
    }

    //根据接口id获取权限信息
    @RequestMapping("/getAuthListByInterfaceId")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "根据接口id获取数据权限信息")
    public Map<String, Object> getAuthListByInterfaceId(String id, Boolean isView) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> mapList = interfaceManageService.getAuthListByInterfaceId(id, isView);
        if (mapList != null) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
        }
        map.put("code", "0");
        map.put("data", mapList);
        map.put("requiredData", interfaceManageService.getRequiredParameterData(id));
        return map;
    }


    //发布接口信息
    @RequestMapping("/pubInterface")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "已接入接口-发布接口")
    public Map<String, Object> pubInterface(@RequestBody InterfaceApplyDTO interfaceApply) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = interfaceManageService.pubInterface(interfaceApply);
        if ((boolean) isOk.get("status")) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg").toString());
        }
        map.put("code", "0");
        return map;
    }

    //停用接口
    @RequestMapping("/stopInterface")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "已接入接口-停用接口")
    public Map<String, Object> stopInterface(@RequestBody InterfaceApplyDTO interfaceApply) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = interfaceManageService.stopInterface(interfaceApply);
        if ((boolean) isOk.get("status")) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg").toString());
        }
        map.put("code", "0");
        return map;
    }

    //获取可申请接口列表信息
    @RequestMapping("/getMayApplyInterfaceList")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取可申请接口列表")
    public Map<String, Object> getMayApplyInterfaceList(InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> map = new HashMap<>();
        interfaceManageDTO.setMayApply(InterfaceStatus.APPROVE.getName());
        Page<InterfaceManageDTO> page1 = interfaceManageService.getInterfaceList(interfaceManageDTO);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }

    //获取已申请接口列表信息
    @RequestMapping("/getAlreadyApplyInterfaceList")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取已申请接口列表")
    public Map<String, Object> getAlreadyApplyInterfaceList(InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> map = new HashMap<>();
        interfaceManageDTO.setMayApply("申请");
        Page<InterfaceManageDTO> page1 = interfaceManageService.getInterfaceList(interfaceManageDTO);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }


    //接口版本维护
    @RequestMapping("/updateVersion")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "修改接口版本信息")
    public Map<String, Object> updateVersion(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = interfaceManageService.updateVersionInfo(interfaceManageDTO);
        map.putAll(dataMap);
        map.put("code", "0");
        return map;
    }

    //删除接口信息
    @RequestMapping("/delAuthInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除接口信息")
    public Map<String, Object> delAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = interfaceManageService.delAuthInfoById(id);
        if ("true".equals(isOk.get("status"))) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg"));
        }
        map.put("code", "0");
        return map;
    }

    //调用申请接口信息
    @RequestMapping("/useInterfaceApply")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "接口调用申请")
    public Map<String, Object> useInterfaceApply(@RequestBody InterfaceApplyDTO interfaceApply) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = interfaceManageService.useInterfaceApply(interfaceApply, true);
        if ((boolean) isOk.get("status")) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg").toString());
        }
        map.put("code", "0");
        return map;
    }

    //调用申请接口信息
    @RequestMapping("/updateUseInterfaceApply")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "变更接口调用申请")
    public Map<String, Object> updateUseInterfaceApply(@RequestBody InterfaceApplyDTO interfaceApply) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> isOk = interfaceManageService.useInterfaceApply(interfaceApply, false);
        if ((boolean) isOk.get("status")) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
            map.put("msg", isOk.get("msg").toString());
        }
        map.put("code", "0");
        return map;
    }

    //获取接口调用申请信息
    @RequestMapping("/getApplyInfoByInterfaceId")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口调用申请信息")
    public Map<String, Object> getApplyInfoByInterfaceId(String id) {
        Map<String, Object> map = new HashMap<>();
        InterfaceApply applyInfo = interfaceManageService.getApplyInfoByInterfaceId(id);
        if (applyInfo != null) {
            map.put("status", "success");
        } else {
            map.put("status", "error");
        }
        map.put("code", "0");
        if (StringUtils.isNotBlank(applyInfo.getUserSecret())) {
            String secret = applyInfo.getUserSecret().substring(0, 4) + "******" + applyInfo.getUserSecret().substring(applyInfo.getUserSecret().length() - 5);
            applyInfo.setUserSecret(secret);
        }
        map.put("data", applyInfo);
        return map;
    }

    //测试接口
    @PostMapping(value = "/testInterface")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "接口测试")
    public Map<String, Object> testInterface(@RequestBody InterfaceManageDTO interfaceManageDTO, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = useInterfaceService.testForward(interfaceManageDTO, request);
        map.put("data", dataMap);
        map.put("code", "0");
        return map;
    }

    //上传接口文档
    @RequestMapping(value = "/uploadInterfaceFile")
    @ResponseBody
    @Transactional
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "上传接口文档")
    public Map<String, Object> uploadInterfaceFile(MultipartFile file, String interfaceId) {
        Map<String, Object> map = interfaceManageService.uploadFile(file, interfaceId);
        map.put("code", "0");
        return map;
    }

    //下载接口文档
    @RequestMapping(value = "/downLoadInterfaceFile")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "下载接口文档")
    public void downLoadInterfaceFile(String sameId, String version, String fileName, HttpServletResponse response) {
        interfaceManageService.downLoadInterfaceFile(sameId, version, fileName, response);
    }
}
