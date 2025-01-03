
import Request from "@/api/lib/request";

const platformRequest = Request();


/**
 * 获取审批列表
 * @param params 
 * @returns 
 */
export const getApplyInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/apply/getApplyInfoById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取审批列表
 * @param params 
 * @returns 
 */
export const getApplyListById = async (params) => {
    return await platformRequest({
        url: "/api/rest/apply/getApplyListById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 下载密钥文件
 * @param params 
 * @returns 
 */
export const downLoadSecret = async (params) => {
    return await platformRequest({
        url: "/api/rest/apply/downLoadSecret",
        method: 'GET',
        cType: false,
        params: params,
        responseType:'blob'
    });
};

/**
 * 获取审批列表
 * @param params 
 * @returns 
 */
export const findIsPass = async (params) => {
    return await platformRequest({
        url: "/api/rest/apply/findIsPass",
        method: 'GET',
        cType: false,
        params: params,
    });
};