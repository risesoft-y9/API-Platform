
import Request from "@/api/lib/request";


/**
 * 获取系统cpu使用率
 * @param data 
 * @returns 
 */
export const getCpuUsed = async (data) => {
    let platformRequest = Request(data.baseUrl);
    return await platformRequest({
        url: "/metrics/system.cpu.usage",
        method: 'GET',
        cType: false
    });
};

/**
 * 获取最大jvm内存
 * @param data 
 * @returns 
 */
export const getMaxMemory = async (data) => {
    let platformRequest = Request(data.baseUrl);
    return await platformRequest({
        url: "/metrics/jvm.memory.max",
        method: 'GET',
        cType: false
    });
};


/**
 * 获取已使用内存
 * @param data 
 * @returns 
 */
export const getUsedMemory = async (data) => {
    let platformRequest = Request(data.baseUrl);
    return await platformRequest({
        url: "/metrics/jvm.memory.used",
        method: 'GET',
        cType: false
    });
};

/**
 * 获取调用次数，指该执行端所有的调用次数
 * @param data 
 * @returns 
 */
export const getHttpServerReqs = async (data) => {
    let platformRequest = Request(data.baseUrl);
    return await platformRequest({
        url: "/metrics/http.server.requests",
        method: 'GET',
        cType: false
    });
};