
import Request from "@/api/lib/request";
import NodeRequest from "@/api/lib/requestNode"
import { dataType } from "element-plus/es/components/table-v2/src/common";

const platformRequest = Request();
const nodeRequest = NodeRequest();

import qs from 'qs'


/**
 * 获取已接入接口列表
 * @param params 
 * @returns 
 */
export const getInterfaceListByPersonId = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getInterfaceListByPersonId",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取接口列表通过状态
 * @param params 
 * @returns 
 */
export const getMayApplyInterfaceList = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getMayApplyInterfaceList",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 保存接口信息
 * @param dataJson 
 * @returns 
 */
export const saveInterfaceInfo = async (data) => {
    return await platformRequest({
        url: "/api/rest/interface/saveInterfaceInfo",
        method: 'POST',
        JSON:true,
        cType: false,
        data
    });
};

/**
 * 测试接口
 * @param dataJson 
 * @returns 
 */
export const testInterface = async (data) => {
    if(data.isResponseFile){
        return await nodeRequest({
            url: "/api/rest/interface/testInterface",
            method: 'POST',
            JSON:true,
            cType: false,
            isDowFile: true,
            responseType:'blob',
            data
        });
    }else{
        return await nodeRequest({
            url: "/api/rest/interface/testInterface",
            method: 'POST',
            JSON:true,
            cType: false,
            data
        });
    }
};

/**
 * 获取接口id,如果是版本维护会复制一份上一个接口的权限参数
 * @param params 
 * @returns 
 */
export const getInterfaceId = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getInterfaceId",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取接口值
 * @param params 
 * @returns 
 */
export const getInterfaceInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getInterfaceById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 根据接口ID删除接口数据
 * @param params 
 * @returns 
 */
export const delInterfaceById = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/delInterfaceById",
        method: 'GET',
        cType: false,
        params: params
    });
};


/**
 * 根据接口ID清理权限信息数据
 * @param params 
 * @returns 
 */
export const delAuthInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/delAuthInfoById",
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 接口版本升级
 * @param data 
 * @returns 
 */
export const saveUpdateVersionInfo = async (data) => {
    return await platformRequest({
        url: "/api/rest/interface/updateVersion",
        method: 'POST',
        cType: false,
        JSON:true,
        data
    });
};

/**
 * 获取权限信息下来值
 * @param params 
 * @returns 
 */
export const getAuthListByInterfaceId = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getAuthListByInterfaceId",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取已申请接口列表
 * @param params 
 * @returns 
 */
export const getAlreadyApplyInterfaceList = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getAlreadyApplyInterfaceList",
        method: 'GET',
        cType: false,
        params: params,
    });
};



/**
 * 获取申请信息
 * @param params 
 * @returns 
 */
export const getApplyInfoByInterfaceId = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getApplyInfoByInterfaceId",
        method: 'GET',
        cType: false,
        params: params,
    });
};

export const exportInterface = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/exportInterface",
        method: 'GET',
        cType: false,
        params: params,
        responseType:'blob'
    });
};

export const downLoadInterfaceFile = async (uri) => {
    return await platformRequest({
        url: uri,
        method: 'GET',
        cType: false,
        responseType:'blob'
    });
};

/**
 * 获取注册接口数量列表--执行端
 * @param params 
 * @returns 
 */
export const getRegisterNum = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getRegisterNum",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取接口注册执行端IP端口
 * @param params 
 * @returns 
 */
export const getIpPortByInterfaceId = async (params) => {
    return await platformRequest({
        url: "/api/rest/interface/getIpPortByInterfaceId",
        method: 'GET',
        cType: false,
        params: params,
    });
};