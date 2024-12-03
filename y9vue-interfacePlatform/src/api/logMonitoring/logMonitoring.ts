
import Request from "@/api/lib/request";
const platformRequest = Request();

/**
 * 获取接口日志下拉选项值
 * @returns
 */
export const getOptions = async () => {
    return await platformRequest({
        url: "/api/rest/statistics/logMonitoring/options",
        method: 'GET',
        cType: false,
    });
};

/**
 * 获取接口日志信息
 * @returns
 */
export const getLogMonitoring = async (data) => {
    return await platformRequest({
        url: "/api/rest/statistics/logMonitoring",
        method: 'POST',
        JSON:true,
        cType: false,
        data
    });
};