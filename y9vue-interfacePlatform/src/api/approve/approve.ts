
import Request from "@/api/lib/request";

const platformRequest = Request();


/**
 * 获取审批列表
 * @param params 
 * @returns 
 */
export const getApproveList = async (params) => {
    return await platformRequest({
        url: "/api/rest/approve/getApproveList",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取审批信息
 * @param params 
 * @returns 
 */
export const getApproveById = async (params) => {
    return await platformRequest({
        url: "/api/rest/approve/getApproveById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取审批进度信息
 * @param params 
 * @returns 
 */
export const getApproveByInterfaceId = async (params) => {
    return await platformRequest({
        url: "/api/rest/approve/getApproveByInterfaceId",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 提交审批数据
 * @param dataJson 
 * @returns 
 */
export const submitApproveData = async (data) => {
    return await platformRequest({
        url: "/api/rest/approve/submit",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};