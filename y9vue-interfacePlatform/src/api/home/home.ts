
import Request from "@/api/lib/request";
const platformRequest = Request();

/**
 * 获取接口概况统计数据
 * @returns
 */
export const getInterfaceOverview = async () => {
    return await platformRequest({
        url: "/api/rest/statistics/interfaceOverview",
        method: 'GET',
        cType: false,
    });
};

export const getRunningCountData = async () => {
    return await platformRequest({
        url: "/api/rest/statistics/runningCount",
        method: 'GET',
        cType: false,
    });
};

/**
 * 接口调用趋势图
 * @params 当天、近一周、近一月、近一年
 * @returns
 */
export const getCallTrend = async (params) => {
    return await platformRequest({
        url: "/api/rest/statistics/trend",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 日志概况 : 总调用量、今日调用量、总异常量、今日异常量
 * @returns
 */
export const getLogOverview = async () => {
    return await platformRequest({
        url: "/api/rest/statistics/logOverview",
        method: 'GET',
        cType: false,
    });
};

/**
 * 异常情况列表
 * @param params ：page、limit
 * @returns
 */
export const getExceptionData = async (params) => {
    return await platformRequest({
        url: "/api/rest/statistics/exception",
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 接口限流情况列表
 * @param params ：page、limit
 * @returns
 */
export const getLimitSituation = async (params) => {
    return await platformRequest({
        url: "/api/rest/statistics/limitSituation",
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 接口实时日志播报
 * @param params ：page、limit
 * @returns
 */
export const getRealTimeLog = async (params) => {
    return await platformRequest({
        url: "/api/rest/statistics/realTimeLog",
        method: 'GET',
        cType: false,
        params: params,
    });
};