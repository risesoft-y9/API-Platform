
import Request from "@/api/lib/request";
import { dataType } from "element-plus/es/components/table-v2/src/common";

const platformRequest = Request();
import qs from 'qs'


/**
 * 获取系统标识管理列表
 * @param params 
 * @returns 
 */
export const getPage = async (params) => {
    return await platformRequest({
        url: "/api/rest/identifier/getPage",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取参数值列表
 * @param params 
 * @returns 
 */
export const getListByType = async (params) => {
    return await platformRequest({
        url: "/api/rest/identifier/getListByType",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取参数值列表
 * @param params 
 * @returns 
 */
export const getListByPid = async (params) => {
    return await platformRequest({
        url: "/api/rest/identifier/getListByPid",
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
export const saveInfo = async (data) => {
    return await platformRequest({
        url: "/api/rest/identifier/saveInfo",
        method: 'POST',
        dataType: 'json',
        cType: false,
        data: data
    });
};

/**
 * 获取值
 * @param params 
 * @returns 
 */
export const getInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/identifier/getInfoById",
        method: 'GET',
        cType: false,
        params: params,
    });
};
/**
 * 删除
 * @param params 
 * @returns 
 */
export const delInfoById = async (params) => {
        return await platformRequest({
            url: "/api/rest/identifier/delInfoById",
            method: 'GET',
            cType: false,
            params: params,
        });
};