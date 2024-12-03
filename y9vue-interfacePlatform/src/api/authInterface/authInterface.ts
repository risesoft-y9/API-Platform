
import Request from "@/api/lib/request";
import { dataType } from "element-plus/es/components/table-v2/src/common";

const platformRequest = Request();
import qs from 'qs'


/**
 * 获取参数key列表
 * @param params 
 * @returns 
 */
export const getAuthKeyList = async (params) => {
    return await platformRequest({
        url: "/api/rest/auth/getAuthKey",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取参数key列表
 * @param params 
 * @returns 
 */
export const getAuthKeyView = async (params) => {
    return await platformRequest({
        url: "/api/rest/auth/getAuthKeyView",
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
export const getAuthValList = async (params) => {
    return await platformRequest({
        url: "/api/rest/auth/getAuthVal",
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
export const saveAuthInfo = async (data) => {
    return await platformRequest({
        url: "/api/rest/auth/saveAuthDictInfo",
        method: 'POST',
        dataType: 'json',
        cType: false,
        data: data
    });
};

/**
 * 获取参数值
 * @param params 
 * @returns 
 */
export const getAuthInfoById = async (params) => {
    return await platformRequest({
        url: "/api/rest/auth/getAuthInfoById",
        method: 'GET',
        cType: false,
        params: params,
    });
};

/**
 * 获取父级ID列表
 * @param params 
 * @returns 
 */
export const getPidList = async (params) => {
    return await platformRequest({
        url: "/api/rest/auth/getPidList",
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
export const delAuthInfoById = async (params) => {
    if(params.type!=undefined){
        let param = {
            id:params.id
        }
        return await platformRequest({
            url: "/api/rest/auth/delAuthInfoByIdPersonal",
            method: 'GET',
            cType: false,
            params: param,
        });
    }else{
        return await platformRequest({
            url: "/api/rest/auth/delAuthInfoById",
            method: 'GET',
            cType: false,
            params: params,
        });
    }
};