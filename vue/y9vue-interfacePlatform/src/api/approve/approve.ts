
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

/**
 * 发布接口信息
 * @param dataJson 
 * @returns 
 */
export const pubInterface = async (data) => {
    return await platformRequest({
        url: "/api/rest/approve/pubInterface",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};

/**
 * 停用接口信息
 * @param dataJson 
 * @returns 
 */
export const stopInterface = async (data) => {
    return await platformRequest({
        url: "/api/rest/approve/stopInterface",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};

/**
 * 接口调用申请
 * @param data 
 * @returns 
 */
export const useInterfaceApply = async (data) => {
    return await platformRequest({
        url: "/api/rest/approve/useInterfaceApply",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};

/**
 * 变更接口调用申请
 * @param data 
 * @returns 
 */
export const updateUseInterfaceApply = async (data) => {
    return await platformRequest({
        url: "/api/rest/approve/updateUseInterfaceApply",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};