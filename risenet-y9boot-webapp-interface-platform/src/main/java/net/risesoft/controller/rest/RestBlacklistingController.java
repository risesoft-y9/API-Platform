package net.risesoft.controller.rest;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.BlacklistingService;
import net.risesoft.y9public.entity.Blacklisting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 黑名单录入
 *
 * @author duanzhixin
 */
@Controller
@RequestMapping("/api/rest/blacklisting")
public class RestBlacklistingController {


    @Autowired
    private BlacklistingService blacklistingService;

    //获取系统标识列表信息
    @RequestMapping("/getPage")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "黑名单录入管理-获取黑名单列表信息")
    public Map<String, Object> getPage(String name, int page, int limit) {
        Blacklisting blacklisting = new Blacklisting();
        blacklisting.setName(name);
        Map<String, Object> map = new HashMap<>();
        Page<Blacklisting> page1 = blacklistingService.getPage(blacklisting, page, limit);
        map.put("data", page1.getContent());
        map.put("count", page1.getTotalElements());
        map.put("code", "0");
        return map;
    }


    //保存信息
    @PostMapping("/saveInfo")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "黑名单录入管理-保存黑名单信息")
    public Map<String, Object> saveInterfaceInfo(Blacklisting blacklisting) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = blacklistingService.saveInfo(blacklisting);
        map.putAll(dataMap);
        map.put("code", "0");
        return map;
    }

    //根据id获取信息
    @RequestMapping("/getInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "黑名单录入管理-获取黑名单详细信息")
    public Map<String, Object> getAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        Blacklisting identifier = blacklistingService.getInfoById(id);
        map.put("data", identifier);
        map.put("code", "0");
        return map;
    }

    //根据id删除黑名单信息
    @RequestMapping("/delInfoById")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "系统标识管理-删除系统标识信息")
    public Map<String, Object> delAuthInfoById(String id) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> isOk = blacklistingService.delInfo(id);
        map.putAll(isOk);
        map.put("code", "0");
        return map;
    }

    //修改黑名单启停用
    @PostMapping("/updateEnable")
    @ResponseBody
    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "流程管理-修改流程启用停用状态")
    public Map<String, Object> updateEnable(Blacklisting blacklisting) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> dataMap = blacklistingService.updateEnable(blacklisting);
        if ((boolean) dataMap.get("status")) {
            map.put("status", "success");
        } else {
            map.put("status", "err");
            map.put("msg", dataMap.get("msg"));
        }
        map.put("code", "0");
        return map;
    }

}
