package net.risesoft.controller.rest;

import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : lxd
 * @description : 用于统计首页可视化的统计数据
 * @createDate : 2024/10/30 14:48:00
 */
@RestController
@RequestMapping("/api/rest/statistics")
public class RestStatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 接口概况：注册数、发布数、停用数
     *
     * @return
     */
    @GetMapping("/interfaceOverview")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口概况：注册数、发布数、停用数")
    public Map<String, Object> getInterfaceOverview() {
        return statisticsService.getInterfaceOverview();
    }

    /**
     * 获取接口运行正常、异常数
     *
     * @return
     */
    @GetMapping("/runningCount")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口运行正常、异常数")
    public Map<String, Object> getRunningCount() {
        return statisticsService.getRunningCount();
    }

    /**
     * 接口调用趋势图
     *
     * @param type 当天、近一周、近一月、近一年
     * @return
     */
    @GetMapping("/trend")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口调用趋势图")
    public Map<String, Object> getCallTrend(String type) {
        return statisticsService.getCallTrend(type);
    }

    /**
     * 日志概况 : 总调用量、今日调用量、总异常量、今日异常量
     *
     * @return
     */
    @GetMapping("/logOverview")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取日志概况统计数：总调用量、今日调用量、总异常量、今日异常量")
    public Map<String, Object> getLogOverview() {
        return statisticsService.getLogOverview();
    }

    /**
     * 异常情况列表
     *
     * @param page  页码
     * @param limit 每页大小
     * @return
     */
    @GetMapping("/exception")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口调用异常情况列表")
    public Map<String, Object> getException(int page, int limit) {
        return statisticsService.getException(page, limit);
    }

    /**
     * 接口日志情况列表
     *
     * @param conditionMap 筛选条件
     * @return
     */
    @PostMapping("/logMonitoring")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "获取接口调用日志情况列表")
    public Map<String, Object> getLogMonitoringInfo(@RequestBody Map<String, Object> conditionMap) {
        if (conditionMap == null) {
            Map res = new HashMap();
            res.put("code", "1");
            return res;
        }
        return statisticsService.getLogMonitoringInfo(conditionMap);
    }

    /**
     * 获取接口日志检索选项值
     *
     * @return
     */
    @GetMapping("/logMonitoring/options")
    public Map<String, Object> getLogMonitoringOptions() {
        return statisticsService.getLogMonitoringOptions();
    }

    /**
     * 接口实时日志播报
     *
     * @param page  页码
     * @param limit 每页大小
     * @return
     */
    @GetMapping("/realTimeLog")
    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "接口实时日志播报")
    public Map<String, Object> getRealTimeLog(int page, int limit) {
        return statisticsService.getRealTimeLog(page, limit);
    }
}
