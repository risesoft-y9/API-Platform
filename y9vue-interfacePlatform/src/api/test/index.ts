
import Request from "@/api/lib/request";

const platformRequest = Request();


/**
 * 获取字典类型列表
 * @param params 
 * @returns 
 */
export const getOptionClassList = async () => {
    return await platformRequest({
        url: "/api/rest/optionClass/getOptionClassList",
        method: 'GET',
        cType: false,
        params: {},
    });
};




