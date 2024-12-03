package net.risesoft.service;

import java.util.Map;

/**
 * @author : lxd
 * @description : 用于统计首页可视化的统计数据
 * @createDate : 2024/10/30 14:48
 */
public interface StatisticsService {

    Map<String, Object> getInterfaceOverview();

    Map<String, Object> getCallTrend(String type);

    Map<String, Object> getLogOverview();

    Map<String, Object> getException(int page, int limit);

    Map<String, Object> getLogMonitoringInfo(Map<String, Object> conditionMap);

    Map<String, Object> getRealTimeLog(int page, int limit);

    Map<String, Object> getRunningCount();

    Map<String, Object> getLogMonitoringOptions();
}
