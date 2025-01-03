
import Request from "@/api/lib/request";

const platformRequest = Request();


/**
 * 获取流程信息
 * @param params 
 * @returns 
 */
export const getFlow = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/getFlow",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取流程节点信息
 * @param params 
 * @returns 
 */
export const getFlowNode = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/getFlowNode",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取流程节点信息
 * @param params 
 * @returns 
 */
export const getSelectUserFlowNode = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/getSelectUserFlowNode",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 保存流程节点信息
 * @param dataJson 
 * @returns 
 */
export const saveFlowNodeInfo = async (data) => {
    return await platformRequest({
        url: "/api/rest/flowNode/saveFlowNodeInfo",
        method: 'POST',
        dataType: 'json',
        cType: false,
        data: data
    });
};

/**
 * 修改启用停用状态
 * @param dataJson 
 * @returns 
 */
export const updateEnable = async (data) => {
    return await platformRequest({
        url: "/api/rest/flowNode/updateEnable",
        method: 'POST',
        dataType: 'json',
        cType: false,
        data: data
    });
};

/**
 * 批量修改节点顺序
 * @param dataJson 
 * @returns 
 */
export const updateFlowNodeSortBatch = async (data) => {
    return await platformRequest({
        url: "/api/rest/flowNode/updateFlowNodeSortBatch",
        method: 'POST',
        dataType: 'json',
        cType: false,
        data: data
    });
};

/**
 * 获取流程节点信息通过id
 * @param params 
 * @returns 
 */
export const getFlowNodeInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/getFlowNodeInfoById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 根据ID删除流程数据
 * @param params 
 * @returns 
 */
export const delFlowNodeInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/delFlowNodeInfoById",
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 获取下拉节点数据
 * @param params 
 * @returns 
 */
export const getFlowSelect = async () => {
    return await platformRequest({
        url: "/api/rest/flowNode/getFlowSelect",
        method: 'GET',
        cType: false,
    });
};

/**
 * 获取审批人员列表
 * @param params 
 * @returns 
 */
export const getSelectUserList = async (params) => {
    return await platformRequest({
        url: "/api/rest/flowNode/getSelectUserList",
        method: 'GET',
        cType: false,
        params: params,
    });
};