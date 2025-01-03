package net.risesoft.service;

import net.risesoft.y9public.dto.InterfaceManageDTO;
import net.risesoft.y9public.entity.Parameter;

import java.util.List;
import java.util.Map;

public interface ParameterService {
    //根据接口id组装数据,并转为JSON
    Map<String,String> getParameterData(String interfaceId,String method,String interfaceType);
    //保存参数数据
    Map<String,Object> saveParameterData(InterfaceManageDTO interfaceManageDTO);

    //根据接口id组装数据,获取必填参数
    List<Parameter> getRequiredParameterData(String interfaceId);

    //根据接口id集合获取参数数据
    List<Parameter> getParameterDataByInterfaceIds(List<String> interfaceIds);

    //保存参数数据
    void saveAllParameterData(List<Parameter> list);
}
